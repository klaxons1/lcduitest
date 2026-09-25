/*
 * M3G Tester - Mesh
 */
package m3gtest.tests;

import javax.microedition.m3g.*;

import m3gtest.*;

public class MeshSuite extends TestSuite {

    public MeshSuite() {
        super("Mesh", "Mesh", "constructor, vertexBuffer, indexBuffer, appearance, submesh count");

        add(new TestCase("constructor_valid") {
            public void run() {
                VertexBuffer vb = createVB(3);
                IndexBuffer ib = new TriangleStripArray(0, new int[]{3});
                Appearance ap = new Appearance();
                Mesh mesh = new Mesh(vb, ib, ap);
                Assert.assertNotNull("mesh not null", mesh);
                Assert.assertEquals("submesh count 1", 1, mesh.getSubmeshCount());
                Assert.assertSame("vertexBuffer", vb, mesh.getVertexBuffer());
                Assert.assertSame("indexBuffer 0", ib, mesh.getIndexBuffer(0));
                Assert.assertSame("appearance 0", ap, mesh.getAppearance(0));
            }
        });

        add(new TestCase("constructor_null_checks") {
            public void run() {
                VertexBuffer vb = createVB(3);
                IndexBuffer ib = new TriangleStripArray(0, new int[]{3});
                Assert.expectException("null vb", NullPointerException.class, new Assert.Code() {
                    public void run() {
                        new Mesh(null, new TriangleStripArray(0, new int[]{3}), new Appearance());
                    }
                });
                Assert.expectException("null ib", NullPointerException.class, new Assert.Code() {
                    public void run() {
                        VertexBuffer v = createVB(3);
                        new Mesh(v, null, new Appearance());
                    }
                });
                // Appearance can be null?
                Assert.expectNoException("null appearance allowed", new Assert.Code() {
                    public void run() {
                        VertexBuffer v = createVB(3);
                        new Mesh(v, new TriangleStripArray(0, new int[]{3}), null);
                    }
                });
            }
        });

        add(new TestCase("submeshes") {
            public void run() {
                VertexBuffer vb = createVB(6);
                IndexBuffer ib1 = new TriangleStripArray(0, new int[]{3});
                IndexBuffer ib2 = new TriangleStripArray(3, new int[]{3});
                Appearance ap1 = new Appearance();
                Appearance ap2 = new Appearance();
                IndexBuffer[] ibs = new IndexBuffer[]{ib1, ib2};
                Appearance[] aps = new Appearance[]{ap1, ap2};
                Mesh mesh = new Mesh(vb, ibs, aps);
                Assert.assertEquals("submesh count 2", 2, mesh.getSubmeshCount());
                Assert.assertSame("ib2", ib2, mesh.getIndexBuffer(1));
                Assert.assertSame("ap2", ap2, mesh.getAppearance(1));

                // set appearance
                Appearance ap3 = new Appearance();
                mesh.setAppearance(0, ap3);
                Assert.assertSame("ap3", ap3, mesh.getAppearance(0));

                Assert.expectException("constructor null ib element", NullPointerException.class, new Assert.Code() {
                    public void run() {
                        VertexBuffer v = createVB(3);
                        IndexBuffer[] bad = new IndexBuffer[]{null};
                        new Mesh(v, bad, new Appearance[]{new Appearance()});
                    }
                });
                Assert.expectException("getIndexBuffer out of bounds", IndexOutOfBoundsException.class, new Assert.Code() {
                    public void run() {
                        VertexBuffer v = createVB(3);
                        Mesh m = new Mesh(v, new TriangleStripArray(0, new int[]{3}), new Appearance());
                        m.getIndexBuffer(1);
                    }
                });
                Assert.expectException("getAppearance out of bounds", IndexOutOfBoundsException.class, new Assert.Code() {
                    public void run() {
                        VertexBuffer v = createVB(3);
                        Mesh m = new Mesh(v, new TriangleStripArray(0, new int[]{3}), new Appearance());
                        m.getAppearance(1);
                    }
                });
            }
        });

        add(new TestCase("getVertexBuffer_and_setAppearance_bounds") {
            public void run() {
                VertexBuffer vb = createVB(3);
                Mesh mesh = new Mesh(vb, new TriangleStripArray(0, new int[]{3}), new Appearance());
                Assert.expectException("setAppearance out of bounds", IndexOutOfBoundsException.class, new Assert.Code() {
                    public void run() {
                        VertexBuffer v = createVB(3);
                        Mesh m = new Mesh(v, new TriangleStripArray(0, new int[]{3}), new Appearance());
                        m.setAppearance(1, new Appearance());
                    }
                });
            }
        });
    }

    private static VertexBuffer createVB(int count) {
        VertexArray pos = new VertexArray(count, 3, 2);
        short[] data = new short[count * 3];
        for (int i = 0; i < data.length; i++) data[i] = (short) (i * 10);
        pos.set(0, count, data);
        VertexBuffer vb = new VertexBuffer();
        vb.setPositions(pos, 1.0f, null);
        return vb;
    }
}
