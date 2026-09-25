/*
 * M3G Tester - Group
 */
package m3gtest.tests;

import javax.microedition.m3g.*;

import m3gtest.*;

public class GroupSuite extends TestSuite {

    public GroupSuite() {
        super("Group", "Group", "addChild, removeChild, getChild, childCount, pick");

        add(new TestCase("add_and_get_child") {
            public void run() {
                Group g = new Group();
                Assert.assertEquals("initial child count 0", 0, g.getChildCount());
                Mesh m1 = createMesh();
                Mesh m2 = createMesh();
                g.addChild(m1);
                Assert.assertEquals("count 1", 1, g.getChildCount());
                Assert.assertSame("getChild 0", m1, g.getChild(0));
                g.addChild(m2);
                Assert.assertEquals("count 2", 2, g.getChildCount());
                Assert.assertSame("getChild 1", m2, g.getChild(1));
            }
        });

        add(new TestCase("add_null_and_duplicate") {
            public void run() {
                Assert.expectException("addChild null", NullPointerException.class, new Assert.Code() {
                    public void run() {
                        new Group().addChild(null);
                    }
                });
                // Adding same child again should throw IllegalArgumentException (child already has parent)
                Assert.expectException("addChild already has parent", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        Group gg = new Group();
                        Mesh mm = createMesh();
                        gg.addChild(mm);
                        Group gg2 = new Group();
                        gg2.addChild(mm);
                    }
                });
                // Adding self should throw IllegalArgumentException
                Assert.expectException("addChild self", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        Group gg = new Group();
                        gg.addChild(gg);
                    }
                });
            }
        });

        add(new TestCase("remove_child") {
            public void run() {
                Group g = new Group();
                Mesh m = createMesh();
                g.addChild(m);
                g.removeChild(m);
                Assert.assertEquals("count 0 after remove", 0, g.getChildCount());
                Assert.assertNull("parent null after remove", m.getParent());

                // remove null is silently ignored per spec
                Assert.expectNoException("remove null ignored", new Assert.Code() {
                    public void run() {
                        new Group().removeChild(null);
                    }
                });
                // remove child not in group is silently ignored per spec
                Assert.expectNoException("remove non-child ignored", new Assert.Code() {
                    public void run() {
                        Group gg = new Group();
                        Mesh mm = createMesh();
                        gg.removeChild(mm);
                    }
                });
            }
        });

        add(new TestCase("getChild_bounds") {
            public void run() {
                Group g = new Group();
                Assert.expectException("getChild negative", IndexOutOfBoundsException.class, new Assert.Code() {
                    public void run() {
                        new Group().getChild(-1);
                    }
                });
                Assert.expectException("getChild out of bounds", IndexOutOfBoundsException.class, new Assert.Code() {
                    public void run() {
                        Group gg = new Group();
                        gg.addChild(createMesh());
                        gg.getChild(1);
                    }
                });
            }
        });

        add(new TestCase("pick") {
            public void run() {
                Group root = new Group();
                Mesh mesh = createMesh();
                mesh.setScope(1);
                root.addChild(mesh);
                Camera cam = new Camera();
                root.addChild(cam);
                // Pick ray - this is implementation dependent, but should not crash
                RayIntersection ri = new RayIntersection();
                boolean hit = root.pick(1, 0, 0, 0, 0, 0, 1, ri);
                Assert.info("pick result", hit);
                // pick with scope
                boolean hit2 = root.pick(1, 0, 0, 0, 0, 0, 1, ri);
                Assert.info("pick with scope", hit2);

                Assert.expectException("pick null RayIntersection", NullPointerException.class, new Assert.Code() {
                    public void run() {
                        new Group().pick(1, 0, 0, 0, 0, 0, 1, null);
                    }
                });
            }
        });
    }

    private static Mesh createMesh() {
        VertexArray pos = new VertexArray(3, 3, 2);
        pos.set(0, 3, new short[]{0, 0, 0, 1, 0, 0, 0, 1, 0});
        VertexBuffer vb = new VertexBuffer();
        vb.setPositions(pos, 1.0f, null);
        IndexBuffer ib = new TriangleStripArray(0, new int[]{3});
        return new Mesh(vb, ib, new Appearance());
    }
}
