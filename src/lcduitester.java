import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Form;
import javax.microedition.midlet.MIDlet;

public class lcduitester extends MIDlet {
    protected void startApp() {
        Form f = new Form("lcduitester");
        f.append("Your MIDlet seems to run.");
        Display.getDisplay(this).setCurrent(f);
    }

    protected void pauseApp() {
    }

    protected void destroyApp(boolean unconditional) {
    }
}
