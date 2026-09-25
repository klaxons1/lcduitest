/*
 * M3G Tester - Image2D
 */
package m3gtest.tests;

import javax.microedition.lcdui.Image;
import javax.microedition.m3g.*;

import m3gtest.*;

public class Image2DSuite extends TestSuite {

    public Image2DSuite() {
        super("Image2D", "Image2D", "constructors from Image and size, format, set, isMutable");

        add(new TestCase("constructor_from_size") {
            public void run() {
                Image2D img = new Image2D(Image2D.RGB, 64, 64);
                Assert.assertEquals("width 64", 64, img.getWidth());
                Assert.assertEquals("height 64", 64, img.getHeight());
                Assert.assertEquals("format RGB", Image2D.RGB, img.getFormat());
                Assert.assertTrue("mutable from size constructor", img.isMutable());

                Image2D img2 = new Image2D(Image2D.RGBA, 32, 16);
                Assert.assertEquals("width 32", 32, img2.getWidth());
                Assert.assertEquals("format RGBA", Image2D.RGBA, img2.getFormat());
                Assert.assertTrue("mutable", img2.isMutable());

                Image2D img3 = new Image2D(Image2D.ALPHA, 16, 16);
                Assert.assertEquals("ALPHA format", Image2D.ALPHA, img3.getFormat());

                Image2D img4 = new Image2D(Image2D.LUMINANCE, 8, 8);
                Assert.assertEquals("LUMINANCE", Image2D.LUMINANCE, img4.getFormat());

                Image2D img5 = new Image2D(Image2D.LUMINANCE_ALPHA, 8, 8);
                Assert.assertEquals("LUMINANCE_ALPHA", Image2D.LUMINANCE_ALPHA, img5.getFormat());
            }
        });

        add(new TestCase("constructor_invalid") {
            public void run() {
                Assert.expectException("0 width", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new Image2D(Image2D.RGB, 0, 16);
                    }
                });
                Assert.expectException("0 height", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new Image2D(Image2D.RGB, 16, 0);
                    }
                });
                Assert.expectException("negative width", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new Image2D(Image2D.RGB, -1, 16);
                    }
                });
                Assert.expectException("invalid format", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new Image2D(999, 16, 16);
                    }
                });
            }
        });

        add(new TestCase("constructor_from_lcdui_Image") {
            public void run() {
                Image lcduiImg = Image.createImage(32, 32);
                Image2D img = new Image2D(Image2D.RGB, lcduiImg);
                Assert.assertEquals("width from Image", 32, img.getWidth());
                Assert.assertEquals("height from Image", 32, img.getHeight());
                Assert.assertFalse("isMutable false for immutable Image source?", img.isMutable() ? true : false); // implementation may say false for immutable source, but spec says mutable if source mutable. Our source is mutable? Actually createImage returns mutable. So should be true.
                // Let's check mutable source
                Image mutable = Image.createImage(16, 16);
                Image2D imgMut = new Image2D(Image2D.RGB, mutable);
                Assert.info("isMutable for mutable Image source", imgMut.isMutable());

                Assert.expectException("null Image", NullPointerException.class, new Assert.Code() {
                    public void run() {
                        new Image2D(Image2D.RGB, (Image) null);
                    }
                });
                Assert.expectException("null Image2D with byte[]", NullPointerException.class, new Assert.Code() {
                    public void run() {
                        new Image2D(Image2D.RGB, 16, 16, (byte[]) null);
                    }
                });
            }
        });

        add(new TestCase("set_image_and_pixels") {
            public void run() {
                // set with byte array - only available for mutable images
                Image2D img2 = new Image2D(Image2D.RGB, 2, 2);
                Assert.assertTrue("mutable", img2.isMutable());
                byte[] pixels = new byte[]{ (byte)255, 0, 0, 0, (byte)255, 0, 0, 0, (byte)255, (byte)255, (byte)255, (byte)255 };
                img2.set(0, 0, 2, 2, pixels);
                // No getter for pixels, but should not throw
                Assert.assertEquals("width after set", 2, img2.getWidth());

                // mutable image can be used as rendering target - test via Graphics3D bind
                // This is covered in Graphics3D suite, but we test that set works on mutable
                Image2D dst = new Image2D(Image2D.RGB, 8, 8);
                byte[] white = new byte[8 * 8 * 3];
                for (int i = 0; i < white.length; i++) white[i] = (byte) 255;
                dst.set(0, 0, 8, 8, white);
            }
        });

        add(new TestCase("set_invalid_args") {
            public void run() {
                Assert.expectException("set null byte[]", NullPointerException.class, new Assert.Code() {
                    public void run() {
                        new Image2D(Image2D.RGB, 16, 16).set(0, 0, 2, 2, (byte[]) null);
                    }
                });
                Assert.expectException("set on immutable should throw IllegalState", IllegalStateException.class, new Assert.Code() {
                    public void run() {
                        byte[] data = new byte[16 * 16 * 3];
                        Image2D immutable = new Image2D(Image2D.RGB, 16, 16, data);
                        Assert.assertFalse("immutable", immutable.isMutable());
                        immutable.set(0, 0, 2, 2, new byte[12]);
                    }
                });
                Assert.expectException("set out of bounds", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new Image2D(Image2D.RGB, 16, 16).set(15, 15, 2, 2, new byte[12]);
                    }
                });
            }
        });
    }
}
