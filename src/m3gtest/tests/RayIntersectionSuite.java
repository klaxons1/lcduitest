/*
 * M3G Tester - RayIntersection
 */
package m3gtest.tests;

import javax.microedition.m3g.*;

import m3gtest.*;

public class RayIntersectionSuite extends TestSuite {

    public RayIntersectionSuite() {
        super("RayIntersection", "RayIntersection", "fields populated by Group.pick");

        add(new TestCase("default_state") {
            public void run() {
                RayIntersection ri = new RayIntersection();
                Assert.info("default distance", ri.getDistance());
                Assert.info("default normal", String.valueOf(ri.getNormalX()) + \",\" + ri.getNormalY() + \",\" + ri.getNormalZ());
                Assert.info("default ray", ri.getRay());
            }
        });

        add(new TestCase("pick_populates") {
            public void run() {
                Group root = new Group();
                // Create a simple mesh at origin
                VertexArray pos = new VertexArray(3, 3, 2);
                pos.set(0, 3, new short[]{0, 0, 0, 10, 0, 0, 0, 10, 0});
                VertexBuffer vb = new VertexBuffer();
                vb.setPositions(pos, 1.0f, null);
                IndexBuffer ib = new TriangleStripArray(0, new int[]{3});
                Mesh mesh = new Mesh(vb, ib, new Appearance());
                mesh.setScope(0xFFFFFFFF);
                root.addChild(mesh);

                RayIntersection ri = new RayIntersection();
                // Ray from (0,0,10) towards (0,0,-1) should hit triangle at origin
                boolean hit = root.pick(-1, 0, 0, 10, 0, 0, -1, ri);
                Assert.info("pick hit", hit);
                if (hit) {
                    Assert.assertNotNull("intersected object not null", ri.getIntersected());
                    Assert.info("distance", ri.getDistance());
                    Assert.info("normal", ri.getNormalX() + \",\" + ri.getNormalY() + \",\" + ri.getNormalZ());
                }
            }
        }.severity(TestCase.SHOULD));

        add(new TestCase("get_methods") {
            public void run() {
                RayIntersection ri = new RayIntersection();
                // Test getters don't throw
                Assert.expectNoException("getIntersected", new Assert.Code() {
                    public void run() {
                        new RayIntersection().getIntersected();
                    }
                });
                Assert.expectNoException("getDistance", new Assert.Code() {
                    public void run() {
                        new RayIntersection().getDistance();
                    }
                });
                Assert.expectNoException("getSubmeshIndex", new Assert.Code() {
                    public void run() {
                        new RayIntersection().getSubmeshIndex();
                    }
                });
                Assert.expectNoException("getNormal", new Assert.Code() {
                    public void run() {
                        RayIntersection r = new RayIntersection();
                        r.getNormalX();
                        r.getNormalY();
                        r.getNormalZ();
                    }
                });
                Assert.expectNoException("getTextureS/T", new Assert.Code() {
                    public void run() {
                        RayIntersection r = new RayIntersection();
                        r.getTextureS(0);
                        r.getTextureT(0);
                    }
                });
            }
        });
    }
}
