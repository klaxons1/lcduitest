package m3gtest.tests;

import javax.microedition.m3g.*;
import m3gtest.*;

public class AppearanceSuite extends TestSuite {

    public AppearanceSuite() {
        super("appearance", "Appearance", "Tests Appearance setters/getters.");

        add(new TestCase("ctor") {
            public void run() {
                Appearance ap = new Appearance();
                Assert.assertNotNull("appearance", ap);
            }
        });

        add(new TestCase("setMaterial getMaterial") {
            public void run() {
                Appearance ap = new Appearance();
                Material mat = new Material();
                ap.setMaterial(mat);
                Assert.assertSame("material", mat, ap.getMaterial());
            }
        });

        add(new TestCase("setCompositingMode") {
            public void run() {
                Appearance ap = new Appearance();
                CompositingMode cm = new CompositingMode();
                ap.setCompositingMode(cm);
                Assert.assertSame("cm", cm, ap.getCompositingMode());
            }
        });

        add(new TestCase("setPolygonMode") {
            public void run() {
                Appearance ap = new Appearance();
                PolygonMode pm = new PolygonMode();
                ap.setPolygonMode(pm);
                Assert.assertSame("pm", pm, ap.getPolygonMode());
            }
        });

        add(new TestCase("setFog") {
            public void run() {
                Appearance ap = new Appearance();
                Fog fog = new Fog();
                ap.setFog(fog);
                Assert.assertSame("fog", fog, ap.getFog());
            }
        });

        add(new TestCase("setTexture") {
            public void run() {
                Appearance ap = new Appearance();
                Image2D img = new Image2D(Image2D.RGB, 2, 2, new byte[12]);
                Texture2D tex = new Texture2D(img);
                ap.setTexture(0, tex);
                Assert.assertSame("tex", tex, ap.getTexture(0));
            }
        });

        add(new TestCase("setLayer") {
            public void run() {
                Appearance ap = new Appearance();
                ap.setLayer(3);
                Assert.assertEquals("layer", 3, ap.getLayer());
            }
        });
    }
}
