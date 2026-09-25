/*
 * M3G Tester - Background
 */
package m3gtest.tests;

import javax.microedition.m3g.*;

import m3gtest.*;

public class BackgroundSuite extends TestSuite {

    public BackgroundSuite() {
        super("Background", "Background", "color, image, mode, crop, depth clear");

        add(new TestCase("default_state") {
            public void run() {
                Background bg = new Background();
                Assert.info("default color", Ui.hex(bg.getColor()));
                Assert.assertNull("default image null", bg.getImage());
                Assert.assertTrue("default color clear enabled", bg.isColorClearEnabled());
                Assert.assertTrue("default depth clear enabled", bg.isDepthClearEnabled());
            }
        });

        add(new TestCase("color") {
            public void run() {
                Background bg = new Background();
                bg.setColor(0xFF0000);
                Assert.assertEquals("red", 0xFF0000, bg.getColor() & 0xFFFFFF);
                bg.setColor(0x00FF00);
                Assert.assertEquals("green", 0x00FF00, bg.getColor() & 0xFFFFFF);
                bg.setColor(0x000000);
                Assert.assertEquals("black", 0x000000, bg.getColor() & 0xFFFFFF);
            }
        });

        add(new TestCase("image_and_mode") {
            public void run() {
                Background bg = new Background();
                Image2D img = new Image2D(Image2D.RGB, 16, 16);
                bg.setImage(img);
                Assert.assertSame("image same", img, bg.getImage());

                bg.setImageMode(Background.BORDER, Background.BORDER);
                Assert.assertEquals("BORDER X", Background.BORDER, bg.getImageModeX());
                Assert.assertEquals("BORDER Y", Background.BORDER, bg.getImageModeY());
                bg.setImageMode(Background.REPEAT, Background.REPEAT);
                Assert.assertEquals("REPEAT X", Background.REPEAT, bg.getImageModeX());
                Assert.assertEquals("REPEAT Y", Background.REPEAT, bg.getImageModeY());

                bg.setImageMode(Background.BORDER, Background.REPEAT);
                Assert.assertEquals("mixed X BORDER", Background.BORDER, bg.getImageModeX());
                Assert.assertEquals("mixed Y REPEAT", Background.REPEAT, bg.getImageModeY());

                Assert.expectException("invalid image mode", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new Background().setImageMode(999, Background.BORDER);
                    }
                });
                Assert.expectException("invalid image mode Y", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new Background().setImageMode(Background.BORDER, 999);
                    }
                });

                bg.setImage(null);
                Assert.assertNull("null image", bg.getImage());
            }
        });

        add(new TestCase("crop") {
            public void run() {
                Background bg = new Background();
                Image2D img = new Image2D(Image2D.RGB, 32, 32);
                bg.setImage(img);
                bg.setCrop(2, 2, 10, 10);
                Assert.assertEquals("cropX 2", 2, bg.getCropX());
                Assert.assertEquals("cropY 2", 2, bg.getCropY());
                Assert.assertEquals("cropWidth 10", 10, bg.getCropWidth());
                Assert.assertEquals("cropHeight 10", 10, bg.getCropHeight());

                bg.setCrop(0, 0, 32, 32);
                Assert.assertEquals("crop full", 32, bg.getCropWidth());
            }
        });

        add(new TestCase("clear_flags") {
            public void run() {
                Background bg = new Background();
                bg.setColorClearEnable(false);
                Assert.assertFalse("color clear false", bg.isColorClearEnabled());
                bg.setColorClearEnable(true);
                Assert.assertTrue("color clear true", bg.isColorClearEnabled());

                bg.setDepthClearEnable(false);
                Assert.assertFalse("depth clear false", bg.isDepthClearEnabled());
                bg.setDepthClearEnable(true);
                Assert.assertTrue("depth clear true", bg.isDepthClearEnabled());
            }
        });
    }
}
