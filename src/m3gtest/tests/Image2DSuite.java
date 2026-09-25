package m3gtest.tests;

import javax.microedition.m3g.*;
import m3gtest.*;

public class Image2DSuite extends TestSuite {

    public Image2DSuite() {
        super("image2d", "Image2D", "Tests Image2D.");

        add(new TestCase("ctor RGB immutable") {
            public void run() {
                byte[] data = new byte[3 * 2 * 2];
                Image2D img = new Image2D(Image2D.RGB, 2, 2, data);
                Assert.assertEquals("format", Image2D.RGB, img.getFormat());
                Assert.assertEquals("width", 2, img.getWidth());
                Assert.assertEquals("height", 2, img.getHeight());
                Assert.assertFalse("is mutable false", img.isMutable());
            }
        });

        add(new TestCase("ctor RGBA immutable") {
            public void run() {
                byte[] data = new byte[4 * 2 * 2];
                Image2D img = new Image2D(Image2D.RGBA, 2, 2, data);
                Assert.assertEquals("format RGBA", Image2D.RGBA, img.getFormat());
            }
        });

        add(new TestCase("mutable ctor and set") {
            public void run() {
                Image2D img = new Image2D(Image2D.RGB, 4, 4);
                Assert.assertTrue("mutable", img.isMutable());
                byte[] newData = new byte[3 * 2 * 2];
                newData[0] = (byte)255;
                img.set(0, 0, 2, 2, newData);
                Assert.assertTrue("set on mutable ok", true);
            }
        });

        add(new TestCase("set on immutable throws") {
            public void run() {
                final Image2D img = new Image2D(Image2D.RGB, 2, 2, new byte[12]);
                Assert.expectException("immutable set", IllegalStateException.class, new Assert.Code() {
                    public void run() {
                        img.set(0, 0, 2, 2, new byte[12]);
                    }
                });
            }
        });

        add(new TestCase("ctor with lcdui Image") {
            public void run() {
                javax.microedition.lcdui.Image lcduiImg = javax.microedition.lcdui.Image.createImage(2, 2);
                Image2D img = new Image2D(Image2D.RGB, lcduiImg);
                Assert.assertEquals("width", 2, img.getWidth());
                Assert.assertFalse("immutable from lcdui Image", img.isMutable());
            }
        });

        add(new TestCase("set out of bounds throws") {
            public void run() {
                final Image2D img = new Image2D(Image2D.RGB, 4, 4);
                Assert.expectException("OOB", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        img.set(3, 3, 2, 2, new byte[12]);
                    }
                });
            }
        });
    }
}
