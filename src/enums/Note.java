package enums;

/**
 * Represents musical notes with their corresponding frequencies.
 * Includes REST and notes from A3 to B6 with their corresponding audio samples.
 */
public enum Note {
    /** Silence - must be the first note in the enum */
    REST,
    /** A3 musical note */
    A3,
    /** A3 sharp musical note */
    A3S,
    /** B3 musical note */
    B3,
    /** C3 musical note */
    C3,
    /** C3 sharp musical note */
    C3S,
    /** D3 musical note */
    D3,
    /** D3 sharp musical note */
    D3S,
    /** E3 musical note */
    E3,
    /** F3 musical note */
    F3,
    /** F3 sharp musical note */
    F3S,
    /** G3 musical note */
    G3,
    /** G3 sharp musical note */
    G3S,
    /** A4 musical note */
    A4,
    /** A4 sharp musical note */
    A4S,
    /** B4 musical note */
    B4,
    /** C4 musical note */
    C4,
    /** C4 sharp musical note */
    C4S,
    /** D4 musical note */
    D4,
    /** D4 sharp musical note */
    D4S,
    /** E4 musical note */
    E4,
    /** F4 musical note */
    F4,
    /** F4 sharp musical note */
    F4S,
    /** G4 musical note */
    G4,
    /** G4 sharp musical note */
    G4S,
    /** A5 musical note */
    A5,
    /** A5 sharp musical note */
    A5S,
    /** B5 musical note */
    B5,
    /** C5 musical note */
    C5,
    /** C5 sharp musical note */
    C5S,
    /** D5 musical note */
    D5,
    /** D5 sharp musical note */
    D5S,
    /** E5 musical note */
    E5,
    /** F5 musical note */
    F5,
    /** F5 sharp musical note */
    F5S,
    /** G5 musical note */
    G5,
    /** G5 sharp musical note */
    G5S,
    /** A6 musical note */
    A6,
    /** A6 sharp musical note */
    A6S,
    /** B6 musical note */
    B6;

    /** The audio sample rate in samples per second (~48KHz) */
    public static final int SAMPLE_RATE = 48 * 1024;

    /** The length of a measure in seconds */
    public static final int MEASURE_LENGTH_SEC = 1;

    /** The angle step for sine wave calculation, based on sample rate */
    private static final double step_alpha = (2.0d * Math.PI) / SAMPLE_RATE;

    /** Base frequency for A notes (A3 = 220Hz) */
    private final double FREQUENCY_A_HZ = 220.0d;

    /** Maximum volume for generated samples */
    private final double MAX_VOLUME = 127.0d;

    /** Pre-calculated sine wave sample data for this note */
    private final byte[] sinSample = new byte[MEASURE_LENGTH_SEC * SAMPLE_RATE];

    /**
     * Constructs a Note with its corresponding frequency.
     * Calculates and stores the sine wave data for this note.
     */
    private Note() {
        int n = this.ordinal();
        if (n > 0) {
            // Calculate the frequency!
            final double halfStepUpFromA = n - 1;
            final double exp = halfStepUpFromA / 12.0d;
            final double freq = FREQUENCY_A_HZ * Math.pow(2.0d, exp);

            // Create sinusoidal data sample for the desired frequency
            final double sinStep = freq * step_alpha;
            for (int i = 0; i < sinSample.length; i++) {
                sinSample[i] = (byte) (Math.sin(i * sinStep) * MAX_VOLUME);
            }
        }
    }

    /**
     * Returns the pre-calculated audio sample for this note.
     *
     * @return A byte array containing the audio waveform data for this note
     */
    public byte[] sample() {
        return sinSample;
    }
}
