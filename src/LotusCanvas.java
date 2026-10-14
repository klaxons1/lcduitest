import java.io.IOException;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Full-screen canvas that plays the film on its own thread. Playback time advances only while
 * the canvas runs. A frame is decoded and redrawn only when the film moves to a new frame.
 *
 * Drawing: the frame's background picture first (drawImage), then the people triangles on top
 * (fillTriangle). The thread and paint() share the film state; the thread waits for paint via
 * serviceRepaints(), and both sides hold the film's lock.
 */
final class LotusCanvas extends Canvas implements Runnable {

    private static final long TICK_MS = 40;    // poll interval for the film clock (~25 Hz)
    private static final long MAX_STEP = 200;  // ignore huge gaps (e.g. after a pause)

    private final LotusFilm film;
    private Image background;                  // picture of the frame on screen

    private volatile Thread thread;
    private long clock;                        // film time, advances only while running

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
                    synchronized (film) {
                        film.seek(wanted);
                        background = Image.createImage(film.payload(),
                                film.backgroundOffset(), film.backgroundLength());
                    }
                    shown = wanted;
                    repaint();
                    serviceRepaints();
                } catch (IOException e) {
                    // A broken read ends playback; the last frame stays on screen.
                    thread = null;
                    break;
                } catch (RuntimeException e) {
                    // A picture that fails to decode ends playback the same way.
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
            if (background != null) g.drawImage(background, 0, 0, Graphics.TOP | Graphics.LEFT);
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
