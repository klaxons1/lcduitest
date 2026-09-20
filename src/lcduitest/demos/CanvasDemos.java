/**
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * Canvas based visual demos: colours, shapes, text/fonts, image anchors and
 * regions, RGB drawing, clipping/translation, ticker and the game API.
 */
package lcduitest.demos;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.lcdui.game.LayerManager;
import javax.microedition.lcdui.game.Sprite;
import javax.microedition.lcdui.game.TiledLayer;

import lcduitest.Ui;

public class CanvasDemos {

    private CanvasDemos() {
    }

    /** Common plumbing: heading line, hint line, body area. */
    abstract static class Base extends Canvas {

        private final String heading;
        private String hint = "";

        Base(String heading) {
            this.heading = heading;
        }

        protected void hint(String text) {
            hint = text;
        }

        protected int bodyTop() {
            return Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL).getHeight() + 2;
        }

        protected void paint(Graphics g) {
            int w = getWidth();
            int h = getHeight();
            Font small = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
            g.setFont(small);
            g.setColor(0x000000);
            g.fillRect(0, 0, w, bodyTop());
            g.setColor(0xFFFFFF);
            g.drawString(heading, 2, 1, Graphics.TOP | Graphics.LEFT);
            int top = bodyTop();
            int bottom = h;
            if (hint.length() > 0) {
                bottom = h - small.getHeight();
                g.setColor(0x000000);
                g.drawString(hint, 2, bottom, Graphics.TOP | Graphics.LEFT);
            }
            g.setColor(0xFFFFFF);
            g.fillRect(0, top, w, bottom - top);
            g.setColor(0x000000);
            paintBody(g, 0, top, w, bottom - top);
        }

        protected abstract void paintBody(Graphics g, int x, int y, int w, int h);
    }

    /* ------------------------------------------------------------------ */

    static class Colors extends Base {

        Colors() {
            super("Colours");
            hint("bars = numColors palette, right = gray ramp");
        }

        protected void paintBody(Graphics g, int x, int y, int w, int h) {
            Display display = Display.getDisplay(Ui.midlet());
            int colors = 256;
            try {
                colors = display.numColors();
            } catch (Throwable t) {
                colors = 0;
            }
            int count = colors >= 256 ? 32 : (colors >= 16 ? 16 : 4);
            int barHeight = (h - 8) / count;
            if (barHeight < 2) {
                barHeight = 2;
            }
            for (int i = 0; i < count; i++) {
                g.setColor(Paint.hsv(i * 255 / (count - 1), 255, 255));
                g.fillRect(x, y + i * barHeight, w / 2 - 1, barHeight - 1);
            }
            int levels = 16;
            for (int i = 0; i < levels; i++) {
                g.setGrayScale(i * 255 / (levels - 1));
                g.fillRect(x + w / 2, y + i * ((h - 8) / levels), w / 2, (h - 8) / levels - 1);
            }
            g.setGrayScale(0);
            Font small = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
            g.setFont(small);
            int line = small.getHeight();
            int ty = y + h - 6 * line;
            if (ty < y) {
                ty = y;
            }
            g.drawString("colors=" + colors + " alpha=" + display.numAlphaLevels(),
                    x + 2, ty, Graphics.TOP | Graphics.LEFT);
            g.drawString("isColor=" + display.isColor(), x + 2, ty + line, Graphics.TOP | Graphics.LEFT);
            constant(g, display, x + 2, ty + 2 * line, new int[]{Display.COLOR_BACKGROUND,
                Display.COLOR_FOREGROUND, Display.COLOR_BORDER}, "bg/fg/border");
            constant(g, display, x + 2, ty + 4 * line, new int[]{Display.COLOR_HIGHLIGHTED_BACKGROUND,
                Display.COLOR_HIGHLIGHTED_FOREGROUND, Display.COLOR_HIGHLIGHTED_BORDER}, "highl.*");
        }

        private void constant(Graphics g, Display display, int x, int y, int[] ids, String label) {
            StringBuffer sb = new StringBuffer();
            sb.append(label).append(':');
            for (int i = 0; i < ids.length; i++) {
                String value;
                try {
                    value = Paint.hex(display.getColor(ids[i]));
                } catch (Throwable t) {
                    value = "n/a";
                }
                sb.append(' ').append(value);
            }
            g.drawString(sb.toString(), x, y, Graphics.TOP | Graphics.LEFT);
        }
    }

    /* ------------------------------------------------------------------ */

    static class Shapes extends Base {

        Shapes() {
            super("Shapes");
            hint("left: outlined, right: filled");
        }

        protected void paintBody(Graphics g, int x, int y, int w, int h) {
            int left = w / 2;
            g.setColor(0x000000);
            g.setStrokeStyle(Graphics.SOLID);
            g.drawRect(x + 1, y + 1, left - 6, 16);
            g.drawRoundRect(x + 1, y + 20, left - 6, 16, 8, 8);
            g.drawLine(x + 1, y + 40, x + left - 5, y + 48);
            g.drawArc(x + 1, y + 50, 28, 22, 30, 240);
            g.drawArc(x + 1, y + 74, 28, 22, 300, 80);
            // MIDP has no drawTriangle(); the same shape is three lines
            g.drawLine(x + 1, y + 100, x + left - 5, y + 100);
            g.drawLine(x + left - 5, y + 100, left / 2 - x - 1, y + 76);
            g.drawLine(left / 2 - x - 1, y + 76, x + 1, y + 100);
            g.setStrokeStyle(Graphics.DOTTED);
            g.drawRect(x + left + 1, y + 1, left - 6, 16);
            g.drawLine(x + left + 1, y + 40, x + w - 3, y + 48);
            g.setStrokeStyle(Graphics.SOLID);
            g.setColor(0x4040C0);
            g.fillRect(x + left + 1, y + 20, left - 6, 16);
            g.fillRoundRect(x + left + 1, y + 40, left - 6, 14, 8, 8);
            g.fillArc(x + left + 1, y + 56, 32, 32, 0, 120);
            g.fillArc(x + left + 1, y + 56, 32, 32, 240, 120);
            g.fillTriangle(x + left + 1, y + 100, x + w - 3, y + 100, left + left / 2, y + 76);
        }
    }

    /* ------------------------------------------------------------------ */

    static class Text extends Base {

        Text() {
            super("Fonts");
            hint("1=small 2=medium 3=large 4=style 6=face");
        }

        private int size = Font.SIZE_MEDIUM;
        private int face = Font.FACE_SYSTEM;
        private int style = Font.STYLE_PLAIN;

        protected void keyPressed(int keyCode) {
            if (keyCode == Canvas.KEY_NUM1) {
                size = Font.SIZE_SMALL;
            } else if (keyCode == Canvas.KEY_NUM2) {
                size = Font.SIZE_MEDIUM;
            } else if (keyCode == Canvas.KEY_NUM3) {
                size = Font.SIZE_LARGE;
            } else if (keyCode == Canvas.KEY_NUM4 || keyCode == Canvas.KEY_STAR) {
                style = style >= Font.STYLE_UNDERLINED ? Font.STYLE_PLAIN : style + 1;
            } else if (keyCode == Canvas.KEY_NUM6) {
                face = face >= Font.FACE_MONOSPACE ? Font.FACE_SYSTEM : face + 1;
            } else {
                return;
            }
            repaint();
        }

        protected void paintBody(Graphics g, int x, int y, int w, int h) {
            Font font = Font.getFont(face, style, size);
            Font small = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
            int line = small.getHeight() + 1;
            int ty = y + h - 8 * line;
            if (ty < y + font.getHeight() + 6) {
                ty = y + font.getHeight() + 6;
            }
            // the three paint anchors of Graphics for text
            int base = y + 2 + font.getBaselinePosition();
            g.setFont(font);
            g.setColor(0x000000);
            g.drawString("Ag", x + w / 2, base, Graphics.BASELINE | Graphics.HCENTER);
            g.drawChar('|', x + 2, base, Graphics.BASELINE | Graphics.LEFT);
            g.drawString("Ag", x + 2, y + 2, Graphics.TOP | Graphics.LEFT);
            g.drawString("Ag", x + w - 2, y + 2, Graphics.TOP | Graphics.RIGHT);
            g.drawLine(x, base, x + w, base);
            g.drawLine(x, y + 2, x + w, y + 2);

            g.setFont(small);
            g.drawString("face=" + faceName(face) + " style=" + styleName(style)
                    + " size=" + sizeName(size), x + 2, ty, Graphics.TOP | Graphics.LEFT);
            g.drawString("height=" + font.getHeight() + " baseline=" + font.getBaselinePosition(),
                    x + 2, ty + line, Graphics.TOP | Graphics.LEFT);
            g.drawString("charWidth('W')=" + font.charWidth('W') + " stringWidth(\"Ag\")="
                    + font.stringWidth("Ag"), x + 2, ty + 2 * line, Graphics.TOP | Graphics.LEFT);
            char[] chars = {'0', '1', '2', '3', '4', '5'};
            g.drawString("drawChars:", x + 2, ty + 3 * line, Graphics.TOP | Graphics.LEFT);
            g.drawChars(chars, 2, 3, x + 2 + small.stringWidth("drawChars:"), ty + 3 * line, Graphics.TOP | Graphics.LEFT);
            g.drawString("drawSubstring:", x + 2, ty + 4 * line, Graphics.TOP | Graphics.LEFT);
            g.drawSubstring("substring", 3, 5, x + 2 + small.stringWidth("drawSubstring:"),
                    ty + 4 * line, Graphics.TOP | Graphics.LEFT);
            g.drawString("bold=" + font.isBold() + " italic=" + font.isItalic()
                    + " underlined=" + font.isUnderlined(), x + 2, ty + 5 * line,
                    Graphics.TOP | Graphics.LEFT);
            g.drawString("plain=" + font.isPlain(), x + 2, ty + 6 * line, Graphics.TOP | Graphics.LEFT);
        }

        private static String faceName(int face) {
            if (face == Font.FACE_MONOSPACE) {
                return "MONOSPACE";
            }
            if (face == Font.FACE_PROPORTIONAL) {
                return "PROPORTIONAL";
            }
            return "SYSTEM";
        }

        private static String sizeName(int size) {
            if (size == Font.SIZE_SMALL) {
                return "SMALL";
            }
            if (size == Font.SIZE_LARGE) {
                return "LARGE";
            }
            return "MEDIUM";
        }

        private static String styleName(int style) {
            if (style == Font.STYLE_PLAIN) {
                return "PLAIN";
            }
            if (style == Font.STYLE_BOLD) {
                return "BOLD";
            }
            if (style == Font.STYLE_ITALIC) {
                return "ITALIC";
            }
            return "UNDERLINED";
        }
    }

    /* ------------------------------------------------------------------ */

    static class Images extends Base {

        Images() {
            super("Image anchors + regions");
            hint("drawImage anchors, drawRegion transforms");
        }

        protected void paintBody(Graphics g, int x, int y, int w, int h) {
            Image image = Paint.sample();
            int iw = image.getWidth();
            int ih = image.getHeight();
            int[] anchors = {Graphics.TOP | Graphics.LEFT, Graphics.TOP | Graphics.HCENTER,
                Graphics.TOP | Graphics.RIGHT, Graphics.VCENTER | Graphics.LEFT,
                Graphics.VCENTER | Graphics.HCENTER, Graphics.VCENTER | Graphics.RIGHT};
            String[] names = {"TL", "TC", "TR", "CL", "CC", "CR"};
            int cellW = iw * 2 + 8;
            int cellH = ih * 2 + 8;
            for (int i = 0; i < anchors.length; i++) {
                int col = i % 3;
                int row = i / 3;
                int cx = x + 4 + col * cellW + iw;
                int cy = y + 4 + row * cellH + ih;
                g.setColor(0xC0C0C0);
                g.drawRect(cx - iw - 1, cy - ih - 1, iw * 2 + 2, ih * 2 + 2);
                g.drawImage(image, cx, cy, anchors[i]);
                g.setColor(0x808080);
                g.drawString(names[i], cx, cy, Graphics.TOP | Graphics.HCENTER);
            }
            int ty = y + 2 * cellH + 6;
            g.setColor(0x000000);
            g.drawString("drawRegion 0..7:", x + 2, ty, Graphics.TOP | Graphics.LEFT);
            ty += 12;
            for (int t = 0; t < 8; t++) {
                int tx = x + 2 + (t % 4) * (iw + 10);
                int rowY = ty + (t / 4) * (ih + 12);
                g.setColor(0xE0E0E0);
                g.fillRect(tx - 1, rowY - 1, iw + 2, ih + 2);
                g.drawRegion(image, 0, 0, iw, ih, t, tx, rowY, Graphics.TOP | Graphics.LEFT);
                g.drawString(String.valueOf(t), tx, rowY + ih + 1, Graphics.TOP | Graphics.LEFT);
            }
        }
    }

    /* ------------------------------------------------------------------ */

    static class Rgb extends Base {

        Rgb() {
            super("drawRGB and alpha");
            hint("opaque bands, then translucent overlays");
        }

        protected void paintBody(Graphics g, int x, int y, int w, int h) {
            int bandHeight = 12;
            int[] pixels = new int[w];
            for (int band = 0; band < 4; band++) {
                for (int px = 0; px < w; px++) {
                    int level = px * 255 / (w - 1);
                    if (band == 0) {
                        pixels[px] = level << 16;
                    } else if (band == 1) {
                        pixels[px] = level << 8;
                    } else if (band == 2) {
                        pixels[px] = level;
                    } else {
                        pixels[px] = (255 - level) << 16 | level << 8;
                    }
                }
                g.drawRGB(pixels, 0, w, x, y + band * bandHeight, w, bandHeight, false);
            }
            int yy = y + 4 * bandHeight + 4;
            int rows = 20;
            int[] checker = new int[w * rows];
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < w; col++) {
                    boolean dark = ((row / 5) + (col / 10)) % 2 == 0;
                    checker[row * w + col] = dark ? 0x000000 : 0xFFFFFF;
                }
            }
            g.drawRGB(checker, 0, w, x, yy, w, rows, false);
            int[] halfRed = new int[w * 10];
            for (int i = 0; i < halfRed.length; i++) {
                halfRed[i] = 0x80FF0000;
            }
            g.drawRGB(halfRed, 0, w, x, yy + 4, w, 10, true);
            int[] quarterBlue = new int[w * 10];
            for (int i = 0; i < quarterBlue.length; i++) {
                quarterBlue[i] = 0x400000FF;
            }
            g.setColor(0x000000);
            g.drawRGB(quarterBlue, 0, w, x, yy + 14, w, 6, true);
            g.drawString("drawRGB 32x6: ", x + 2, yy + 24, Graphics.TOP | Graphics.LEFT);
            int[] block = new int[32 * 6];
            for (int i = 0; i < block.length; i++) {
                block[i] = 0x00FF00;
            }
            g.setColor(0x000000);
            g.drawRGB(block, 0, 32,
                    x + 2 + Font.getDefaultFont().stringWidth("drawRGB 32x6: "), yy + 24, 32, 6,
                    false);
            g.setColor(0x000000);
            g.drawRGB(block, 0, 32, x + 2, yy + 32, 32, 6, false);
        }
    }

    /* ------------------------------------------------------------------ */

    static class Clipping extends Base {

        Clipping() {
            super("Clipping & translation");
            hint("left/right move the clipped region");
        }

        private int offset = 0;

        protected void keyPressed(int keyCode) {
            int action = getGameAction(keyCode);
            if (action == Canvas.LEFT) {
                offset -= 4;
            } else if (action == Canvas.RIGHT) {
                offset += 4;
            } else if (action == Canvas.FIRE) {
                offset = 0;
            } else {
                return;
            }
            repaint();
        }

        protected void paintBody(Graphics g, int x, int y, int w, int h) {
            int clipX = x + 4 + offset;
            int clipY = y + 14;
            int clipW = w - 20;
            int clipH = h - 34;
            if (clipW < 4 || clipH < 4) {
                clipW = 4;
                clipH = 4;
            }
            int savedX = g.getClipX();
            int savedY = g.getClipY();
            int savedW = g.getClipWidth();
            int savedH = g.getClipHeight();

            g.setClip(clipX, clipY, clipW, clipH);
            g.translate(clipX, clipY);
            g.setColor(0x00A000);
            g.fillRect(0, 0, clipW, clipH);
            g.setColor(0xFFFFFF);
            for (int i = -clipW; i < clipW * 2; i += 10) {
                g.drawLine(i, 0, i - clipH, clipH);
            }
            g.setColor(0x000080);
            g.drawRect(0, 0, clipW - 1, clipH - 1);
            // clipRect intersects the current clip: this must stay inside
            g.setClip(0, 0, clipW / 2, clipH / 2);
            g.setColor(0xFF0000);
            g.fillRect(0, 0, clipW * 2, clipH * 2);
            g.translate(-clipX, -clipY);
            g.setClip(savedX, savedY, savedW, savedH);

            g.setColor(0x000000);
            g.drawString("clip " + g.getClipX() + "," + g.getClipY() + " "
                    + g.getClipWidth() + "x" + g.getClipHeight(), x + 2, y + 2,
                    Graphics.TOP | Graphics.LEFT);
            g.drawString("translate " + g.getTranslateX() + "," + g.getTranslateY(), x + 2,
                    y + h - 14, Graphics.TOP | Graphics.LEFT);
        }
    }

    /* ------------------------------------------------------------------ */

    static class TickerDemo extends Base {

        private int repaints = 0;

        TickerDemo() {
            super("Ticker");
            hint("the ticker above the canvas is animated by the platform");
            setTicker(new javax.microedition.lcdui.Ticker("Ticker on a Canvas - scrolling text -"
                    + " the emulator animation timer moves it - 1234567890 - "));
        }

        protected void showNotify() {
            repaints = 0;
        }

        protected void paintBody(Graphics g, int x, int y, int w, int h) {
            repaints++;
            g.setColor(0x000000);
            g.drawString("paints since showNotify: " + repaints, x + 2, y + 2,
                    Graphics.TOP | Graphics.LEFT);
            g.drawString("getTicker().getString():", x + 2, y + 16, Graphics.TOP | Graphics.LEFT);
            javax.microedition.lcdui.Ticker ticker = getTicker();
            if (ticker != null) {
                g.drawString(ticker.getString(), x + 2, y + 30, Graphics.TOP | Graphics.LEFT);
            }
            g.drawString("The ticker scrolls without repaint() calls", x + 2, y + 46,
                    Graphics.TOP | Graphics.LEFT);
        }
    }

    /* ------------------------------------------------------------------ */

    static class Game extends GameCanvas {

        private final Sprite sprite;
        private final TiledLayer background;
        private final LayerManager manager = new LayerManager();
        private int ticks = 0;
        private Thread animator;

        Game() {
            super(false);
            setTitle("Game API");
            Image sheet = Paint.spriteSheet();
            sprite = new Sprite(sheet, 8, 8);
            sprite.setFrameSequence(new int[]{0, 1, 2, 3, 1, 0});
            sprite.setPosition(20, 40);
            sprite.defineReferencePixel(4, 4);
            background = new TiledLayer(8, 6, Paint.tiles(), 16, 16);
            for (int row = 0; row < 6; row++) {
                for (int col = 0; col < 8; col++) {
                    background.setCell(col, row, ((row + col) % 4) + 1);
                }
            }
            manager.append(background);
            manager.append(sprite);
            setTicker(new javax.microedition.lcdui.Ticker("GameCanvas: flushGraphics + getKeyStates"));
        }

        public void paint(Graphics g) {
            ticks++;
            int keys = getKeyStates();
            int dx = 0;
            int dy = 0;
            if ((keys & LEFT_PRESSED) != 0) {
                dx = -2;
            }
            if ((keys & RIGHT_PRESSED) != 0) {
                dx = 2;
            }
            if ((keys & UP_PRESSED) != 0) {
                dy = -2;
            }
            if ((keys & DOWN_PRESSED) != 0) {
                dy = 2;
            }
            sprite.move(dx, dy);
            sprite.nextFrame();
            g.setColor(0x101040);
            g.fillRect(0, 0, getWidth(), getHeight());
            manager.setViewWindow(0, 0, getWidth(), getHeight());
            manager.paint(g, 0, 0);
            g.setColor(0xFFFFFF);
            Font small = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
            g.setFont(small);
            g.drawString("frame " + sprite.getFrame() + " pos " + sprite.getX() + ","
                    + sprite.getY() + " ticks " + ticks, 2, 2, Graphics.TOP | Graphics.LEFT);
            g.drawString("keyStates=0x" + Integer.toHexString(keys), 2, 2 + small.getHeight(),
                    Graphics.TOP | Graphics.LEFT);
            g.drawString("arrows move the sprite", 2, getHeight() - small.getHeight(),
                    Graphics.TOP | Graphics.LEFT);
            flushGraphics();
        }

        protected void showNotify() {
            if (animator == null) {
                animator = new Thread(new Runnable() {
                    public void run() {
                        while (true) {
                            repaint();
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                return;
                            }
                        }
                    }
                });
                animator.start();
            }
        }

        protected void keyPressed(int keyCode) {
            repaint();
        }

        protected void keyReleased(int keyCode) {
            repaint();
        }
    }
}
