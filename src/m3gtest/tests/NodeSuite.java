package m3gtest.tests;

import javax.microedition.m3g.*;
import m3gtest.*;

public class NodeSuite extends TestSuite {

    public NodeSuite() {
        super("node", "Node", "Tests Node base class.");

        add(new TestCase("setTransform") {
            public void run() {
                Group g = new Group();
                Transform t = new Transform();
                t.postTranslate(1,2,3);
                g.setTransform(t);
                Transform out = new Transform();
                g.getTransform(out);
                float[] m = new float[16];
                out.get(m);
                Assert.assertEquals("tx", 1.0f, m[3], 0.001f);
            }
        });

        add(new TestCase("setScope") {
            public void run() {
                Group g = new Group();
                g.setScope(5);
                Assert.assertEquals("scope", 5, g.getScope());
            }
        });

        add(new TestCase("setAlphaFactor") {
            public void run() {
                Group g = new Group();
                g.setAlphaFactor(0.3f);
                Assert.assertEquals("alpha", 0.3f, g.getAlphaFactor(), 0.001f);
            }
        });

        add(new TestCase("setRenderingEnable") {
            public void run() {
                Group g = new Group();
                g.setRenderingEnable(false);
                Assert.assertFalse("rendering", g.isRenderingEnabled());
                g.setRenderingEnable(true);
                Assert.assertTrue("rendering true", g.isRenderingEnabled());
            }
        });

        add(new TestCase("setPickingEnable") {
            public void run() {
                Group g = new Group();
                g.setPickingEnable(false);
                Assert.assertFalse("picking", g.isPickingEnabled());
            }
        });
    }
}
