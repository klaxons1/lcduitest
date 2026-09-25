/*
 * M3G Tester - VertexArray
 */
package m3gtest.tests;

import javax.microedition.m3g.*;

import m3gtest.*;

public class VertexArraySuite extends TestSuite {

    public VertexArraySuite() {
        super("VertexArray", "VertexArray", "constructor, set/get byte and short, component count, vertex count, error cases");

        add(new TestCase("constructor_valid") {
            public void run() {
                VertexArray va = new VertexArray(10, 3, 1);
                Assert.assertEquals("vertex count", 10, va.getVertexCount());
                Assert.assertEquals("component count", 3, va.getComponentCount());
                Assert.assertEquals("component type 1", 1, va.getComponentType());

                VertexArray va2 = new VertexArray(5, 2, 2);
                Assert.assertEquals("vertex count 5", 5, va2.getVertexCount());
                Assert.assertEquals("component count 2", 2, va2.getComponentCount());
                Assert.assertEquals("component type 2", 2, va2.getComponentType());

                VertexArray va3 = new VertexArray(1, 4, 1);
                Assert.assertEquals("4 components", 4, va3.getComponentCount());

                VertexArray vaMax = new VertexArray(65535, 4, 2);
                Assert.assertEquals("max vertices", 65535, vaMax.getVertexCount());
            }
        });

        add(new TestCase("constructor_invalid") {
            public void run() {
                Assert.expectException("0 vertices", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new VertexArray(0, 3, 1);
                    }
                });
                Assert.expectException("65536 vertices", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new VertexArray(65536, 3, 1);
                    }
                });
                Assert.expectException("1 component", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new VertexArray(1, 1, 1);
                    }
                });
                Assert.expectException("5 components", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new VertexArray(1, 5, 1);
                    }
                });
                Assert.expectException("componentSize 0", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new VertexArray(1, 3, 0);
                    }
                });
                Assert.expectException("componentSize 3", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new VertexArray(1, 3, 3);
                    }
                });
            }
        });

        add(new TestCase("set_get_byte") {
            public void run() {
                VertexArray va = new VertexArray(3, 3, 1);
                byte[] data = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
                va.set(0, 3, data);
                byte[] out = new byte[9];
                va.get(0, 3, out);
                for (int i = 0; i < 9; i++) {
                    Assert.assertEquals("byte element " + i, data[i], out[i]);
                }

                // partial set
                byte[] partial = new byte[]{10, 11, 12};
                va.set(1, 1, partial);
                byte[] check = new byte[3];
                va.get(1, 1, check);
                Assert.assertEquals("partial set 0", 10, check[0]);
                Assert.assertEquals("partial set 1", 11, check[1]);
                Assert.assertEquals("partial set 2", 12, check[2]);
            }
        });

        add(new TestCase("set_get_short") {
            public void run() {
                VertexArray va = new VertexArray(2, 3, 2);
                short[] data = new short[]{100, 200, 300, 400, 500, 600};
                va.set(0, 2, data);
                short[] out = new short[6];
                va.get(0, 2, out);
                for (int i = 0; i < 6; i++) {
                    Assert.assertEquals("short element " + i, data[i], out[i]);
                }
            }
        });

        add(new TestCase("set_type_mismatch_throws_IllegalState") {
            public void run() {
                VertexArray byteArray = new VertexArray(1, 3, 1);
                VertexArray shortArray = new VertexArray(1, 3, 2);

                Assert.expectException("set short on byte array", IllegalStateException.class, new Assert.Code() {
                    public void run() {
                        byteArray.set(0, 1, new short[]{1, 2, 3});
                    }
                });
                Assert.expectException("set byte on short array", IllegalStateException.class, new Assert.Code() {
                    public void run() {
                        shortArray.set(0, 1, new byte[]{1, 2, 3});
                    }
                });
                Assert.expectException("get short on byte array", IllegalStateException.class, new Assert.Code() {
                    public void run() {
                        byteArray.get(0, 1, new short[3]);
                    }
                });
                Assert.expectException("get byte on short array", IllegalStateException.class, new Assert.Code() {
                    public void run() {
                        shortArray.get(0, 1, new byte[3]);
                    }
                });
            }
        });

        add(new TestCase("set_null_and_bounds_checks") {
            public void run() {
                VertexArray va = new VertexArray(4, 3, 1);
                Assert.expectException("set null byte[]", NullPointerException.class, new Assert.Code() {
                    public void run() {
                        va.set(0, 1, (byte[]) null);
                    }
                });
                Assert.expectException("get null byte[]", NullPointerException.class, new Assert.Code() {
                    public void run() {
                        va.get(0, 1, (byte[]) null);
                    }
                });
                Assert.expectException("firstVertex negative", IndexOutOfBoundsException.class, new Assert.Code() {
                    public void run() {
                        new VertexArray(4, 3, 1).set(-1, 1, new byte[]{1, 2, 3});
                    }
                });
                Assert.expectException("firstVertex+num > count", IndexOutOfBoundsException.class, new Assert.Code() {
                    public void run() {
                        new VertexArray(4, 3, 1).set(3, 2, new byte[]{1, 2, 3, 4, 5, 6});
                    }
                });
                Assert.expectException("numVertices negative", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new VertexArray(4, 3, 1).set(0, -1, new byte[]{1, 2, 3});
                    }
                });
                Assert.expectException("values too short", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        new VertexArray(4, 3, 1).set(0, 2, new byte[]{1, 2, 3});
                    }
                });
            }
        });

        add(new TestCase("initial_zero_and_duplicate") {
            public void run() {
                VertexArray va = new VertexArray(2, 3, 1);
                byte[] out = new byte[6];
                va.get(0, 2, out);
                for (int i = 0; i < 6; i++) {
                    Assert.assertEquals("initial zero " + i, 0, out[i]);
                }
                VertexArray dup = (VertexArray) va.duplicate();
                Assert.assertEquals("dup vertex count", va.getVertexCount(), dup.getVertexCount());
                byte[] outDup = new byte[6];
                dup.get(0, 2, outDup);
                for (int i = 0; i < 6; i++) {
                    Assert.assertEquals("dup initial zero " + i, 0, outDup[i]);
                }
            }
        });
    }
}
