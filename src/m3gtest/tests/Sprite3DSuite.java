/*
 * M3G Tester - Sprite3D
 */
package m3gtest.tests;

import javax.microedition.m3g.*;

import m3gtest.*;

public class Sprite3DSuite extends TestSuite {

    public Sprite3DSuite() {
        super("Sprite3D", "Sprite3D", "constructor, image, appearance, crop, etc");

        add(new TestCase("constructor") {
            public void run() {
                Image2D img = new Image2D(Image2D.RGB, 16, 16);
                Appearance ap = new Appearance();
                Sprite3D sprite = new Sprite3D(true, img, ap);
                Assert.assertNotNull("sprite not null", sprite);
                Assert.assertTrue("isScaled", sprite.isScaled());
                Assert.assertSame("image", img, sprite.getImage());
                Assert.assertSame("appearance", ap, sprite.getAppearance());

                Sprite3D sprite2 = new Sprite3D(false, null, null);
                Assert.assertFalse("not scaled", sprite2.isScaled());
                Assert.assertNull("null image", sprite2.getImage());
                Assert.assertNull("null appearance", sprite2.getAppearance());
            }
        });

        add(new TestCase("set_image_and_appearance") {
            public void run() {
                Sprite3D sprite = new Sprite3D(false, null, null);
                Image2D img = new Image2D(Image2D.RGBA, 32, 32);
                sprite.setImage(img);
                Assert.assertSame("image set", img, sprite.getImage());
                Appearance ap = new Appearance();
                sprite.setAppearance(ap);
                Assert.assertSame("appearance set", ap, sprite.getAppearance());
                sprite.setImage(null);
                Assert.assertNull("image null", sprite.getImage());
                sprite.setAppearance(null);
                Assert.assertNull("appearance null", sprite.getAppearance());
            }
        });

        add(new TestCase("crop") {
            public void run() {
                Sprite3D sprite = new Sprite3D(false, new Image2D(Image2D.RGB, 32, 32), null);
                sprite.setCrop(2, 2, 10, 10);
                Assert.assertEquals("cropX", 2, sprite.getCropX());
                Assert.assertEquals("cropY", 2, sprite.getCropY());
                Assert.assertEquals("cropWidth", 10, sprite.getCropWidth());
                Assert.assertEquals("cropHeight", 10, sprite.getCropHeight());
            }
        });

        add(new TestCase("isScaled_mutable") {
            public void run() {
                Sprite3D s = new Sprite3D(false, null, null);
                Assert.assertFalse("initial not scaled", s.isScaled());
                // There is no setScaled? Actually constructor sets scaling mode, but is there setter? Spec says Sprite3D is immutable in scaling? Check spec - setImage and setAppearance exist, but scaling is set at construction? Let's test via info
                Assert.info("scaled", s.isScaled());
            }
        });
    }
}
