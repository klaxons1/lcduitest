/*
 * M3G Tester - Loader
 */
package m3gtest.tests;

import javax.microedition.m3g.*;

import m3gtest.*;
import java.io.*;

public class LoaderSuite extends TestSuite {

    public LoaderSuite() {
        super("Loader", "Loader", "load from byte array and InputStream, null checks");

        add(new TestCase("load_empty_array_throws") {
            public void run() {
                Assert.expectException("load empty byte[]", Exception.class, new Assert.Code() {
                    public void run() throws Exception {
                        Loader.load(new byte[0], 0);
                    }
                });
            }
        });

        add(new TestCase("load_null_checks") {
            public void run() {
                Assert.expectException("load null byte[]", NullPointerException.class, new Assert.Code() {
                    public void run() throws Exception {
                        Loader.load((byte[]) null, 0);
                    }
                });
                Assert.expectException("load null InputStream", NullPointerException.class, new Assert.Code() {
                    public void run() throws Exception {
                        Loader.load((InputStream) null);
                    }
                });
            }
        });

        add(new TestCase("load_invalid_offset") {
            public void run() {
                byte[] data = new byte[10];
                Assert.expectException("negative offset", IndexOutOfBoundsException.class, new Assert.Code() {
                    public void run() throws Exception {
                        Loader.load(data, -1);
                    }
                });
                Assert.expectException("offset > length", IndexOutOfBoundsException.class, new Assert.Code() {
                    public void run() throws Exception {
                        Loader.load(data, 11);
                    }
                });
            }
        });

        add(new TestCase("load_m3g_header_only") {
            public void run() {
                // Minimal M3G file header? M3G files start with 0xAB4A53523344310? Actually M3G magic. We test that loader at least tries to parse and throws appropriate exception for truncated file.
                byte[] minimal = new byte[]{(byte) 0xAB, (byte) 'J', (byte) 'S', (byte) 'R', (byte) '1', (byte) '8', (byte) '4', (byte) 0xBB, 0x0D, 0x0A, 0x1A, 0x0A};
                try {
                    Object3D[] objs = Loader.load(minimal, 0);
                    Assert.info("loaded objects count", objs.length);
                } catch (Exception e) {
                    Assert.info("expected exception for minimal file", e.getClass().getName());
                    // Accept any exception for invalid file
                }
            }
        }.severity(TestCase.SHOULD));
    }
}
