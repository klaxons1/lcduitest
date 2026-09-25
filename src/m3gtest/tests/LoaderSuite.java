package m3gtest.tests;

import javax.microedition.m3g.*;
import m3gtest.*;

public class LoaderSuite extends TestSuite {

    public LoaderSuite() {
        super("loader", "Loader", "Tests Loader.load.");

        add(new TestCase("load invalid throws") {
            public void run() {
                final byte[] invalid = new byte[]{0,1,2,3,4,5};
                Assert.expectException("load invalid", Exception.class, new Assert.Code() {
                    public void run() throws Exception {
                        Loader.load(invalid, 0);
                    }
                });
            }
        });

        add(new TestCase("load empty throws") {
            public void run() {
                final byte[] empty = new byte[0];
                Assert.expectException("empty", Exception.class, new Assert.Code() {
                    public void run() throws Exception {
                        Loader.load(empty, 0);
                    }
                });
            }
        });

        add(new TestCase("load null throws") {
            public void run() {
                Assert.expectException("null", Exception.class, new Assert.Code() {
                    public void run() throws Exception {
                        Loader.load((byte[])null, 0);
                    }
                });
            }
        });
    }
}
