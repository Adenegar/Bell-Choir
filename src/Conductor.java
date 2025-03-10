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

public class Conductor implements Runnable {

    private final AudioFormat af;
    private final Thread thread;
    private final Map<Note, Member> choir = new HashMap<>();
    private List<BellNote> song;
    private boolean timeToWork = false;

    // instance method: parseNotes()
    private List<BellNote> parseNotes(String filename) {
        File file = new File(filename);
        if (file.exists()) {
            final List<BellNote> notes = new ArrayList<>();
            String line;
            String[] elements;
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                while ((line = reader.readLine()) != null) {
                    elements = line.split(" ");
                    if (elements.length < 2) {
                        System.err.println("Couldn't extract two elements from line: " + line);
                        continue;
                    }
                    Note n = parseNote(elements[0]);
                    NoteLength nl = parseNoteLength(elements[1]);
                    if (n == null || nl == null)
                        continue;
                    notes.add(new BellNote(n, nl));
                }
                this.song = notes;
                return notes;
            } catch (IOException ignored) {
                System.err.println("File " + filename + " exists, this should never happen");
            }
        } else {
            System.err.println("File: " + filename + " not found");
        }
        return null;
    }

    // instance method: parseNote()
    private Note parseNote(String note) {
        try {
            return Note.valueOf(note);
        } catch (IllegalArgumentException e) {
            System.err.println("Failed to parse note: " + note);
            return null;
        }
    }

    // instance method: parseNoteLength()
    private NoteLength parseNoteLength(String noteLength) {
        try {
            int temp = Integer.parseInt(noteLength.strip());
            return NoteLength.fromLength(1 / ((float) temp));
        } catch (IllegalArgumentException e) {
            System.err.println("Failed to parse note length: " + noteLength);
            return null;
        }
    }

    private synchronized void assignParts(List<BellNote> notes, SourceDataLine line) {
        for (BellNote bNote : notes) {
            Note note = bNote.getNote();
            Member m = choir.getOrDefault(note, null);

            if (m == null) { 
                m = new Member(note, new Object(), af, line);
                choir.put(note, m);
            } 
            m.assignPart(bNote.getLength());
        }
    }

    private synchronized void startThreads() {
        for (Member m : choir.values()) {
            m.startMember();
        }
        // timeToWork = true;
        // thread.start(); // TODO: Implement run playSong integration
    }

    private void stopThreads() {
        for (Member m : choir.values()) {
            m.stopMember();
        }
    }

    public static void main(String[] args) {
        final AudioFormat af = new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, false);
        Conductor conductor = new Conductor(af);
        List<BellNote> notes = null;
        for (int i = 0; i < args.length; i++) {
            switch (i) {
                case 0:
                    notes = conductor.parseNotes(args[i]);
                    break;
            }
        }
        // By default, load Mary had a little lamb
        if (notes == null) {
            notes = conductor.parseNotes("songs/MaryHadALittleLamb.txt");
        }
        // conductor.assignParts(notes);

        // conductor.startThreads();

        conductor.playSong();

        conductor.stopThreads();
    }

    public Conductor(AudioFormat af) {
        thread = new Thread(this, "Conductor");
        this.af = af;
    }

    public void playSong() {
        thread.start();     
    }

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
                    player.setHasNewNote(true); // signal that a new note is waiting
                    player.notify();             // wake this member to process its note
                    try {
                        // Now wait until the member signals that it's done playing
                        while (player.hasNewNote()) {
                            player.wait();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.err.println("Interrupted while waiting for player to finish.");
                    }
                }
            }

            line.drain();
    } catch (LineUnavailableException e) {
        System.err.println("playSong: The Audio System tried to read an unavailable line.");
        }
    }

}
