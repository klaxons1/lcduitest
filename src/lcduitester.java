import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;

import lcdtest.Host;
import lcdtest.MenuCanvas;
import lcdtest.Runner;
import lcdtest.Suite;
import lcdtest.Util;

/**
 * LCDUI conformance tester.
 *
 * This MIDlet is intentionally tiny: all logic lives in the lcdtest
 * package so that the whole suite can also be driven from a harness.
 */
public class lcduitester extends MIDlet {

    private MenuCanvas menu;
    private Runner runner;
    private boolean started;

    protected void startApp() {
        if (started) {
            return;
        }
        started = true;
        Display display = Display.getDisplay(this);
        Host.midlet = this;
        Host.display = display;
        Host.args = new String[0];
        Host.catalog = "true".equalsIgnoreCase(
                Host.appProperty("LCDUITest-Catalog", "false"));

        Suite suite = Suite.build();
        menu = new MenuCanvas(display, suite);
        runner = new Runner(this, display, suite, menu);
        menu.setRunner(runner);
        display.setCurrent(menu);

        if (autorun()) {
            runner.setAutorun(true);
            menu.startAutorun();
        }
    }

    private boolean autorun() {
        String v = Host.appProperty("LCDUITest-Autorun", "false");
        if ("true".equalsIgnoreCase(v) || "1".equals(v)) {
            return true;
        }
        String p = Util.prop("lcdtest.autorun");
        return "true".equalsIgnoreCase(p) || "1".equals(p);
    }

    protected void pauseApp() {
        // nothing to release
    }

    protected void destroyApp(boolean unconditional) {
        // nothing to release
    }
}
