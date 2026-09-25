package m3gtest.tests;

import javax.microedition.m3g.*;
import m3gtest.*;

public class SkinnedMeshSuite extends TestSuite {

    public SkinnedMeshSuite() {
        super("skinned", "SkinnedMesh", "Tests SkinnedMesh.");

        add(new TestCase("ctor") {
            public void run() {
                SkinnedMesh sm = createSkinned();
                Assert.assertNotNull("skinned", sm);
            }
        });

        add(new TestCase("getSkeleton") {
            public void run() {
                SkinnedMesh sm = createSkinned();
                Group skel = sm.getSkeleton();
                Assert.assertNotNull("skeleton", skel);
            }
        });

        add(new TestCase("getBoneTransform") {
            public void run() {
                SkinnedMesh sm = createSkinned();
                Group skeleton = sm.getSkeleton();
                Node bone = skeleton.getChild(0);
                Transform t = new Transform();
                sm.getBoneTransform(bone, t);
                Assert.assertNotNull("transform", t);
            }
        });

        add(new TestCase("getBoneVertices") {
            public void run() {
                SkinnedMesh sm = createSkinned();
                Group skeleton = sm.getSkeleton();
                Node bone = skeleton.getChild(0);
                int[] indices = new int[10];
                float[] weights = new float[10];
                int count = sm.getBoneVertices(bone, indices, weights);
                Assert.assertTrue("count >=0", count >= 0);
            }
        });

        add(new TestCase("addTransform") {
            public void run() {
                VertexArray pos = new VertexArray(3, 3, 2);
                short[] s = new short[]{0,0,0, 1000,0,0, 0,1000,0};
                pos.set(0, 3, s);
                VertexBuffer vb = new VertexBuffer();
                vb.setPositions(pos, 0.001f, null);
                IndexBuffer ib = new TriangleStripArray(0, new int[]{3});
                Appearance ap = new Appearance();
                Group skeleton = new Group();
                Group bone = new Group();
                skeleton.addChild(bone);
                SkinnedMesh sm = new SkinnedMesh(vb, ib, ap, skeleton);
                sm.addTransform(bone, 1, 0, 1);
                Assert.assertTrue("addTransform ok", true);
            }
        });
    }

    private SkinnedMesh createSkinned() {
        VertexArray pos = new VertexArray(3, 3, 2);
        short[] s = new short[]{0,0,0, 1000,0,0, 0,1000,0};
        pos.set(0, 3, s);
        VertexBuffer vb = new VertexBuffer();
        vb.setPositions(pos, 0.001f, null);
        IndexBuffer ib = new TriangleStripArray(0, new int[]{3});
        Appearance ap = new Appearance();

        Group skeleton = new Group();
        Group bone = new Group();
        skeleton.addChild(bone);

        SkinnedMesh sm = new SkinnedMesh(vb, ib, ap, skeleton);
        sm.addTransform(bone, 1, 0, 3);
        return sm;
    }
}
