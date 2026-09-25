package m3gtest.tests;

import javax.microedition.m3g.*;
import m3gtest.*;

public class Image2DSuite extends TestSuite {

    public Image2DSuite() {
        super("image2d", "Image2D", "Tests Image2D.");

        add(new TestCase("ctor RGB") {
            public void run() {
                byte[] data = new byte[3 * 2 * 2];
                Image2D img = new Image2D(Image2D.RGB, 2, 2, data);
                Assert.assertEquals("format", Image2D.RGB, img.getFormat());
                Assert.assertEquals("width", 2, img.getWidth());
                Assert.assertEquals("height", 2, img.getHeight());
                Assert.assertFalse("is mutable", img.isMutable());
            }
        });

        add(new TestCase("ctor RGBA") {
            public void run() {
                byte[] data = new byte[4 * 2 * 2];
                Image2D img = new Image2D(Image2D.RGBA, 2, 2, data);
                Assert.assertEquals("format RGBA", Image2D.RGBA, img.getFormat());
            }
        });

        add(new TestCase("set") {
            public void run() {
                byte[] data = new byte[12];
                Image2D img = new Image2D(Image2D.RGB, 2, 2, data);
                byte[] newData = new byte[12];
                newData[0] = (byte)255;
                img.set(0, 0, 2, 2, newData);
                byte[] out = new byte[12];
                img.get(0, 0, 2, 2, out);
                Assert.assertEquals("first byte", (byte)255, out[0]);
            }
        });

        add(new TestCase("ctor with lcdui Image") {
            public void run() {
                javax.microedition.lcdui.Image lcduiImg = javax.microedition.lcdui.Image.createImage(2, 2);
                Image2D img = new Image2D(Image2D.RGB, lcduiImg);
                Assert.assertEquals("width", 2, img.getWidth());
            }
        });
    }
}
