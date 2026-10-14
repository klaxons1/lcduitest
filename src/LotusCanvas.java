import java.io.IOException;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.media.Player;

/**
 * Full-screen canvas that plays the film on its own thread. Playback time advances only while
 * the canvas runs. A frame is decoded and redrawn only when the film moves to a new frame.
 *
 * Drawing: the environment grid first (two flat triangles per cell), then the people triangles
 * on top. All with Graphics.fillTriangle. The thread and paint() share the film state; the
 * thread waits for paint via serviceRepaints(), and both sides hold the film's lock.
 *
 * Sync: while an audio player is set with setAudioClock(), the film clock follows its media time,
 * so the picture stays on the soundtrack. Otherwise the clock runs on the wall clock.
 */
final class LotusCanvas extends Canvas implements Runnable {

    private static final long TICK_MS = 40;    // poll interval for the film clock (~25 Hz)
    private static final long MAX_STEP = 200;  // ignore huge gaps (e.g. after a pause)

    private final LotusFilm film;

    private volatile Thread thread;
    private volatile long clock;               // film time, advances only while running
    private volatile Player audioClock;        // player whose media time drives the clock, or null

    LotusCanvas(LotusFilm film) {
        setFullScreenMode(true);
        this.film = film;
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

    /** Player whose media time drives the film clock, or null for the wall clock. */
    void setAudioClock(Player p) {
        audioClock = p;
    }

    /** Current film time in milliseconds. */
    long clockMs() {
        return clock;
    }

    /** Media time of the player in milliseconds, or -1 if there is no player or it is unknown. */
    private static long audioMs(Player p) {
        if (p == null) return -1;
        try {
            long us = p.getMediaTime();
            return us < 0 ? -1 : us / 1000;
        } catch (Exception e) {
            return -1;
        }
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
            long audio = audioMs(audioClock);
            clock = audio >= 0 ? audio : clock + dt;

            int wanted = film.frameAt(clock);
            if (wanted != shown) {
                try {
                    synchronized (film) {
                        film.seek(wanted);
                    }
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
        int w = getWidth();
        int h = getHeight();
        g.setColor(0x000000);
        g.fillRect(0, 0, w, h);
        synchronized (film) {
            int cols = film.cols;
            int rows = film.rows;
            for (int cy = 0; cy < rows; cy++) {
                int y0 = (cy * h) / rows;
                int y1 = ((cy + 1) * h) / rows;
                for (int cx = 0; cx < cols; cx++) {
                    int x0 = (cx * w) / cols;
                    int x1 = ((cx + 1) * w) / cols;
                    int cell = cy * cols + cx;
                    // Upper-right triangle (TL, TR, BR) and lower-left triangle (TL, BR, BL).
                    g.setColor(film.envColour(cell, 0));
                    g.fillTriangle(x0, y0, x1, y0, x1, y1);
                    g.setColor(film.envColour(cell, 1));
                    g.fillTriangle(x0, y0, x1, y1, x0, y1);
                }
            }

            int sw = film.srcW;
            int sh = film.srcH;
            for (int k = 0; k < film.leafCount; k++) {
                int x0 = (film.lx0[k] * w) / sw;
                int y0 = (film.ly0[k] * h) / sh;
                int x1 = (film.lx1[k] * w) / sw;
                int y1 = (film.ly1[k] * h) / sh;
                if (film.ldiag[k] == 0) {
                    // TL-BR diagonal: upper-right and lower-left triangles.
                    g.setColor(film.lcolA[k]);
                    g.fillTriangle(x0, y0, x1, y0, x1, y1);
                    g.setColor(film.lcolB[k]);
                    g.fillTriangle(x0, y0, x1, y1, x0, y1);
                } else {
                    // TR-BL diagonal: upper-left and lower-right triangles.
                    g.setColor(film.lcolA[k]);
                    g.fillTriangle(x0, y0, x1, y0, x0, y1);
                    g.setColor(film.lcolB[k]);
                    g.fillTriangle(x1, y0, x1, y1, x0, y1);
                }
            }
        }
    }
}
