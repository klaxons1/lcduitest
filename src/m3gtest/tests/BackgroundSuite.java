package m3gtest.tests;

import javax.microedition.m3g.*;
import m3gtest.*;

public class BackgroundSuite extends TestSuite {

    public BackgroundSuite() {
        super("background", "Background", "Tests Background.");

        add(new TestCase("setColor") {
            public void run() {
                Background bg = new Background();
                bg.setColor(0xFF00FF);
                Assert.assertEquals("color", 0xFF00FF, bg.getColor());
            }
        });

        add(new TestCase("setImage") {
            public void run() {
                Background bg = new Background();
                Image2D img = new Image2D(Image2D.RGB, 2, 2, new byte[12]);
                bg.setImage(img);
                Assert.assertSame("image", img, bg.getImage());
            }
        });

        add(new TestCase("setCrop") {
            public void run() {
                Background bg = new Background();
                bg.setCrop(1, 2, 3, 4);
                Assert.assertEquals("cropX", 1, bg.getCropX());
                Assert.assertEquals("cropY", 2, bg.getCropY());
                Assert.assertEquals("cropW", 3, bg.getCropWidth());
                Assert.assertEquals("cropH", 4, bg.getCropHeight());
            }
        });

        add(new TestCase("setImageMode") {
            public void run() {
                Background bg = new Background();
                bg.setImageMode(Background.BORDER);
                Assert.assertEquals("mode", Background.BORDER, bg.getImageModeX());
                Assert.assertEquals("modeY", Background.BORDER, bg.getImageModeY());
            }
        });

        add(new TestCase("isColorClearEnabled") {
            public void run() {
                Background bg = new Background();
                bg.setColorClearEnable(true);
                Assert.assertTrue("color clear", bg.isColorClearEnabled());
                bg.setColorClearEnable(false);
                Assert.assertFalse("color clear false", bg.isColorClearEnabled());
            }
        });
    }
}
