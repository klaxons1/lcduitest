/*
 * M3G Tester
 */
package m3gtest;

import java.util.Enumeration;

public class TestRunner {

    public interface Listener {
        void suiteStarted(TestSuite suite, int total);
        void testFinished(TestSuite suite, TestCase test, TestResult result);
        void suiteFinished(TestSuite suite, SuiteResult result);
    }

    public static class SuiteResult {
        public int passed;
        public int failed;
        public int errors;
        public int infos;
        public int total;
        public long millis;

        public int failures() {
            return failed + errors;
        }
    }

    private final Listener listener;
    private boolean cancelled;

    public TestRunner(Listener listener) {
        this.listener = listener;
    }

    public void cancel() {
        cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public static TestResult execute(TestCase test) {
        long started = System.currentTimeMillis();
        test.clearObservation();
        TestCase.current = test;
        int status;
        String message = null;
        try {
            test.run();
            if (test.getSeverity() == TestCase.INFO || test.getSeverity() == TestCase.MANUAL) {
                status = TestResult.INFO;
            } else {
                status = TestResult.PASS;
            }
        } catch (AssertionFailure failure) {
            status = TestResult.FAIL;
            message = failure.getMessage();
        } catch (Throwable t) {
            status = TestResult.ERROR;
            message = t.getClass().getName() + \": \" + t;
        } finally {
            TestCase.current = null;
        }
        long millis = System.currentTimeMillis() - started;
        if (message == null) {
            message = test.getObservation();
        } else if (test.getObservation() != null) {
            message = message + \" [\" + test.getObservation() + \"]\";
        }
        return new TestResult(test, status, message, millis);
    }

    public SuiteResult run(TestSuite suite) {
        SuiteResult result = new SuiteResult();
        result.total = suite.size();
        long started = System.currentTimeMillis();
        if (listener != null) {
            listener.suiteStarted(suite, suite.size());
        }
        Enumeration e = suite.elements();
        while (e.hasMoreElements()) {
            if (cancelled) {
                break;
            }
            TestCase test = (TestCase) e.nextElement();
            TestResult testResult = execute(test);
            if (testResult.getStatus() == TestResult.PASS) {
                result.passed++;
            } else if (testResult.getStatus() == TestResult.FAIL) {
                result.failed++;
            } else if (testResult.getStatus() == TestResult.ERROR) {
                result.errors++;
            } else {
                result.infos++;
            }
            if (listener != null) {
                listener.testFinished(suite, test, testResult);
            }
        }
        result.millis = System.currentTimeMillis() - started;
        if (listener != null) {
            listener.suiteFinished(suite, result);
        }
        return result;
    }
}
