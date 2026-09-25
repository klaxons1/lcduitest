/*
 * M3G Tester - Transform
 */
package m3gtest.tests;

import javax.microedition.m3g.*;

import m3gtest.*;

public class TransformSuite extends TestSuite {

    public TransformSuite() {
        super("Transform", "Transform matrix", "identity, set/get, invert, transpose, postMultiply, postTranslate/Rotate/Scale, transform");

        add(new TestCase("default_is_identity") {
            public void run() {
                Transform t = new Transform();
                float[] m = new float[16];
                t.get(m);
                float[] identity = new float[]{1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1};
                Assert.assertEquals("identity matrix", identity, m, 0.0001f);
            }
        });

        add(new TestCase("setIdentity") {
            public void run() {
                Transform t = new Transform();
                t.postTranslate(1, 2, 3);
                t.setIdentity();
                float[] m = new float[16];
                t.get(m);
                Assert.assertEquals("after setIdentity x translation 0", 0f, m[3], 0.0001f);
                Assert.assertEquals("after setIdentity y translation 0", 0f, m[7], 0.0001f);
                Assert.assertEquals("after setIdentity z translation 0", 0f, m[11], 0.0001f);
                Assert.assertEquals("diag 1", 1f, m[0], 0.0001f);
                Assert.assertEquals("diag 2", 1f, m[5], 0.0001f);
                Assert.assertEquals("diag 3", 1f, m[10], 0.0001f);
                Assert.assertEquals("diag 4", 1f, m[15], 0.0001f);
            }
        });

        add(new TestCase("copy_constructor_and_set") {
            public void run() {
                Transform a = new Transform();
                a.postTranslate(5, 6, 7);
                Transform b = new Transform(a);
                float[] ma = new float[16];
                float[] mb = new float[16];
                a.get(ma);
                b.get(mb);
                Assert.assertEquals("copy constructor copies values", ma, mb, 0.0001f);

                Transform c = new Transform();
                c.set(a);
                float[] mc = new float[16];
                c.get(mc);
                Assert.assertEquals("set(Transform) copies", ma, mc, 0.0001f);

                float[] raw = new float[16];
                for (int i = 0; i < 16; i++) raw[i] = i;
                Transform d = new Transform();
                d.set(raw);
                float[] md = new float[16];
                d.get(md);
                Assert.assertEquals("set(float[]) copies", raw, md, 0.0001f);
            }
        });

        add(new TestCase("set_get_null_and_length_checks") {
            public void run() {
                Assert.expectException("set(null Transform)", NullPointerException.class, new Assert.Code() {
                    public void run() {
                        new Transform().set((Transform) null);
                    }
                });
                Assert.expectException("set(null float[])", NullPointerException.class, new Assert.Code() {
                    public void run() {
                        new Transform().set((float[]) null);
                    }
                });
                Assert.expectException("set short array", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new Transform().set(new float[15]);
                    }
                });
                Assert.expectException("get(null)", NullPointerException.class, new Assert.Code() {
                    public void run() {
                        new Transform().get(null);
                    }
                });
                Assert.expectException("get short array", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new Transform().get(new float[15]);
                    }
                });
                Assert.expectException("copy constructor null", NullPointerException.class, new Assert.Code() {
                    public void run() {
                        new Transform((Transform) null);
                    }
                });
            }
        });

        add(new TestCase("postTranslate") {
            public void run() {
                Transform t = new Transform();
                t.postTranslate(1, 2, 3);
                float[] m = new float[16];
                t.get(m);
                // Row-major: translation in last column: indices 3,7,11
                Assert.assertEquals("tx", 1f, m[3], 0.0001f);
                Assert.assertEquals("ty", 2f, m[7], 0.0001f);
                Assert.assertEquals("tz", 3f, m[11], 0.0001f);
                t.postTranslate(1, 0, 0);
                t.get(m);
                Assert.assertEquals("postTranslate accumulates", 2f, m[3], 0.0001f);
            }
        });

        add(new TestCase("postScale") {
            public void run() {
                Transform t = new Transform();
                t.postScale(2, 3, 4);
                float[] m = new float[16];
                t.get(m);
                Assert.assertEquals("sx", 2f, m[0], 0.0001f);
                Assert.assertEquals("sy", 3f, m[5], 0.0001f);
                Assert.assertEquals("sz", 4f, m[10], 0.0001f);
            }
        });

        add(new TestCase("postRotate_and_postRotateQuat") {
            public void run() {
                Transform t = new Transform();
                t.postRotate(90, 0, 0, 1);
                float[] m = new float[16];
                t.get(m);
                // 90 deg around Z should map (1,0,0) to (0,1,0) approx
                float[] vec = new float[]{1, 0, 0, 1};
                t.transform(vec);
                Assert.assertEquals("rotated x ~0", 0f, vec[0], 0.01f);
                Assert.assertEquals("rotated y ~1", 1f, vec[1], 0.01f);

                Transform q = new Transform();
                // quaternion for 90 deg around Z: axis 0,0,1 angle 90 => q = (0,0, sin45, cos45)
                float sin = (float) Math.sin(Math.PI / 4);
                float cos = (float) Math.cos(Math.PI / 4);
                q.postRotateQuat(0, 0, sin, cos);
                float[] mq = new float[16];
                q.get(mq);
                Assert.info("quat matrix[0]", mq[0]);
                // Should be similar to previous rotation
                float[] vec2 = new float[]{1, 0, 0, 1};
                q.transform(vec2);
                Assert.assertEquals("quat rotated x ~0", 0f, vec2[0], 0.02f);
                Assert.assertEquals("quat rotated y ~1", 1f, vec2[1], 0.02f);
            }
        });

        add(new TestCase("postMultiply") {
            public void run() {
                Transform a = new Transform();
                a.postTranslate(1, 0, 0);
                Transform b = new Transform();
                b.postTranslate(0, 1, 0);
                a.postMultiply(b);
                float[] m = new float[16];
                a.get(m);
                Assert.assertEquals("postMultiply tx", 1f, m[3], 0.0001f);
                Assert.assertEquals("postMultiply ty", 1f, m[7], 0.0001f);

                Assert.expectException("postMultiply null", NullPointerException.class, new Assert.Code() {
                    public void run() {
                        new Transform().postMultiply(null);
                    }
                });
            }
        });

        add(new TestCase("invert_and_transpose") {
            public void run() {
                Transform t = new Transform();
                t.postTranslate(1, 2, 3);
                t.invert();
                float[] m = new float[16];
                t.get(m);
                Assert.assertEquals("invert translation negated x", -1f, m[3], 0.0001f);
                Assert.assertEquals("invert translation negated y", -2f, m[7], 0.0001f);
                Assert.assertEquals("invert translation negated z", -3f, m[11], 0.0001f);

                // Invert singular should throw ArithmeticException
                Transform singular = new Transform();
                singular.postScale(0, 0, 0);
                Assert.expectException("invert singular", ArithmeticException.class, new Assert.Code() {
                    public void run() {
                        Transform s = new Transform();
                        s.postScale(0, 0, 0);
                        s.invert();
                    }
                });

                Transform tr = new Transform();
                float[] orig = new float[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
                tr.set(orig);
                tr.transpose();
                float[] trans = new float[16];
                tr.get(trans);
                Assert.assertEquals("transpose [1] should be 5", 5f, trans[1], 0.0001f);
                Assert.assertEquals("transpose [4] should be 2", 2f, trans[4], 0.0001f);
            }
        });

        add(new TestCase("transform_vectors") {
            public void run() {
                Transform t = new Transform();
                t.postTranslate(10, 20, 30);
                float[] vecs = new float[]{0, 0, 0, 1, 1, 1, 1, 1};
                t.transform(vecs);
                Assert.assertEquals("first vec x translated", 10f, vecs[0], 0.0001f);
                Assert.assertEquals("first vec y translated", 20f, vecs[1], 0.0001f);
                Assert.assertEquals("first vec z translated", 30f, vecs[2], 0.0001f);
                Assert.assertEquals("second vec x", 11f, vecs[4], 0.0001f);
                // w should stay 1
                Assert.assertEquals("w stays 1", 1f, vecs[3], 0.0001f);
            }
        });

        add(new TestCase("transform_vertexarray") {
            public void run() {
                VertexArray va = new VertexArray(2, 3, 2);
                short[] data = new short[]{0, 0, 0, 1, 0, 0};
                va.set(0, 2, data);
                Transform t = new Transform();
                t.postTranslate(5, 0, 0);
                float[] out = new float[6];
                t.transform(va, out, false);
                Assert.assertEquals("transformed x 0+5", 5f, out[0], 0.0001f);
                Assert.assertEquals("transformed x 1+5", 6f, out[3], 0.0001f);

                float[] outW = new float[8];
                t.transform(va, outW, true);
                // with W true, out should be 4 components per vertex
                Assert.info("outW length 8", outW.length);
            }
        });
    }
}
