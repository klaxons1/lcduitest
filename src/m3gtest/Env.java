/**
 * M3GTester - MIDP 2.0 M3G conformance test MIDlet.
 *
 * Reads the MIDP system properties. This is the cheapest way to find out what
 * a given emulator claims to implement, and it is also a good smoke test: on a
 * broken emulator the properties are missing or wrong.
 */
package m3gtest;

import javax.microedition.midlet.MIDlet;

public class Env {

    private final MIDlet midlet;

    public Env(MIDlet midlet) {
        this.midlet = midlet;
    }

    /** System.getProperty that never throws (property support varies). */
    public static String prop(String key) {
        try {
            return System.getProperty(key);
        } catch (Throwable t) {
            return null;
        }
    }

    public static String prop(String key, String fallback) {
        String value = prop(key);
        return value == null ? fallback : value;
    }

    public static boolean flag(String key) {
        String value = prop(key);
        return value != null && value.length() > 0 && !"false".equals(value) && !"0".equals(value);
    }

    /** Multi line dump used by the "Environment" screen and by the report. */
    public String describe() {
        StringBuffer sb = new StringBuffer();
        line(sb, "microedition.platform", prop("microedition.platform", "(not set)"));
        line(sb, "microedition.configuration", prop("microedition.configuration", "(not set)"));
        line(sb, "microedition.profiles", prop("microedition.profiles", "(not set)"));
        line(sb, "microedition.encoding", prop("microedition.encoding", "(not set)"));
        line(sb, "microedition.locale", prop("microedition.locale", "(not set)"));
        line(sb, "MIDlet-Name", prop("MIDlet-Name", "(not set)"));
        line(sb, "MIDlet-Vendor", prop("MIDlet-Vendor", "(not set)"));
        line(sb, "MIDlet-Version", prop("MIDlet-Version", "(not set)"));
        line(sb, "microedition.jtwi.version", prop("microedition.jtwi.version", "(not set)"));
        line(sb, "microedition.media.version", prop("microedition.media.version", "(not set)"));
        line(sb, "microedition.m3g.version", prop("microedition.m3g.version", "(not set)"));
        line(sb, "microedition.pim.version", prop("microedition.pim.version", "(not set)"));
        line(sb, "microedition.io.file.FileConnection.version",
                prop("microedition.io.file.FileConnection.version", "(not set)"));
        line(sb, "java.version", prop("java.version", "(not set)"));
        line(sb, "os.name", prop("os.name", "(not set)"));
        line(sb, "user.language", prop("user.language", "(not set)"));
        line(sb, "user.region", prop("user.region", "(not set)"));
        line(sb, "file.separator", prop("file.separator", "(not set)"));
        if (midlet != null) {
            try {
                line(sb, "MIDlet-1 (MIDlet.getAppProperty)", midlet.getAppProperty("MIDlet-1"));
            } catch (Throwable t) {
                sb.append("MIDlet.getAppProperty threw ").append(t).append('\n');
            }
        }
        return sb.toString();
    }

    private static void line(StringBuffer sb, String key, String value) {
        sb.append(key).append(": ").append(value).append('\n');
    }
}
