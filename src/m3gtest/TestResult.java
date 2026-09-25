/*
 * M3G Tester
 */
package m3gtest;

public class TestResult {

    public static final int PASS = 0;
    public static final int FAIL = 1;
    public static final int ERROR = 2;
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

    public int getSeverity() {
        return test.getSeverity();
    }

    public int getStatus() {
        return status;
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
                return \"PASS\";
            case FAIL:
                return \"FAIL\";
            case ERROR:
                return \"ERROR\";
            default:
                return \"INFO\";
        }
    }
}
