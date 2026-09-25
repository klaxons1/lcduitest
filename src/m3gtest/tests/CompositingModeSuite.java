/*
 * M3G Tester - CompositingMode
 */
package m3gtest.tests;

import javax.microedition.m3g.*;

import m3gtest.*;

public class CompositingModeSuite extends TestSuite {

    public CompositingModeSuite() {
        super("CompositingMode", "CompositingMode", "blending, alpha threshold, depth test/write, color/depth mask, alpha add");

        add(new TestCase("blending") {
            public void run() {
                CompositingMode cm = new CompositingMode();
                cm.setBlending(CompositingMode.ALPHA);
                Assert.assertEquals("ALPHA", CompositingMode.ALPHA, cm.getBlending());
                cm.setBlending(CompositingMode.ALPHA_ADD);
                Assert.assertEquals("ALPHA_ADD", CompositingMode.ALPHA_ADD, cm.getBlending());
                cm.setBlending(CompositingMode.MODULATE);
                Assert.assertEquals("MODULATE", CompositingMode.MODULATE, cm.getBlending());
                cm.setBlending(CompositingMode.MODULATE_X2);
                Assert.assertEquals("MODULATE_X2", CompositingMode.MODULATE_X2, cm.getBlending());
                cm.setBlending(CompositingMode.REPLACE);
                Assert.assertEquals("REPLACE", CompositingMode.REPLACE, cm.getBlending());

                Assert.expectException("invalid blending", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new CompositingMode().setBlending(999);
                    }
                });
            }
        });

        add(new TestCase("alpha_threshold") {
            public void run() {
                CompositingMode cm = new CompositingMode();
                cm.setAlphaThreshold(0.5f);
                Assert.assertEquals("threshold 0.5", 0.5f, cm.getAlphaThreshold(), 0.001f);
                cm.setAlphaThreshold(0.0f);
                Assert.assertEquals("threshold 0", 0.0f, cm.getAlphaThreshold(), 0.001f);
                cm.setAlphaThreshold(1.0f);
                Assert.assertEquals("threshold 1", 1.0f, cm.getAlphaThreshold(), 0.001f);

                Assert.expectException("threshold negative", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new CompositingMode().setAlphaThreshold(-0.1f);
                    }
                });
                Assert.expectException("threshold >1", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new CompositingMode().setAlphaThreshold(1.1f);
                    }
                });
            }
        });

        add(new TestCase("depth_test_and_write") {
            public void run() {
                CompositingMode cm = new CompositingMode();
                cm.setDepthTestEnable(true);
                Assert.assertTrue("depth test true", cm.isDepthTestEnabled());
                cm.setDepthTestEnable(false);
                Assert.assertFalse("depth test false", cm.isDepthTestEnabled());

                cm.setDepthWriteEnable(true);
                Assert.assertTrue("depth write true", cm.isDepthWriteEnabled());
                cm.setDepthWriteEnable(false);
                Assert.assertFalse("depth write false", cm.isDepthWriteEnabled());
            }
        });

        add(new TestCase("color_and_depth_mask_and_alpha_add") {
            public void run() {
                CompositingMode cm = new CompositingMode();
                cm.setColorWriteEnable(true);
                Assert.assertTrue("color write true", cm.isColorWriteEnabled());
                cm.setColorWriteEnable(false);
                Assert.assertFalse("color write false", cm.isColorWriteEnabled());

                cm.setAlphaWriteEnable(true);
                Assert.assertTrue("alpha write true", cm.isAlphaWriteEnabled());
                cm.setAlphaWriteEnable(false);
                Assert.assertFalse("alpha write false", cm.isAlphaWriteEnabled());
            }
        });
    }
}
