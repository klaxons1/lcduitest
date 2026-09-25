package m3gtest.tests;

import javax.microedition.m3g.*;
import m3gtest.*;

public class MaterialSuite extends TestSuite {

    public MaterialSuite() {
        super("material", "Material", "Tests Material.");

        add(new TestCase("setColor getColor") {
            public void run() {
                Material m = new Material();
                m.setColor(Material.DIFFUSE, 0xFF00FF);
                Assert.assertEquals("diffuse", 0xFF00FF, m.getColor(Material.DIFFUSE));
            }
        });

        add(new TestCase("setVertexColorTracking") {
            public void run() {
                Material m = new Material();
                m.setVertexColorTracking(true);
                Assert.assertTrue("tracking", m.isVertexColorTracking());
                m.setVertexColorTracking(false);
                Assert.assertFalse("no tracking", m.isVertexColorTracking());
            }
        });

        add(new TestCase("setShininess") {
            public void run() {
                Material m = new Material();
                m.setShininess(10.0f);
                Assert.assertEquals("shininess", 10.0f, m.getShininess(), 0.001f);
            }
        });
    }
}
