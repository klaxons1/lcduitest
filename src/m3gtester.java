/*
 * M3G Tester - a MIDP 2.0 MIDlet that runs conformance tests against the
 * javax.microedition.m3g (JSR-184) implementation of a device or emulator.
 *
 * Same harness as LCDUITester, only for M3G.
 * MIDlet entry point is m3gtester (MIDlet-2 in MANIFEST.MF).
 */

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import m3gtest.ExitHook;
import m3gtest.ui.App;

public class m3gtester extends MIDlet implements ExitHook {

    private App app;

    protected void startApp() throws MIDletStateChangeException {
        if (app == null) {
            app = new App(this, this);
            app.start();
        }
    }

    protected void pauseApp() {
        if (app != null) {
            app.pause();
        }
    }

    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
        if (app != null) {
            app.destroy();
        }
    }

    public void requestExit() {
        try {
            destroyApp(true);
        } catch (MIDletStateChangeException e) {
        }
        notifyDestroyed();
    }
}
