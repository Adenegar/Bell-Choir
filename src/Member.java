import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.SourceDataLine;

import enums.Note;
import enums.NoteLength;

public class Member implements Runnable {

    private final List<NoteLength> songParts;
    private final Note note; 
    private final Thread thread;
    private final Object lock;
    private final AudioFormat af;
    private final SourceDataLine line;
    private boolean playing = false;
    private boolean hasNewNote = false;

    Member(Note note, Object lock, AudioFormat af, SourceDataLine line) {
        this.songParts = new ArrayList<>();
        this.note = note;
        this.lock = lock;
        this.af = af;
        this.line = line;
        thread = new Thread(this, "Member " + note);
    }

    public void startMember() {
        thread.start();   
        playing = true;
    }

    public void stopMember() {
        setPlaying(false);
        // Wake up the thread if it's waiting
        synchronized(this) {
            this.notify();
        }
        waitToStop();
    }

    public void waitToStop() {
        try {
            thread.join();
        } catch (InterruptedException e) {
            System.err.println(thread.getName() + " stop malfunction");
        }
    }

    public void assignPart(NoteLength nl) {
        songParts.add(nl);
    }

    public synchronized void setHasNewNote(boolean flag) {
        hasNewNote = flag;
    }
    
    public synchronized boolean hasNewNote() {
        return hasNewNote;
    }

    public synchronized void setPlaying(boolean playing) {
        this.playing = playing;
    }
    
    public synchronized boolean isPlaying() {
        return playing;
    }

    @Override
    public void run() {
        synchronized (this) {
            try {
                while (playing) {
                    // Wait until a new note is assigned or we're asked to stop
                    while (!hasNewNote && playing) {
                        this.wait(500); // Add timeout to check playing flag periodically
                    }
                    if (!playing) break; // exit loop if stopped
                    
                    if (hasNewNote) { // Only play if there's actually a note to play
                        playNote();
                    }
                    
                    // Reset flag and notify conductor that this note has been played
                    hasNewNote = false;
                    this.notify();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println(thread.getName() + " was interrupted");
            }
        }
    }

    private void playNote() {
        NoteLength nl = songParts.remove(0);
        if (nl == null) {
            System.err.println("What's my line? Member was asked to play note when they have no song parts left");
            return;
        }
        System.out.println(thread + " playing"); // Uncomment this line to show that different threads are playing
        BellNote note = new BellNote(this.note, nl);
        playNote(line, note);
    }

    private void playNote(SourceDataLine line, BellNote bn) {
        final int ms = Math.min(bn.length.timeMs(), Note.MEASURE_LENGTH_SEC * 1000);
        final int length = Note.SAMPLE_RATE * ms / 1000;
        line.write(bn.note.sample(), 0, length);
        line.write(Note.REST.sample(), 0, 50);
    }
}