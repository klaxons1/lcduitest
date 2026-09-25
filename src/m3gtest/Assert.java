/*
 * M3G Tester
 */
package m3gtest;

public class Assert {

    private Assert() {
    }

    public interface Code {
        void run() throws Exception;
    }

    public static void fail(String message) {
        throw new AssertionFailure(message);
    }

    public static void fail(String what, String message) {
        throw new AssertionFailure(what + \" -> \" + message);
    }

    public static void assertTrue(String what, boolean condition) {
        if (!condition) {
            throw new AssertionFailure(what + \" -> expected true but was false\");
        }
    }

    public static void assertFalse(String what, boolean condition) {
        if (condition) {
            throw new AssertionFailure(what + \" -> expected false but was true\");
        }
    }

    public static void assertEquals(String what, int expected, int actual) {
        if (expected != actual) {
            throw new AssertionFailure(what + \" -> expected \" + expected + \" but was \" + actual);
        }
    }

    public static void assertEquals(String what, long expected, long actual) {
        if (expected != actual) {
            throw new AssertionFailure(what + \" -> expected \" + expected + \" but was \" + actual);
        }
    }

    public static void assertEquals(String what, boolean expected, boolean actual) {
        if (expected != actual) {
            throw new AssertionFailure(what + \" -> expected \" + expected + \" but was \" + actual);
        }
    }

    public static void assertEquals(String what, String expected, String actual) {
        if (expected == null) {
            if (actual != null) {
                throw new AssertionFailure(what + \" -> expected null but was \\\"\" + actual + \"\\\"\");
            }
            return;
        }
        if (!expected.equals(actual)) {
            throw new AssertionFailure(what + \" -> expected \\\"\" + expected + \"\\\" but was \\\"\" + (actual == null ? \"null\" : actual) + \"\\\"\");
        }
    }

    public static void assertNotEquals(String what, int unexpected, int actual) {
        if (unexpected == actual) {
            throw new AssertionFailure(what + \" -> value must not be \" + unexpected);
        }
    }

    public static void assertEquals(String what, Object expected, Object actual) {
        if (expected == null) {
            if (actual != null) {
                throw new AssertionFailure(what + \" -> expected null but was \" + actual);
            }
            return;
        }
        if (!expected.equals(actual)) {
            throw new AssertionFailure(what + \" -> expected \" + expected + \" but was \" + actual);
        }
    }

    public static void assertSame(String what, Object expected, Object actual) {
        if (expected != actual) {
            throw new AssertionFailure(what + \" -> expected the same object instance (expected \" + expected + \", was \" + actual + \")\");
        }
    }

    public static void assertNotSame(String what, Object unexpected, Object actual) {
        if (unexpected == actual) {
            throw new AssertionFailure(what + \" -> expected a different object instance than \" + actual);
        }
    }

    public static void assertNull(String what, Object value) {
        if (value != null) {
            throw new AssertionFailure(what + \" -> expected null but was \" + value);
        }
    }

    public static void assertNotNull(String what, Object value) {
        if (value == null) {
            throw new AssertionFailure(what + \" -> expected a non-null value\");
        }
    }

    public static void assertInRange(String what, int value, int min, int max) {
        if (value < min || value > max) {
            throw new AssertionFailure(what + \" -> expected \" + min + \"..\" + max + \" but was \" + value);
        }
    }

    public static void assertGreater(String what, int value, int minimum) {
        if (value <= minimum) {
            throw new AssertionFailure(what + \" -> expected a value greater than \" + minimum + \" but was \" + value);
        }
    }

    public static void assertLess(String what, int value, int maximum) {
        if (value >= maximum) {
            throw new AssertionFailure(what + \" -> expected a value less than \" + maximum + \" but was \" + value);
        }
    }

    public static void assertEquals(String what, float expected, float actual, float epsilon) {
        float diff = expected - actual;
        if (diff < 0) diff = -diff;
        if (diff > epsilon) {
            throw new AssertionFailure(what + \" -> expected \" + expected + \" but was \" + actual + \" diff \" + diff);
        }
    }

    public static void assertEquals(String what, int[] expected, int[] actual) {
        if (expected == null || actual == null) {
            throw new AssertionFailure(what + \" -> null array\");
        }
        if (expected.length != actual.length) {
            throw new AssertionFailure(what + \" -> expected length \" + expected.length + \" but was \" + actual.length);
        }
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != actual[i]) {
                throw new AssertionFailure(what + \" -> element [\" + i + \"] expected \" + expected[i] + \" but was \" + actual[i]);
            }
        }
    }

    public static void assertEquals(String what, float[] expected, float[] actual, float epsilon) {
        if (expected == null || actual == null) {
            throw new AssertionFailure(what + \" -> null array\");
        }
        if (expected.length != actual.length) {
            throw new AssertionFailure(what + \" -> expected length \" + expected.length + \" but was \" + actual.length);
        }
        for (int i = 0; i < expected.length; i++) {
            float diff = expected[i] - actual[i];
            if (diff < 0) diff = -diff;
            if (diff > epsilon) {
                throw new AssertionFailure(what + \" -> element [\" + i + \"] expected \" + expected[i] + \" but was \" + actual[i]);
            }
        }
    }

    public static void expectException(String what, Class expected, Code body) {
        try {
            body.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) {
                return;
            }
            throw new AssertionFailure(what + \" -> expected \" + expected.getName() + \" but got \" + t.getClass().getName() + \" (\" + t + \")\");
        }
        throw new AssertionFailure(what + \" -> expected \" + expected.getName() + \" but no exception was thrown\");
    }

    public static void expectNoException(String what, Code body) {
        try {
            body.run();
        } catch (Throwable t) {
            throw new AssertionFailure(what + \" -> unexpected \" + t.getClass().getName() + \" (\" + t + \")\");
        }
    }

    public static void info(String message) {
        TestCase test = TestCase.current;
        if (test != null) {
            test.note(message);
        }
    }

    public static void info(String name, int value) {
        info(name + \"=\" + value);
    }

    public static void info(String name, String value) {
        info(name + \"=\" + value);
    }

    public static void info(String name, boolean value) {
        info(name + \"=\" + (value ? \"true\" : \"false\"));
    }

    public static void info(String name, float value) {
        info(name + \"=\" + value);
    }
}
