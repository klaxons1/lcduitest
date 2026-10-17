package lcdtest;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/** Small utilities shared by the checks. */
public final class Util {

    public static final int WHITE = 0xFFFFFF;
    public static final int BLACK = 0x000000;
    public static final int RED = 0xFF0000;
    public static final int GREEN = 0x00FF00;
    public static final int BLUE = 0x0000FF;
    public static final int YELLOW = 0xFFFF00;
    public static final int MAGENTA = 0xFF00FF;
    public static final int CYAN = 0x00FFFF;

    private Util() {
    }

    public static int rgb(int r, int g, int b) {
        return ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    public static int red(int rgb) {
        return (rgb >> 16) & 0xFF;
    }

    public static int green(int rgb) {
        return (rgb >> 8) & 0xFF;
    }

    public static int blue(int rgb) {
        return rgb & 0xFF;
    }

    public static String hex6(int v) {
        String s = Integer.toHexString(v & 0xFFFFFF).toUpperCase();
        while (s.length() < 6) {
            s = "0" + s;
        }
        return "0x" + s;
    }

    public static String hex8(int v) {
        String s = Integer.toHexString(v).toUpperCase();
        while (s.length() < 8) {
            s = "0" + s;
        }
        return "0x" + s;
    }

    public static String padRight(String s, int n) {
        StringBuffer b = new StringBuffer(s == null ? "" : s);
        while (b.length() < n) {
            b.append(' ');
        }
        return b.toString();
    }

    public static String padLeft(String s, int n) {
        StringBuffer b = new StringBuffer();
        for (int i = (s == null ? 0 : s.length()); i < n; i++) {
            b.append(' ');
        }
        b.append(s == null ? "" : s);
        return b.toString();
    }

    public static String prop(String key) {
        try {
            String v = System.getProperty(key);
            return v == null ? "?" : v;
        } catch (Throwable t) {
            return "?";
        }
    }

    public static byte[] readAll(InputStream in) throws java.io.IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[512];
        int n;
        while ((n = in.read(buf)) > 0) {
            out.write(buf, 0, n);
        }
        return out.toByteArray();
    }

    public static String[] splitLines(String text) {
        java.util.Vector v = new java.util.Vector();
        int start = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                v.addElement(text.substring(start, i));
                start = i + 1;
            }
        }
        v.addElement(text.substring(start));
        String[] a = new String[v.size()];
        v.copyInto(a);
        return a;
    }

    /** Wrap text to the pixel width of the given font. */
    public static String[] wrap(String text, Font font, int width) {
        String[] raw = splitLines(text);
        java.util.Vector out = new java.util.Vector();
        for (int i = 0; i < raw.length; i++) {
            String line = raw[i];
            if (line.length() == 0) {
                out.addElement("");
                continue;
            }
            while (line.length() > 0) {
                int fit = fit(line, font, width);
                if (fit >= line.length()) {
                    out.addElement(line);
                    break;
                }
                // Prefer breaking at a space.
                int brk = fit;
                for (int j = fit; j > 0; j--) {
                    if (line.charAt(j - 1) == ' ') {
                        brk = j;
                        break;
                    }
                }
                if (brk < 1) {
                    brk = 1;
                }
                out.addElement(line.substring(0, brk));
                line = line.substring(brk);
                if (line.length() > 0 && line.charAt(0) == ' ') {
                    line = line.substring(1);
                }
            }
        }
        String[] a = new String[out.size()];
        out.copyInto(a);
        return a;
    }

    private static int fit(String s, Font f, int width) {
        if (width <= 0) {
            return 1;
        }
        int lo = 0;
        int hi = s.length();
        while (lo < hi) {
            int mid = (lo + hi + 1) / 2;
            if (f.substringWidth(s, 0, mid) <= width) {
                lo = mid;
            } else {
                hi = mid - 1;
            }
        }
        return lo == 0 ? 1 : lo;
    }

    /**
     * Find requested colours that the device can actually display as
     * distinct colours.  Returns the requested RGB values (not the
     * device-mapped values) or null when the device palette is too
     * small for the check.
     */
    public static int[] distinctPalette(Display display, int[] candidates, int need) {
        Image im;
        try {
            im = Image.createImage(1, 1);
        } catch (Throwable t) {
            return null;
        }
        Graphics g = im.getGraphics();
        int[] req = new int[candidates.length];
        int[] mapped = new int[candidates.length];
        int n = 0;
        for (int i = 0; i < candidates.length && n < need; i++) {
            int m = g.getDisplayColor(candidates[i]) & 0xFFFFFF;
            boolean clash = false;
            for (int j = 0; j < n; j++) {
                if (mapped[j] == m) {
                    clash = true;
                    break;
                }
            }
            if (!clash) {
                req[n] = candidates[i];
                mapped[n] = m;
                n++;
            }
        }
        if (n < need) {
            return null;
        }
        int[] r = new int[need];
        System.arraycopy(req, 0, r, 0, need);
        return r;
    }

    public static int mapped(Graphics g, int rgb) {
        return g.getDisplayColor(rgb) & 0xFFFFFF;
    }

    public static String fontSpec(Font f) {
        return "face=" + faceName(f.getFace()) + " style=" + f.getStyle()
                + " size=" + sizeName(f.getSize()) + " h=" + f.getHeight()
                + " baseline=" + f.getBaselinePosition();
    }

    public static String faceName(int face) {
        switch (face) {
            case Font.FACE_SYSTEM: return "system";
            case Font.FACE_MONOSPACE: return "monospace";
            case Font.FACE_PROPORTIONAL: return "proportional";
            default: return "?" + face;
        }
    }

    public static String sizeName(int size) {
        switch (size) {
            case Font.SIZE_SMALL: return "small";
            case Font.SIZE_MEDIUM: return "medium";
            case Font.SIZE_LARGE: return "large";
            default: return "?" + size;
        }
    }

    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (Throwable t) {
            // ignore
        }
    }
}
