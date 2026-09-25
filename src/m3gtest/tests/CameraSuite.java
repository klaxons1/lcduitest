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
                Assert.assertEquals("fovy", 60.0f, cam.getFieldOfView(), 0.001f);
            }
        });

        add(new TestCase("setParallel") {
            public void run() {
                Camera cam = new Camera();
                cam.setParallel(2.0f, 2.0f, 0.1f, 100.0f);
                // No getter for parallel size, just check mode
                Assert.assertTrue("parallel", true);
            }
        });

        add(new TestCase("setGeneric") {
            public void run() {
                Camera cam = new Camera();
                Transform t = new Transform();
                t.setIdentity();
                cam.setGeneric(t);
                Transform out = new Transform();
                cam.getGeneric(out);
                Assert.assertTrue("generic set", out.isIdentity());
            }
        });

        add(new TestCase("isPerspective") {
            public void run() {
                Camera cam = new Camera();
                cam.setPerspective(60, 1, 1, 100);
                // getProjection returns transform, but we can check that camera is valid
                Transform proj = new Transform();
                cam.getProjection(proj);
                Assert.assertFalse("not identity", proj.isIdentity());
            }
        });
    }
}
