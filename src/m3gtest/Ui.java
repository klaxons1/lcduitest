/*
 * M3G Tester
 */
package m3gtest;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.midlet.MIDlet;

import javax.microedition.m3g.Graphics3D;

public class Ui {

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

    public static void show(Displayable displayable) {
        display().setCurrent(displayable);
        settle();
    }

    public static Image mutableImage(int width, int height) {
        Image image = Image.createImage(width, height);
        image.getGraphics().setColor(0xFFFFFF);
        image.getGraphics().fillRect(0, 0, width, height);
        return image;
    }

    public static Image solidImage(int width, int height, int rgb) {
        Image image = Image.createImage(width, height);
        image.getGraphics().setColor(rgb);
        image.getGraphics().fillRect(0, 0, width, height);
        return image;
    }

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
            s = \"0\" + s;
        }
        return \"0x\" + s;
    }

    // M3G helpers

    public static boolean isM3GAvailable() {
        try {
            Class.forName(\"javax.microedition.m3g.Graphics3D\");
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Creates a small offscreen rendering to test Graphics3D.
     * Returns true if binding works.
     */
    public static boolean testGraphics3DBind() {
        try {
            Image img = Image.createImage(32, 32);
            Graphics g = img.getGraphics();
            Graphics3D g3d = Graphics3D.getInstance();
            g3d.bindTarget(g);
            g3d.releaseTarget();
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static String actionName(Canvas canvas, int action) {
        switch (action) {
            case Canvas.UP:
                return \"UP\";
            case Canvas.DOWN:
                return \"DOWN\";
            case Canvas.LEFT:
                return \"LEFT\";
            case Canvas.RIGHT:
                return \"RIGHT\";
            case Canvas.FIRE:
                return \"FIRE\";
            case Canvas.GAME_A:
                return \"GAME_A\";
            case Canvas.GAME_B:
                return \"GAME_B\";
            case Canvas.GAME_C:
                return \"GAME_C\";
            case Canvas.GAME_D:
                return \"GAME_D\";
            default:
                return \"none\";
        }
    }
}
