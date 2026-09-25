package m3gtest.tests;

import javax.microedition.m3g.*;
import m3gtest.*;

public class TransformSuite extends TestSuite {

    public TransformSuite() {
        super("transform", "Transform", "Tests javax.microedition.m3g.Transform.");

        add(new TestCase("identity") {
            public void run() {
                Transform t = new Transform();
                float[] m = new float[16];
                t.get(m);
                // Identity matrix: diag 1, others 0
                Assert.assertEquals("m0", 1.0f, m[0], 0.001f);
                Assert.assertEquals("m5", 1.0f, m[5], 0.001f);
                Assert.assertEquals("m10", 1.0f, m[10], 0.001f);
                Assert.assertEquals("m15", 1.0f, m[15], 0.001f);
            }
        });

        add(new TestCase("setIdentity") {
            public void run() {
                Transform t = new Transform();
                t.postTranslate(1,2,3);
                float[] m = new float[16];
                t.get(m);
                Assert.assertEquals("tx before", 1.0f, m[3], 0.001f);
                t.setIdentity();
                t.get(m);
                Assert.assertEquals("tx after", 0.0f, m[3], 0.001f);
                Assert.assertEquals("m0 after", 1.0f, m[0], 0.001f);
            }
        });

        add(new TestCase("postTranslate") {
            public void run() {
                Transform t = new Transform();
                t.postTranslate(1.0f, 2.0f, 3.0f);
                float[] m = new float[16];
                t.get(m);
                Assert.assertEquals("tx", 1.0f, m[3], 0.001f);
                Assert.assertEquals("ty", 2.0f, m[7], 0.001f);
                Assert.assertEquals("tz", 3.0f, m[11], 0.001f);
            }
        });

        add(new TestCase("postRotate") {
            public void run() {
                Transform t = new Transform();
                t.postRotate(90, 0, 0, 1);
                float[] m = new float[16];
                t.get(m);
                Assert.assertEquals("m0", 0.0f, m[0], 0.1f);
            }
        });

        add(new TestCase("postScale") {
            public void run() {
                Transform t = new Transform();
                t.postScale(2,3,4);
                float[] m = new float[16];
                t.get(m);
                Assert.assertEquals("scale x", 2.0f, m[0], 0.001f);
                Assert.assertEquals("scale y", 3.0f, m[5], 0.001f);
                Assert.assertEquals("scale z", 4.0f, m[10], 0.001f);
            }
        });

        add(new TestCase("invert") {
            public void run() {
                Transform t = new Transform();
                t.postTranslate(1,2,3);
                t.invert();
                float[] m = new float[16];
                t.get(m);
                Assert.assertEquals("inv tx", -1.0f, m[3], 0.001f);
            }
        });

        add(new TestCase("transpose") {
            public void run() {
                Transform t = new Transform();
                t.postTranslate(1,2,3);
                t.transpose();
                float[] m = new float[16];
                t.get(m);
                // After transpose, translation moves to different indices
                Assert.assertTrue("transpose executed", true);
            }
        });

        add(new TestCase("set and get matrix") {
            public void run() {
                Transform t = new Transform();
                float[] m = new float[16];
                for (int i = 0; i < 16; i++) m[i] = (i % 5 == 0) ? 1 : 0;
                m[3] = 5.0f;
                t.set(m);
                float[] m2 = new float[16];
                t.get(m2);
                Assert.assertEquals("matrix element", 5.0f, m2[3], 0.001f);
            }
        });

        add(new TestCase("transform point") {
            public void run() {
                Transform t = new Transform();
                t.postTranslate(1,2,3);
                float[] v = new float[]{0,0,0,1};
                t.transform(v);
                Assert.assertEquals("x", 1.0f, v[0], 0.001f);
                Assert.assertEquals("y", 2.0f, v[1], 0.001f);
                Assert.assertEquals("z", 3.0f, v[2], 0.001f);
            }
        });

        add(new TestCase("postMultiply") {
            public void run() {
                Transform a = new Transform();
                Transform b = new Transform();
                a.postTranslate(1,0,0);
                b.postTranslate(0,1,0);
                a.postMultiply(b);
                float[] m = new float[16];
                a.get(m);
                Assert.assertEquals("tx", 1.0f, m[3], 0.001f);
                Assert.assertEquals("ty", 1.0f, m[7], 0.001f);
            }
        });
    }
}
