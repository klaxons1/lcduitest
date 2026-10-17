package lcdtest;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Pixel-level oracle for rasterisation checks.
 *
 * All expectations are expressed as requested RGB values and mapped
 * through Graphics.getDisplayColor(), so the checks remain valid on
 * grayscale, 4096-colour and true-colour implementations.
 */
public class Pix {

    public final int w;
    public final int h;
    public final Image image;
    public final Graphics g;
    private final int bgRequested;

    public Pix(int w, int h) {
        this(w, h, Util.WHITE);
    }

    public Pix(int w, int h, int background) {
        this.w = w;
        this.h = h;
        this.bgRequested = background;
        Image im = null;
        Graphics gr = null;
        try {
            im = Image.createImage(w, h);
            gr = im.getGraphics();
            gr.setColor(background);
            gr.fillRect(0, 0, w, h);
        } catch (Throwable t) {
            // The caller's assertions will fail; never throw out of a
            // helper (it would hide which check failed).
        }
        this.image = im;
        this.g = gr;
    }

    public int mapped(int rgb) {
        return Util.mapped(g, rgb);
    }

    public int background() {
        return mapped(bgRequested);
    }

    /** ARGB pixels, row-major. */
    public int[] snapshot() {
        int[] a = new int[w * h];
        try {
            image.getRGB(a, 0, w, 0, 0, w, h);
        } catch (Throwable t) {
            // leave zeroes; expect() will report
        }
        return a;
    }

    /** Expected buffer filled with the mapped background. */
    public int[] blank() {
        int[] a = new int[w * h];
        int bg = 0xFF000000 | background();
        for (int i = 0; i < a.length; i++) {
            a[i] = bg;
        }
        return a;
    }

    public void fill(int[] a, int x, int y, int width, int height, int rgb) {
        int v = 0xFF000000 | mapped(rgb);
        for (int yy = y; yy < y + height; yy++) {
            if (yy < 0 || yy >= h) {
                continue;
            }
            for (int xx = x; xx < x + width; xx++) {
                if (xx >= 0 && xx < w) {
                    a[yy * w + xx] = v;
                }
            }
        }
    }

    public void set(int[] a, int x, int y, int rgb) {
        if (x >= 0 && x < w && y >= 0 && y < h) {
            a[y * w + x] = 0xFF000000 | mapped(rgb);
        }
    }

    /** Copy a rectangle from a source snapshot into an expected buffer. */
    public static void blit(int[] dst, int dstW, int[] src, int srcW, int srcH,
                            int sx, int sy, int dx, int dy, int cw, int ch) {
        for (int y = 0; y < ch; y++) {
            for (int x = 0; x < cw; x++) {
                int sxx = sx + x;
                int syy = sy + y;
                int dxx = dx + x;
                int dyy = dy + y;
                if (sxx >= 0 && syy >= 0 && sxx < srcW && syy < srcH
                        && dxx >= 0 && dyy >= 0 && dxx < dstW && dyy * dstW + dxx < dst.length) {
                    dst[dyy * dstW + dxx] = src[syy * srcW + sxx];
                }
            }
        }
    }

    /**
     * Compare every pixel against the expected opaque ARGB buffer.
     * Reports at most three offending pixels.
     */
    public void expect(Ctx c, int[] expected, String what) {
        int[] actual = snapshot();
        int bad = 0;
        for (int i = 0; i < expected.length && bad < 3; i++) {
            if (actual[i] != expected[i]) {
                int x = i % w;
                int y = i / w;
                c.isTrue(what + " @" + x + "," + y
                        + " exp=" + Util.hex8(expected[i])
                        + " got=" + Util.hex8(actual[i]), false);
                bad++;
            }
        }
        if (bad == 0) {
            c.isTrue(what, true);
        }
    }

    /** Compare two snapshots (e.g. for transparent-source behaviour). */
    public void expectSame(Ctx c, int[] before, String what) {
        int[] after = snapshot();
        int bad = 0;
        for (int i = 0; i < before.length && bad < 3; i++) {
            if (before[i] != after[i]) {
                c.isTrue(what + " @" + (i % w) + "," + (i / w)
                        + " exp=" + Util.hex8(before[i])
                        + " got=" + Util.hex8(after[i]), false);
                bad++;
            }
        }
        if (bad == 0) {
            c.isTrue(what, true);
        }
    }

    public void expectPixel(Ctx c, int x, int y, int rgb, String what) {
        int[] a = snapshot();
        int expect = 0xFF000000 | mapped(rgb);
        int actual = a[y * w + x];
        c.isTrue(what + " @" + x + "," + y
                + " exp=" + Util.hex8(expect) + " got=" + Util.hex8(actual),
                expect == actual);
    }

    /**
     * Verify that exactly one colour (the mapped colour) is present in
     * the given rectangle of an expected buffer, and nothing else.
     */
    public static boolean uniform(int[] a, int width, int x, int y, int rw, int rh, int argb) {
        for (int yy = y; yy < y + rh; yy++) {
            for (int xx = x; xx < x + rw; xx++) {
                if (a[yy * width + xx] != argb) {
                    return false;
                }
            }
        }
        return true;
    }
}
