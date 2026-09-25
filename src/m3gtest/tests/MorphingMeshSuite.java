package m3gtest.tests;

import javax.microedition.m3g.*;
import m3gtest.*;

public class MorphingMeshSuite extends TestSuite {

    public MorphingMeshSuite() {
        super("morphing", "MorphingMesh", "Tests MorphingMesh.");

        add(new TestCase("ctor") {
            public void run() {
                MorphingMesh mm = createMorph();
                Assert.assertNotNull("morph", mm);
            }
        });

        add(new TestCase("setWeights getWeights") {
            public void run() {
                MorphingMesh mm = createMorph();
                float[] w = new float[]{0.5f};
                mm.setWeights(w);
                float[] out = new float[1];
                mm.getWeights(out);
                Assert.assertEquals("weight", 0.5f, out[0], 0.001f);
            }
        });

        add(new TestCase("getMorphTarget") {
            public void run() {
                MorphingMesh mm = createMorph();
                VertexBuffer target = mm.getMorphTarget(0);
                Assert.assertNotNull("target", target);
            }
        });
    }

    private MorphingMesh createMorph() {
        VertexArray posBase = new VertexArray(3, 3, 2);
        short[] s1 = new short[]{0,0,0, 1000,0,0, 0,1000,0};
        posBase.set(0, 3, s1);
        VertexBuffer base = new VertexBuffer();
        base.setPositions(posBase, 0.001f, null);

        VertexArray posTarget = new VertexArray(3, 3, 2);
        short[] s2 = new short[]{0,0,0, 1000,0,0, 500,1500,0};
        posTarget.set(0, 3, s2);
        VertexBuffer target = new VertexBuffer();
        target.setPositions(posTarget, 0.001f, null);

        IndexBuffer ib = new TriangleStripArray(0, new int[]{3});
        Appearance ap = new Appearance();
        return new MorphingMesh(base, new VertexBuffer[]{target}, ib, ap);
    }
}
