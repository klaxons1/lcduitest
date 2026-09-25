/*
 * M3G Tester
 */
package m3gtest;

import javax.microedition.midlet.MIDlet;

public class Env {

    private final MIDlet midlet;

    public Env(MIDlet midlet) {
        this.midlet = midlet;
    }

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
        return value != null && value.length() > 0 && !\"false\".equals(value) && !\"0\".equals(value);
    }

    public String describe() {
        StringBuffer sb = new StringBuffer();
        line(sb, \"microedition.platform\", prop(\"microedition.platform\", \"(not set)\"));
        line(sb, \"microedition.configuration\", prop(\"microedition.configuration\", \"(not set)\"));
        line(sb, \"microedition.profiles\", prop(\"microedition.profiles\", \"(not set)\"));
        line(sb, \"microedition.encoding\", prop(\"microedition.encoding\", \"(not set)\"));
        line(sb, \"microedition.locale\", prop(\"microedition.locale\", \"(not set)\"));
        line(sb, \"MIDlet-Name\", prop(\"MIDlet-Name\", \"(not set)\"));
        line(sb, \"MIDlet-Vendor\", prop(\"MIDlet-Vendor\", \"(not set)\"));
        line(sb, \"MIDlet-Version\", prop(\"MIDlet-Version\", \"(not set)\"));
        line(sb, \"microedition.jtwi.version\", prop(\"microedition.jtwi.version\", \"(not set)\"));
        line(sb, \"microedition.media.version\", prop(\"microedition.media.version\", \"(not set)\"));
        line(sb, \"microedition.m3g.version\", prop(\"microedition.m3g.version\", \"(not set)\"));
        line(sb, \"microedition.pim.version\", prop(\"microedition.pim.version\", \"(not set)\"));
        line(sb, \"microedition.io.file.FileConnection.version\", prop(\"microedition.io.file.FileConnection.version\", \"(not set)\"));
        line(sb, \"java.version\", prop(\"java.version\", \"(not set)\"));
        line(sb, \"os.name\", prop(\"os.name\", \"(not set)\"));
        line(sb, \"supports.m3g\", isM3GSupported() ? \"true\" : \"false\"));
        line(sb, \"m3g.properties\", m3gProperties());
        if (midlet != null) {
            try {
                line(sb, \"MIDlet-1 (getAppProperty)\", midlet.getAppProperty(\"MIDlet-1\"));
                line(sb, \"MIDlet-2 (getAppProperty)\", midlet.getAppProperty(\"MIDlet-2\"));
            } catch (Throwable t) {
                sb.append(\"getAppProperty threw \").append(t).append('\\n');
            }
        }
        return sb.toString();
    }

    public static boolean isM3GSupported() {
        try {
            Class.forName(\"javax.microedition.m3g.Graphics3D\");
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static String m3gProperties() {
        StringBuffer sb = new StringBuffer();
        try {
            Class c = Class.forName(\"javax.microedition.m3g.Graphics3D\");
            // Try to get some properties via Graphics3D if possible, otherwise just return version
            String v = prop(\"microedition.m3g.version\", \"unknown\");
            sb.append(\"version=\").append(v);
            // Query Graphics3D.getProperties() via reflection if available
            try {
                java.util.Hashtable props = (java.util.Hashtable) c.getMethod(\"getProperties\", null).invoke(c.getMethod(\"getInstance\", null).invoke(null, null), null);
                if (props != null) {
                    java.util.Enumeration keys = props.keys();
                    while (keys.hasMoreElements()) {
                        Object k = keys.nextElement();
                        sb.append(\", \").append(k).append(\"=\").append(props.get(k));
                    }
                }
            } catch (Throwable ignored) {
            }
        } catch (Throwable t) {
            sb.append(\"not available: \").append(t);
        }
        return sb.toString();
    }

    private static void line(StringBuffer sb, String key, String value) {
        sb.append(key).append(\": \").append(value).append('\\n');
    }
}
