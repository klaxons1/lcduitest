/**
 * M3G Tester - Assertion helpers
 */
package m3gtest;

public class Assert {

    private Assert() {}

    public interface Code {
        void run() throws Exception;
    }

    public static void fail(String message) {
        throw new AssertionFailure(message);
    }

    public static void fail(String what, String message) {
        throw new AssertionFailure(what + " -> " + message);
    }

    public static void assertTrue(String what, boolean condition) {
        if (!condition) {
            throw new AssertionFailure(what + " -> expected true but was false");
        }
    }

    public static void assertFalse(String what, boolean condition) {
        if (condition) {
            throw new AssertionFailure(what + " -> expected false but was true");
        }
    }

    public static void assertEquals(String what, int expected, int actual) {
        if (expected != actual) {
            throw new AssertionFailure(what + " -> expected " + expected + " but was " + actual);
        }
    }

    public static void assertEquals(String what, long expected, long actual) {
        if (expected != actual) {
            throw new AssertionFailure(what + " -> expected " + expected + " but was " + actual);
        }
    }

    public static void assertEquals(String what, boolean expected, boolean actual) {
        if (expected != actual) {
            throw new AssertionFailure(what + " -> expected " + expected + " but was " + actual);
        }
    }

    public static void assertEquals(String what, float expected, float actual, float epsilon) {
        if (Math.abs(expected - actual) > epsilon) {
            throw new AssertionFailure(what + " -> expected " + expected + " but was " + actual + " (eps " + epsilon + ")");
        }
    }

    public static void assertEquals(String what, String expected, String actual) {
        if (expected == null) {
            if (actual != null) {
                throw new AssertionFailure(what + " -> expected null but was \"" + actual + "\"");
            }
            return;
        }
        if (!expected.equals(actual)) {
            throw new AssertionFailure(what + " -> expected \"" + expected + "\" but was \""
                    + (actual == null ? "null" : actual) + "\"");
        }
    }

    public static void assertNotEquals(String what, int unexpected, int actual) {
        if (unexpected == actual) {
            throw new AssertionFailure(what + " -> value must not be " + unexpected);
        }
    }

    public static void assertEquals(String what, Object expected, Object actual) {
        if (expected == null) {
            if (actual != null) {
                throw new AssertionFailure(what + " -> expected null but was " + actual);
            }
            return;
        }
        if (!expected.equals(actual)) {
            throw new AssertionFailure(what + " -> expected " + expected + " but was " + actual);
        }
    }

    public static void assertNotNull(String what, Object obj) {
        if (obj == null) {
            throw new AssertionFailure(what + " -> expected not null");
        }
    }

    public static void assertNull(String what, Object obj) {
        if (obj != null) {
            throw new AssertionFailure(what + " -> expected null but was " + obj);
        }
    }

    public static void assertSame(String what, Object expected, Object actual) {
        if (expected != actual) {
            throw new AssertionFailure(what + " -> expected same instance but got different: expected=" + expected + " actual=" + actual);
        }
    }

    public static void assertNotSame(String what, Object a, Object b) {
        if (a == b) {
            throw new AssertionFailure(what + " -> expected different instances but both same: " + a);
        }
    }

    public static void expectException(String what, Class expectedClass, Code code) {
        try {
            code.run();
        } catch (Throwable t) {
            if (expectedClass.isInstance(t)) {
                return;
            }
            throw new AssertionFailure(what + " -> expected exception " + expectedClass.getName() + " but got " + t.getClass().getName() + ": " + t.getMessage());
        }
        throw new AssertionFailure(what + " -> expected exception " + expectedClass.getName() + " but no exception thrown");
    }

    public static void assertInRange(String what, int value, int min, int max) {
        if (value < min || value > max) {
            throw new AssertionFailure(what + " -> expected in [" + min + "," + max + "] but was " + value);
        }
    }
}
