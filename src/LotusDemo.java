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
 * polygons. The sound is the film soundtrack (AMR-NB), in step with the picture. The soft key
 * switches between the film sound, the MIDI score and silence.
 */
public class LotusDemo extends MIDlet implements CommandListener {

    private static final String FILM_RESOURCE = "/film-soundtrack.amr";
    private static final String FILM_TYPE = "audio/amr";
    private static final String MIDI_RESOURCE = "/phantasm-8bit.mid";
    private static final String MIDI_TYPE = "audio/midi";

    private static final int SOUND_FILM = 0;
    private static final int SOUND_MUSIC = 1;
    private static final int SOUND_OFF = 2;
    private static final String[] SOUND_NAMES = {"Sound: film", "Sound: music", "Sound: off"};

    private final Command exitCommand = new Command("Exit", Command.EXIT, 1);
    private Command soundCommand = new Command(SOUND_NAMES[SOUND_FILM], Command.SCREEN, 2);

    private LotusCanvas canvas;
    private Player film;
    private Player music;
    private int sound = SOUND_FILM;

    protected void startApp() {
        if (canvas == null) {
            LotusFilm data;
            try {
                data = new LotusFilm(getClass());
            } catch (IOException e) {
                Form error = new Form("Lotus");
                error.append("Data error: " + e.getMessage());
                error.addCommand(exitCommand);
                error.setCommandListener(this);
                Display.getDisplay(this).setCurrent(error);
                return;
            }
            canvas = new LotusCanvas(data);
            canvas.addCommand(exitCommand);
            canvas.addCommand(soundCommand);
            canvas.setCommandListener(this);
            Display.getDisplay(this).setCurrent(canvas);
        }
        canvas.start();
        applySound();
    }

    protected void pauseApp() {
        if (canvas != null) canvas.stop();
        stopSound();
    }

    protected void destroyApp(boolean unconditional) {
        if (canvas != null) canvas.stop();
        closeFilm();
        closeMusic();
    }

    public void commandAction(Command c, Displayable d) {
        if (c == exitCommand) {
            destroyApp(false);
            notifyDestroyed();
        } else if (c == soundCommand) {
            sound = (sound + 1) % SOUND_NAMES.length;
            applySound();
        }
    }

    /** Plays the sound selected in {@link #sound}; falls back to the MIDI score if the film sound fails. */
    private void applySound() {
        canvas.setAudioClock(null);
        stopSound();
        if (sound == SOUND_FILM && !startFilm()) {
            sound = SOUND_MUSIC;
        }
        if (sound == SOUND_MUSIC) {
            playMusic();
        }
        updateSoundCommand();
    }

    private void updateSoundCommand() {
        canvas.removeCommand(soundCommand);
        soundCommand = new Command(SOUND_NAMES[sound], Command.SCREEN, 2);
        canvas.addCommand(soundCommand);
    }

    private void stopSound() {
        stopFilm();
        stopMusic();
    }

    /**
     * Starts the film soundtrack from the current film time and makes it the clock of the picture.
     * Returns false if the device cannot play it; the film then keeps running on the wall clock.
     */
    private boolean startFilm() {
        try {
            Player p = loadFilm();
            if (p == null) return false;
            long ms = canvas.clockMs();
            long dur = p.getDuration();
            if (dur > 0) ms %= dur / 1000L;
            try {
                p.setMediaTime(ms * 1000L);
            } catch (Exception e) {
                // the sound starts from its beginning; the picture follows it
            }
            p.start();
            canvas.setAudioClock(p);
            return true;
        } catch (Exception e) {
            closeFilm();
            return false;
        }
    }

    /** Creates and prepares the AMR player once; it is kept for the rest of the session. */
    private Player loadFilm() {
        if (film != null) return film;
        try {
            InputStream in = getClass().getResourceAsStream(FILM_RESOURCE);
            if (in == null) return null;
            try {
                Player p = Manager.createPlayer(in, FILM_TYPE);
                p.realize();
                p.prefetch();
                p.setLoopCount(-1);
                film = p;
            } finally {
                in.close();
            }
        } catch (Exception e) {
            closeFilm();
            return null;
        }
        return film;
    }

    private void stopFilm() {
        if (film == null) return;
        try {
            film.stop();
        } catch (Exception e) {
            // nothing useful to do
        }
    }

    private void closeFilm() {
        if (film == null) return;
        try {
            film.close();
        } catch (Exception e) {
            // nothing useful to do
        }
        film = null;
    }

    /** Starts the MIDI score on a loop. Missing MMAPI/MIDI support must not stop the demo. */
    private void playMusic() {
        try {
            if (music == null) {
                InputStream in = getClass().getResourceAsStream(MIDI_RESOURCE);
                if (in == null) return;
                try {
                    music = Manager.createPlayer(in, MIDI_TYPE);
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
