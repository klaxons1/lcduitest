/*
 * M3G Tester - MorphingMesh
 */
package m3gtest.tests;

import javax.microedition.m3g.*;

import m3gtest.*;

public class MorphingMeshSuite extends TestSuite {

    public MorphingMeshSuite() {
        super("MorphingMesh", "MorphingMesh", "constructor, morph targets, weights");

        add(new TestCase("constructor") {
            public void run() {
                VertexBuffer base = createVB(3);
                VertexBuffer target = createVB(3);
                VertexBuffer[] targets = new VertexBuffer[]{target};
                IndexBuffer ib = new TriangleStripArray(0, new int[]{3});
                Appearance ap = new Appearance();
                MorphingMesh mm = new MorphingMesh(base, targets, ib, ap);
                Assert.assertNotNull("mm not null", mm);
                Assert.assertEquals("morph target count 1", 1, mm.getMorphTargetCount());
                Assert.assertSame("base", base, mm.getVertexBuffer());
                Assert.assertSame("target 0", target, mm.getMorphTarget(0));
            }
        });

        add(new TestCase("weights") {
            public void run() {
                VertexBuffer base = createVB(3);
                VertexBuffer t1 = createVB(3);
                VertexBuffer t2 = createVB(3);
                MorphingMesh mm = new MorphingMesh(base, new VertexBuffer[]{t1, t2}, new TriangleStripArray(0, new int[]{3}), new Appearance());
                mm.setWeights(new float[]{0.5f, 0.5f});
                float[] w = new float[2];
                mm.getWeights(w);
                Assert.assertEquals("weight 0", 0.5f, w[0], 0.001f);
                Assert.assertEquals("weight 1", 0.5f, w[1], 0.001f);

                mm.setWeights(new float[]{1.0f, 0.0f});
                mm.getWeights(w);
                Assert.assertEquals("weight 0 after", 1.0f, w[0], 0.001f);
            }
        });

        add(new TestCase("invalid_args") {
            public void run() {
                Assert.expectException("null base", NullPointerException.class, new Assert.Code() {
                    public void run() {
                        new MorphingMesh(null, new VertexBuffer[]{createVB(3)}, new TriangleStripArray(0, new int[]{3}), new Appearance());
                    }
                });
                Assert.expectException("null targets", NullPointerException.class, new Assert.Code() {
                    public void run() {
                        new MorphingMesh(createVB(3), null, new TriangleStripArray(0, new int[]{3}), new Appearance());
                    }
                });
                Assert.expectException("empty targets", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new MorphingMesh(createVB(3), new VertexBuffer[0], new TriangleStripArray(0, new int[]{3}), new Appearance());
                    }
                });
                Assert.expectException("null target element", NullPointerException.class, new Assert.Code() {
                    public void run() {
                        new MorphingMesh(createVB(3), new VertexBuffer[]{null}, new TriangleStripArray(0, new int[]{3}), new Appearance());
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
