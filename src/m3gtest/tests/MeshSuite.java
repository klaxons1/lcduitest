package m3gtest.tests;

import javax.microedition.m3g.*;
import m3gtest.*;

public class MeshSuite extends TestSuite {

    public MeshSuite() {
        super("mesh", "Mesh", "Tests Mesh.");

        add(new TestCase("ctor") {
            public void run() {
                Mesh m = createMesh();
                Assert.assertNotNull("mesh", m);
            }
        });

        add(new TestCase("getVertexBuffer") {
            public void run() {
                Mesh m = createMesh();
                Assert.assertNotNull("vb", m.getVertexBuffer());
            }
        });

        add(new TestCase("getIndexBuffer") {
            public void run() {
                Mesh m = createMesh();
                Assert.assertNotNull("ib", m.getIndexBuffer());
            }
        });

        add(new TestCase("getAppearance") {
            public void run() {
                Mesh m = createMesh();
                Assert.assertNotNull("ap", m.getAppearance());
            }
        });

        add(new TestCase("setAppearance") {
            public void run() {
                Mesh m = createMesh();
                Appearance ap = new Appearance();
                m.setAppearance(ap);
                Assert.assertSame("ap", ap, m.getAppearance());
            }
        });
    }

    private Mesh createMesh() {
        VertexArray pos = new VertexArray(3, 3, 2);
        short[] s = new short[]{0,0,0, 1000,0,0, 0,1000,0};
        pos.set(0, 3, s);
        VertexBuffer vb = new VertexBuffer();
        vb.setPositions(pos, 0.001f, null);
        IndexBuffer ib = new TriangleStripArray(0, new int[]{3});
        Appearance ap = new Appearance();
        return new Mesh(vb, ib, ap);
    }
}
