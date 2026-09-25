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

        add(new TestCase("getHints setHints") {
            public void run() {
                Graphics3D g3d = Graphics3D.getInstance();
                int old = g3d.getHints();
                g3d.setHints(0);
                Assert.assertEquals("hints 0", 0, g3d.getHints());
                g3d.setHints(old);
            }
        });

        add(new TestCase("addLight resetLights") {
            public void run() {
                Graphics3D g3d = Graphics3D.getInstance();
                Light light = new Light();
                light.setMode(Light.DIRECTIONAL);
                g3d.addLight(light, new Transform());
                g3d.resetLights();
                // Should not throw
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
                int[] vp = new int[4];
                try {
                    g3d.getViewport(vp);
                } catch (Exception e) {
                    // may throw if no target
                }
            }
        });
    }
}
