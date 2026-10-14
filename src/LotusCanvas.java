import java.io.IOException;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;

/**
 * Full-screen canvas that plays the film on its own thread. Playback time advances only while
 * the canvas runs. A frame is decoded and redrawn only when the film moves to a new frame.
 */
final class LotusCanvas extends Canvas implements Runnable {

    private static final long TICK_MS = 40;    // poll interval for the film clock (~25 Hz)
    private static final long MAX_STEP = 200;  // ignore huge gaps (e.g. after a pause)

    private final LotusFilm film;
    private final int width;
    private final int height;
    private final int[] frame;

    private volatile Thread thread;
    private long clock;                        // film time, advances only while running

    LotusCanvas(LotusFilm film) {
        setFullScreenMode(true);
        this.film = film;
        width = getWidth();
        height = getHeight();
        frame = new int[width * height];
    }

    synchronized void start() {
        if (thread != null) return;
        Thread t = new Thread(this);
        thread = t;
        t.start();
    }

    synchronized void stop() {
        thread = null;
    }

    public void run() {
        Thread me = Thread.currentThread();
        long last = System.currentTimeMillis();
        int shown = -1;
        while (thread == me) {
            long now = System.currentTimeMillis();
            long dt = now - last;
            last = now;
            if (dt > MAX_STEP) dt = MAX_STEP;
            clock += dt;

            int wanted = film.frameAt(clock);
            if (wanted != shown) {
                try {
                    film.seek(wanted);
                    film.render(frame, width, height);
                    shown = wanted;
                    repaint();
                    serviceRepaints();
                } catch (IOException e) {
                    // A broken read ends playback; the last frame stays on screen.
                    thread = null;
                    break;
                }
            }

            long spent = System.currentTimeMillis() - now;
            if (spent < TICK_MS) {
                try {
                    Thread.sleep(TICK_MS - spent);
                } catch (InterruptedException e) {
                    // just continue
                }
            }
        }
    }

    protected void paint(Graphics g) {
        g.setColor(0x000000);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.drawRGB(frame, 0, width, 0, 0, width, height, false);
    }
}
