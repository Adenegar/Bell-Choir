package enums;

public enum NoteLength {
    WHOLE(1.0f),
    DOTTEDHALF(0.75f),
    HALF(0.5f),
    DOTTEDQUARTER(0.375f),
    QUARTER(0.25f),
    EIGTH(0.125f);

    private final int timeMs;
    private final float length;

    private NoteLength(float length) {
        this.length = length;
        timeMs = (int)(length * Note.MEASURE_LENGTH_SEC * 1000);
    }

    public static NoteLength fromLength(float length) {
        for (NoteLength nl : values()) {
            if (nl.length == length) {
                return nl;
            }
        }
        throw new IllegalArgumentException("No NoteLength with length: " + length);
    }

    public int timeMs() {
        return timeMs;
    }
}