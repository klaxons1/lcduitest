/**
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * A visual demo: a screen that is meant to be looked at. Rendering cannot be
 * verified by an assertion in every detail (glyph shapes, arc rasterisation,
 * layout are implementation defined), so the demos show the same features that
 * the test suites check numerically.
 */
package lcduitest.demos;

import javax.microedition.lcdui.Displayable;

public abstract class Demo {

    private final String title;

    protected Demo(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    /**
     * Creates a fresh screen. The caller adds a Back command and a command
     * listener, so the demo must not set a command listener of its own.
     */
    public abstract Displayable createScreen();
}
