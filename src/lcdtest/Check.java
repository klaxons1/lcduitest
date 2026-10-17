package lcdtest;

import javax.microedition.lcdui.Display;

/**
 * One conformance check.  The id is stable and is used by reports and
 * by docs/TEST_CATALOG.md.
 */
public class Check {

    final String id;
    final String group;
    final String title;
    final String spec;
    final int kind;
    final Body body;

    Check(String id, String group, String title, String spec, int kind, Body body) {
        this.id = id;
        this.group = group;
        this.title = title;
        this.spec = spec;
        this.kind = kind;
        this.body = body;
    }
}

