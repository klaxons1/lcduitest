/*
 * M3G Tester - JSR-184 conformance test MIDlet.
 */
package m3gtest;

public abstract class TestCase {

    public static final int MUST = 0;
    public static final int SHOULD = 1;
    public static final int INFO = 2;
    public static final int MANUAL = 3;

    private final String name;
    private String description = "";
    private int severity = MUST;
    private boolean interactive = false;

    static TestCase current = null;

    public TestCase(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public TestCase describe(String text) {
        description = text;
        return this;
    }

    public int getSeverity() {
        return severity;
    }

    public TestCase severity(int level) {
        severity = level;
        return this;
    }

    public TestCase manual() {
        severity = MANUAL;
        interactive = true;
        return this;
    }

    public boolean isInteractive() {
        return interactive;
    }

    public abstract void run() throws Exception;

    private String observed = null;

    void note(String text) {
        if (text == null) {
            return;
        }
        if (observed == null) {
            observed = text;
        } else {
            observed = observed + "; " + text;
        }
    }

    public String getObservation() {
        return observed;
    }

    void clearObservation() {
        observed = null;
    }
}
