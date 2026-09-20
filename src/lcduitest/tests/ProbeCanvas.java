/**
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * A Canvas that counts everything the implementation calls and can replay the
 * callbacks from a subclass (they are protected in Canvas, so only a subclass
 * can invoke them deliberately).
 */
package lcduitest.tests;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;

public class ProbeCanvas extends Canvas {

    public int paintCalls;
    public int showNotifyCalls;
    public int hideNotifyCalls;
    public int sizeChangedCalls;
    public int keyPressedCalls;
    public int keyReleasedCalls;
    public int keyRepeatedCalls;
    public int pointerPressedCalls;
    public int pointerReleasedCalls;
    public int pointerDraggedCalls;
    public int lastKeyCode = Integer.MIN_VALUE;
    public int lastKeyAction = Integer.MIN_VALUE;
    public int lastPointerX = Integer.MIN_VALUE;
    public int lastPointerY = Integer.MIN_VALUE;
    public int lastWidth = Integer.MIN_VALUE;
    public int lastHeight = Integer.MIN_VALUE;
    public boolean lastPaintHadClip;
    public int lastClipX = Integer.MIN_VALUE;
    public int lastClipY = Integer.MIN_VALUE;

    private boolean doubleBuffered = super.isDoubleBuffered();

    protected void paint(Graphics g) {
        paintCalls++;
        lastClipX = g.getClipX();
        lastClipY = g.getClipY();
        lastPaintHadClip = g.getClipWidth() > 0 && g.getClipHeight() > 0;
        g.setColor(0x000000);
        g.fillRect(g.getClipX(), g.getClipY(), g.getClipWidth(), g.getClipHeight());
    }

    protected void showNotify() {
        showNotifyCalls++;
    }

    protected void hideNotify() {
        hideNotifyCalls++;
    }

    protected void sizeChanged(int w, int h) {
        sizeChangedCalls++;
        lastWidth = w;
        lastHeight = h;
    }

    protected void keyPressed(int keyCode) {
        keyPressedCalls++;
        lastKeyCode = keyCode;
        lastKeyAction = safeGameAction(keyCode);
    }

    protected void keyReleased(int keyCode) {
        keyReleasedCalls++;
        lastKeyCode = keyCode;
    }

    protected void keyRepeated(int keyCode) {
        keyRepeatedCalls++;
        lastKeyCode = keyCode;
    }

    protected void pointerPressed(int x, int y) {
        pointerPressedCalls++;
        lastPointerX = x;
        lastPointerY = y;
    }

    protected void pointerReleased(int x, int y) {
        pointerReleasedCalls++;
        lastPointerX = x;
        lastPointerY = y;
    }

    protected void pointerDragged(int x, int y) {
        pointerDraggedCalls++;
        lastPointerX = x;
        lastPointerY = y;
    }

    /* ---------------- deliberate replay, used by the test suites -------- */

    public void fireKeyPressed(int keyCode) {
        keyPressed(keyCode);
    }

    public void fireKeyReleased(int keyCode) {
        keyReleased(keyCode);
    }

    public void fireKeyRepeated(int keyCode) {
        keyRepeated(keyCode);
    }

    public void firePointerPressed(int x, int y) {
        pointerPressed(x, y);
    }

    public void firePointerReleased(int x, int y) {
        pointerReleased(x, y);
    }

    public void firePointerDragged(int x, int y) {
        pointerDragged(x, y);
    }

    public void fireSizeChanged(int width, int height) {
        sizeChanged(width, height);
    }

    public void fireShowNotify() {
        showNotify();
    }

    public void fireHideNotify() {
        hideNotify();
    }

    /** getGameAction that does not throw for an unknown key. */
    public int safeGameAction(int keyCode) {
        try {
            return getGameAction(keyCode);
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    public boolean doubleBuffered() {
        return doubleBuffered;
    }
}
