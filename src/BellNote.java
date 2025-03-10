import enums.Note;
import enums.NoteLength;

public class BellNote {
    final Note note;
    final NoteLength length;

    BellNote(Note note, NoteLength length) {
        this.note = note;
        this.length = length;
    }

    public NoteLength getLength() {
        return length;
    }

    public Note getNote() {
        return note;
    }
}