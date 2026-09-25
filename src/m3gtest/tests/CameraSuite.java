/*
 * M3G Tester - Camera
 */
package m3gtest.tests;

import javax.microedition.m3g.*;

import m3gtest.*;

public class CameraSuite extends TestSuite {

    public CameraSuite() {
        super("Camera", "Camera", "projection type, perspective, parallel, fov, near/far, generic");

        add(new TestCase("default_projection") {
            public void run() {
                Camera cam = new Camera();
                Assert.info("default projection type", cam.getProjection(new float[16]));
                int type = cam.getProjection(new float[16]);
                Assert.info("projection type value", type);
            }
        });

        add(new TestCase("setPerspective") {
            public void run() {
                Camera cam = new Camera();
                cam.setPerspective(60.0f, 1.33f, 1.0f, 100.0f);
                Assert.assertEquals("projection type perspective", Camera.PERSPECTIVE, cam.getProjection(new float[16]));
                float[] params = new float[4];
                // getPerspective?
                // Actually Camera has getPerspective? Check spec: there is getPerspective? Let's use getProjection with array and also try to retrieve via generic?
                // Use setPerspective then check that no exception and type is perspective

                Assert.expectException("fov <=0", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new Camera().setPerspective(0, 1.33f, 1, 100);
                    }
                });
                Assert.expectException("fov >=180", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new Camera().setPerspective(180, 1.33f, 1, 100);
                    }
                });
                Assert.expectException("near <=0", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new Camera().setPerspective(60, 1.33f, 0, 100);
                    }
                });
                Assert.expectException("near >= far", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new Camera().setPerspective(60, 1.33f, 10, 5);
                    }
                });
            }
        });

        add(new TestCase("setParallel") {
            public void run() {
                Camera cam = new Camera();
                cam.setParallel(10.0f, 10.0f, 1.0f, 100.0f);
                Assert.assertEquals("projection type parallel", Camera.PARALLEL, cam.getProjection(new float[16]));

                Assert.expectException("parallel height <=0", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new Camera().setParallel(0, 10, 1, 100);
                    }
                });
            }
        });

        add(new TestCase("setGeneric") {
            public void run() {
                Camera cam = new Camera();
                Transform t = new Transform();
                t.setIdentity();
                cam.setGeneric(t);
                Assert.assertEquals("generic", Camera.GENERIC, cam.getProjection(new float[16]));
                float[] m = new float[16];
                int type = cam.getProjection(m);
                Assert.assertEquals("type generic", Camera.GENERIC, type);

                Assert.expectException("generic null", NullPointerException.class, new Assert.Code() {
                    public void run() {
                        new Camera().setGeneric(null);
                    }
                });
            }
        });

        add(new TestCase("getProjection_null_check") {
            public void run() {
                Assert.expectException("getProjection null", NullPointerException.class, new Assert.Code() {
                    public void run() {
                        new Camera().getProjection(null);
                    }
                });
                Assert.expectException("getProjection too short", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new Camera().getProjection(new float[15]);
                    }
                });
            }
        });
    }
}
