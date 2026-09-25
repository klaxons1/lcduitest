/**
 * M3GTester - MIDP 2.0 M3G conformance test MIDlet.
 *
 * Implemented by the MIDlet so that the UI (which is in another package and
 * therefore cannot call MIDlet.notifyDestroyed(), a protected method) can ask
 * the MIDlet to shut down in automated mode.
 */
package m3gtest;

public interface ExitHook {
    void requestExit();
}
