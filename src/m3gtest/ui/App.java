package m3gtest.ui;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;

import m3gtest.ExitHook;
import m3gtest.Ui;
import m3gtest.demos.Demos;

public class App implements CommandListener {

    private final MIDlet midlet;
    private final ExitHook exitHook;
    private final Display display;
    private Canvas canvasScreen;
    private final Command exitCommand = new Command("Exit", Command.EXIT, 1);

    public App(MIDlet midlet, ExitHook exitHook) {
        this.midlet = midlet;
        this.exitHook = exitHook;
        this.display = Display.getDisplay(midlet);
        Ui.init(midlet);
    }

    public void start() {
        Displayable demo = Demos.create(0);
        if (demo != null) {
            demo.addCommand(exitCommand);
            demo.setCommandListener(this);
            if (demo instanceof Canvas) {
                canvasScreen = (Canvas) demo;
            }
            display.setCurrent(demo);
        }
    }

    public void pause() {}
    public void destroy() {}

    public void commandAction(Command c, Displayable d) {
        if (c == exitCommand) {
            exitHook.requestExit();
        }
    }

    public Canvas getCanvasScreen() {
        return canvasScreen;
    }
}
