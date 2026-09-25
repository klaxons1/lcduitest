package m3gtest.tests;

import javax.microedition.m3g.*;
import m3gtest.*;

public class VertexArraySuite extends TestSuite {

    public VertexArraySuite() {
        super("vertexarray", "VertexArray", "Tests VertexArray storage.");

        add(new TestCase("ctor and get") {
            public void run() {
                VertexArray va = new VertexArray(3, 3, 2);
                Assert.assertEquals("vertex count", 3, va.getVertexCount());
                Assert.assertEquals("component count", 3, va.getComponentCount());
                Assert.assertEquals("component type", 2, va.getComponentType());
            }
        });

        add(new TestCase("set byte") {
            public void run() {
                VertexArray va = new VertexArray(2, 3, 1);
                byte[] b = new byte[]{1,2,3, 4,5,6};
                va.set(0, 2, b);
                byte[] out = new byte[6];
                va.get(0, 2, out);
                Assert.assertEquals("b0", 1, out[0]);
                Assert.assertEquals("b5", 6, out[5]);
            }
        });

        add(new TestCase("set short") {
            public void run() {
                VertexArray va = new VertexArray(2, 2, 2);
                short[] s = new short[]{100,200,300,400};
                va.set(0, 2, s);
                short[] out = new short[4];
                va.get(0, 2, out);
                Assert.assertEquals("s0", 100, out[0]);
                Assert.assertEquals("s3", 400, out[3]);
            }
        });

        add(new TestCase("set out of bounds throws") {
            public void run() {
                final VertexArray va = new VertexArray(1, 3, 2);
                Assert.expectException("OOB", IndexOutOfBoundsException.class, new Assert.Code() {
                    public void run() {
                        short[] s = new short[]{0,0,0};
                        va.set(1, 1, s);
                    }
                });
            }
        });

        add(new TestCase("getVertexCount") {
            public void run() {
                VertexArray va = new VertexArray(10, 3, 2);
                Assert.assertEquals("count 10", 10, va.getVertexCount());
            }
        });
    }
}
