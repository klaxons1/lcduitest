package lcdtest;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Font;

/** Environment description included in every report. */
public final class Env {

    private Env() {
    }

    public static String collect(Display d) {
        StringBuffer b = new StringBuffer(512);
        prop(b, "platform", "microedition.platform");
        prop(b, "configuration", "microedition.configuration");
        prop(b, "profiles", "microedition.profiles");
        prop(b, "locale", "microedition.locale");
        prop(b, "encoding", "microedition.encoding");
        prop(b, "media", "microedition.media.version");
        prop(b, "pim", "microedition.pim.version");
        prop(b, "file", "microedition.io.file.FileConnection.version");
        prop(b, "m3g", "microedition.m3g.version");
        prop(b, "jsr75", "microedition.io.file.FileConnection.version");
        b.append("midlet=").append(Host.appProperty("MIDlet-Name", "?"));
        b.append(" ").append(Host.appProperty("MIDlet-Version", "?"));
        b.append("; vendor=").append(Host.appProperty("MIDlet-Vendor", "?"));
        b.append("; suite=").append("1.0");
        if (d != null) {
            try {
                b.append("; colors=").append(d.numColors());
                b.append("; alpha=").append(d.numAlphaLevels());
                b.append("; colorDisplay=").append(d.isColor());
            } catch (Throwable t) {
                b.append("; displayCaps=").append(Ctx.exName(t));
            }
            try {
                b.append("; borderNormal=").append(d.getBorderStyle(false));
                b.append("; borderFull=").append(d.getBorderStyle(true));
            } catch (Throwable t) {
                b.append("; border=?");
            }
            try {
                b.append("; bg=").append(Util.hex6(d.getColor(Display.COLOR_BACKGROUND)));
                b.append("; fg=").append(Util.hex6(d.getColor(Display.COLOR_FOREGROUND)));
            } catch (Throwable t) {
                b.append("; colorsSpec=?");
            }
        }
        try {
            ProbeCanvas p = new ProbeCanvas("env");
            b.append("; screen=").append(p.getWidth()).append('x').append(p.getHeight());
        } catch (Throwable t) {
            b.append("; screen=?");
        }
        try {
            Font f = Font.getDefaultFont();
            b.append("; font=").append(Util.fontSpec(f));
            b.append("; fontHeight=").append(f.getHeight());
            b.append("; baseline=").append(f.getBaselinePosition());
        } catch (Throwable t) {
            b.append("; font=?");
        }
        try {
            Runtime rt = Runtime.getRuntime();
            b.append("; heapFree=").append(rt.freeMemory() / 1024).append("k");
            b.append("; heapTotal=").append(rt.totalMemory() / 1024).append("k");
        } catch (Throwable t) {
            // ignore
        }
        b.append("; time=").append(Runner.now());
        return b.toString();
    }

    private static void prop(StringBuffer b, String name, String key) {
        String v = Util.prop(key);
        if (v != null && v.length() > 0 && !"?".equals(v)) {
            b.append(name).append('=').append(v).append("; ");
        }
    }
}
