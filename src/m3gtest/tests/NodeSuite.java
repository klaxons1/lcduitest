/*
 * M3G Tester - Node
 */
package m3gtest.tests;

import javax.microedition.m3g.*;

import m3gtest.*;

public class NodeSuite extends TestSuite {

    public NodeSuite() {
        super("Node", "Node base", "enable, parent, transform, scope, alpha, alignment");

        add(new TestCase("enable_and_parent") {
            public void run() {
                Group g = new Group();
                Mesh mesh = createMesh();
                Assert.assertTrue("default enabled", mesh.isRenderingEnabled());
                mesh.setRenderingEnable(false);
                Assert.assertFalse("disabled", mesh.isRenderingEnabled());
                mesh.setRenderingEnable(true);
                Assert.assertTrue("enabled again", mesh.isRenderingEnabled());

                Assert.assertNull("no parent initially", mesh.getParent());
                g.addChild(mesh);
                Assert.assertSame("parent is group", g, mesh.getParent());
                g.removeChild(mesh);
                Assert.assertNull("no parent after remove", mesh.getParent());
            }
        });

        add(new TestCase("transform") {
            public void run() {
                Mesh mesh = createMesh();
                Transform t = new Transform();
                t.postTranslate(1, 2, 3);
                mesh.setTransform(t);
                Transform out = new Transform();
                mesh.getTransform(out);
                float[] m = new float[16];
                out.get(m);
                Assert.assertEquals("tx 1", 1f, m[3], 0.0001f);
                Assert.assertEquals("ty 2", 2f, m[7], 0.0001f);

                // set null should reset to identity?
                mesh.setTransform(null);
                mesh.getTransform(out);
                out.get(m);
                Assert.assertEquals("after set null, identity tx 0", 0f, m[3], 0.0001f);
            }
        });

        add(new TestCase("scope") {
            public void run() {
                Node node = createMesh();
                node.setScope(0x01);
                Assert.assertEquals("scope 0x01", 0x01, node.getScope());
                node.setScope(0xFFFFFFFF);
                Assert.assertEquals("scope all", 0xFFFFFFFF, node.getScope());
                node.setScope(0);
                Assert.assertEquals("scope 0", 0, node.getScope());
            }
        });

        add(new TestCase("alpha_factor") {
            public void run() {
                Node node = createMesh();
                node.setAlphaFactor(0.5f);
                Assert.assertEquals("alpha 0.5", 0.5f, node.getAlphaFactor(), 0.001f);
                node.setAlphaFactor(0.0f);
                Assert.assertEquals("alpha 0", 0.0f, node.getAlphaFactor(), 0.001f);
                node.setAlphaFactor(1.0f);
                Assert.assertEquals("alpha 1", 1.0f, node.getAlphaFactor(), 0.001f);

                Assert.expectException("alpha negative", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        createMesh().setAlphaFactor(-0.1f);
                    }
                });
                Assert.expectException("alpha >1", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        createMesh().setAlphaFactor(1.1f);
                    }
                });
            }
        });

        add(new TestCase("alignment") {
            public void run() {
                Node node = createMesh();
                Group ref = new Group();
                node.setAlignment(ref, Node.Z_AXIS, ref, Node.Y_AXIS);
                Assert.assertSame("alignment reference", ref, node.getAlignmentReference(Node.Z_AXIS));
                Assert.assertEquals("alignment target Z_AXIS", Node.Z_AXIS, node.getAlignmentTarget(Node.Z_AXIS));

                node.setAlignment(null, Node.NONE, null, Node.NONE);
                Assert.assertNull("alignment null after clear", node.getAlignmentReference(Node.Z_AXIS));

                // Invalid axis
                Assert.expectException("invalid axis", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        createMesh().setAlignment(null, 999, null, 0);
                    }
                });
            }
        });

        add(new TestCase("getTransformTo") {
            public void run() {
                Group root = new Group();
                Group child = new Group();
                root.addChild(child);
                Transform t = new Transform();
                child.getTransformTo(root, t);
                // Should not throw
                Assert.expectException("getTransformTo null target", NullPointerException.class, new Assert.Code() {
                    public void run() {
                        new Group().getTransformTo(null, new Transform());
                    }
                });
                Assert.expectException("getTransformTo null transform", NullPointerException.class, new Assert.Code() {
                    public void run() {
                        new Group().getTransformTo(new Group(), null);
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
