/*
 * M3G Tester - SkinnedMesh
 */
package m3gtest.tests;

import javax.microedition.m3g.*;

import m3gtest.*;

public class SkinnedMeshSuite extends TestSuite {

    public SkinnedMeshSuite() {
        super("SkinnedMesh", "SkinnedMesh", "constructor, skeleton, bone transforms");

        add(new TestCase("constructor") {
            public void run() {
                VertexBuffer vb = createVB(3);
                IndexBuffer ib = new TriangleStripArray(0, new int[]{3});
                Appearance ap = new Appearance();
                Group skeleton = new Group();
                Group bone = new Group();
                skeleton.addChild(bone);
                SkinnedMesh sm = new SkinnedMesh(vb, ib, ap, skeleton);
                Assert.assertNotNull("sm not null", sm);
                Assert.assertSame("skeleton", skeleton, sm.getSkeleton());
                // Bone count via getBoneVertices: initially 0 bones have vertices
                int count = sm.getBoneVertices(bone, null, null);
                Assert.assertEquals("initial bone vertices 0", 0, count);
            }
        });

        add(new TestCase("bones") {
            public void run() {
                VertexBuffer vb = createVB(3);
                IndexBuffer ib = new TriangleStripArray(0, new int[]{3});
                Appearance ap = new Appearance();
                Group skeleton = new Group();
                Group bone1 = new Group();
                Group bone2 = new Group();
                skeleton.addChild(bone1);
                skeleton.addChild(bone2);
                SkinnedMesh sm = new SkinnedMesh(vb, ib, ap, skeleton);
                // getBoneTransform?
                Transform t = new Transform();
                sm.getBoneTransform(bone1, t);
                // Should not throw
                Assert.expectException("getBoneTransform null bone", NullPointerException.class, new Assert.Code() {
                    public void run() {
                        SkinnedMesh s = createSkinnedMesh();
                        s.getBoneTransform(null, new Transform());
                    }
                });
                Assert.expectException("getBoneTransform null transform", NullPointerException.class, new Assert.Code() {
                    public void run() {
                        SkinnedMesh s = createSkinnedMesh();
                        s.getBoneTransform(new Group(), null);
                    }
                });
            }
        });

        add(new TestCase("invalid_args") {
            public void run() {
                Assert.expectException("null vb", NullPointerException.class, new Assert.Code() {
                    public void run() {
                        new SkinnedMesh(null, new TriangleStripArray(0, new int[]{3}), new Appearance(), new Group());
                    }
                });
                Assert.expectException("null skeleton", NullPointerException.class, new Assert.Code() {
                    public void run() {
                        new SkinnedMesh(createVB(3), new TriangleStripArray(0, new int[]{3}), new Appearance(), null);
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

    private static SkinnedMesh createSkinnedMesh() {
        VertexBuffer vb = createVB(3);
        IndexBuffer ib = new TriangleStripArray(0, new int[]{3});
        Appearance ap = new Appearance();
        Group skeleton = new Group();
        skeleton.addChild(new Group());
        return new SkinnedMesh(vb, ib, ap, skeleton);
    }
}
