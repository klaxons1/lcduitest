/**
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * javax.microedition.lcdui.Image - creation from scratch, from other images,
 * from PNG data and from an InputStream, plus the pixel access methods.
 */
package lcduitest.tests;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;

import lcduitest.Assert;
import lcduitest.TestCase;
import lcduitest.TestSuite;
import lcduitest.Ui;

public class ImageSuite extends TestSuite {

    /** 4x2 PNG, 8 bit truecolour: red, green, blue, white / black, grey, yellow, magenta. */
    static final byte[] PNG = {-119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82,
        0, 0, 0, 4, 0, 0, 0, 2, 8, 2, 0, 0, 0, -16, -54, -22, 52, 0, 0, 0, 25, 73, 68, 65, 84,
        120, -38, 99, -8, -49, -64, -64, 0, -58, 64, 0, -92, 24, 26, 26, 26, -128, 12, 32, 4, 0,
        -122, -97, 11, 119, 103, 108, -37, 99, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126};

    static final int[] PNG_PIXELS = {0xFF0000, 0x00FF00, 0x0000FF, 0xFFFFFF,
        0x000000, 0x808080, 0xFFFF00, 0xFF00FF};

    public ImageSuite() {
        super("Image", "Images", "mutable and immutable images, PNG decoding, pixel access, "
                + "region copies and createRGBImage");

        add(new TestCase("mutable_image_is_writable") {
            public void run() {
                Image image = Image.createImage(10, 6);
                Assert.assertEquals("width", 10, image.getWidth());
                Assert.assertEquals("height", 6, image.getHeight());
                Assert.assertTrue("an image created with createImage(w,h) is mutable",
                        image.isMutable());
                Graphics g = image.getGraphics();
                Assert.assertNotNull("getGraphics() of a mutable image", g);
                g.setClip(0, 0, 10, 6);
                g.setColor(0x00FF00);
                g.fillRect(0, 0, 10, 6);
                Assert.assertEquals("pixels written through Graphics are readable again",
                        0x00FF00, Ui.pixel(image, 4, 3));
            }
        });

        add(new TestCase("createImage_rejects_zero_or_negative_size") {
            public void run() {
                Assert.expectException("createImage(0, 4)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                Image.createImage(0, 4);
                            }
                        });
                Assert.expectException("createImage(4, 0)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                Image.createImage(4, 0);
                            }
                        });
                Assert.expectException("createImage(-1, 4)", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                Image.createImage(-1, 4);
                            }
                        });
            }
        });

        add(new TestCase("copy_of_an_image_is_an_immutable_snapshot") {
            public void run() {
                Image source = Image.createImage(8, 8);
                Graphics g = source.getGraphics();
                g.setColor(0xFF0000);
                g.fillRect(0, 0, 8, 8);
                final Image copy = Image.createImage(source);
                Assert.assertEquals("width of the copy", 8, copy.getWidth());
                Assert.assertEquals("height of the copy", 8, copy.getHeight());
                Assert.assertFalse("a copy is immutable", copy.isMutable());
                Assert.assertEquals("the copy contains the pixels of the source", 0xFF0000,
                        Ui.pixel(copy, 3, 3));
                g.setColor(0x0000FF);
                g.fillRect(0, 0, 8, 8);
                Assert.assertEquals("the copy is a snapshot: later changes to the source do "
                        + "not show up", 0xFF0000, Ui.pixel(copy, 3, 3));
                Assert.expectException("getGraphics() of an immutable image",
                        IllegalStateException.class, new Assert.Code() {
                            public void run() {
                                copy.getGraphics();
                            }
                        });
                Assert.expectException("createImage((Image) null)", NullPointerException.class,
                        new Assert.Code() {
                            public void run() {
                                Image.createImage((Image) null);
                            }
                        });
            }
        });

        add(new TestCase("createImage_from_a_region_with_transform") {
            public void run() {
                Image source = Image.createImage(4, 4);
                Graphics g = source.getGraphics();
                g.setColor(0xFF0000);
                g.fillRect(0, 0, 2, 2);
                g.setColor(0x00FF00);
                g.fillRect(2, 0, 2, 2);
                g.setColor(0x0000FF);
                g.fillRect(0, 2, 2, 2);
                g.setColor(0xFFFFFF);
                g.fillRect(2, 2, 2, 2);

                Image red = Image.createImage(source, 0, 0, 2, 2, Sprite.TRANS_NONE);
                Assert.assertEquals("the region is 2x2", 2, red.getWidth());
                Assert.assertEquals("the region is 2x2", 2, red.getHeight());
                Assert.assertEquals("the top left region is red", 0xFF0000, Ui.pixel(red, 0, 0));

                Image rotated = Image.createImage(source, 0, 0, 2, 2, Sprite.TRANS_ROT180);
                Assert.assertEquals("TRANS_ROT180 of the red region is white",
                        0xFFFFFF, Ui.pixel(rotated, 0, 0));

                Image mirrored = Image.createImage(source, 0, 0, 2, 2, Sprite.TRANS_MIRROR);
                Assert.assertEquals("TRANS_MIRROR of the red region is green",
                        0x00FF00, Ui.pixel(mirrored, 0, 0));
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("createImage_from_a_region_rejects_bad_arguments") {
            public void run() {
                final Image source = Image.createImage(4, 4);
                Assert.expectException("a region that exceeds the source",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                Image.createImage(source, 2, 2, 4, 4, Sprite.TRANS_NONE);
                            }
                        });
                Assert.expectException("a region with a zero width",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                Image.createImage(source, 0, 0, 0, 2, Sprite.TRANS_NONE);
                            }
                        });
                Assert.expectException("an invalid transform", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                Image.createImage(source, 0, 0, 2, 2, 42);
                            }
                        });
            }
        });

        add(new TestCase("createImage_from_png_data") {
            public void run() {
                Image image = Image.createImage(PNG, 0, PNG.length);
                Assert.assertNotNull("the PNG is decoded", image);
                Assert.assertEquals("width of the decoded PNG", 4, image.getWidth());
                Assert.assertEquals("height of the decoded PNG", 2, image.getHeight());
                for (int i = 0; i < PNG_PIXELS.length; i++) {
                    int x = i % 4;
                    int y = i / 4;
                    Assert.assertEquals("pixel " + x + "," + y + " of the decoded PNG",
                            PNG_PIXELS[i], Ui.pixel(image, x, y));
                }
            }
        });

        add(new TestCase("createImage_from_an_input_stream") {
            public void run() {
                ByteArrayInputStream in = new ByteArrayInputStream(PNG);
                try {
                    Image image = Image.createImage(in);
                    Assert.assertEquals("width of the decoded stream", 4, image.getWidth());
                    Assert.assertEquals("height of the decoded stream", 2, image.getHeight());
                    Assert.assertEquals("first pixel of the decoded stream", 0xFF0000,
                            Ui.pixel(image, 0, 0));
                } catch (IOException e) {
                    Assert.fail("createImage(InputStream)", "IOException: " + e);
                }
            }
        });

        add(new TestCase("createImage_rejects_invalid_data") {
            public void run() {
                final byte[] broken = new byte[PNG.length];
                System.arraycopy(PNG, 0, broken, 0, PNG.length);
                broken[1] = 1;
                Assert.expectException("data that is not a PNG", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                Image.createImage(broken, 0, broken.length);
                            }
                        });
                Assert.expectException("createImage(null, 0, 1)", NullPointerException.class,
                        new Assert.Code() {
                            public void run() {
                                Image.createImage(null, 0, 1);
                            }
                        });
                Assert.expectException("a length beyond the array",
                        ArrayIndexOutOfBoundsException.class, new Assert.Code() {
                            public void run() {
                                Image.createImage(PNG, 4, PNG.length);
                            }
                        });
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("createImage_from_a_resource_name") {
            public void run() {
                Assert.expectException("a resource that does not exist",
                        IOException.class, new Assert.Code() {
                            public void run() throws Exception {
                                Image.createImage("/does-not-exist.png");
                            }
                        });
                try {
                    Image image = Image.createImage("/lcduitest.png");
                    Assert.assertEquals("width of the packaged resource", 4, image.getWidth());
                    Assert.assertEquals("height of the packaged resource", 2, image.getHeight());
                    Assert.assertEquals("first pixel of the packaged resource", 0xFF0000,
                            Ui.pixel(image, 0, 0));
                } catch (IOException e) {
                    Assert.info("the resource /lcduitest.png is not packaged in this jar; "
                            + "copy res/lcduitest.png next to the MIDlet classes to test it");
                }
            }
        });

        add(new TestCase("getRGB_reads_a_region") {
            public void run() {
                Image image = Image.createImage(PNG, 0, PNG.length);
                int[] all = new int[4 * 2];
                image.getRGB(all, 0, 4, 0, 0, 4, 2);
                for (int i = 0; i < all.length; i++) {
                    Assert.assertEquals("getRGB of pixel " + i, PNG_PIXELS[i],
                            all[i] & 0xFFFFFF);
                }
                // a region in the middle, with a scanlength that is larger than the width
                int[] wide = new int[2 * 6];
                image.getRGB(wide, 1, 6, 1, 0, 2, 2);
                Assert.assertEquals("offset and scanlength: first row, first pixel", 0x00FF00,
                        wide[1] & 0xFFFFFF);
                Assert.assertEquals("offset and scanlength: first row, second pixel", 0x0000FF,
                        wide[2] & 0xFFFFFF);
                Assert.assertEquals("offset and scanlength: second row, first pixel", 0x808080,
                        wide[7] & 0xFFFFFF);
                Assert.assertEquals("the padding between the rows stays untouched", 0, wide[3]);
            }
        });

        add(new TestCase("getRGB_rejects_bad_arguments") {
            public void run() {
                final Image image = Image.createImage(PNG, 0, PNG.length);
                Assert.expectException("a region outside the image",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                int[] data = new int[4];
                                image.getRGB(data, 0, 4, 0, 0, 4, 2);
                            }
                        });
                Assert.expectException("a scanlength smaller than the width",
                        IllegalArgumentException.class, new Assert.Code() {
                            public void run() {
                                int[] data = new int[16];
                                image.getRGB(data, 0, 2, 0, 0, 4, 1);
                            }
                        });
                Assert.expectException("getRGB(null, ...)", NullPointerException.class,
                        new Assert.Code() {
                            public void run() {
                                image.getRGB(null, 0, 4, 0, 0, 2, 1);
                            }
                        });
            }
        });

        add(new TestCase("createRGBImage") {
            public void run() {
                int[] rgb = {0xFF0000, 0x00FF00, 0x0000FF, 0xFFFFFF};
                Image image = Image.createRGBImage(rgb, 2, 2, false);
                Assert.assertEquals("width", 2, image.getWidth());
                Assert.assertEquals("height", 2, image.getHeight());
                Assert.assertFalse("images from createRGBImage are immutable", image.isMutable());
                Assert.assertEquals("pixel 0,0", 0xFF0000, Ui.pixel(image, 0, 0));
                Assert.assertEquals("pixel 1,0", 0x00FF00, Ui.pixel(image, 1, 0));
                Assert.assertEquals("pixel 0,1", 0x0000FF, Ui.pixel(image, 0, 1));
                Assert.assertEquals("pixel 1,1", 0xFFFFFF, Ui.pixel(image, 1, 1));
            }
        });

        add(new TestCase("createRGBImage_rejects_bad_arguments") {
            public void run() {
                final int[] rgb = {0xFF0000, 0x00FF00, 0x0000FF, 0xFFFFFF};
                Assert.expectException("width zero", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                Image.createRGBImage(rgb, 0, 2, false);
                            }
                        });
                Assert.expectException("height negative", IllegalArgumentException.class,
                        new Assert.Code() {
                            public void run() {
                                Image.createRGBImage(rgb, 2, -1, false);
                            }
                        });
                Assert.expectException("an array that is too short",
                        ArrayIndexOutOfBoundsException.class, new Assert.Code() {
                            public void run() {
                                Image.createRGBImage(rgb, 4, 4, false);
                            }
                        });
                Assert.expectException("createRGBImage(null, ...)", NullPointerException.class,
                        new Assert.Code() {
                            public void run() {
                                Image.createRGBImage(null, 2, 2, false);
                            }
                        });
            }
        });

        add(new TestCase("alpha_channel_of_createRGBImage") {
            public void run() {
                int[] halfTransparent = {0x80FF0000, 0x80FF0000, 0x80FF0000, 0x80FF0000};
                Image image = Image.createRGBImage(halfTransparent, 2, 2, true);
                Image targetImage = Ui.solidImage(2, 2, 0xFFFFFF);
                Graphics target = targetImage.getGraphics();
                target.drawImage(image, 0, 0, Graphics.TOP | Graphics.LEFT);
                int blended = Ui.pixel(targetImage, 0, 0);
                Assert.info("50% red on white", Ui.hex(blended));
                Assert.assertTrue("a translucent pixel must be composited with the background, "
                        + "was " + Ui.hex(blended), blended != 0xFF0000 && blended != 0xFFFFFF);

                Image opaque = Image.createRGBImage(halfTransparent, 2, 2, false);
                Image target2 = Ui.solidImage(2, 2, 0xFFFFFF);
                Graphics g2 = target2.getGraphics();
                g2.drawImage(opaque, 0, 0, Graphics.TOP | Graphics.LEFT);
                Assert.assertEquals("with processAlpha=false the source is opaque",
                        0xFF0000, Ui.pixel(target2, 0, 0));
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("images_are_shared_by_the_display") {
            public void run() {
                Image image = Ui.solidImage(4, 4, 0x00FF00);
                Assert.info("getWidth/getHeight", image.getWidth() + "x" + image.getHeight());
                Assert.info("alpha levels of the display", Ui.display().numAlphaLevels());
                Assert.assertTrue("a 4 by 4 image must have 16 displayable pixels",
                        image.getWidth() * image.getHeight() == 16);
            }
        }.severity(TestCase.INFO));
    }
}
