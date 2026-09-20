/**
 * LcduiTest - MIDP 2.0 LCDUI conformance test MIDlet.
 *
 * Assertion helpers in the style of JUnit 3, re-implemented for CLDC 1.1.
 *
 * Every assertion takes a short human readable description of what is being
 * checked ("what") so that a failure message on a 128x128 screen still says
 * something useful, e.g.
 *
 *   Form.size() after append -> expected 3 but was 2
 */
package lcduitest;

public class Assert {

    private Assert() {
    }

    /** A piece of code that may throw, used by expectException(). */
    public interface Code {
        void run() throws Exception;
    }

    /* ------------------------------------------------------------------ */
    /* failures                                                            */
    /* ------------------------------------------------------------------ */

    public static void fail(String message) {
        throw new AssertionFailure(message);
    }

    public static void fail(String what, String message) {
        throw new AssertionFailure(what + " -> " + message);
    }

    /* ------------------------------------------------------------------ */
    /* booleans                                                            */
    /* ------------------------------------------------------------------ */

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

    /* ------------------------------------------------------------------ */
    /* equality                                                            */
    /* ------------------------------------------------------------------ */

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

    public static void assertSame(String what, Object expected, Object actual) {
        if (expected != actual) {
            throw new AssertionFailure(what + " -> expected the same object instance (expected "
                    + expected + ", was " + actual + ")");
        }
    }

    public static void assertNotSame(String what, Object unexpected, Object actual) {
        if (unexpected == actual) {
            throw new AssertionFailure(what + " -> expected a different object instance than " + actual);
        }
    }

    /* ------------------------------------------------------------------ */
    /* null-ness                                                           */
    /* ------------------------------------------------------------------ */

    public static void assertNull(String what, Object value) {
        if (value != null) {
            throw new AssertionFailure(what + " -> expected null but was " + value);
        }
    }

    public static void assertNotNull(String what, Object value) {
        if (value == null) {
            throw new AssertionFailure(what + " -> expected a non-null value");
        }
    }

    /* ------------------------------------------------------------------ */
    /* ranges                                                              */
    /* ------------------------------------------------------------------ */

    /** inclusive range check, used for implementation defined values. */
    public static void assertInRange(String what, int value, int min, int max) {
        if (value < min || value > max) {
            throw new AssertionFailure(what + " -> expected " + min + ".." + max + " but was " + value);
        }
    }

    public static void assertGreater(String what, int value, int minimum) {
        if (value <= minimum) {
            throw new AssertionFailure(what + " -> expected a value greater than " + minimum
                    + " but was " + value);
        }
    }

    public static void assertLess(String what, int value, int maximum) {
        if (value >= maximum) {
            throw new AssertionFailure(what + " -> expected a value less than " + maximum
                    + " but was " + value);
        }
    }

    public static void assertBitSet(String what, int mask, int value) {
        if ((value & mask) != mask) {
            throw new AssertionFailure(what + " -> expected all bits of 0x" + Integer.toHexString(mask)
                    + " set in 0x" + Integer.toHexString(value));
        }
    }

    public static void assertBitClear(String what, int mask, int value) {
        if ((value & mask) != 0) {
            throw new AssertionFailure(what + " -> expected all bits of 0x" + Integer.toHexString(mask)
                    + " clear in 0x" + Integer.toHexString(value));
        }
    }

    /* ------------------------------------------------------------------ */
    /* arrays                                                              */
    /* ------------------------------------------------------------------ */

    public static void assertEquals(String what, int[] expected, int[] actual) {
        if (expected == null || actual == null) {
            throw new AssertionFailure(what + " -> null array (expected " + expected + ", was " + actual + ")");
        }
        if (expected.length != actual.length) {
            throw new AssertionFailure(what + " -> expected array of length " + expected.length
                    + " but was length " + actual.length);
        }
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != actual[i]) {
                throw new AssertionFailure(what + " -> array element [" + i + "] expected " + expected[i]
                        + " but was " + actual[i]);
            }
        }
    }

    public static void assertEquals(String what, boolean[] expected, boolean[] actual) {
        if (expected == null || actual == null) {
            throw new AssertionFailure(what + " -> null array (expected " + expected + ", was " + actual + ")");
        }
        if (expected.length != actual.length) {
            throw new AssertionFailure(what + " -> expected array of length " + expected.length
                    + " but was length " + actual.length);
        }
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != actual[i]) {
                throw new AssertionFailure(what + " -> array element [" + i + "] expected " + expected[i]
                        + " but was " + actual[i]);
            }
        }
    }

    /* ------------------------------------------------------------------ */
    /* exceptions                                                          */
    /* ------------------------------------------------------------------ */

    /**
     * Runs body and requires it to throw an instance of the expected type.
     * Uses only CLDC 1.1 APIs (Class.isInstance).
     */
    public static void expectException(String what, Class expected, Code body) {
        try {
            body.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) {
                return;
            }
            throw new AssertionFailure(what + " -> expected " + expected.getName() + " but got "
                    + t.getClass().getName() + " (" + t + ")");
        }
        throw new AssertionFailure(what + " -> expected " + expected.getName() + " but no exception was thrown");
    }

    /** Requires body NOT to throw anything. */
    public static void expectNoException(String what, Code body) {
        try {
            body.run();
        } catch (Throwable t) {
            throw new AssertionFailure(what + " -> unexpected " + t.getClass().getName() + " (" + t + ")");
        }
    }

    /** Requires an exception of a type, but accepts a subclass message check. */
    public static void expectExceptionMessage(String what, Class expected, String expectedFragment, Code body) {
        try {
            body.run();
        } catch (Throwable t) {
            if (!expected.isInstance(t)) {
                throw new AssertionFailure(what + " -> expected " + expected.getName() + " but got "
                        + t.getClass().getName());
            }
            String message = t.toString();
            if (message == null || message.indexOf(expectedFragment) < 0) {
                throw new AssertionFailure(what + " -> exception message should contain \""
                        + expectedFragment + "\" but was \"" + message + "\"");
            }
            return;
        }
        throw new AssertionFailure(what + " -> expected " + expected.getName() + " but no exception was thrown");
    }

    /* ------------------------------------------------------------------ */
    /* observations                                                        */
    /* ------------------------------------------------------------------ */

    /**
     * Records an observed platform behaviour. INFO and MANUAL tests use this
     * to publish values (font metrics, colour counts, ...) that are allowed to
     * differ between implementations.
     */
    public static void info(String message) {
        TestCase test = TestCase.current;
        if (test != null) {
            test.note(message);
        }
    }

    /** Same as info() but formatted as name=value. */
    public static void info(String name, int value) {
        info(name + "=" + value);
    }

    public static void info(String name, String value) {
        info(name + "=" + value);
    }

    public static void info(String name, boolean value) {
        info(name + "=" + (value ? "true" : "false"));
    }
}
