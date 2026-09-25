package m3gtest.tests;

import javax.microedition.m3g.*;
import m3gtest.*;

public class Sprite3DSuite extends TestSuite {

    public Sprite3DSuite() {
        super("sprite3d", "Sprite3D", "Tests Sprite3D.");

        add(new TestCase("ctor") {
            public void run() {
                Image2D img = new Image2D(Image2D.RGBA, 2, 2, new byte[16]);
                Appearance ap = new Appearance();
                Sprite3D sprite = new Sprite3D(false, img, ap);
                Assert.assertNotNull("sprite", sprite);
            }
        });

        add(new TestCase("isScaled") {
            public void run() {
                Image2D img = new Image2D(Image2D.RGBA, 2, 2, new byte[16]);
                Appearance ap = new Appearance();
                Sprite3D s1 = new Sprite3D(true, img, ap);
                Assert.assertTrue("scaled", s1.isScaled());
                Sprite3D s2 = new Sprite3D(false, img, ap);
                Assert.assertFalse("not scaled", s2.isScaled());
            }
        });

        add(new TestCase("setImage getImage") {
            public void run() {
                Image2D img1 = new Image2D(Image2D.RGBA, 2, 2, new byte[16]);
                Image2D img2 = new Image2D(Image2D.RGB, 2, 2, new byte[12]);
                Appearance ap = new Appearance();
                Sprite3D sprite = new Sprite3D(false, img1, ap);
                sprite.setImage(img2);
                Assert.assertSame("image", img2, sprite.getImage());
            }
        });

        add(new TestCase("setAppearance getAppearance") {
            public void run() {
                Image2D img = new Image2D(Image2D.RGBA, 2, 2, new byte[16]);
                Appearance ap1 = new Appearance();
                Appearance ap2 = new Appearance();
                Sprite3D sprite = new Sprite3D(false, img, ap1);
                sprite.setAppearance(ap2);
                Assert.assertSame("appearance", ap2, sprite.getAppearance());
            }
        });

        add(new TestCase("setCrop getCrop") {
            public void run() {
                Image2D img = new Image2D(Image2D.RGBA, 4, 4, new byte[64]);
                Appearance ap = new Appearance();
                Sprite3D sprite = new Sprite3D(false, img, ap);
                sprite.setCrop(1,1,2,2);
                Assert.assertEquals("cropX", 1, sprite.getCropX());
                Assert.assertEquals("cropY", 1, sprite.getCropY());
                Assert.assertEquals("cropW", 2, sprite.getCropWidth());
                Assert.assertEquals("cropH", 2, sprite.getCropHeight());
            }
        });
    }
}
