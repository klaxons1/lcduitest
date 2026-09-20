/**
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * A single test. Subclasses implement run() and use the Assert methods.
 *
 * Severity tells the reporter how a failure must be interpreted:
 *
 *   MUST   - the behaviour is mandated by the MIDP 2.0 specification.
 *            A failure means the emulator/device violates the specification.
 *   SHOULD - the behaviour is strongly implied by the specification but a
 *            platform is allowed some latitude (e.g. exact pixel of a rounded
 *            rectangle). Failures are reported separately from MUST failures.
 *   INFO   - pure observation. The test never fails; it records what the
 *            platform does so the value can be compared between emulators.
 *   MANUAL - the test needs a human (press this key, look at the screen).
 *            It records what was observed and never fails the run.
 *
 * Test cases are usually written as anonymous subclasses of an inner Case
 * class so that a whole area of the API can live in one source file.
 */
package lcduitest;

public abstract class TestCase {

    /** Behaviour mandated by the specification. */
    public static final int MUST = 0;
    /** Behaviour implied by the specification but with platform latitude. */
    public static final int SHOULD = 1;
    /** Observation only; never fails. */
    public static final int INFO = 2;
    /** Needs a human in front of the device; never fails. */
    public static final int MANUAL = 3;

    private final String name;
    private String description = "";
    private int severity = MUST;
    private boolean interactive = false;

    /**
     * Set by the runner while this test executes so that helpers such as
     * Assert.info() know where to record observations.
     */
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

    /** Marks the test as manually driven; it is run last, with a long timeout. */
    public TestCase manual() {
        severity = MANUAL;
        interactive = true;
        return this;
    }

    public boolean isInteractive() {
        return interactive;
    }

    public abstract void run() throws Exception;

    /*
     * Observation recorded by Assert.info(...) during the test.
     */
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
