package m3gtest.tests;

import javax.microedition.m3g.*;
import m3gtest.*;

public class IndexBufferSuite extends TestSuite {

    public IndexBufferSuite() {
        super("indexbuffer", "IndexBuffer", "Tests TriangleStripArray etc.");

        add(new TestCase("TriangleStripArray ctor") {
            public void run() {
                int[] indices = new int[]{0,1,2};
                int[] strips = new int[]{3};
                TriangleStripArray tsa = new TriangleStripArray(indices, strips);
                Assert.assertEquals("index count", 3, tsa.getIndexCount());
            }
        });

        add(new TestCase("TriangleStripArray get") {
            public void run() {
                int[] indices = new int[]{0,1,2, 2,1,3};
                int[] strips = new int[]{3,3};
                TriangleStripArray tsa = new TriangleStripArray(indices, strips);
                int[] out = new int[6];
                tsa.getIndices(out);
                Assert.assertEquals("first", 0, out[0]);
                Assert.assertEquals("last", 3, out[5]);
            }
        });

        add(new TestCase("TriangleStripArray implicit") {
            public void run() {
                TriangleStripArray tsa = new TriangleStripArray(0, new int[]{3});
                Assert.assertEquals("index count", 3, tsa.getIndexCount());
            }
        });

        add(new TestCase("IndexBuffer polymorphism") {
            public void run() {
                IndexBuffer ib = new TriangleStripArray(0, new int[]{3});
                Assert.assertTrue("instance of IndexBuffer", ib instanceof IndexBuffer);
            }
        });
    }
}
