/**
 * M3GTester - MIDP 2.0 M3G conformance test MIDlet.
 *
 * Helpers that let a test case talk to the Display. MIDlet suites run on a
 * background thread (the MIDP event thread must stay free so that callSerially
 * runnables, repaints and show/hide notifications can be delivered), so these
 * helpers are the only place where tests wait for the UI.
 *
 * They deliberately use nothing but MIDP 2.0: Display.setCurrent from any
 * thread, Display.callSerially to get back onto the event thread and
 * Object.wait/notify plus Thread.sleep (CLDC 1.1) for synchronisation.
 */
package m3gtest;

import javax.microedition.M3G.Canvas;
import javax.microedition.M3G.Display;
import javax.microedition.M3G.Displayable;
import javax.microedition.M3G.Image;
import javax.microedition.midlet.MIDlet;

public class Ui {

    /** How long a test waits for the event thread before giving up. */
    public static final long SETTLE_MS = 120;
    public static final long TIMEOUT_MS = 5000;

    private static MIDlet midlet;
    private static Display display;

    private Ui() {
    }

    public static void init(MIDlet theMidlet) {
        midlet = theMidlet;
        display = Display.getDisplay(theMidlet);
    }

    public static MIDlet midlet() {
        return midlet;
    }

    public static Display display() {
        return display == null ? Display.getDisplay(midlet) : display;
    }

    /* ------------------------------------------------------------------ */
    /* synchronisation                                                     */
    /* ------------------------------------------------------------------ */

    /** Gives the event thread time to process what the test just asked for. */
    public static void settle() {
        sleep(SETTLE_MS);
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Runs code on the MIDP event thread (Display.callSerially) and waits for
     * it to complete. Must not be called from the event thread itself.
     *
     * @return true if the runnable was executed before the timeout expired.
     */
    public static boolean callSeriallyAndWait(Runnable runnable) {
        return callSeriallyAndWait(runnable, TIMEOUT_MS);
    }

    public static boolean callSeriallyAndWait(final Runnable runnable, long timeout) {
        final Object lock = new Object();
        final boolean[] done = new boolean[1];
        display().callSerially(new Runnable() {
            public void run() {
                try {
                    runnable.run();
                } finally {
                    synchronized (lock) {
                        done[0] = true;
                        lock.notifyAll();
                    }
                }
            }
        });
        long deadline = System.currentTimeMillis() + timeout;
        synchronized (lock) {
            while (!done[0]) {
                long left = deadline - System.currentTimeMillis();
                if (left <= 0) {
                    break;
                }
                try {
                    lock.wait(left);
                } catch (InterruptedException e) {
                    break;
                }
            }
            return done[0];
        }
    }

    /**
     * Waits until a flag that is set from the event thread becomes true.
     * Used by tests for asynchronous callbacks (itemStateChanged, ...).
     */
    public static boolean waitForFlag(final boolean[] flag) {
        return waitForFlag(flag, TIMEOUT_MS);
    }

    public static boolean waitForFlag(final boolean[] flag, long timeout) {
        long deadline = System.currentTimeMillis() + timeout;
        while (!flag[0]) {
            if (System.currentTimeMillis() > deadline) {
                return false;
            }
            sleep(20);
        }
        return true;
    }

    /** Waits for a counter (kept in an int[]) to reach at least value. */
    public static boolean waitForCount(final int[] counter, int value) {
        return waitForCount(counter, value, TIMEOUT_MS);
    }

    public static boolean waitForCount(final int[] counter, int value, long timeout) {
        long deadline = System.currentTimeMillis() + timeout;
        while (counter[0] < value) {
            if (System.currentTimeMillis() > deadline) {
                return false;
            }
            sleep(20);
        }
        return true;
    }

    /* ------------------------------------------------------------------ */
    /* displayables                                                        */
    /* ------------------------------------------------------------------ */

    /** setCurrent + settle; the standard way for a test to show a screen. */
    public static void show(Displayable displayable) {
        display().setCurrent(displayable);
        settle();
    }

    /** Width of the display area, or -1 if the displayable is not shown. */
    public static int width(Displayable displayable) {
        return displayable.getWidth();
    }

    /* ------------------------------------------------------------------ */
    /* images                                                              */
    /* ------------------------------------------------------------------ */

    /** Creates a small mutable image and clears it to white. */
    public static Image mutableImage(int width, int height) {
        Image image = Image.createImage(width, height);
        image.getGraphics().setColor(0xFFFFFF);
        image.getGraphics().fillRect(0, 0, width, height);
        return image;
    }

    /** Mutable image with every pixel of the given colour. */
    public static Image solidImage(int width, int height, int rgb) {
        Image image = Image.createImage(width, height);
        image.getGraphics().setColor(rgb);
        image.getGraphics().fillRect(0, 0, width, height);
        return image;
    }

    /** Reads all pixels of an image as 0x00RRGGBB values. */
    public static int[] pixels(Image image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] rgb = new int[width * height];
        image.getRGB(rgb, 0, width, 0, 0, width, height);
        return rgb;
    }

    public static int pixel(Image image, int x, int y) {
        int[] rgb = new int[1];
        image.getRGB(rgb, 0, 1, x, y, 1, 1);
        return rgb[0] & 0x00FFFFFF;
    }

    public static String hex(int value) {
        String s = Integer.toHexString(value & 0xFFFFFF).toUpperCase();
        while (s.length() < 6) {
            s = "0" + s;
        }
        return "0x" + s;
    }

    /* ------------------------------------------------------------------ */
    /* key names                                                           */
    /* ------------------------------------------------------------------ */

    /** Safe wrapper: a device may not know a key name for an action. */
    public static String actionName(Canvas canvas, int action) {
        switch (action) {
            case Canvas.UP:
                return "UP";
            case Canvas.DOWN:
                return "DOWN";
            case Canvas.LEFT:
                return "LEFT";
            case Canvas.RIGHT:
                return "RIGHT";
            case Canvas.FIRE:
                return "FIRE";
            case Canvas.GAME_A:
                return "GAME_A";
            case Canvas.GAME_B:
                return "GAME_B";
            case Canvas.GAME_C:
                return "GAME_C";
            case Canvas.GAME_D:
                return "GAME_D";
            default:
                return "none";
        }
    }

    public static String keyName(Canvas canvas, int keyCode) {
        try {
            String name = canvas.getKeyName(keyCode);
            return name == null ? "null" : name;
        } catch (Throwable t) {
            return "?" + keyCode;
        }
    }
}
