/**
 * M3G Tester - collects test outcomes, prints machine readable log with TAG [M3GTEST]
 */
package m3gtest;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

public class Results {

    public static final String TAG = "[M3GTEST]";
    public static final String STORE_NAME = "m3gtest-results";
    public static final int FORMAT_VERSION = 1;

    public static class SuiteRun {
        public final TestSuite suite;
        public final TestRunner.SuiteResult stats;
        public final Vector testResults = new Vector();

        public SuiteRun(TestSuite suite, TestRunner.SuiteResult stats) {
            this.suite = suite;
            this.stats = stats;
        }

        public int failures() {
            return stats.failed + stats.errors;
        }
    }

    private final Vector runs = new Vector();
    private long startedAt = System.currentTimeMillis();
    private long finishedAt = 0;
    private String platform = "";

    public void addSuiteRun(SuiteRun run) {
        runs.addElement(run);
    }

    public int suiteCount() {
        return runs.size();
    }

    public SuiteRun suiteRun(int index) {
        return (SuiteRun) runs.elementAt(index);
    }

    public Enumeration suiteRuns() {
        return runs.elements();
    }

    public boolean isEmpty() {
        return runs.size() == 0;
    }

    public long getMillis() {
        return finishedAt == 0 ? System.currentTimeMillis() - startedAt : finishedAt - startedAt;
    }

    public void finish() {
        finishedAt = System.currentTimeMillis();
        platform = Env.prop("microedition.platform", "unknown");
    }

    public String getPlatform() {
        return platform;
    }

    public int total() {
        int n = 0;
        for (int i = 0; i < runs.size(); i++) {
            n += suiteRun(i).stats.total;
        }
        return n;
    }

    public int passed() {
        int n = 0;
        for (int i = 0; i < runs.size(); i++) {
            n += suiteRun(i).stats.passed;
        }
        return n;
    }

    public int failed() {
        int n = 0;
        for (int i = 0; i < runs.size(); i++) {
            n += suiteRun(i).stats.failed;
        }
        return n;
    }

    public int errors() {
        int n = 0;
        for (int i = 0; i < runs.size(); i++) {
            n += suiteRun(i).stats.errors;
        }
        return n;
    }

    public int infos() {
        int n = 0;
        for (int i = 0; i < runs.size(); i++) {
            n += suiteRun(i).stats.infos;
        }
        return n;
    }

    public String summary() {
        return "tests=" + total() + " passed=" + passed() + " failed=" + failed()
                + " errors=" + errors() + " info=" + infos() + " ms=" + getMillis();
    }

    public String verdict() {
        if (failed() + errors() == 0) {
            return "ALL PASS (" + passed() + "/" + total() + ")";
        }
        return failed() + errors() + " FAILURES of " + total();
    }

    private static String escape(String s) {
        if (s == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '|') {
                sb.append('/');
            } else if (c == '\n' || c == '\r') {
                sb.append(' ');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static void line(String text) {
        try {
            System.out.println(TAG + " " + text);
        } catch (Throwable t) {
        }
    }

    public void announceBegin() {
        line("BEGIN version=" + FORMAT_VERSION + " total=" + total()
                + " platform=" + escape(Env.prop("microedition.platform", "unknown")));
        line("ENV " + escape(Env.prop("microedition.configuration", "?"))
                + " | " + escape(Env.prop("microedition.profiles", "?"))
                + " | screen=" + escape(Env.prop("m3gtest.screen", "?")));
    }

    public static void announceTest(SuiteRun run, TestResult result) {
        line("TEST suite=" + run.suite.getId()
                + " name=" + escape(result.getName())
                + " status=" + result.getStatusText()
                + " severity=" + severityName(result.getSeverity())
                + " ms=" + result.getMillis()
                + " message=" + escape(result.getMessage()));
    }

    public static void announceTest(String suiteId, TestResult result) {
        line("TEST suite=" + suiteId
                + " name=" + escape(result.getName())
                + " status=" + result.getStatusText()
                + " severity=" + severityName(result.getSeverity())
                + " ms=" + result.getMillis()
                + " message=" + escape(result.getMessage()));
    }

    public static void announceSuite(SuiteRun run) {
        line("SUITE id=" + run.suite.getId()
                + " total=" + run.stats.total
                + " passed=" + run.stats.passed
                + " failed=" + run.stats.failed
                + " errors=" + run.stats.errors
                + " info=" + run.stats.infos
                + " ms=" + run.stats.millis);
    }

    public void announceEnd() {
        line("END " + summary());
    }

    public static String severityName(int severity) {
        switch (severity) {
            case TestCase.MUST:
                return "MUST";
            case TestCase.SHOULD:
                return "SHOULD";
            case TestCase.INFO:
                return "INFO";
            default:
                return "MANUAL";
        }
    }

    public void save() {
        RecordStore store = null;
        try {
            try {
                RecordStore.deleteRecordStore(STORE_NAME);
            } catch (Throwable ignored) {
            }
            store = RecordStore.openRecordStore(STORE_NAME, true);
            addRecord(store, "M3GTEST|" + FORMAT_VERSION
                    + "|" + System.currentTimeMillis()
                    + "|" + escape(platform)
                    + "|" + total() + "|" + passed() + "|" + failed() + "|" + errors() + "|" + infos());
            for (int i = 0; i < runs.size(); i++) {
                SuiteRun run = suiteRun(i);
                addRecord(store, "SUITE|" + run.suite.getId() + "|" + run.stats.total + "|" + run.stats.passed
                        + "|" + run.stats.failed + "|" + run.stats.errors + "|" + run.stats.infos);
                for (int j = 0; j < run.testResults.size(); j++) {
                    TestResult result = (TestResult) run.testResults.elementAt(j);
                    if (result.isFailure()) {
                        addRecord(store, "FAIL|" + run.suite.getId() + "|" + escape(result.getName())
                                + "|" + result.getStatusText() + "|" + escape(result.getMessage()));
                    }
                }
            }
        } catch (Throwable t) {
            line("RMS-ERROR " + escape(t.toString()));
        } finally {
            close(store);
        }
    }

    private static void addRecord(RecordStore store, String text) throws RecordStoreException {
        byte[] data = text.getBytes();
        store.addRecord(data, 0, data.length);
    }

    private static void close(RecordStore store) {
        if (store != null) {
            try {
                store.closeRecordStore();
            } catch (Throwable ignored) {
            }
        }
    }

    public static Vector loadSummary() {
        Vector lines = new Vector();
        RecordStore store = null;
        try {
            store = RecordStore.openRecordStore(STORE_NAME, false);
            int count = store.getNumRecords();
            for (int i = 1; i <= count; i++) {
                byte[] data = store.getRecord(i);
                if (data != null) {
                    lines.addElement(new String(data));
                }
            }
        } catch (Throwable t) {
        } finally {
            close(store);
        }
        return lines;
    }
}
