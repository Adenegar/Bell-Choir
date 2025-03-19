import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import enums.Note;
import enums.NoteLength;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * Orchestrates the playback of a musical piece by coordinating multiple Member
 * threads.
 * Handles parsing of musical notation from files and manages the timing
 * of note playback across different threads.
 */
public class Conductor implements Runnable {

    /** The audio format used for playback */
    private final AudioFormat af;

    /** The thread that runs the conductor */
    private final Thread thread;

    /** Map of notes to their corresponding member threads */
    private final Map<Note, Member> choir = new HashMap<>();

    /** How long to pause between notes, adding a staccato effect */
    private final int STACCATO_PAUSE = 80;

    /** The sequence of notes that form the song to be played */
    private List<BellNote> song;

    /**
     * Parses a file containing musical notation into a list of BellNotes.
     * The file format should have one note per line with the format: "NOTE LENGTH"
     * Where NOTE is the name of a Note enum value and LENGTH is a number
     * representing
     * the note duration (1 for whole note, 2 for half note, 4 for quarter note,
     * etc.).
     *
     * @param filename The path of the file to parse
     * @return A list of BellNotes representing the song, or null if parsing failed
     */
    public List<BellNote> parseNotes(String filename) {
        File file = new File(filename);
        if (file.exists()) {
            final List<BellNote> notes = new ArrayList<>();
            String line;
            String[] elements;
            boolean valid = true;
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                while ((line = reader.readLine()) != null) {
                    elements = line.split(" ");
                    if (elements.length < 2) {
                        System.err.println("Couldn't extract two elements from line: " + line);
                        valid = false;
                        continue;
                    }
                    Note n = parseNote(elements[0]);
                    NoteLength nl = parseNoteLength(elements[1]);
                    if (n == null || nl == null) {
                        valid = false;
                        continue;
                    }
                    notes.add(new BellNote(n, nl));
                }
                if (valid == false) {
                    System.err.println("At least one line failed to read, please review errors");
                    return null;
                }
                this.song = notes;
                return notes;
            } catch (IOException ignored) {
                System.err.println("File " + filename + " exists, this should never happen");
            }
        } else {
            // If the file is not found, try prepending the songs/ directory or adding .txt
            if (!filename.startsWith("songs/")) {
                return parseNotes("songs/" + filename);
            } else if (!filename.endsWith(".txt")) {
                return parseNotes(filename + ".txt");
            }
            System.err.println("File: " + filename + " not found");
        }
        return null;
    }

    /**
     * Parses a string into a Note enum value.
     *
     * @param note The string representation of the note
     * @return The corresponding Note enum value, or null if parsing fails
     */
    private Note parseNote(String note) {
        try {
            return Note.valueOf(note);
        } catch (IllegalArgumentException e) {
            System.err.println("Failed to parse note: " + note);
            return null;
        }
    }

    /**
     * Parses a string into a NoteLength enum value.
     * Supports standard note lengths as well as dotted notes.
     * Example: "4" = quarter note, "2" = half note, "1" = whole note
     * Special cases: "3" = dotted half note, "6" = dotted quarter note
     *
     * @param noteLength The string representation of the note length
     * @return The corresponding NoteLength enum value, or null if parsing fails
     */
    private NoteLength parseNoteLength(String noteLength) {
        try {
            int temp = Integer.parseInt(noteLength.strip());
            // Our program has custom support for dotted quarters and halves, we handle
            // those here.
            if (temp == 3) {
                return NoteLength.fromLength(0.75f);
            } else if (temp == 6) {
                return NoteLength.fromLength(0.375f);
            }
            return NoteLength.fromLength(1 / ((float) temp));
        } catch (IllegalArgumentException e) {
            System.err.println("Failed to parse note length: " + noteLength);
            return null;
        }
    }

    /**
     * Assigns notes to the appropriate Member threads for playback.
     * Creates new Member threads as needed.
     *
     * @param notes The list of notes to assign
     * @param line  The audio output line for playback
     */
    private void assignParts(List<BellNote> notes, SourceDataLine line) {
        for (BellNote bNote : notes) {
            Note note = bNote.getNote();
            Member m = choir.getOrDefault(note, null);

            if (m == null) {
                m = new Member(note, line);
                choir.put(note, m);
            }
            m.assignPart(bNote.getLength());
        }
    }

    /**
     * Starts all Member threads in the choir.
     */
    private void startThreads() {
        for (Member m : choir.values()) {
            m.startMember();
        }
    }

    /**
     * Stops all Member threads in the choir.
     * Ensures clean shutdown by first signaling threads to stop,
     * then waiting for them to complete any current operations.
     */
    private void stopThreads() {
        // First signal all threads to stop
        for (Member m : choir.values()) {
            m.stopMember();
            // Wake up any waiting threads to process their stop signal
            synchronized (m) {
                m.notify();
            }
        }
    }

    // Fixed: Repeat playing bug
    
    /**
     * Main entry point for the application.
     * Parses a song file and plays it.
     *
     * @param args Command line arguments, optionally containing the path to a song
     *             file
     */
    public static void main(String[] args) {
        final AudioFormat af = new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, false);
        Conductor conductor = new Conductor(af);
        List<BellNote> notes = null;
        if (args != null && args.length > 0) {
            notes = conductor.parseNotes(args[0]);
        }
        if (notes == null) { // If we fail to read in our file, end the program. Some output should have been
            System.err.println("Failed to read in notes. Pass in a file using argument -Dsong=[PATH_TO_SONG]");
            return;
        }
        conductor.playSong();

        // Wait for the song to finish before stopping threads
        try {
            conductor.thread.join();
        } catch (InterruptedException e) {
            System.err.println("Main thread interrupted while waiting for conductor");
        }

        conductor.stopThreads();
    }

    /**
     * Stops the conductor thread by waiting for the conductor thread to finish
     * execution
     */
    public void stop() {
        try {
            thread.join();
        } catch (InterruptedException e) {
            System.err.println(thread.getName() + " stop malfunction");
        }
    }

    /**
     * Constructs a Conductor with the specified audio format.
     *
     * @param af The audio format to use for playback
     */
    public Conductor(AudioFormat af) {
        thread = new Thread(this, "Conductor");
        this.af = af;
    }

    /**
     * Starts playing the current song.
     */
    public void playSong() {
        thread.start();
    }

    /**
     * The main execution method for the conductor thread.
     * Orchestrates the playback of notes by signaling Member threads at the right
     * time.
     */
    @Override
    public void run() {
        try (final SourceDataLine line = AudioSystem.getSourceDataLine(af)) {
            line.open();
            line.start();

            assignParts(song, line);
            startThreads();

            for (BellNote bn : song) {
                Note note = bn.getNote();
                Member player = choir.get(note);
                synchronized (player) {
                    if (!player.isPlaying())
                        break; // Exit if we've been asked to stop

                    player.setHasNewNote(true); // signal that a new note is waiting
                    player.notify(); // wake this member to process its note
                    try {
                        // Now wait until the member signals that it's done playing
                        while (player.hasNewNote() && player.isPlaying()) {
                            player.wait(1000); // Add timeout to prevent deadlock
                            if (player.hasNewNote() && !player.isPlaying()) {
                                // If we're stopping but thread has unprocessed note
                                player.setHasNewNote(false);
                                break;
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.err.println("Interrupted while waiting for player to finish.");
                        break;
                    }
                }
                synchronized (this) {
                    try {
                        Thread.sleep(STACCATO_PAUSE);
                    } catch (InterruptedException e) {
                        System.err.println("Interrupted while pausing between notes (adding staccato effect).");
                    }
                }
            }

            line.drain();
        } catch (LineUnavailableException e) {
            System.err.println("playSong: The Audio System tried to read an unavailable line.");
        }
    }
}
