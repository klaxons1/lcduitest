package lcdtest;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;

/** The running MIDlet, available to checks that need it. */
public final class Host {

    public static MIDlet midlet;
    public static Display display;
    public static String[] args = new String[0];
    public static boolean autorun;
    public static boolean catalog;

    private Host() {
    }

    public static String appProperty(String key, String def) {
        if (midlet == null) {
            return def;
        }
        try {
            String v = midlet.getAppProperty(key);
            return v == null ? def : v;
        } catch (Throwable t) {
            return def;
        }
    }

    public static boolean appFlag(String key) {
        return "true".equalsIgnoreCase(appProperty(key, "false"))
                || "1".equals(appProperty(key, ""));
    }
}
