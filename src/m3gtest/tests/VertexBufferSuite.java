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
                vb.setPositions(pos, 0.001f, null);
                Assert.assertEquals("vertex count", 3, vb.getVertexCount());
                float[] bias = new float[3];
                VertexArray got = vb.getPositions(bias);
                Assert.assertNotNull("positions", got);
                Assert.assertEquals("scale", 0.001f, bias[0], 0.0001f);
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
                vb.setPositions(pos, 0.001f, null);
                vb.setTexCoords(0, tex, 0.001f, null);
                float[] bias = new float[3];
                VertexArray got = vb.getTexCoords(0, bias);
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
                vb.setPositions(pos, 0.001f, null);
                vb.setColors(col);
                Assert.assertEquals("vertex count still 3", 3, vb.getVertexCount());
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
                vb.setPositions(pos, 0.001f, null);
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
                vb.setPositions(pos, 1.0f, null);
                Assert.assertEquals("5 verts", 5, vb.getVertexCount());
            }
        });
    }
}
