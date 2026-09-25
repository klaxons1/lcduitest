/*
 * M3G Tester - visual demo base
 */
package m3gtest.demos;

import javax.microedition.lcdui.Displayable;

public abstract class Demo {

    private final String title;

    protected Demo(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public abstract Displayable createScreen();
}
