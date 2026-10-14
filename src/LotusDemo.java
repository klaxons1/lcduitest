import java.io.IOException;
import java.io.InputStream;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.midlet.MIDlet;

/**
 * "Lotus" demo: the video played back as a film, every frame drawn with single-colour
 * polygons, with the MIDI score looping in the background.
 */
public class LotusDemo extends MIDlet implements CommandListener {

    private static final String MIDI_RESOURCE = "/phantasm-8bit.mid";

    private final Command exitCommand = new Command("Exit", Command.EXIT, 1);
    private Command musicCommand = new Command("Music off", Command.SCREEN, 2);

    private LotusCanvas canvas;
    private Player music;
    private boolean musicOn = true;

    protected void startApp() {
        if (canvas == null) {
            LotusFilm film;
            try {
                film = new LotusFilm(getClass());
            } catch (IOException e) {
                Form error = new Form("Lotus");
                error.append("Data error: " + e.getMessage());
                error.addCommand(exitCommand);
                error.setCommandListener(this);
                Display.getDisplay(this).setCurrent(error);
                return;
            }
            canvas = new LotusCanvas(film);
            canvas.addCommand(exitCommand);
            canvas.addCommand(musicCommand);
            canvas.setCommandListener(this);
            Display.getDisplay(this).setCurrent(canvas);
        }
        canvas.start();
        if (musicOn) playMusic();
    }

    protected void pauseApp() {
        if (canvas != null) canvas.stop();
        stopMusic();
    }

    protected void destroyApp(boolean unconditional) {
        if (canvas != null) canvas.stop();
        closeMusic();
    }

    public void commandAction(Command c, Displayable d) {
        if (c == exitCommand) {
            destroyApp(false);
            notifyDestroyed();
        } else if (c == musicCommand) {
            musicOn = !musicOn;
            if (musicOn) {
                playMusic();
            } else {
                stopMusic();
            }
            canvas.removeCommand(musicCommand);
            musicCommand = new Command(musicOn ? "Music off" : "Music on", Command.SCREEN, 2);
            canvas.addCommand(musicCommand);
        }
    }

    /** Starts the MIDI score on a loop. Missing MMAPI/MIDI support must not stop the demo. */
    private void playMusic() {
        try {
            if (music == null) {
                InputStream in = getClass().getResourceAsStream(MIDI_RESOURCE);
                if (in == null) return;
                try {
                    music = Manager.createPlayer(in, "audio/midi");
                } finally {
                    in.close();
                }
                music.realize();
                music.setLoopCount(-1);
            }
            music.start();
        } catch (Exception e) {
            music = null;
        }
    }

    private void stopMusic() {
        if (music == null) return;
        try {
            music.stop();
        } catch (Exception e) {
            // nothing useful to do
        }
    }

    private void closeMusic() {
        if (music == null) return;
        try {
            music.close();
        } catch (Exception e) {
            // nothing useful to do
        }
        music = null;
    }
}
