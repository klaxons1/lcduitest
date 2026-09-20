/*
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * Thrown by the Assert helper methods when a test's expectation is not met.
 * It extends Error (not Exception) so that application-level "catch (Exception)"
 * blocks inside tests do not swallow assertion failures by accident.
 */
package lcduitest;

public class AssertionFailure extends Error {

    private static final long serialVersionUID = 1L;

    public AssertionFailure(String message) {
        super(message);
    }
}
