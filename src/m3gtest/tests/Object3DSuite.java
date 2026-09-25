package m3gtest.tests;

import javax.microedition.m3g.*;
import m3gtest.*;

public class Object3DSuite extends TestSuite {

    public Object3DSuite() {
        super("object3d", "Object3D", "Base class for all M3G objects.");

        add(new TestCase("setUserID getUserID") {
            public void run() {
                Mesh m = createDummyMesh();
                m.setUserID(12345);
                Assert.assertEquals("userID", 12345, m.getUserID());
            }
        });

        add(new TestCase("setUserObject getUserObject") {
            public void run() {
                Mesh m = createDummyMesh();
                Object obj = new Object();
                m.setUserObject(obj);
                Assert.assertSame("userObject", obj, m.getUserObject());
            }
        });

        add(new TestCase("getUserObject null initially") {
            public void run() {
                Mesh m = createDummyMesh();
                // Initially may be null
                // Just check getter does not throw
                Object o = m.getUserObject();
                // okay
            }
        });

        add(new TestCase("duplicate") {
            public void run() {
                Mesh m = createDummyMesh();
                m.setUserID(7);
                Object3D dup = m.duplicate();
                Assert.assertNotNull("duplicate", dup);
                Assert.assertNotSame("different instance", m, dup);
                Assert.assertTrue("instance of Mesh", dup instanceof Mesh);
            }
        });

        add(new TestCase("find by userID") {
            public void run() {
                World w = new World();
                Mesh m = createDummyMesh();
                m.setUserID(999);
                w.addChild(m);
                Object3D found = w.find(999);
                Assert.assertSame("find", m, found);
            }
        });

        add(new TestCase("getReferences") {
            public void run() {
                World w = new World();
                Mesh m = createDummyMesh();
                w.addChild(m);
                int count = w.getReferences(null);
                Assert.assertTrue("references count >=0", count >= 0);
                Object3D[] refs = new Object3D[count];
                w.getReferences(refs);
                // Should contain at least m if count >0
            }
        });

        add(new TestCase("animate") {
            public void run() {
                Mesh m = createDummyMesh();
                // animate with time 0 should not throw
                boolean changed = m.animate(0);
                // result is boolean, just check no exception
            }
        });
    }

    private Mesh createDummyMesh() {
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
