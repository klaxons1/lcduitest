import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Plays the video as a film modelled with flat-coloured triangles. Every frame has two layers:
 *
 *  1. Environment: a static, coarse mesh of big triangles covering the whole screen (the same
 *     triangles every frame, with a new flat colour per frame). Drawn first.
 *  2. People: an adaptive quadtree over the people regions (marked by hand). Each leaf is cut
 *     into two flat-coloured triangles along the diagonal that fits the picture best. Small
 *     triangles follow the outlines and details. Drawn on top of the environment.
 *
 * The data (res/lotus.bin, made by tools/make_lotus_data.py) is format "LOT5". Each frame is a
 * tree bitstream plus colour bytes: one per environment triangle, then two per people leaf.
 */
final class LotusFilm {
    static final String RESOURCE = "/lotus.bin";

    /** Coordinates of the environment mesh are stored in 0..NORM (see the tool). */
    private static final int NORM = 1024;

    final int fps;
    final int frames;
    final int srcW;
    final int srcH;
    final int minCell;
    final int maxLeaves;
    /** Palette entries as 0xRRGGBB. */
    final int[] palette;

    /** Environment mesh: vertices in 0..NORM, triangles as three vertex indices. */
    final int envVertCount;
    final int envTriCount;
    private final int[] envVX;
    private final int[] envVY;
    private final int[] envA;
    private final int[] envB;
    private final int[] envC;
    private final int[] envCol;       // ARGB, per frame

    private final Class owner;
    private final int headerBytes;
    private final byte[] payload;
    private final int payloadCapacity;

    // Decoded people leaves of the current frame, in traversal order.
    private final int[] lx0;
    private final int[] ly0;
    private final int[] lx1;
    private final int[] ly1;
    private final int[] ldiag;
    private final int[] lcolA;        // ARGB
    private final int[] lcolB;
    private int leafCount;

    private DataInputStream in;       // positioned at frame `pos`
    private int pos;                  // index of the next frame the stream yields
    private int current = -1;         // frame held in the arrays, -1 = none

    // Bit reader over the payload.
    private int bitPos;
    private int bitEnd;
    private int colourBase;

    private final int[] tx = new int[3];
    private final int[] ty = new int[3];

    /**
     * @param owner class whose resource loader is used to open RESOURCE (and reopen it on rewind)
     */
    LotusFilm(Class owner) throws IOException {
        this.owner = owner;
        DataInputStream d = new DataInputStream(open());
        byte[] magic = new byte[4];
        d.readFully(magic);
        if (magic[0] != 'L' || magic[1] != 'O' || magic[2] != 'T' || magic[3] != '5') {
            throw new IOException("lotus.bin: bad magic");
        }
        int version = d.readUnsignedByte();
        if (version != 5) throw new IOException("lotus.bin: unsupported version " + version);
        fps = d.readUnsignedByte();
        frames = d.readUnsignedShort();
        srcW = d.readUnsignedShort();
        srcH = d.readUnsignedShort();
        minCell = d.readUnsignedByte();
        maxLeaves = d.readUnsignedShort();
        int colours = d.readUnsignedByte();
        envVertCount = d.readUnsignedShort();
        envTriCount = d.readUnsignedShort();
        if (fps == 0 || frames == 0 || maxLeaves == 0 || srcW == 0 || srcH == 0 || minCell < 2
                || envTriCount == 0 || colours == 0) {
            throw new IOException("lotus.bin: empty film");
        }

        envVX = new int[envVertCount];
        envVY = new int[envVertCount];
        for (int i = 0; i < envVertCount; i++) {
            envVX[i] = d.readUnsignedShort();
            envVY[i] = d.readUnsignedShort();
        }
        envA = new int[envTriCount];
        envB = new int[envTriCount];
        envC = new int[envTriCount];
        for (int i = 0; i < envTriCount; i++) {
            envA[i] = d.readUnsignedShort();
            envB[i] = d.readUnsignedShort();
            envC[i] = d.readUnsignedShort();
            if (envA[i] >= envVertCount || envB[i] >= envVertCount || envC[i] >= envVertCount) {
                throw new IOException("lotus.bin: bad environment triangle");
            }
        }
        envCol = new int[envTriCount];

        palette = new int[colours];
        for (int i = 0; i < colours; i++) {
            int r = d.readUnsignedByte();
            int g = d.readUnsignedByte();
            int b = d.readUnsignedByte();
            palette[i] = (r << 16) | (g << 8) | b;
        }
        // magic 4, fixed fields 12, mesh counts 4, mesh 4*verts + 6*tris, palette 3 per colour
        headerBytes = 4 + 12 + 4 + 4 * envVertCount + 6 * envTriCount + 3 * colours;

        // Tree bits: at most 16 per leaf is generous for a present bit, a split bit and a
        // diagonal bit over all nodes. Then the environment colours and two colours per leaf.
        int bitBytes = (16 * maxLeaves + 32) / 8;
        payloadCapacity = 2 + bitBytes + envTriCount + 2 * maxLeaves + 8;
        payload = new byte[payloadCapacity];

        lx0 = new int[maxLeaves];
        ly0 = new int[maxLeaves];
        lx1 = new int[maxLeaves];
        ly1 = new int[maxLeaves];
        ldiag = new int[maxLeaves];
        lcolA = new int[maxLeaves];
        lcolB = new int[maxLeaves];

        in = d;
        pos = 0;
    }

    private InputStream open() throws IOException {
        InputStream s = owner.getResourceAsStream(RESOURCE);
        if (s == null) throw new IOException("missing " + RESOURCE);
        return s;
    }

    /** Makes frame `index` the current one: reads its payload and decodes it. */
    void seek(int index) throws IOException {
        if (index == current) return;
        if (index < 0 || index >= frames) throw new IOException("frame " + index + " out of range");
        if (index < pos) {
            // Rewind: reopen the resource and skip the header again.
            in.close();
            in = new DataInputStream(open());
            in.readFully(new byte[headerBytes]);
            pos = 0;
        }
        while (pos < index) {
            skipFrame();
            pos++;
        }
        readFrame();
        pos++;
        decodeFrame();
        current = index;
    }

    private void skipFrame() throws IOException {
        int len = in.readUnsignedShort();
        if (len > payloadCapacity) throw new IOException("lotus.bin: frame too large");
        in.readFully(payload, 0, len);
    }

    private void readFrame() throws IOException {
        int len = in.readUnsignedShort();
        if (len > payloadCapacity) throw new IOException("lotus.bin: frame too large");
        in.readFully(payload, 0, len);
        int bitBytes = ((payload[0] & 0xff) << 8) | (payload[1] & 0xff);
        if (2 + bitBytes > len) throw new IOException("lotus.bin: bad frame");
        bitPos = 16;
        bitEnd = 16 + bitBytes * 8;
        colourBase = 2 + bitBytes;
        leafCount = 0;
    }

    /** Frame index for a playback clock in milliseconds (loops forever). */
    int frameAt(long ms) {
        return (int) ((ms * fps / 1000L) % frames);
    }

    private int readBit() throws IOException {
        if (bitPos >= bitEnd) throw new IOException("lotus.bin: tree overrun");
        int b = (payload[bitPos >> 3] >> (7 - (bitPos & 7))) & 1;
        bitPos++;
        return b;
    }

    private void decodeFrame() throws IOException {
        // Environment colours come first in the colour block.
        for (int i = 0; i < envTriCount; i++) {
            int idx = payload[colourBase + i] & 0xff;
            if (idx >= palette.length) idx = palette.length - 1;
            envCol[i] = 0xff000000 | palette[idx];
        }
        visit(0, 0, srcW, srcH);
    }

    /**
     * Same traversal as the tool. A node reads a "present" bit (0 = no people here, nothing is
     * emitted). If present and splittable, a "split" bit. A leaf reads a "diagonal" bit.
     * Children are TL, TR, BL, BR at the integer midpoint.
     */
    private void visit(int x0, int y0, int x1, int y1) throws IOException {
        if (readBit() == 0) return;
        int w = x1 - x0;
        int h = y1 - y0;
        if (w >= 2 * minCell && h >= 2 * minCell) {
            if (readBit() == 1) {
                int mx = x0 + w / 2;
                int my = y0 + h / 2;
                visit(x0, y0, mx, my);
                visit(mx, y0, x1, my);
                visit(x0, my, mx, y1);
                visit(mx, my, x1, y1);
                return;
            }
        }
        if (leafCount >= maxLeaves) throw new IOException("lotus.bin: too many leaves");
        int k = leafCount++;
        lx0[k] = x0;
        ly0[k] = y0;
        lx1[k] = x1;
        ly1[k] = y1;
        ldiag[k] = readBit();
        int ia = payload[colourBase + envTriCount + 2 * k] & 0xff;
        int ib = payload[colourBase + envTriCount + 2 * k + 1] & 0xff;
        if (ia >= palette.length) ia = palette.length - 1;
        if (ib >= palette.length) ib = palette.length - 1;
        lcolA[k] = 0xff000000 | palette[ia];
        lcolB[k] = 0xff000000 | palette[ib];
    }

    /**
     * Draws the current frame into fb (ARGB, w*h): first the environment triangles, then the
     * people leaves on top.
     */
    void render(int[] fb, int w, int h) {
        int size = w * h;
        for (int i = 0; i < size; i++) fb[i] = 0xff000000;

        for (int t = 0; t < envTriCount; t++) {
            int a = envA[t];
            int b = envB[t];
            int c = envC[t];
            setTri(envVX[a] * w / NORM, envVY[a] * h / NORM,
                   envVX[b] * w / NORM, envVY[b] * h / NORM,
                   envVX[c] * w / NORM, envVY[c] * h / NORM);
            fillTriangle(fb, w, h, envCol[t]);
        }

        for (int k = 0; k < leafCount; k++) {
            int x0 = (lx0[k] * w) / srcW;
            int y0 = (ly0[k] * h) / srcH;
            int x1 = (lx1[k] * w) / srcW;
            int y1 = (ly1[k] * h) / srcH;
            if (ldiag[k] == 0) {
                // TL-BR diagonal: upper-right and lower-left triangles.
                setTri(x0, y0, x1, y0, x1, y1);
                fillTriangle(fb, w, h, lcolA[k]);
                setTri(x0, y0, x1, y1, x0, y1);
                fillTriangle(fb, w, h, lcolB[k]);
            } else {
                // TR-BL diagonal: upper-left and lower-right triangles.
                setTri(x0, y0, x1, y0, x0, y1);
                fillTriangle(fb, w, h, lcolA[k]);
                setTri(x1, y0, x1, y1, x0, y1);
                fillTriangle(fb, w, h, lcolB[k]);
            }
        }
    }

    private void setTri(int ax, int ay, int bx, int by, int cx, int cy) {
        tx[0] = ax; ty[0] = ay;
        tx[1] = bx; ty[1] = by;
        tx[2] = cx; ty[2] = cy;
    }

    /**
     * Fills the triangle (tx, ty) with one flat colour. Scanline based: for each row the three
     * edges are intersected and the span between the extremes is painted.
     */
    private void fillTriangle(int[] fb, int w, int h, int col) {
        int ymin = ty[0];
        int ymax = ty[0];
        for (int i = 1; i < 3; i++) {
            if (ty[i] < ymin) ymin = ty[i];
            if (ty[i] > ymax) ymax = ty[i];
        }
        if (ymax < 0 || ymin >= h) return;
        if (ymin < 0) ymin = 0;
        if (ymax > h - 1) ymax = h - 1;

        for (int y = ymin; y <= ymax; y++) {
            int xl = 0x7fffffff;
            int xr = -0x7fffffff;
            for (int e = 0; e < 3; e++) {
                int a = e;
                int b = (e + 1) % 3;
                int ay = ty[a];
                int by = ty[b];
                if (ay == by) {
                    if (ay == y) {
                        int lo = tx[a] < tx[b] ? tx[a] : tx[b];
                        int hi = tx[a] < tx[b] ? tx[b] : tx[a];
                        if (lo < xl) xl = lo;
                        if (hi > xr) xr = hi;
                    }
                    continue;
                }
                if ((y < ay && y < by) || (y > ay && y > by)) continue;
                int x = tx[a] + ((y - ay) * (tx[b] - tx[a])) / (by - ay);
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
