import enums.Note;
import enums.NoteLength;

/**
 * Represents a musical note with its associated length.
 * Combines a Note enum value with a NoteLength enum value to define
 * a complete musical note for playback.
 */
public class BellNote {
    /** The pitch of the note */
    private final Note note;

    /** The duration of the note */
    private final NoteLength length;

    /**
     * Constructs a BellNote with the specified pitch and duration.
     *
     * @param note   The pitch of the note
     * @param length The duration of the note
     */
    public BellNote(Note note, NoteLength length) {
        this.note = note;
        this.length = length;
    }

    /**
     * Gets the duration of this note.
     *
     * @return The NoteLength enum value representing the duration
     */
    public NoteLength getLength() {
        return length;
    }

    /**
     * Gets the pitch of this note.
     *
     * @return The Note enum value representing the pitch
     */
    public Note getNote() {
        return note;
    }
}