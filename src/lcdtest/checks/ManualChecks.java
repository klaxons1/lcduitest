package lcdtest.checks;

import lcdtest.Suite;

/**
 * Checks that require a human to look at the device.  They are never
 * executed automatically; the Manual checks menu shows the instruction
 * and records PASS / FAIL / SKIP.
 */
public final class ManualChecks {

    private ManualChecks() {
    }

    public static void register(Suite s) {
        final String G = "Manual";

        s.manual("MAN-01", G, "Ticker scrolls continuously",
                "Open Gallery > Component zoo. The Ticker at the top must scroll "
                + "its text smoothly, without flicker or gaps, and must not overlap "
                + "the title or the item area.");

        s.manual("MAN-02", G, "Alert shows an icon, text and image",
                "Open Gallery > Alert types > INFO. The alert must display the "
                + "title, the text and the image, and must dismiss itself after "
                + "a short timeout (getDefaultTimeout).");

        s.manual("MAN-03", G, "Alert with two commands is modal",
                "Open Gallery > Alert types > CONFIRMATION. With two commands the "
                + "alert must stay on screen until Yes or No is invoked; "
                + "getTimeout() should be FOREVER.");

        s.manual("MAN-04", G, "NUMERIC TextField opens a numeric editor",
                "Open Gallery > Component zoo. Select the NUMERIC field. The "
                + "device should offer digits (and an optional minus sign) and "
                + "must reject letters.");

        s.manual("MAN-05", G, "PASSWORD TextField obscures its contents",
                "Open Gallery > Component zoo and edit the PASSWORD field. The "
                + "existing content must not be readable; entered characters "
                + "should be masked.");

        s.manual("MAN-06", G, "DateField uses a locale date/time editor",
                "Open Gallery > Component zoo and edit the DATE and TIME fields. "
                + "The editor must use the device's locale format and preserve "
                + "values when leaving and re-entering the field.");

        s.manual("MAN-07", G, "Interactive Gauge reacts to keys or pointer",
                "Open Gallery > Component zoo. Change the interactive gauge with "
                + "LEFT/RIGHT (or the pointer). The value must change and the "
                + "graphic must follow it; an indefinite gauge must animate.");

        s.manual("MAN-08", G, "POPUP ChoiceGroup opens a popup",
                "Open Gallery > Component zoo and activate the POPUP choice. A "
                + "popup list must appear with the three options and the selected "
                + "one must be shown when it closes.");

        s.manual("MAN-09", G, "IMPLICIT List select key invokes a command",
                "Open Gallery > Lists > IMPLICIT list and press the select key. "
                + "The list's select command must fire and show the selected "
                + "element.");

        s.manual("MAN-10", G, "Screen rotation delivers sizeChanged and repaint",
                "On a device or emulator that can rotate the screen, rotate while "
                + "Gallery > Graphics primitives is visible. The canvas must be "
                + "repainted at the new size and sizeChanged() must be called.");

        s.manual("MAN-11", G, "Key mapping matches the device labels",
                "Open Gallery > Graphics primitives. Press the arrow keys and the "
                + "select key: UP/DOWN must change pages, LEFT must return. "
                + "Game actions must match the printed key labels.");

        s.manual("MAN-12", G, "Pointer events reach a Canvas",
                "On a touch device, open Gallery > Graphics primitives and tap "
                + "the upper and lower halves. Tapping must change pages; "
                + "hasPointerEvents() must be true.");

        s.manual("MAN-13", G, "Full-screen mode hides title and soft keys",
                "From the main menu, choose About, then rotate or use the device "
                + "full-screen gesture. Verify that a full-screen Canvas occupies "
                + "the whole display area and that leaving it restores the "
                + "previous layout.");

        s.manual("MAN-14", G, "Command labels and ordering are sensible",
                "Open Gallery > Component zoo. The Back command must be on a soft "
                + "key, and item commands (Hyperlink, Button) must be reachable "
                + "and invoked from the focused item.");

        s.manual("MAN-15", G, "No clipping or overlap at the screen edges",
                "Open Gallery > Graphics primitives and step through all four "
                + "pages. Lines, arcs, text and images must stay inside the "
                + "screen and must not be cut off at the right or bottom edge.");

        s.manual("MAN-16", G, "Alerts and forms restore focus on return",
                "Open Gallery > Component zoo, invoke the Button item command, "
                + "dismiss the alert, and verify that focus returns to the same "
                + "item and the form is not reset.");
    }
}
