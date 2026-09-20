/*
 * LCDUI Tester - a MIDP 2.0 MIDlet that runs conformance tests against the
 * javax.microedition.lcdui implementation of a device or emulator.
 *
 * The class name and the jar entry point are kept from the original project
 * (MIDlet-1: LCDUITester,,lcduitester in META-INF/MANIFEST.MF), all the logic
 * lives in the lcduitest.* packages.
 *
 * Start it normally to get the menu, or with the system property / JAD
 * attribute lcduitest.autorun=true to run every test automatically and print
 * a machine readable log on the standard output (see README.md).
 */

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import lcduitest.ExitHook;
import lcduitest.ui.App;

public class lcduitester extends MIDlet implements ExitHook {

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

    /** Called by the UI in automated mode (lcduitest.exit=true). */
    public void requestExit() {
        try {
            destroyApp(true);
        } catch (MIDletStateChangeException e) {
            // never thrown for unconditional destroy
        }
        notifyDestroyed();
    }
}
