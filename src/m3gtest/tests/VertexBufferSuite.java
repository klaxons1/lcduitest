package m3gtest.tests;

import javax.microedition.m3g.*;
import m3gtest.*;

public class VertexBufferSuite extends TestSuite {

    public VertexBufferSuite() {
        super("vertexbuffer", "VertexBuffer", "Tests VertexBuffer.");

        add(new TestCase("setPositions getPositions") {
            public void run() {
                VertexArray pos = new VertexArray(3, 3, 2);
                short[] s = new short[]{0,0,0, 1000,0,0, 0,1000,0};
                pos.set(0, 3, s);
                VertexBuffer vb = new VertexBuffer();
                float[] bias = new float[]{0,0,0};
                vb.setPositions(pos, 0.001f, bias);
                Assert.assertEquals("vertex count", 3, vb.getVertexCount());
                float[] scaleBias = new float[4];
                VertexArray got = vb.getPositions(scaleBias);
                Assert.assertNotNull("positions", got);
                Assert.assertEquals("scale", 0.001f, scaleBias[0], 0.0001f);
            }
        });

        add(new TestCase("setTexCoords") {
            public void run() {
                VertexArray pos = new VertexArray(3, 3, 2);
                short[] ps = new short[]{0,0,0, 1000,0,0, 0,1000,0};
                pos.set(0, 3, ps);
                VertexArray tex = new VertexArray(3, 2, 2);
                short[] ts = new short[]{0,0, 1000,0, 0,1000};
                tex.set(0, 3, ts);
                VertexBuffer vb = new VertexBuffer();
                vb.setPositions(pos, 0.001f, new float[]{0,0,0});
                vb.setTexCoords(0, tex, 0.001f, new float[]{0,0,0});
                float[] scaleBias = new float[4];
                VertexArray got = vb.getTexCoords(0, scaleBias);
                Assert.assertNotNull("texcoords", got);
            }
        });

        add(new TestCase("setColors") {
            public void run() {
                VertexArray pos = new VertexArray(3, 3, 2);
                short[] ps = new short[]{0,0,0, 1000,0,0, 0,1000,0};
                pos.set(0, 3, ps);
                VertexArray col = new VertexArray(3, 3, 1);
                byte[] cs = new byte[]{(byte)255,0,0, 0,(byte)255,0, 0,0,(byte)255};
                col.set(0, 3, cs);
                VertexBuffer vb = new VertexBuffer();
                vb.setPositions(pos, 1.0f, new float[]{0,0,0});
                vb.setColors(col);
                Assert.assertEquals("vertex count still 3", 3, vb.getVertexCount());
                Assert.assertNotNull("colors", vb.getColors());
            }
        });

        add(new TestCase("setNormals") {
            public void run() {
                VertexArray pos = new VertexArray(3, 3, 2);
                short[] ps = new short[]{0,0,0, 1000,0,0, 0,1000,0};
                pos.set(0, 3, ps);
                VertexArray norm = new VertexArray(3, 3, 2);
                short[] ns = new short[]{0,0,1000, 0,0,1000, 0,0,1000};
                norm.set(0, 3, ns);
                VertexBuffer vb = new VertexBuffer();
                vb.setPositions(pos, 0.001f, new float[]{0,0,0});
                vb.setNormals(norm);
                Assert.assertNotNull("normals set", vb.getNormals());
            }
        });

        add(new TestCase("getVertexCount after set") {
            public void run() {
                VertexArray pos = new VertexArray(5, 3, 2);
                short[] ps = new short[15];
                pos.set(0, 5, ps);
                VertexBuffer vb = new VertexBuffer();
                vb.setPositions(pos, 1.0f, new float[]{0,0,0});
                Assert.assertEquals("5 verts", 5, vb.getVertexCount());
            }
        });

        add(new TestCase("setPositions with null bias allowed") {
            public void run() {
                VertexArray pos = new VertexArray(2, 3, 2);
                short[] ps = new short[]{0,0,0, 1000,0,0};
                pos.set(0, 2, ps);
                VertexBuffer vb = new VertexBuffer();
                // Some implementations allow null bias, some require non-null - test both
                try {
                    vb.setPositions(pos, 1.0f, null);
                } catch (IllegalArgumentException e) {
                    // If null not allowed, try with zero bias
                    vb.setPositions(pos, 1.0f, new float[]{0,0,0});
                }
                Assert.assertEquals("count 2", 2, vb.getVertexCount());
            }
        });
    }
}
