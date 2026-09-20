/**
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * Shared drawing helpers for the demos: images that are built in code so that
 * the demos do not depend on resources, plus small formatting helpers.
 */
package lcduitest.demos;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class Paint {

    private static Image sample;
    private static Image checker;
    private static Image tiles;

    private Paint() {
    }

    /** 12x12 image with a triangle, a square and a border. */
    public static Image sample() {
        if (sample == null) {
            Image image = Image.createImage(12, 12);
            Graphics g = image.getGraphics();
            g.setColor(0xE0E0E0);
            g.fillRect(0, 0, 12, 12);
            g.setColor(0xC03030);
            g.fillTriangle(0, 0, 11, 0, 0, 11);
            g.setColor(0x3050C0);
            g.fillRect(6, 6, 6, 6);
            g.setColor(0x000000);
            g.drawRect(0, 0, 11, 11);
            sample = image;
        }
        return sample;
    }

    /** 32x16 sprite sheet: 4 frames of 8x8 in the top row, 4 below. */
    public static Image spriteSheet() {
        if (checker == null) {
            Image image = Image.createImage(32, 16);
            Graphics g = image.getGraphics();
            for (int row = 0; row < 2; row++) {
                for (int col = 0; col < 4; col++) {
                    g.setColor(((row + col) % 2 == 0) ? 0xFFFFFF : 0x303030);
                    g.fillRect(col * 8, row * 8, 8, 8);
                    g.setColor(0xC00000);
                    g.fillRect(col * 8 + 2, row * 8 + 2, 4, 4);
                }
            }
            checker = image;
        }
        return checker;
    }

    /** 64x16 tile set with four 16x16 tiles. */
    public static Image tiles() {
        if (tiles == null) {
            Image image = Image.createImage(64, 16);
            Graphics g = image.getGraphics();
            for (int t = 0; t < 4; t++) {
                g.setColor(0x102040 + t * 0x003010);
                g.fillRect(t * 16, 0, 16, 16);
                g.setColor(0xFFFFFF);
                g.drawRect(t * 16, 0, 15, 15);
                g.drawLine(t * 16, 0, t * 16 + 15, 15);
            }
            tiles = image;
        }
        return tiles;
    }

    public static String hex(int value) {
        String s = Integer.toHexString(value & 0xFFFFFF).toUpperCase();
        while (s.length() < 6) {
            s = "0" + s;
        }
        return "0x" + s;
    }

    /** hsv (0..255) to 0xRRGGBB, used by the colour demo. */
    public static int hsv(int h, int s, int v) {
        int region = h / 43;
        int remainder = (h - (region * 43)) * 6;
        int p = (v * (255 - s)) >> 8;
        int q = (v * (255 - ((s * remainder) >> 8))) >> 8;
        int t = (v * (255 - ((s * (255 - remainder)) >> 8))) >> 8;
        switch (region) {
            case 0:
                return (v << 16) | (t << 8) | p;
            case 1:
                return (q << 16) | (v << 8) | p;
            case 2:
                return (p << 16) | (v << 8) | t;
            case 3:
                return (p << 16) | (q << 8) | v;
            case 4:
                return (t << 16) | (p << 8) | v;
            default:
                return (v << 16) | (p << 8) | q;
        }
    }
}
