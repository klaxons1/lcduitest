/**
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * Outcome of a single TestCase.
 */
package m3gtest;

public class TestResult {

    /** The test met every MUST expectation. */
    public static final int PASS = 0;
    /** An expectation of the specification was not met. */
    public static final int FAIL = 1;
    /** The test threw an exception that was not an assertion failure. */
    public static final int ERROR = 2;
    /** Observation only (severity INFO) or manual test. */
    public static final int INFO = 3;

    private final TestCase test;
    private final int status;
    private final String message;
    private final long millis;

    public TestResult(TestCase test, int status, String message, long millis) {
        this.test = test;
        this.status = status;
        this.message = message;
        this.millis = millis;
    }

    public TestCase getTest() {
        return test;
    }

    public String getName() {
        return test.getName();
    }

    public int getStatus() {
        return status;
    }

    public int getSeverity() {
        return test.getSeverity();
    }

    public String getMessage() {
        return message;
    }

    public long getMillis() {
        return millis;
    }

    public boolean isFailure() {
        return status == FAIL || status == ERROR;
    }

    public String getStatusText() {
        switch (status) {
            case PASS:
                return "PASS";
            case FAIL:
                return "FAIL";
            case ERROR:
                return "ERROR";
            default:
                return "INFO";
        }
    }

    /** Single character used as a prefix in the results list. */
    public String getMark() {
        switch (status) {
            case PASS:
                return "+";
            case FAIL:
                return "x";
            case ERROR:
                return "!";
            default:
                return "i";
        }
    }
}
