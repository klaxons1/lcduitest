package m3gtest.tests;

import javax.microedition.m3g.*;
import m3gtest.*;

public class CameraSuite extends TestSuite {

    public CameraSuite() {
        super("camera", "Camera", "Tests Camera.");

        add(new TestCase("setPerspective") {
            public void run() {
                Camera cam = new Camera();
                cam.setPerspective(60.0f, 1.33f, 0.1f, 100.0f);
                Transform t = new Transform();
                int type = cam.getProjection(t);
                Assert.assertEquals("type PERSPECTIVE", Camera.PERSPECTIVE, type);
            }
        });

        add(new TestCase("setParallel") {
            public void run() {
                Camera cam = new Camera();
                cam.setParallel(2.0f, 2.0f, 0.1f, 100.0f);
                Transform t = new Transform();
                int type = cam.getProjection(t);
                Assert.assertEquals("type PARALLEL", Camera.PARALLEL, type);
            }
        });

        add(new TestCase("setGeneric") {
            public void run() {
                Camera cam = new Camera();
                Transform t = new Transform();
                t.setIdentity();
                cam.setGeneric(t);
                Transform out = new Transform();
                int type = cam.getProjection(out);
                Assert.assertEquals("type GENERIC", Camera.GENERIC, type);
            }
        });

        add(new TestCase("getProjection float[]") {
            public void run() {
                Camera cam = new Camera();
                cam.setPerspective(60, 1, 1, 100);
                float[] params = new float[4];
                int type = cam.getProjection(params);
                Assert.assertEquals("type", Camera.PERSPECTIVE, type);
                Assert.assertTrue("fovy >0", params[0] > 0);
            }
        });
    }
}
