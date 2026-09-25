/*
 * M3G Tester - VertexBuffer
 */
package m3gtest.tests;

import javax.microedition.m3g.*;

import m3gtest.*;

public class VertexBufferSuite extends TestSuite {

    public VertexBufferSuite() {
        super("VertexBuffer", "VertexBuffer", "positions, normals, colors, texcoords, scale/bias, defaultColor");

        add(new TestCase("default_state") {
            public void run() {
                VertexBuffer vb = new VertexBuffer();
                Assert.assertEquals("default vertex count 0", 0, vb.getVertexCount());
                Assert.assertNull("default positions null", vb.getPositions(null));
                Assert.assertNull("default normals null", vb.getNormals());
                Assert.assertNull("default colors null", vb.getColors());
                Assert.assertEquals("default defaultColor 0xFFFFFFFF", 0xFFFFFFFF, vb.getDefaultColor());
            }
        });

        add(new TestCase("set_positions") {
            public void run() {
                VertexBuffer vb = new VertexBuffer();
                VertexArray pos = new VertexArray(3, 3, 2);
                short[] data = new short[]{0, 0, 0, 1, 0, 0, 0, 1, 0};
                pos.set(0, 3, data);
                vb.setPositions(pos, 1.0f, null);
                Assert.assertEquals("vertex count after setPositions", 3, vb.getVertexCount());
                VertexArray retrieved = vb.getPositions(null);
                Assert.assertSame("getPositions returns same", pos, retrieved);

                // with bias
                float[] bias = new float[]{0, 0, 0};
                float[] scaleBias = new float[4];
                vb.setPositions(pos, 2.0f, bias);
                VertexArray ret = vb.getPositions(scaleBias);
                Assert.assertSame("getPositions returns same after scale", pos, ret);
                Assert.assertEquals("scale 2.0", 2.0f, scaleBias[0], 0.0001f);
                Assert.assertEquals("bias X", 0f, scaleBias[1], 0.0001f);

                // null positions should be allowed to clear? spec says null to disable
                Assert.expectNoException("setPositions null clears", new Assert.Code() {
                    public void run() {
                        VertexBuffer v = new VertexBuffer();
                        VertexArray p = new VertexArray(1, 3, 1);
                        p.set(0, 1, new byte[]{0, 0, 0});
                        v.setPositions(p, 1.0f, null);
                        v.setPositions(null, 1.0f, null);
                    }
                });
            }
        });

        add(new TestCase("set_normals") {
            public void run() {
                VertexBuffer vb = new VertexBuffer();
                VertexArray pos = new VertexArray(3, 3, 2);
                pos.set(0, 3, new short[]{0, 0, 0, 1, 0, 0, 0, 1, 0});
                vb.setPositions(pos, 1.0f, null);

                VertexArray normals = new VertexArray(3, 3, 1);
                normals.set(0, 3, new byte[]{0, 0, 127, 0, 0, 127, 0, 0, 127});
                vb.setNormals(normals);
                Assert.assertSame("normals", normals, vb.getNormals());

                vb.setNormals(null);
                Assert.assertNull("normals null after set null", vb.getNormals());
            }
        });

        add(new TestCase("set_colors") {
            public void run() {
                VertexBuffer vb = new VertexBuffer();
                VertexArray pos = new VertexArray(3, 3, 2);
                pos.set(0, 3, new short[]{0, 0, 0, 1, 0, 0, 0, 1, 0});
                vb.setPositions(pos, 1.0f, null);

                VertexArray colors = new VertexArray(3, 4, 1);
                colors.set(0, 3, new byte[]{(byte) 255, 0, 0, (byte) 255, 0, (byte) 255, 0, (byte) 255, 0, 0, (byte) 255, (byte) 255});
                vb.setColors(colors);
                Assert.assertSame("colors", colors, vb.getColors());

                vb.setColors(null);
                Assert.assertNull("colors null after set null", vb.getColors());
            }
        });

        add(new TestCase("set_texcoords") {
            public void run() {
                VertexBuffer vb = new VertexBuffer();
                VertexArray pos = new VertexArray(3, 3, 2);
                pos.set(0, 3, new short[]{0, 0, 0, 1, 0, 0, 0, 1, 0});
                vb.setPositions(pos, 1.0f, null);

                VertexArray tex = new VertexArray(3, 2, 2);
                tex.set(0, 3, new short[]{0, 0, 1, 0, 0, 1});
                vb.setTexCoords(0, tex, 1.0f, null);
                // No getTexCoordCount in spec, so test by getting back
                VertexArray ret = vb.getTexCoords(0, null);
                Assert.assertSame("getTexCoords returns same", tex, ret);
                float[] sb = new float[3];
                VertexArray ret2 = vb.getTexCoords(0, sb);
                Assert.assertSame("getTexCoords with scaleBias", tex, ret2);
                Assert.assertEquals("tex scale 1.0", 1.0f, sb[0], 0.0001f);

                // set second texcoord
                VertexArray tex2 = new VertexArray(3, 2, 1);
                tex2.set(0, 3, new byte[]{0, 0, 1, 0, 0, 1});
                vb.setTexCoords(1, tex2, 1.0f, null);
                VertexArray ret3 = vb.getTexCoords(1, null);
                Assert.assertSame("second texcoord", tex2, ret3);

                // invalid texcoord index
                Assert.expectException("texcoord index negative", IndexOutOfBoundsException.class, new Assert.Code() {
                    public void run() {
                        VertexBuffer v = new VertexBuffer();
                        VertexArray p = new VertexArray(1, 3, 1);
                        p.set(0, 1, new byte[]{0, 0, 0});
                        v.setPositions(p, 1.0f, null);
                        VertexArray t = new VertexArray(1, 2, 1);
                        t.set(0, 1, new byte[]{0, 0});
                        v.setTexCoords(-1, t, 1.0f, null);
                    }
                });
            }
        });

        add(new TestCase("default_color") {
            public void run() {
                VertexBuffer vb = new VertexBuffer();
                vb.setDefaultColor(0xFF00FF00);
                Assert.assertEquals("default color set", 0xFF00FF00, vb.getDefaultColor());
                vb.setDefaultColor(0x00000000);
                Assert.assertEquals("transparent black", 0x00000000, vb.getDefaultColor());
            }
        });

        add(new TestCase("vertex_count_consistency") {
            public void run() {
                VertexBuffer vb = new VertexBuffer();
                VertexArray pos = new VertexArray(4, 3, 1);
                pos.set(0, 4, new byte[]{0, 0, 0, 1, 0, 0, 0, 1, 0, 1, 1, 0});
                vb.setPositions(pos, 1.0f, null);
                Assert.assertEquals("4 vertices", 4, vb.getVertexCount());

                VertexArray normals = new VertexArray(4, 3, 1);
                normals.set(0, 4, new byte[]{0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1});
                vb.setNormals(normals);
                Assert.assertEquals("still 4 vertices", 4, vb.getVertexCount());

                // Mismatched vertex count should throw IllegalArgumentException?
                VertexArray bad = new VertexArray(3, 3, 1);
                bad.set(0, 3, new byte[]{0, 0, 1, 0, 0, 1, 0, 0, 1});
                Assert.expectException("mismatched vertex count for normals", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        VertexBuffer v = new VertexBuffer();
                        VertexArray p = new VertexArray(4, 3, 1);
                        p.set(0, 4, new byte[12]);
                        v.setPositions(p, 1.0f, null);
                        VertexArray n = new VertexArray(3, 3, 1);
                        n.set(0, 3, new byte[9]);
                        v.setNormals(n);
                    }
                });
            }
        });
    }
}
