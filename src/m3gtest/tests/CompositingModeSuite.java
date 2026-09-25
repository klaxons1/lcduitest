package m3gtest.tests;

import javax.microedition.m3g.*;
import m3gtest.*;

public class CompositingModeSuite extends TestSuite {

    public CompositingModeSuite() {
        super("compositing", "CompositingMode", "Tests CompositingMode.");

        add(new TestCase("setBlending") {
            public void run() {
                CompositingMode cm = new CompositingMode();
                cm.setBlending(CompositingMode.ALPHA);
                Assert.assertEquals("blending", CompositingMode.ALPHA, cm.getBlending());
            }
        });

        add(new TestCase("setAlphaThreshold") {
            public void run() {
                CompositingMode cm = new CompositingMode();
                cm.setAlphaThreshold(0.5f);
                Assert.assertEquals("threshold", 0.5f, cm.getAlphaThreshold(), 0.001f);
            }
        });

        add(new TestCase("setAlphaWriteEnable") {
            public void run() {
                CompositingMode cm = new CompositingMode();
                cm.setAlphaWriteEnable(false);
                Assert.assertFalse("alpha write", cm.isAlphaWriteEnabled());
                cm.setAlphaWriteEnable(true);
                Assert.assertTrue("alpha write true", cm.isAlphaWriteEnabled());
            }
        });

        add(new TestCase("setColorWriteEnable") {
            public void run() {
                CompositingMode cm = new CompositingMode();
                cm.setColorWriteEnable(false);
                Assert.assertFalse("color write", cm.isColorWriteEnabled());
            }
        });

        add(new TestCase("setDepthWriteEnable") {
            public void run() {
                CompositingMode cm = new CompositingMode();
                cm.setDepthWriteEnable(false);
                Assert.assertFalse("depth write", cm.isDepthWriteEnabled());
            }
        });

        add(new TestCase("setDepthTestEnable") {
            public void run() {
                CompositingMode cm = new CompositingMode();
                cm.setDepthTestEnable(false);
                Assert.assertFalse("depth test", cm.isDepthTestEnabled());
            }
        });
    }
}
