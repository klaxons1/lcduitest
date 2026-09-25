/*
 * M3G Tester - IndexBuffer and TriangleStripArray
 */
package m3gtest.tests;

import javax.microedition.m3g.*;

import m3gtest.*;

public class IndexBufferSuite extends TestSuite {

    public IndexBufferSuite() {
        super("IndexBuffer", "IndexBuffer & TriangleStripArray", "construction, index count, get/set indices, strips");

        add(new TestCase("TriangleStripArray_constructor") {
            public void run() {
                TriangleStripArray tsa = new TriangleStripArray(0, new int[]{3});
                Assert.assertEquals("index count 3", 3, tsa.getIndexCount());
                Assert.assertEquals("strip count 1", 1, tsa.getStripCount());

                TriangleStripArray tsa2 = new TriangleStripArray(new int[]{0, 1, 2}, new int[]{3});
                Assert.assertEquals("explicit indices count 3", 3, tsa2.getIndexCount());

                TriangleStripArray multi = new TriangleStripArray(0, new int[]{3, 3, 3});
                Assert.assertEquals("3 strips", 3, multi.getStripCount());
                Assert.assertEquals("total 9 indices", 9, multi.getIndexCount());
            }
        });

        add(new TestCase("TriangleStripArray_invalid_args") {
            public void run() {
                Assert.expectException("null strips", NullPointerException.class, new Assert.Code() {
                    public void run() {
                        new TriangleStripArray(0, null);
                    }
                });
                Assert.expectException("empty strips", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new TriangleStripArray(0, new int[0]);
                    }
                });
                Assert.expectException("strip length <3", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new TriangleStripArray(0, new int[]{2});
                    }
                });
                Assert.expectException("firstIndex negative", IndexOutOfBoundsException.class, new Assert.Code() {
                    public void run() {
                        new TriangleStripArray(-1, new int[]{3});
                    }
                });
                Assert.expectException("indices and strips length mismatch", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new TriangleStripArray(new int[]{0, 1}, new int[]{3});
                    }
                });
            }
        });

        add(new TestCase("getIndices") {
            public void run() {
                TriangleStripArray tsa = new TriangleStripArray(new int[]{5, 6, 7, 8, 9}, new int[]{3, 2});
                int[] out = new int[5];
                tsa.getIndices(out);
                Assert.assertEquals("first index", 5, out[0]);
                Assert.assertEquals("last index", 9, out[4]);

                int[] strips = new int[2];
                tsa.getStripLengths(strips);
                Assert.assertEquals("first strip len", 3, strips[0]);
                Assert.assertEquals("second strip len", 2, strips[1]);

                // implicit indices
                TriangleStripArray implicit = new TriangleStripArray(10, new int[]{3});
                int[] out2 = new int[3];
                implicit.getIndices(out2);
                Assert.assertEquals("implicit first", 10, out2[0]);
                Assert.assertEquals("implicit second", 11, out2[1]);
                Assert.assertEquals("implicit third", 12, out2[2]);
            }
        });

        add(new TestCase("setIndices_via_subclass") {
            public void run() {
                // IndexBuffer is abstract, TriangleStripArray is concrete
                // Test that duplicate works
                TriangleStripArray tsa = new TriangleStripArray(0, new int[]{3});
                TriangleStripArray dup = (TriangleStripArray) tsa.duplicate();
                Assert.assertEquals("dup index count", 3, dup.getIndexCount());
                Assert.assertNotSame("dup different instance", tsa, dup);
            }
        });

        add(new TestCase("IndexBuffer_is_abstract_but_TSA_is_IndexBuffer") {
            public void run() {
                TriangleStripArray tsa = new TriangleStripArray(0, new int[]{3});
                Assert.assertTrue("TSA instanceof IndexBuffer", tsa instanceof IndexBuffer);
                Assert.assertTrue("TSA instanceof Object3D", tsa instanceof Object3D);
            }
        });
    }
}
