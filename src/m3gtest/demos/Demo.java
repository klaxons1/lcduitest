package m3gtest.demos;

import javax.microedition.lcdui.Displayable;

public interface Demo {
    String getTitle();
    Displayable create();
}
