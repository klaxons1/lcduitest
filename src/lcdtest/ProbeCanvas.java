package lcdtest;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 * A live UI probe.  The suite is normally run while this Canvas is
 * current so that paint(), showNotify(), hideNotify(), key events and
 * serviceRepaints() can be checked against real implementation
 * behaviour.
 */
public class ProbeCanvas extends Canvas {

    boolean showNotified;
    boolean hideNotified;
    boolean painted;
    int paintCount;
    int sizeChangedCount;
    int lastWidth;
    int lastHeight;

    int showAt = -1;
    int firstPaintAt = -1;
    int eventSeq;

    int gColor = -1;
    int gStroke = -1;
    int gFontIdentity; // 1 = same object as default font, 0 = different
    int gFontFace = -1;
    int gFontSize = -1;
    int gFontStyle = -1;
    int gTranslateX = 9999;
    int gTranslateY = 9999;
    int gClipX = -1;
    int gClipY = -1;
    int gClipW = -1;
    int gClipH = -1;
    int gShownInPaint; // 1 = isShown() true during paint
    String copyAreaException;

    int fullscreenSizeChangedAt = -1;
    int normalW = -1;
    int normalH = -1;
    int fullW = -1;
    int fullH = -1;

    String lastKeyEvent;
    int lastKeyCode = -999;
    int lastPointerX = -1;
    int lastPointerY = -1;

    private final String title;

    ProbeCanvas(String title) {
        this.title = title;
        setTitle(title);
    }

    protected void showNotify() {
        eventSeq++;
        showNotified = true;
        showAt = eventSeq;
    }

    protected void hideNotify() {
        eventSeq++;
        hideNotified = true;
    }

    protected void sizeChanged(int w, int h) {
        sizeChangedCount++;
        lastWidth = w;
        lastHeight = h;
        if (fullscreenSizeChangedAt < 0 && normalW > 0 && (w != normalW || h != normalH)) {
            fullscreenSizeChangedAt = sizeChangedCount;
        }
    }

    protected void paint(javax.microedition.lcdui.Graphics g) {
        eventSeq++;
        paintCount++;
        if (firstPaintAt < 0) {
            firstPaintAt = eventSeq;
            painted = true;
            gColor = g.getColor();
            gStroke = g.getStrokeStyle();
            gTranslateX = g.getTranslateX();
            gTranslateY = g.getTranslateY();
            gClipX = g.getClipX();
            gClipY = g.getClipY();
            gClipW = g.getClipWidth();
            gClipH = g.getClipHeight();
            gShownInPaint = isShown() ? 1 : 0;
            javax.microedition.lcdui.Font f = g.getFont();
            javax.microedition.lcdui.Font d = javax.microedition.lcdui.Font.getDefaultFont();
            gFontIdentity = f == d ? 1 : 0;
            gFontFace = f.getFace();
            gFontSize = f.getSize();
            gFontStyle = f.getStyle();
            try {
                g.copyArea(0, 0, 1, 1, 0, 0,
                        javax.microedition.lcdui.Graphics.TOP
                                | javax.microedition.lcdui.Graphics.LEFT);
                copyAreaException = "none";
            } catch (Throwable t) {
                copyAreaException = t.getClass().getName();
            }
        }
        int w = getWidth();
        int h = getHeight();
        g.setColor(0xFFFFFF);
        g.fillRect(0, 0, w, h);
        g.setColor(0x000000);
        g.drawString(title, w / 2, 2, javax.microedition.lcdui.Graphics.TOP
                | javax.microedition.lcdui.Graphics.HCENTER);
        g.drawString("live probe", w / 2, 2 + javax.microedition.lcdui.Font.getDefaultFont().getHeight(),
                javax.microedition.lcdui.Graphics.TOP
                        | javax.microedition.lcdui.Graphics.HCENTER);
    }

    protected void keyPressed(int keyCode) {
        eventSeq++;
        lastKeyEvent = "pressed";
        lastKeyCode = keyCode;
    }

    protected void keyRepeated(int keyCode) {
        eventSeq++;
        lastKeyEvent = "repeated";
        lastKeyCode = keyCode;
    }

    protected void keyReleased(int keyCode) {
        eventSeq++;
        lastKeyEvent = "released";
        lastKeyCode = keyCode;
    }

    protected void pointerPressed(int x, int y) {
        eventSeq++;
        lastPointerX = x;
        lastPointerY = y;
    }

    protected void pointerDragged(int x, int y) {
        eventSeq++;
        lastPointerX = x;
        lastPointerY = y;
    }

    protected void pointerReleased(int x, int y) {
        eventSeq++;
        lastPointerX = x;
        lastPointerY = y;
    }
}

