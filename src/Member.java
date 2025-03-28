import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.SourceDataLine;

import enums.Note;
import enums.NoteLength;

/**
 * Represents a member of the musical ensemble that plays a specific note.
 * Each Member runs in its own thread and is responsible for playing its
 * assigned note when requested by the Conductor.
 */
public class Member implements Runnable {

    /** List of note durations this member needs to play */
    private final List<NoteLength> songParts;

    /** The specific note this member is responsible for playing */
    private final Note note;

    /** The thread that runs this member */
    private final Thread thread;

    /** Audio line for output */
    private final SourceDataLine line;

    /** Flag indicating if this member is currently active */
    private boolean playing = false;

    /** Flag indicating if a new note is ready to be played */
    private boolean hasNewNote = false;

    /**
     * Constructs a Member that will play a specific note.
     *
     * @param note The note this member will play
     * @param lock Synchronization object
     * @param af   Audio format for playback
     * @param line Audio output line
     */
    Member(Note note, SourceDataLine line) {
        this.songParts = new ArrayList<>();
        this.note = note;
        this.line = line;
        thread = new Thread(this, "Member " + note);
    }

    /**
     * Starts this member's thread.
     */
    public void startMember() {
        thread.start();
        playing = true;
    }

    /**
     * Stops this member's thread and waits for it to complete.
     */
    public void stopMember() {
        setPlaying(false);
        // Wake up the thread if it's waiting
        synchronized (this) {
            this.notify();
        }
        waitToStop();
    }

    /**
     * Waits for this member's thread to finish execution.
     */
    public void waitToStop() {
        try {
            thread.join();
        } catch (InterruptedException e) {
            System.err.println(thread.getName() + " stop malfunction");
        }
    }

    /**
     * Assigns a note duration to this member's play queue.
     *
     * @param nl The note length to add to the queue
     */
    public void assignPart(NoteLength nl) {
        songParts.add(nl);
    }

    /**
     * Sets the flag indicating whether a new note is ready to be played.
     *
     * @param flag True if a new note is ready, false otherwise
     */
    public synchronized void setHasNewNote(boolean flag) {
        hasNewNote = flag;
    }

    /**
     * Checks if a new note is ready to be played.
     *
     * @return True if a new note is ready, false otherwise
     */
    public synchronized boolean hasNewNote() {
        return hasNewNote;
    }

    /**
     * Sets the playing state of this member.
     *
     * @param playing True to indicate playing, false to stop
     */
    public synchronized void setPlaying(boolean playing) {
        this.playing = playing;
    }

    /**
     * Checks if this member is currently playing.
     *
     * @return True if the member is playing, false otherwise
     */
    public synchronized boolean isPlaying() {
        return playing;
    }

    /**
     * The main execution method for this member's thread.
     * Waits for signals to play notes and handles them accordingly.
     */
    @Override
    public void run() {
        synchronized (this) {
            try {
                while (playing) {
                    // Wait until a new note is signaled or a stop request occurs.
                    while (!hasNewNote && playing) {
                        // Timeout ensures periodic check for the playing state.
                        this.wait(500);
                    }
                    if (!playing)
                        break; // Exit loop if playback has been stopped.

                    if (hasNewNote) { // Process the new note.
                        playNote();
                    }

                    // Reset the flag and notify any thread waiting for the note to complete.
                    hasNewNote = false;
                    this.notify();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println(thread.getName() + " was interrupted");
            }
        }
    }

    /**
     * Plays the next note from the queue.
     * Removes the first note from songParts, constructs a BellNote and plays it.
     */
    private void playNote() {
        NoteLength nl = songParts.remove(0);
        if (nl == null) {
            System.err.println("What's my line? Member was asked to play note when they have no song parts left");
            return;
        }
        // Debug log to show which thread is playing.
        System.out.println(thread + " playing");
        BellNote note = new BellNote(this.note, nl);
        playNote(line, note);
    }

    /**
     * Writes the audio data for the BellNote to the output line.
     * Plays the note followed by a short rest to simulate a staccato pause.
     *
     * @param line The SourceDataLine to write audio data.
     * @param bn   The BellNote to play.
     */
    private void playNote(SourceDataLine line, BellNote bn) {
        final int ms = Math.min(bn.getLength().timeMs(), Note.MEASURE_LENGTH_SEC * 1000);
        final int length = Note.SAMPLE_RATE * ms / 1000;
        // Write the note samples.
        line.write(bn.getNote().sample(), 0, length);
        // Add a short rest after playing the note.
        line.write(Note.REST.sample(), 0, 50);
    }
}