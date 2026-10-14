import java.util.Random;

/**
 * "Lotus" tunnel: a software-rasterised, integer-only flow of flat-coloured quads.
 *
 * The tunnel is built from the film strip: every quad is one cell of a frame
 * from the video and is drawn as a single solid colour. Rings have a lotus-petal
 * cross-section, twist along their depth and breathe over time, and the whole
 * strip flows toward the viewer, looping back to the first frame.
 *
 * Only integer arithmetic is used at runtime (CLDC 1.1 has no Math.sin/cos);
 * the sine table is computed once with float arithmetic.
 */
final class LotusTunnel {

    // Depth/world units: one band (one row of cells) is L units deep.
    private static final int L = 256;
    private static final int ZNEAR = 128;
    private static final int RINGS = 11;              // visible bands in front of the eye
    private static final int ZFAR = (RINGS + 1) * L;  // fully faded beyond this depth
    private static final int RANGE = ZFAR - ZNEAR;

    private static final int BANDS_PER_SEC = 3;       // speed of the strip flowing past
    private static final int TWIST = 2;               // angle index step per ring (of 256)
    private static final int PETALS = 8;              // lotus petals
    private static final int RADIUS = 230;            // base tunnel radius
    private static final int PETAL_AMP = 62;          // petal depth
    private static final int DUST = 48;               // flying specks

    private static final float PI = 3.14159265f;
    private static final int[] SIN = new int[256];    // sin * 1024, index 0..255 = full turn

    static {
        for (int i = 0; i < 256; i++) {
            float v = fsin(i * 2.0f * PI / 256.0f) * 1024.0f;
            SIN[i] = (int) (v >= 0 ? v + 0.5f : v - 0.5f);
        }
    }

    private static float fsin(float x) {
        // Reduce to [-PI, PI] and evaluate a Taylor polynomial.
        while (x > PI) x -= 2.0f * PI;
        while (x < -PI) x += 2.0f * PI;
        float x2 = x * x;
        float term = x;
        float sum = x;
        for (int n = 1; n <= 8; n++) {
            term *= -x2 / ((2 * n) * (2 * n + 1));
            sum += term;
        }
        return sum;
    }

    private static int cos(int a) {
        return SIN[(a + 64) & 255];
    }

    private final LotusData data;
    private final int sectors;
    private final int step;          // angle index between neighbouring sectors
    private final int totalBands;

    // Per-frame scratch: projected ring vertices, ring depths.
    private final int[] vx;
    private final int[] vy;
    private final int[] ringZ;
    private final int[] qx = new int[4];
    private final int[] qy = new int[4];

    // Dust specks (fixed random layout, flowing with time).
    private final int[] dustX = new int[DUST];
    private final int[] dustY = new int[DUST];
    private final int[] dustZ = new int[DUST];

    LotusTunnel(LotusData data) {
        this.data = data;
        this.sectors = data.sectors;
        this.step = 256 / sectors;
        this.totalBands = data.totalBands();
        this.vx = new int[(RINGS + 2) * sectors];
        this.vy = new int[(RINGS + 2) * sectors];
        this.ringZ = new int[RINGS + 2];

        Random rnd = new Random(1234);
        for (int i = 0; i < DUST; i++) {
            dustX[i] = rnd.nextInt(3 * L) - (3 * L) / 2;
            dustY[i] = rnd.nextInt(3 * L) - (3 * L) / 2;
            dustZ[i] = rnd.nextInt(RANGE);
        }
    }

    /**
     * Renders one frame into fb (ARGB, w*h ints).
     *
     * @param ms animation clock in milliseconds
     */
    void render(int[] fb, int w, int h, long ms) {
        int size = w * h;
        for (int i = 0; i < size; i++) {
            fb[i] = 0xff000000;
        }

        int cx = w / 2;
        int cy = h / 2;
        int focal = (w < h ? w : h) * 9 / 10;

        // Where we are along the strip: whole bands n and a fraction fr (0..999).
        long travel = ms * BANDS_PER_SEC;
        int n = (int) ((travel / 1000) % totalBands);
        int fr = (int) (travel % 1000);

        int rot = (int) ((ms / 40) & 255);      // slow spin of the whole tunnel
        int morph = (int) ((ms / 22) & 255);    // petals ripple
        int ox = (SIN[(int) ((ms / 37) & 255)] * 40) >> 10;   // camera drift
        int oy = (SIN[(int) ((ms / 53) & 255)] * 30) >> 10;
        int pulse = 200 + (((SIN[(int) ((ms / 16) & 255)] + 1024) * 55) >> 11);

        // Ring depths and projected vertices. Ring k sits at depth ZNEAR + (k - f) * L.
        for (int k = 1; k <= RINGS + 1; k++) {
            int z = ZNEAR + ((k * 1000 - fr) * L) / 1000;
            ringZ[k] = z;
            for (int s = 0; s < sectors; s++) {
                int a = (s * step + rot + k * TWIST) & 255;
                int pidx = ((a << 3) + morph) & 255;   // 8 petals around the circle
                int r = RADIUS + ((PETAL_AMP * SIN[pidx]) >> 10);
                int x = ((r * cos(a)) >> 10) - ox;
                int y = ((r * SIN[a]) >> 10) - oy;
                int idx = k * sectors + s;
                vx[idx] = cx + (x * focal) / z;
                vy[idx] = cy + (y * focal) / z;
            }
        }

        // Quads from the back to the front, so nearer cells cover farther ones.
        int base = n;
        for (int k = RINGS; k >= 1; k--) {
            int band = (base + k) % totalBands;   // which row of the film this quad shows
            int zMid = (ringZ[k] + ringZ[k + 1]) >> 1;
            int shade = fog(zMid) * 3 / 2;
            if (shade > 255) shade = 255;
            shade = (shade * pulse) >> 8;

            for (int s = 0; s < sectors; s++) {
                int s1 = (s + 1 == sectors) ? 0 : s + 1;
                int ci = data.cells[band * sectors + s] & 0xff;
                int rgb = data.palette[ci];
                int r = ((rgb >> 16) & 255) * shade >> 8;
                int g = ((rgb >> 8) & 255) * shade >> 8;
                int b = (rgb & 255) * shade >> 8;
                int col = 0xff000000 | (r << 16) | (g << 8) | b;

                int a0 = k * sectors + s;
                int a1 = k * sectors + s1;
                int a2 = (k + 1) * sectors + s1;
                int a3 = (k + 1) * sectors + s;
                qx[0] = vx[a0]; qy[0] = vy[a0];
                qx[1] = vx[a1]; qy[1] = vy[a1];
                qx[2] = vx[a2]; qy[2] = vy[a2];
                qx[3] = vx[a3]; qy[3] = vy[a3];
                fillQuad(fb, w, h, col);
            }
        }

        // Specks rushing toward the viewer.
        long flow = (ms * 420) / 1000;
        for (int i = 0; i < DUST; i++) {
            long zz = ((long) dustZ[i] - flow) % RANGE;
            if (zz < 0) zz += RANGE;
            int z = ZNEAR + (int) zz;
            int px = cx + (dustX[i] * focal) / z;
            int py = cy + (dustY[i] * focal) / z;
            int br = fog(z) * 3 / 2;
            if (br > 255) br = 255;
            if (px < 1 || py < 1 || px >= w - 1 || py >= h - 1) continue;
            int col = 0xff000000 | (br << 16) | (br << 8) | br;
            int p = py * w + px;
            fb[p] = col;
            fb[p + 1] = col;
            fb[p + w] = col;
        }
    }

    /** 0..256 brightness: full near the eye, black beyond ZFAR. */
    private static int fog(int z) {
        int v = ((ZFAR - z) * 256) / RANGE;
        if (v < 0) return 0;
        if (v > 256) return 256;
        return v;
    }

    /**
     * Fills the convex quad (qx, qy) with one flat colour. Scanline based: for each
     * row we intersect the four edges and paint the span between the extremes.
     */
    private void fillQuad(int[] fb, int w, int h, int col) {
        int ymin = qy[0], ymax = qy[0];
        int xmin = qx[0], xmax = qx[0];
        for (int i = 1; i < 4; i++) {
            if (qy[i] < ymin) ymin = qy[i];
            if (qy[i] > ymax) ymax = qy[i];
            if (qx[i] < xmin) xmin = qx[i];
            if (qx[i] > xmax) xmax = qx[i];
        }
        if (ymax < 0 || ymin >= h || xmax < 0 || xmin >= w) return;
        if (ymin < 0) ymin = 0;
        if (ymax > h - 1) ymax = h - 1;

        for (int y = ymin; y <= ymax; y++) {
            int xl = 0x7fffffff;
            int xr = -0x7fffffff;
            for (int e = 0; e < 4; e++) {
                int a = e;
                int b = (e + 1) & 3;
                int ay = qy[a];
                int by = qy[b];
                if (ay == by) {
                    if (ay == y) {
                        int lo = qx[a] < qx[b] ? qx[a] : qx[b];
                        int hi = qx[a] < qx[b] ? qx[b] : qx[a];
                        if (lo < xl) xl = lo;
                        if (hi > xr) xr = hi;
                    }
                    continue;
                }
                if ((y < ay && y < by) || (y > ay && y > by)) continue;
                int x = qx[a] + ((y - ay) * (qx[b] - qx[a])) / (by - ay);
                if (x < xl) xl = x;
                if (x > xr) xr = x;
            }
            if (xl > xr) continue;
            if (xl < 0) xl = 0;
            if (xr > w - 1) xr = w - 1;
            int row = y * w;
            for (int x = xl; x <= xr; x++) {
                fb[row + x] = col;
            }
        }
    }
}
