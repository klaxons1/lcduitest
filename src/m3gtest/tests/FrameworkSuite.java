/*
 * M3G Tester - self test of the harness.
 */
package m3gtest.tests;

import m3gtest.*;

public class FrameworkSuite extends TestSuite {

    public FrameworkSuite() {
        super("Framework", "Self test of the harness", "assertions, exceptions, observation");

        add(new TestCase("assertions_work") {
            public void run() {
                Assert.assertEquals("int equals", 1, 1);
                Assert.assertTrue("true", true);
                Assert.assertFalse("false", false);
                Assert.assertNotNull("not null", new Object());
                Assert.assertNull("null", null);
                Assert.expectException("expect IAE", IllegalArgumentException.class, new Assert.Code() {
                    public void run() {
                        throw new IllegalArgumentException("test");
                    }
                });
            }
        });

        add(new TestCase("m3g_availability") {
            public void run() {
                boolean available = Ui.isM3GAvailable();
                Assert.info("M3G available", available);
                if (!available) {
                    Assert.info("M3G not available on this platform - other tests will ERROR");
                }
            }
        }.severity(TestCase.INFO));
    }
}
