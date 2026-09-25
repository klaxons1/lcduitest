package m3gtest.demos;

import javax.microedition.lcdui.Displayable;

public class Demos {

    private Demos() {}

    public static String[] titles() {
        return new String[] {
            "Cat on grass - running demo"
        };
    }

    public static Displayable create(int index) {
        return new CanvasDemos.CatGrassDemo();
    }
}
