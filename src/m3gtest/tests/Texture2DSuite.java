package m3gtest.tests;

import javax.microedition.m3g.*;
import m3gtest.*;

public class Texture2DSuite extends TestSuite {

    public Texture2DSuite() {
        super("texture2d", "Texture2D", "Tests Texture2D.");

        add(new TestCase("ctor with Image2D") {
            public void run() {
                Image2D img = new Image2D(Image2D.RGB, 2, 2, new byte[12]);
                Texture2D tex = new Texture2D(img);
                Assert.assertSame("image", img, tex.getImage());
            }
        });

        add(new TestCase("setFiltering") {
            public void run() {
                Image2D img = new Image2D(Image2D.RGB, 2, 2, new byte[12]);
                Texture2D tex = new Texture2D(img);
                tex.setFiltering(Texture2D.FILTER_LINEAR, Texture2D.FILTER_LINEAR);
                Assert.assertEquals("level filter", Texture2D.FILTER_LINEAR, tex.getLevelFilter());
                Assert.assertEquals("image filter", Texture2D.FILTER_LINEAR, tex.getImageFilter());
            }
        });

        add(new TestCase("setWrapping") {
            public void run() {
                Image2D img = new Image2D(Image2D.RGB, 2, 2, new byte[12]);
                Texture2D tex = new Texture2D(img);
                tex.setWrapping(Texture2D.WRAP_REPEAT, Texture2D.WRAP_REPEAT);
                Assert.assertEquals("wrap S", Texture2D.WRAP_REPEAT, tex.getWrappingS());
                Assert.assertEquals("wrap T", Texture2D.WRAP_REPEAT, tex.getWrappingT());
            }
        });

        add(new TestCase("setBlending") {
            public void run() {
                Image2D img = new Image2D(Image2D.RGB, 2, 2, new byte[12]);
                Texture2D tex = new Texture2D(img);
                tex.setBlending(Texture2D.FUNC_MODULATE);
                Assert.assertEquals("blend", Texture2D.FUNC_MODULATE, tex.getBlending());
            }
        });

        add(new TestCase("setImage") {
            public void run() {
                Image2D img1 = new Image2D(Image2D.RGB, 2, 2, new byte[12]);
                Image2D img2 = new Image2D(Image2D.RGBA, 2, 2, new byte[16]);
                Texture2D tex = new Texture2D(img1);
                tex.setImage(img2);
                Assert.assertSame("image2", img2, tex.getImage());
            }
        });
    }
}
