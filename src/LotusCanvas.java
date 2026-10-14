import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;

/**
 * Full-screen canvas that runs the tunnel animation on its own thread.
 * The frame is rendered into an int[] and blitted with drawRGB, which works on
 * every MIDP 2.0 device (no M3G / Nokia-specific API needed).
 */
final class LotusCanvas extends Canvas implements Runnable {

    private static final long FRAME_MS = 40;   // ~25 fps target
    private static final long MAX_STEP = 200;  // ignore huge gaps (e.g. after a pause)

    private final LotusTunnel tunnel;
    private final int width;
    private final int height;
    private final int[] frame;

    private volatile Thread thread;
    private long clock;                        // animation time, advances only while running

    LotusCanvas(LotusData data) {
        setFullScreenMode(true);
        width = getWidth();
        height = getHeight();
        frame = new int[width * height];
        tunnel = new LotusTunnel(data);
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
        while (thread == me) {
            long now = System.currentTimeMillis();
            long dt = now - last;
            last = now;
            if (dt > MAX_STEP) dt = MAX_STEP;
            clock += dt;

            tunnel.render(frame, width, height, clock);
            repaint();
            serviceRepaints();

            long spent = System.currentTimeMillis() - now;
            if (spent < FRAME_MS) {
                try {
                    Thread.sleep(FRAME_MS - spent);
                } catch (InterruptedException e) {
                    // just run the next frame
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
