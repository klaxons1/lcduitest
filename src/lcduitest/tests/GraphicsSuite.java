/**
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * javax.microedition.lcdui.Graphics - every drawing primitive, the colour
 * model, the clipping rectangle, translation, anchors, fonts and the RGB
 * methods. The tests draw into mutable Images and read the pixels back, which
 * turns "does the primitive work" into something that can be asserted.
 */
package lcduitest.tests;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;

import lcduitest.Assert;
import lcduitest.TestCase;
import lcduitest.TestSuite;
import lcduitest.Ui;

public class GraphicsSuite extends TestSuite {

    private static final int W = 32;
    private static final int H = 32;
    private static final int RED = 0xFF0000;
    private static final int GREEN = 0x00FF00;

    public GraphicsSuite() {
        super("Graphics", "Graphics", "the initial state, colours, grayscale, rectangles, "
                + "lines, arcs, triangles, rounded rectangles, text, fonts, clipping, "
                + "translation, images, regions, copying and drawRGB");

        /* ---------------------------- initial state ---------------------- */

        add(new TestCase("initial_state_of_a_new_graphics_object") {
            public void run() {
                Image image = Image.createImage(W, H);
                Graphics g = image.getGraphics();
                Assert.assertEquals("the initial clip covers the whole image (x)",
                        0, g.getClipX());
                Assert.assertEquals("the initial clip covers the whole image (y)",
                        0, g.getClipY());
                Assert.assertEquals("the initial clip width", W, g.getClipWidth());
                Assert.assertEquals("the initial clip height", H, g.getClipHeight());
                Assert.assertEquals("the initial translation (x)", 0, g.getTranslateX());
                Assert.assertEquals("the initial translation (y)", 0, g.getTranslateY());
                Assert.assertEquals("the initial colour is black", 0x000000, g.getColor());
                Assert.assertEquals("the initial stroke style is SOLID", Graphics.SOLID,
                        g.getStrokeStyle());
                Font font = g.getFont();
                Assert.assertNotNull("there is always a current font", font);
                Font expected = Font.getDefaultFont();
                Assert.assertEquals("the initial font is the default font (height)",
                        expected.getHeight(), font.getHeight());
                Assert.assertEquals("the initial font is the default font (size)",
                        expected.getSize(), font.getSize());
                Assert.assertEquals("the initial font is the default font (style)",
                        expected.getStyle(), font.getStyle());
            }
        });

        /* ---------------------------- colours ---------------------------- */

        add(new TestCase("setColor_int_and_the_components") {
            public void run() {
                Graphics g = newImage().getGraphics();
                g.setColor(0x123456);
                Assert.assertEquals("getColor()", 0x123456, g.getColor());
                Assert.assertEquals("getRedComponent()", 0x12, g.getRedComponent());
                Assert.assertEquals("getGreenComponent()", 0x34, g.getGreenComponent());
                Assert.assertEquals("getBlueComponent()", 0x56, g.getBlueComponent());
                g.setColor(0xFFFFFF);
                Assert.assertEquals("white", 0xFFFFFF, g.getColor());
                g.setColor(0x000000);
                Assert.assertEquals("black", 0x000000, g.getColor());
            }
        });

        add(new TestCase("setColor_ignores_the_high_byte") {
            public void run() {
                Graphics g = newImage().getGraphics();
                g.setColor(0xFF00FF00);
                Assert.info("getColor() after setColor(0xFF00FF00)", Ui.hex(g.getColor()));
                Assert.assertEquals("the high order byte is not part of the colour",
                        0x00FF00, g.getColor() & 0xFFFFFF);
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("setColor_components_and_range_checks") {
            public void run() {
                Graphics g = newImage().getGraphics();
                g.setColor(0x11, 0x22, 0x33);
                Assert.assertEquals("setColor(r, g, b)", 0x112233, g.getColor());
                g.setColor(0, 0, 0);
                Assert.assertEquals("black", 0x000000, g.getColor());
                g.setColor(255, 255, 255);
                Assert.assertEquals("white", 0xFFFFFF, g.getColor());
                Assert.expectException("a red component of 256",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                newImage().getGraphics().setColor(256, 0, 0);
                            }
                        });
                Assert.expectException("a green component of -1",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                newImage().getGraphics().setColor(0, -1, 0);
                            }
                        });
                Assert.expectException("a blue component of 1000",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                newImage().getGraphics().setColor(0, 0, 1000);
                            }
                        });
            }
        });

        add(new TestCase("grayscale") {
            public void run() {
                Graphics g = newImage().getGraphics();
                g.setGrayScale(0);
                Assert.assertEquals("setGrayScale(0)", 0, g.getGrayScale());
                g.setGrayScale(255);
                Assert.assertEquals("setGrayScale(255)", 255, g.getGrayScale());
                g.setGrayScale(128);
                Assert.assertEquals("setGrayScale(128)", 128, g.getGrayScale());
                Assert.assertEquals("setGrayScale() also sets a colour with equal "
                        + "components", 0x808080, g.getColor());
                Assert.expectException("setGrayScale(256)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                newImage().getGraphics().setGrayScale(256);
                            }
                        });
                Assert.expectException("setGrayScale(-1)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                newImage().getGraphics().setGrayScale(-1);
                            }
                        });
            }
        }.severity(TestCase.SHOULD));

        /* ---------------------------- primitives ------------------------- */

        add(new TestCase("fillRect_and_drawRect") {
            public void run() {
                Image image = Ui.solidImage(W, H, 0xFFFFFF);
                Graphics g = image.getGraphics();
                g.setColor(RED);
                g.fillRect(2, 3, 4, 5);
                Assert.assertEquals("the top left pixel of the filled rectangle", RED,
                        Ui.pixel(image, 2, 3));
                Assert.assertEquals("the bottom right pixel of the filled rectangle", RED,
                        Ui.pixel(image, 5, 7));
                Assert.assertEquals("one pixel to the left is untouched", 0xFFFFFF,
                        Ui.pixel(image, 1, 3));
                Assert.assertEquals("one pixel below is untouched", 0xFFFFFF,
                        Ui.pixel(image, 2, 8));

                Image other = Ui.solidImage(W, H, 0xFFFFFF);
                Graphics g2 = other.getGraphics();
                g2.setColor(GREEN);
                g2.drawRect(2, 3, 4, 5);
                Assert.assertEquals("the outline is drawn", GREEN, Ui.pixel(other, 2, 3));
                Assert.assertEquals("the outline is drawn", GREEN, Ui.pixel(other, 5, 7));
                Assert.assertEquals("the inside stays empty", 0xFFFFFF, Ui.pixel(other, 3, 4));
            }
        });

        add(new TestCase("fillRect_with_a_negative_or_zero_size") {
            public void run() {
                Image image = Ui.solidImage(W, H, 0xFFFFFF);
                Graphics g = image.getGraphics();
                g.setColor(RED);
                g.fillRect(5, 5, 0, 0);
                g.fillRect(5, 5, -3, -3);
                Assert.assertEquals("a zero or negative size draws nothing", 0xFFFFFF,
                        Ui.pixel(image, 5, 5));
            }
        });

        add(new TestCase("drawLine") {
            public void run() {
                Image image = Ui.solidImage(W, H, 0xFFFFFF);
                Graphics g = image.getGraphics();
                g.setColor(RED);
                g.drawLine(1, 1, 5, 1);
                for (int x = 1; x <= 5; x++) {
                    Assert.assertEquals("the horizontal line at x = " + x, RED,
                            Ui.pixel(image, x, 1));
                }
                Assert.assertEquals("above the line", 0xFFFFFF, Ui.pixel(image, 3, 0));
                Assert.assertEquals("below the line", 0xFFFFFF, Ui.pixel(image, 3, 2));
            }
        });

        add(new TestCase("arcs_triangles_and_rounded_rectangles_draw_something") {
            public void run() {
                Image image = Ui.solidImage(W, H, 0xFFFFFF);
                Graphics g = image.getGraphics();
                g.setColor(RED);
                g.drawArc(1, 1, 10, 10, 0, 360);
                g.fillArc(14, 1, 10, 10, 0, 180);
                g.drawLine(1, 14, 10, 14);
                g.drawLine(10, 14, 5, 24);
                g.drawLine(5, 24, 1, 14);
                g.fillTriangle(14, 14, 24, 14, 19, 24);
                g.drawRoundRect(1, 26, 10, 5, 4, 4);
                g.fillRoundRect(14, 26, 10, 5, 4, 4);
                int painted = 0;
                for (int y = 0; y < H; y++) {
                    for (int x = 0; x < W; x++) {
                        if (Ui.pixel(image, x, y) != 0xFFFFFF) {
                            painted++;
                        }
                    }
                }
                Assert.info("pixels changed by the shapes", painted);
                Assert.assertTrue("drawArc, fillArc, drawLine, fillTriangle, "
                        + "drawRoundRect and fillRoundRect must paint at least one pixel",
                        painted > 0);
            }
        }.severity(TestCase.SHOULD).describe("the exact rasterisation of arcs and "
                + "triangles is implementation defined, only 'something is drawn' is "
                + "checked"));

        add(new TestCase("stroke_style") {
            public void run() {
                Graphics g = newImage().getGraphics();
                Assert.assertEquals("the default stroke style is SOLID", Graphics.SOLID,
                        g.getStrokeStyle());
                g.setStrokeStyle(Graphics.DOTTED);
                Assert.assertEquals("setStrokeStyle(DOTTED)", Graphics.DOTTED,
                        g.getStrokeStyle());
                g.setStrokeStyle(Graphics.SOLID);
                Assert.assertEquals("setStrokeStyle(SOLID)", Graphics.SOLID,
                        g.getStrokeStyle());
                Assert.expectException("setStrokeStyle(2)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                newImage().getGraphics().setStrokeStyle(2);
                            }
                        });
            }
        });

        /* ---------------------------- text and fonts --------------------- */

        add(new TestCase("text_rendering") {
            public void run() {
                Image image = Ui.solidImage(80, 40, 0xFFFFFF);
                Graphics g = image.getGraphics();
                g.setColor(0x000000);
                g.drawString("LcduiTest", 0, 0, Graphics.TOP | Graphics.LEFT);
                g.drawChar('X', 0, 20, Graphics.TOP | Graphics.LEFT);
                g.drawChars(new char[]{'a', 'b', 'c'}, 0, 3, 20, 20, Graphics.TOP
                        | Graphics.LEFT);
                g.drawSubstring("substring", 3, 5, 40, 20, Graphics.TOP | Graphics.LEFT);
                int painted = 0;
                for (int y = 0; y < 40; y++) {
                    for (int x = 0; x < 80; x++) {
                        if (Ui.pixel(image, x, y) != 0xFFFFFF) {
                            painted++;
                        }
                    }
                }
                Assert.assertTrue("drawString, drawChar, drawChars and drawSubstring must "
                        + "paint pixels, painted " + painted, painted > 0);
            }
        }.severity(TestCase.SHOULD).describe("glyph shapes are implementation defined"));

        add(new TestCase("text_anchors") {
            public void run() {
                Image image = Ui.solidImage(80, 40, 0xFFFFFF);
                Graphics g = image.getGraphics();
                g.setColor(0x000000);
                Font font = Font.getDefaultFont();
                int[] anchors = {Graphics.TOP | Graphics.LEFT, Graphics.TOP | Graphics.HCENTER,
                    Graphics.TOP | Graphics.RIGHT, Graphics.BOTTOM | Graphics.LEFT,
                    Graphics.BOTTOM | Graphics.HCENTER, Graphics.BOTTOM | Graphics.RIGHT,
                    Graphics.BASELINE | Graphics.LEFT, Graphics.VCENTER | Graphics.HCENTER};
                for (int i = 0; i < anchors.length; i++) {
                    Assert.expectNoException("drawString with anchor 0x"
                            + Integer.toHexString(anchors[i]), new Assert.Code() {
                                public void run() {
                                }
                            });
                    g.drawString("Ag", 40, 20, anchors[i]);
                }
                Assert.expectException("drawString with anchor 0",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                newImage().getGraphics().drawString("X", 0, 0, 0);
                            }
                        });
                Assert.expectException("drawString with a null string",
                        NullPointerException.class, new Assert.Code() {
                            public void run() {
                                newImage().getGraphics().drawString(null, 0, 0,
                                        Graphics.TOP | Graphics.LEFT);
                            }
                        });
                Assert.assertTrue("the font has a positive height", font.getHeight() > 0);
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("font_handling") {
            public void run() {
                Graphics g = newImage().getGraphics();
                Font bold = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_LARGE);
                g.setFont(bold);
                Font current = g.getFont();
                Assert.assertEquals("setFont() changes the current font (style)",
                        bold.getStyle(), current.getStyle());
                Assert.assertEquals("setFont() changes the current font (size)",
                        bold.getSize(), current.getSize());
                Assert.assertEquals("setFont() changes the current font (face)",
                        bold.getFace(), current.getFace());
                g.setFont(null);
                Font fallback = g.getFont();
                Assert.assertNotNull("setFont(null) leaves a usable font", fallback);
                Assert.assertEquals("setFont(null) is setFont(Font.getDefaultFont())",
                        Font.getDefaultFont().getHeight(), fallback.getHeight());
            }
        });

        /* ---------------------------- clipping and translation ----------- */

        add(new TestCase("clipping") {
            public void run() {
                Image image = Ui.solidImage(W, H, 0xFFFFFF);
                Graphics g = image.getGraphics();
                g.setClip(4, 4, 6, 6);
                Assert.assertEquals("getClipX()", 4, g.getClipX());
                Assert.assertEquals("getClipY()", 4, g.getClipY());
                Assert.assertEquals("getClipWidth()", 6, g.getClipWidth());
                Assert.assertEquals("getClipHeight()", 6, g.getClipHeight());
                g.setColor(RED);
                g.fillRect(0, 0, W, H);
                Assert.assertEquals("inside the clip the fill is painted", RED,
                        Ui.pixel(image, 5, 5));
                Assert.assertEquals("outside the clip nothing is painted", 0xFFFFFF,
                        Ui.pixel(image, 0, 0));
                Assert.assertEquals("just outside the clip", 0xFFFFFF, Ui.pixel(image, 10, 10));

                g.clipRect(6, 6, 2, 2);
                Assert.assertEquals("clipRect() intersects the clip (x)", 6, g.getClipX());
                Assert.assertEquals("clipRect() intersects the clip (width)", 2,
                        g.getClipWidth());
                g.clipRect(0, 0, W, H);
                Assert.assertEquals("clipRect() can not make the clip larger", 6,
                        g.getClipX());
                Assert.assertEquals("clipRect() can not make the clip larger", 2,
                        g.getClipWidth());
                g.setClip(0, 0, W, H);
                Assert.assertEquals("setClip() can make the clip larger again", W,
                        g.getClipWidth());
            }
        });

        add(new TestCase("translation") {
            public void run() {
                Image image = Ui.solidImage(W, H, 0xFFFFFF);
                Graphics g = image.getGraphics();
                g.translate(3, 4);
                Assert.assertEquals("getTranslateX()", 3, g.getTranslateX());
                Assert.assertEquals("getTranslateY()", 4, g.getTranslateY());
                Assert.assertEquals("the clip is not affected by translate()", 0, g.getClipX());
                g.setColor(RED);
                g.fillRect(0, 0, 2, 2);
                Assert.assertEquals("drawing coordinates are translated", RED,
                        Ui.pixel(image, 3, 4));
                Assert.assertEquals("the untranslated origin is untouched", 0xFFFFFF,
                        Ui.pixel(image, 0, 0));
                g.translate(-3, -4);
                Assert.assertEquals("translate() is relative", 0, g.getTranslateX());
                Assert.assertEquals("translate() is relative", 0, g.getTranslateY());
                g.fillRect(0, 0, 2, 2);
                Assert.assertEquals("after translating back the origin is painted", RED,
                        Ui.pixel(image, 0, 0));
            }
        });

        /* ---------------------------- images ----------------------------- */

        add(new TestCase("drawImage_with_anchors") {
            public void run() {
                Image source = newSample();
                Image target = Ui.solidImage(W, H, 0xFFFFFF);
                Graphics g = target.getGraphics();
                g.drawImage(source, 0, 0, Graphics.TOP | Graphics.LEFT);
                Assert.assertEquals("the first pixel of the source image", 0xFF0000,
                        Ui.pixel(target, 0, 0));
                Assert.assertEquals("the last pixel of the source image", 0x00FFFF,
                        Ui.pixel(target, 3, 1));
                Assert.expectException("drawImage(null, ...)", NullPointerException.class,
                        new Assert.Code() {
                            public void run() {
                                newImage().getGraphics().drawImage(null, 0, 0,
                                        Graphics.TOP | Graphics.LEFT);
                            }
                        });
                Assert.expectException("drawImage with an invalid anchor",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                newImage().getGraphics().drawImage(newSample(), 0, 0, 0);
                            }
                        });
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("drawImage_anchor_positions") {
            public void run() {
                Image source = newSample();
                int width = source.getWidth();
                int height = source.getHeight();

                Image bottomRight = Ui.solidImage(W, H, 0xFFFFFF);
                bottomRight.getGraphics().drawImage(source, 20, 20,
                        Graphics.BOTTOM | Graphics.RIGHT);
                Assert.assertEquals("BOTTOM|RIGHT puts the top left corner at "
                        + "(20 - width, 20 - height)", 0xFF0000, Ui.pixel(bottomRight,
                        20 - width, 20 - height));

                Image centered = Ui.solidImage(W, H, 0xFFFFFF);
                centered.getGraphics().drawImage(source, 16, 16,
                        Graphics.VCENTER | Graphics.HCENTER);
                Assert.assertEquals("VCENTER|HCENTER centers the image on the anchor",
                        0xFF0000, Ui.pixel(centered, 16 - width / 2, 16 - height / 2));
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("drawRegion_with_transforms") {
            public void run() {
                Image source = newSample();          // 4 x 2, red green blue white /
                Image target = Ui.solidImage(W, H, 0xFFFFFF);  // black grey yellow magenta
                Graphics g = target.getGraphics();
                g.drawRegion(source, 0, 0, 4, 2, Sprite.TRANS_NONE, 0, 0,
                        Graphics.TOP | Graphics.LEFT);
                Assert.assertEquals("TRANS_NONE copies the pixels", 0xFF0000,
                        Ui.pixel(target, 0, 0));
                Assert.assertEquals("TRANS_NONE copies the pixels", 0x00FFFF,
                        Ui.pixel(target, 3, 1));

                Image mirrored = Ui.solidImage(W, H, 0xFFFFFF);
                mirrored.getGraphics().drawRegion(source, 0, 0, 4, 2, Sprite.TRANS_MIRROR, 0, 0,
                        Graphics.TOP | Graphics.LEFT);
                Assert.assertEquals("TRANS_MIRROR mirrors horizontally", 0xFFFFFF,
                        Ui.pixel(mirrored, 0, 0));

                Image rotated = Ui.solidImage(W, H, 0xFFFFFF);
                rotated.getGraphics().drawRegion(source, 0, 0, 4, 2, Sprite.TRANS_ROT180, 4, 2,
                        Graphics.TOP | Graphics.LEFT);
                Assert.assertEquals("TRANS_ROT180 is equivalent to a point reflection",
                        0x00FFFF, Ui.pixel(rotated, 0, 0));
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("drawRegion_argument_checks") {
            public void run() {
                final Image source = newSample();
                final Image target = Ui.solidImage(W, H, 0xFFFFFF);
                Assert.expectException("a transform that does not exist",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                target.getGraphics().drawRegion(source, 0, 0, 4, 2, 8, 0, 0,
                                        Graphics.TOP | Graphics.LEFT);
                            }
                        });
                Assert.expectException("a region that is larger than the source",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                target.getGraphics().drawRegion(source, 0, 0, 4, 20,
                                        Sprite.TRANS_NONE, 0, 0, Graphics.TOP | Graphics.LEFT);
                            }
                        });
                Assert.expectException("source and destination are the same image",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                source.getGraphics().drawRegion(source, 0, 0, 2, 2,
                                        Sprite.TRANS_NONE, 2, 0, Graphics.TOP | Graphics.LEFT);
                            }
                        });
                Assert.expectException("drawRegion(null, ...)", NullPointerException.class,
                        new Assert.Code() {
                            public void run() {
                                target.getGraphics().drawRegion(null, 0, 0, 1, 1,
                                        Sprite.TRANS_NONE, 0, 0, Graphics.TOP | Graphics.LEFT);
                            }
                        });
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("copyArea") {
            public void run() {
                Image image = Ui.solidImage(16, 16, 0xFFFFFF);
                Graphics g = image.getGraphics();
                g.setColor(RED);
                g.fillRect(1, 1, 3, 3);
                g.copyArea(1, 1, 3, 3, 8, 8, Graphics.TOP | Graphics.LEFT);
                Assert.assertEquals("the copied area contains the source pixels", RED,
                        Ui.pixel(image, 8, 8));
                Assert.assertEquals("the copied area contains the source pixels", RED,
                        Ui.pixel(image, 10, 10));
                Assert.assertEquals("the source is still there", RED, Ui.pixel(image, 1, 1));
                Assert.assertEquals("next to the copy nothing changed", 0xFFFFFF,
                        Ui.pixel(image, 11, 11));

                final Graphics copy = image.getGraphics();
                Assert.expectException("a source region that is outside the image",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                copy.copyArea(14, 14, 4, 4, 0, 0, Graphics.TOP | Graphics.LEFT);
                            }
                        });
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("drawRGB") {
            public void run() {
                int[] rgb = {0x00FF0000, 0x0000FF00, 0x000000FF, 0x00FFFF00};
                Image image = Ui.solidImage(8, 8, 0xFFFFFF);
                Graphics g = image.getGraphics();
                g.drawRGB(rgb, 0, 2, 0, 0, 2, 2, false);
                Assert.assertEquals("the first pixel", 0xFF0000, Ui.pixel(image, 0, 0));
                Assert.assertEquals("the second pixel", 0x00FF00, Ui.pixel(image, 1, 0));
                Assert.assertEquals("the pixel of the second row", 0x0000FF,
                        Ui.pixel(image, 0, 1));

                int[] withAlpha = {0x00FF0000, 0x0000FF00};
                Image blend = Ui.solidImage(4, 4, 0xFFFFFF);
                Graphics g2 = blend.getGraphics();
                g2.drawRGB(withAlpha, 0, 2, 0, 0, 2, 1, true);
                Assert.assertEquals("a fully transparent pixel leaves the destination "
                        + "untouched", 0xFFFFFF, Ui.pixel(blend, 0, 0));
                Assert.assertEquals("a fully opaque pixel is copied", 0x00FF00,
                        Ui.pixel(blend, 1, 0));
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("getDisplayColor") {
            public void run() {
                Graphics g = newImage().getGraphics();
                int[] colours = {0x000000, 0xFFFFFF, 0xFF0000, 0x00FF00, 0x0000FF, 0x123456};
                for (int i = 0; i < colours.length; i++) {
                    int shown = g.getDisplayColor(colours[i]);
                    Assert.assertEquals("getDisplayColor must return a 0x00RRGGBB value "
                            + "for " + Ui.hex(colours[i]), 0, shown & 0xFF000000);
                    Assert.info("requested " + Ui.hex(colours[i]), Ui.hex(shown));
                }
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("drawing_into_a_graphic_that_has_an_image_destination") {
            public void run() {
                Image image = Image.createImage(8, 8);
                Graphics g = image.getGraphics();
                Assert.assertEquals("a fresh image is white", 0xFFFFFF, Ui.pixel(image, 0, 0));
                g.setColor(0x000000);
                g.fillRect(0, 0, 8, 8);
                Assert.assertEquals("after filling it is black", 0x000000,
                        Ui.pixel(image, 7, 7));
            }
        });
    }

    /** 32x32 white image. */
    private static Image newImage() {
        return Ui.solidImage(W, H, 0xFFFFFF);
    }

    /** A 4x2 image with the colours red, green, blue, white / black, grey, yellow, cyan. */
    private static Image newSample() {
        int[] rgb = {0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFFFFFFFF,
            0xFF000000, 0xFF808080, 0xFFFFFF00, 0xFF00FFFF};
        return Image.createRGBImage(rgb, 4, 2, false);
    }
}
