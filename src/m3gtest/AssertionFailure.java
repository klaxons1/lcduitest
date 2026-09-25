/*
 * M3G Tester - JSR-184 conformance test MIDlet.
 * Same harness as LCDUI Tester, adapted for M3G.
 */
package m3gtest;

public class AssertionFailure extends Error {

    private static final long serialVersionUID = 1L;

    public AssertionFailure(String message) {
        super(message);
    }
}
