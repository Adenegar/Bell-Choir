package enums;

/**
 * Represents the length of a musical note in relation to a measure.
 * Provides standard note lengths used in musical notation.
 */
public enum NoteLength {
    /** A whole note, equal to a full measure (1.0) */
    WHOLE(1.0f),
    /** A dotted half note, equal to 3/4 of a measure (0.75) */
    DOTTEDHALF(0.75f),
    /** A half note, equal to 1/2 of a measure (0.5) */
    HALF(0.5f),
    /** A dotted quarter note, equal to 3/8 of a measure (0.375) */
    DOTTEDQUARTER(0.375f),
    /** A quarter note, equal to 1/4 of a measure (0.25) */
    QUARTER(0.25f),
    /** An eighth note, equal to 1/8 of a measure (0.125) */
    EIGTH(0.125f);

    /** The duration of the note in milliseconds */
    private final int timeMs;

    /** The length of the note as a fraction of a measure */
    private final float length;

    /**
     * Constructs a note length with the specified fraction of a measure.
     *
     * @param length The length of the note as a fraction of a measure (1.0 = full
     *               measure)
     */
    private NoteLength(float length) {
        this.length = length;
        timeMs = (int) (length * Note.MEASURE_LENGTH_SEC * 1000);
    }

    /**
     * Finds a NoteLength enum value that matches the specified length.
     *
     * @param length The length to search for, as a fraction of a measure
     * @return The matching NoteLength enum value
     * @throws IllegalArgumentException If no matching NoteLength is found
     */
    public static NoteLength fromLength(float length) {
        for (NoteLength nl : values()) {
            if (nl.length == length) {
                return nl;
            }
        }
        throw new IllegalArgumentException("No NoteLength with length: " + length);
    }

    /**
     * Gets the duration of this note in milliseconds.
     *
     * @return The time in milliseconds that this note should be played
     */
    public int timeMs() {
        return timeMs;
    }
}