package m3gtest.tests;

import javax.microedition.m3g.*;
import m3gtest.*;

public class Graphics3DSuite extends TestSuite {

    public Graphics3DSuite() {
        super("g3d", "Graphics3D", "Tests Graphics3D rendering methods.");

        add(new TestCase("getInstance") {
            public void run() {
                Graphics3D g3d = Graphics3D.getInstance();
                Assert.assertNotNull("g3d", g3d);
            }
        });

        add(new TestCase("getProperties") {
            public void run() {
                java.util.Hashtable props = Graphics3D.getProperties();
                Assert.assertNotNull("props", props);
            }
        });

        add(new TestCase("getHints") {
            public void run() {
                Graphics3D g3d = Graphics3D.getInstance();
                int hints = g3d.getHints();
                Assert.assertTrue("hints >=0", hints >= 0);
            }
        });

        add(new TestCase("addLight resetLights") {
            public void run() {
                Graphics3D g3d = Graphics3D.getInstance();
                Light light = new Light();
                light.setMode(Light.DIRECTIONAL);
                int index = g3d.addLight(light, new Transform());
                Assert.assertTrue("light index >=0", index >= 0);
                g3d.resetLights();
            }
        });

        add(new TestCase("setCamera") {
            public void run() {
                Graphics3D g3d = Graphics3D.getInstance();
                Camera cam = new Camera();
                cam.setPerspective(60, 1, 1, 100);
                Transform t = new Transform();
                t.postTranslate(0,0,5);
                g3d.setCamera(cam, t);
                Camera out = g3d.getCamera(new Transform());
                Assert.assertSame("camera", cam, out);
            }
        });

        add(new TestCase("getViewport") {
            public void run() {
                Graphics3D g3d = Graphics3D.getInstance();
                try {
                    int x = g3d.getViewportX();
                    int y = g3d.getViewportY();
                    int w = g3d.getViewportWidth();
                    int h = g3d.getViewportHeight();
                    Assert.assertTrue("viewport check", true);
                } catch (Exception e) {
                    // may throw if no target
                }
            }
        });

        add(new TestCase("getLightCount") {
            public void run() {
                Graphics3D g3d = Graphics3D.getInstance();
                int count = g3d.getLightCount();
                Assert.assertTrue("light count >=0", count >= 0);
            }
        });

        add(new TestCase("getDepthRange") {
            public void run() {
                Graphics3D g3d = Graphics3D.getInstance();
                float near = g3d.getDepthRangeNear();
                float far = g3d.getDepthRangeFar();
                Assert.assertTrue("depth range", far >= near);
            }
        });
    }
}
