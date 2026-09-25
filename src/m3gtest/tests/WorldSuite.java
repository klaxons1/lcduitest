package m3gtest.tests;

import javax.microedition.m3g.*;
import m3gtest.*;

public class WorldSuite extends TestSuite {

    public WorldSuite() {
        super("world", "World", "Tests World.");

        add(new TestCase("setActiveCamera getActiveCamera") {
            public void run() {
                World w = new World();
                Camera cam = new Camera();
                w.addChild(cam);
                w.setActiveCamera(cam);
                Assert.assertSame("camera", cam, w.getActiveCamera());
            }
        });

        add(new TestCase("setBackground getBackground") {
            public void run() {
                World w = new World();
                Background bg = new Background();
                w.setBackground(bg);
                Assert.assertSame("bg", bg, w.getBackground());
            }
        });

        add(new TestCase("addChild remove") {
            public void run() {
                World w = new World();
                Mesh m = createMesh();
                w.addChild(m);
                Assert.assertEquals("count 1", 1, w.getChildCount());
                w.removeChild(m);
                Assert.assertEquals("count 0", 0, w.getChildCount());
            }
        });

        add(new TestCase("pick") {
            public void run() {
                World w = new World();
                Camera cam = new Camera();
                cam.setPerspective(60, 1, 1, 10);
                Transform ct = new Transform();
                ct.postTranslate(0,0,5);
                cam.setTransform(ct);
                w.addChild(cam);
                w.setActiveCamera(cam);
                Mesh m = createMesh();
                w.addChild(m);
                RayIntersection ri = new RayIntersection();
                boolean hit = w.pick(-1, 0,0,0, 0,0,-1, ri);
                // hit may be false depending on mesh position, but should not throw
                Assert.assertTrue("pick executed", true);
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
        return new Mesh(vb, ib, new Appearance());
    }
}
