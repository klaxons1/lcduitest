/**
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * Tests of the test harness itself. If these fail, the results of all other
 * suites cannot be trusted, so this suite is always run first.
 */
package lcduitest.tests;

import lcduitest.Assert;
import lcduitest.AssertionFailure;
import lcduitest.TestCase;
import lcduitest.TestResult;
import lcduitest.TestRunner;
import lcduitest.TestSuite;
import lcduitest.Ui;

public class FrameworkSuite extends TestSuite {

    public FrameworkSuite() {
        super("Framework", "Self test of the test harness", "verifies that the runner "
                + "classifies pass / failure / error correctly and that assertions "
                + "and helpers behave");

        add(new TestCase("runner_classifies_pass") {
            public void run() {
                TestResult result = TestRunner.execute(new TestCase("inline pass") {
                    public void run() {
                        Assert.assertEquals("1+1", 2, 1 + 1);
                    }
                });
                Assert.assertEquals("status of a passing test", TestResult.PASS, result.getStatus());
            }
        });

        add(new TestCase("runner_classifies_fail") {
            public void run() {
                TestResult result = TestRunner.execute(new TestCase("inline failure") {
                    public void run() {
                        Assert.assertEquals("deliberate", 1, 2);
                    }
                });
                Assert.assertEquals("status", TestResult.FAIL, result.getStatus());
                Assert.assertNotNull("failure message", result.getMessage());
            }
        });

        add(new TestCase("runner_classifies_error") {
            public void run() {
                TestResult result = TestRunner.execute(new TestCase("inline error") {
                    public void run() {
                        throw new IllegalStateException("boom");
                    }
                });
                Assert.assertEquals("status", TestResult.ERROR, result.getStatus());
                Assert.assertNotNull("error message", result.getMessage());
            }
        });

        add(new TestCase("assertion_failure_is_an_error") {
            public void run() {
                AssertionFailure failure = new AssertionFailure("x");
                Assert.assertTrue("AssertionFailure extends java.lang.Error",
                        failure instanceof Error);
                Assert.assertFalse("AssertionFailure is not an Exception (a test failure must "
                        + "not be caught by code that handles exceptions)",
                        Exception.class.isInstance(failure));
            }
        });

        add(new TestCase("assertEquals_message_contains_values") {
            public void run() {
                String message = null;
                try {
                    Assert.assertEquals("sample check", 7, 9);
                } catch (AssertionFailure failure) {
                    message = failure.getMessage();
                }
                Assert.assertNotNull("message", message);
                Assert.assertTrue("message contains the description: " + message,
                        message.indexOf("sample check") >= 0);
                Assert.assertTrue("message contains expected value: " + message,
                        message.indexOf("7") >= 0);
                Assert.assertTrue("message contains actual value: " + message,
                        message.indexOf("9") >= 0);
            }
        });

        add(new TestCase("expectException_accepts_subclasses") {
            public void run() {
                Assert.expectException("java.io.IOException from a subclass",
                        java.io.IOException.class, new Assert.Code() {
                            public void run() throws Exception {
                                throw new java.io.EOFException();
                            }
                        });
            }
        });

        add(new TestCase("expectException_reports_missing_exception") {
            public void run() {
                TestResult result = TestRunner.execute(new TestCase("inline") {
                    public void run() {
                        Assert.expectException("nothing thrown", IllegalArgumentException.class,
                                new Assert.Code() {
                                    public void run() {
                                    }
                                });
                    }
                });
                Assert.assertEquals("must fail when no exception is thrown",
                        TestResult.FAIL, result.getStatus());
            }
        });

        add(new TestCase("expectNoException_reports_unexpected_exception") {
            public void run() {
                TestResult result = TestRunner.execute(new TestCase("inline") {
                    public void run() {
                        Assert.expectNoException("code must not throw", new Assert.Code() {
                            public void run() {
                                throw new RuntimeException("unexpected");
                            }
                        });
                    }
                });
                Assert.assertEquals("must fail", TestResult.FAIL, result.getStatus());
            }
        });

        add(new TestCase("info_is_recorded_as_observation") {
            public void run() {
                TestResult result = TestRunner.execute(new TestCase("informative") {
                    public void run() {
                        Assert.info("screen", 176);
                        Assert.info("colour", true);
                    }
                }.severity(TestCase.INFO));
                Assert.assertEquals("info severity maps to INFO status", TestResult.INFO,
                        result.getStatus());
                Assert.assertTrue("observation recorded: " + result.getMessage(),
                        result.getMessage() != null && result.getMessage().indexOf("176") >= 0);
            }
        });

        add(new TestCase("suite_keeps_insertion_order") {
            public void run() {
                TestSuite suite = new TestSuite("x", "y", "");
                suite.add(new TestCase("a") {
                    public void run() {
                    }
                });
                suite.add(new TestCase("b") {
                    public void run() {
                    }
                });
                Assert.assertEquals("size", 2, suite.size());
                Assert.assertEquals("first", "a", suite.get(0).getName());
                Assert.assertEquals("second", "b", suite.get(1).getName());
            }
        });

        add(new TestCase("runner_counts_results") {
            public void run() {
                TestSuite suite = new TestSuite("counts", "counts", "");
                suite.add(new TestCase("pass") {
                    public void run() {
                    }
                });
                suite.add(new TestCase("fail") {
                    public void run() {
                        Assert.fail("nope");
                    }
                });
                suite.add(new TestCase("observe") {
                    public void run() {
                        Assert.info("observation");
                    }
                }.severity(TestCase.INFO));
                TestRunner.SuiteResult result = new TestRunner(null).run(suite);
                Assert.assertEquals("total", 3, result.total);
                Assert.assertEquals("passed", 1, result.passed);
                Assert.assertEquals("failed", 1, result.failed);
                Assert.assertEquals("errors", 0, result.errors);
                Assert.assertEquals("observations", 1, result.infos);
            }
        });

        add(new TestCase("hex_helper_formats_rgb") {
            public void run() {
                Assert.assertEquals("0x0000FF", "0x0000FF", Ui.hex(0xFF));
                Assert.assertEquals("0xABCDEF", "0xABCDEF", Ui.hex(0xABCDEF));
                Assert.assertEquals("0x000000", "0x000000", Ui.hex(0));
            }
        });

        add(new TestCase("sleep_and_timers_work") {
            public void run() {
                long start = System.currentTimeMillis();
                Ui.sleep(50);
                long elapsed = System.currentTimeMillis() - start;
                Assert.assertTrue("Thread.sleep(50) took " + elapsed + " ms", elapsed >= 20);
                Assert.assertTrue("clock advances", System.currentTimeMillis() > 0);
            }
        });
    }
}
