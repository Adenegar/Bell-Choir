import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

import javax.sound.sampled.AudioFormat;

import enums.Note;

/**
 * Test class for the Conductor's file parsing capabilities.
 * Tests valid and invalid song files to ensure proper parsing behavior.
 */
public class ConductorTest {

    /** Directory containing test files */
    private static final String TEST_DIR = "songs/test/";

    /** Original error output stream to restore after tests */
    private static PrintStream originalErr;

    /**
     * Main entry point for the test application.
     *
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        runAllTests();
    }

    /**
     * Runs all tests by finding test files in the test directory and processing
     * them.
     * Files starting with "Valid-" should parse successfully, others should fail.
     */
    private static void runAllTests() {
        File testDir = new File(TEST_DIR);
        if (!testDir.exists() || !testDir.isDirectory()) {
            System.err.println("Test directory not found: " + TEST_DIR);
            return;
        }

        File[] testFiles = testDir.listFiles((dir, name) -> name.endsWith(".txt"));
        if (testFiles == null || testFiles.length == 0) {
            System.err.println("No test files found in: " + TEST_DIR);
            return;
        }

        int passCount = 0;
        int totalTests = 0;

        System.out.println("=== Running Conductor Tests ===");

        for (File file : testFiles) {
            String filename = file.getName();
            boolean expectedValid = filename.startsWith("Valid-");

            System.out.println("File: " + filename);
            System.out.println("Expected: " + (expectedValid ? "Valid" : "Invalid"));

            suppressErrors();
            boolean result = testFile(file.getAbsolutePath(), expectedValid);
            restoreErrors();

            if (result) {
                System.out.println("Result: PASS");
                passCount++;
            } else {
                System.out.println("Result: FAIL");
            }
            System.out.println();
            totalTests++;
        }

        System.out.println("\n=== Test Summary ===");
        System.out.println("Valid-Invalid Tests Passed: " + passCount + "/" + totalTests);
    }

    /**
     * Suppresses error output during tests by redirecting System.err
     * to a null output stream.
     */
    private static void suppressErrors() {
        // Create a null output stream that discards error output.
        System.setErr(new PrintStream(new OutputStream() {
            public void write(int b) {
                // Discard the output.
            }
        }));
    }

    /**
     * Restores the original error output stream.
     */
    private static void restoreErrors() {
        System.setErr(originalErr);
    }

    /**
     * Tests a single song file to verify its parsing behavior.
     *
     * @param filepath      The absolute path to the test file.
     * @param expectedValid Boolean indicating whether the file is expected to parse
     *                      successfully.
     * @return True if the test result matches the expected result, false otherwise.
     */
    private static boolean testFile(String filepath, boolean expectedValid) {
        try {
            // Create a Conductor with a specified audio format.
            final AudioFormat af = new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, false);
            Conductor conductor = new Conductor(af);

            List<BellNote> notes = conductor.parseNotes(filepath);

            // A non-null 'notes' indicates the file was parsed successfully.
            boolean actualValid = (notes != null);

            System.out.println("Actual: " + (actualValid ? "Valid" : "Invalid"));

            return actualValid == expectedValid;
        } catch (Exception e) {
            System.err.println("Error testing file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
