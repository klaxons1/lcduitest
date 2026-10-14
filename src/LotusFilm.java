import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Plays the video as a facet film: a jittered triangle mesh covers the whole screen, and every
 * triangle is drawn in one flat colour taken from the current video frame.
 *
 * The data (res/lotus.bin, made by tools/make_lotus_data.py) is format "LOT3": a static mesh
 * (vertices and triangles, stored once) followed by the packed per-frame triangle colours. The
 * frames are read as a stream, so only one packed frame is held in memory. Playback is
 * sequential; a seek only rewinds when it goes backwards.
 */
final class LotusFilm {
    static final String RESOURCE = "/lotus.bin";
    private static final int NORM = 1024;

    final int bits;
    final int fps;
    final int frames;
    final int vertexCount;
    final int triangleCount;
    /** Palette entries as 0xRRGGBB. */
    final int[] palette;

    private final Class owner;
    private final int colours;
    private final int headerBytes;
    private final int[] vertX;        // normalised 0..1024
    private final int[] vertY;
    private final int[] triA;         // vertex indices, 3 per triangle
    private final int[] triB;
    private final int[] triC;
    private final int frameBytes;
    private final byte[] packed;
    private final int[] triColour;    // ARGB colour per triangle of the current frame
    private final int[] sx;           // screen-space vertices for the current size
    private final int[] sy;
    private int screenW = -1;
    private int screenH = -1;
    private DataInputStream in;       // positioned at frame `pos`
    private int pos;                  // index of the next frame the stream yields
    private int current = -1;         // frame held in triColour, -1 = none
    private final int[] tx = new int[3];
    private final int[] ty = new int[3];

    /**
     * @param owner class whose resource loader is used to open RESOURCE (and reopen it on rewind)
     */
    LotusFilm(Class owner) throws IOException {
        this.owner = owner;
        InputStream first = open();
        DataInputStream d = new DataInputStream(first);
        byte[] magic = new byte[4];
        d.readFully(magic);
        if (magic[0] != 'L' || magic[1] != 'O' || magic[2] != 'T' || magic[3] != '3') {
            throw new IOException("lotus.bin: bad magic");
        }
        int version = d.readUnsignedByte();
        if (version != 3) throw new IOException("lotus.bin: unsupported version " + version);
        bits = d.readUnsignedByte();
        if (bits != 4 && bits != 8) throw new IOException("lotus.bin: bad bit depth " + bits);
        colours = d.readUnsignedByte();
        fps = d.readUnsignedByte();
        frames = d.readUnsignedShort();
        vertexCount = d.readUnsignedShort();
        triangleCount = d.readUnsignedShort();
        if (fps == 0 || frames == 0 || triangleCount == 0 || vertexCount < 3) {
            throw new IOException("lotus.bin: empty film");
        }

        vertX = new int[vertexCount];
        vertY = new int[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            vertX[i] = d.readUnsignedShort();
            vertY[i] = d.readUnsignedShort();
        }
        triA = new int[triangleCount];
        triB = new int[triangleCount];
        triC = new int[triangleCount];
        for (int t = 0; t < triangleCount; t++) {
            triA[t] = d.readUnsignedShort();
            triB[t] = d.readUnsignedShort();
            triC[t] = d.readUnsignedShort();
            if (triA[t] >= vertexCount || triB[t] >= vertexCount || triC[t] >= vertexCount) {
                throw new IOException("lotus.bin: triangle index out of range");
            }
        }

        palette = new int[colours];
        for (int i = 0; i < colours; i++) {
            int r = d.readUnsignedByte();
            int g = d.readUnsignedByte();
            int b = d.readUnsignedByte();
            palette[i] = (r << 16) | (g << 8) | b;
        }

        // Bytes before the first frame: fixed header + vertices + triangles + palette.
        headerBytes = 4 + 1 + 1 + 1 + 1 + 2 + 2 + 2
                + 4 * vertexCount + 6 * triangleCount + 3 * colours;
        frameBytes = bits == 4 ? (triangleCount + 1) / 2 : triangleCount;
        packed = new byte[frameBytes];
        triColour = new int[triangleCount];
        sx = new int[vertexCount];
        sy = new int[vertexCount];
        in = d;
        pos = 0;
    }

    private InputStream open() throws IOException {
        InputStream s = owner.getResourceAsStream(RESOURCE);
        if (s == null) throw new IOException("missing " + RESOURCE);
        return s;
    }

    /** Makes frame `index` the current one, decoding its triangle colours. */
    void seek(int index) throws IOException {
        if (index == current) return;
        if (index < 0 || index >= frames) throw new IOException("frame " + index + " out of range");
        if (index < pos) {
            // Rewind: reopen the resource and skip everything before the first frame.
            in.close();
            in = new DataInputStream(open());
            in.readFully(new byte[headerBytes]);
            pos = 0;
        }
        while (pos < index) {
            in.readFully(packed);
            pos++;
        }
        in.readFully(packed);
        pos++;
        decode();
        current = index;
    }

    /** Frame index for a playback clock in milliseconds (loops forever). */
    int frameAt(long ms) {
        return (int) ((ms * fps / 1000L) % frames);
    }

    private void decode() {
        for (int t = 0; t < triangleCount; t++) {
            int idx;
            if (bits == 8) {
                idx = packed[t] & 0xff;
            } else {
                int b = packed[t >> 1] & 0xff;
                idx = (t & 1) == 0 ? (b >> 4) : (b & 15);
            }
            if (idx >= colours) idx = colours - 1;
            triColour[t] = 0xff000000 | palette[idx];
        }
    }

    /**
     * Draws the current frame into fb (ARGB, w*h). The mesh covers the whole screen, so the
     * film fills it edge to edge.
     */
    void render(int[] fb, int w, int h) {
        if (w != screenW || h != screenH) {
            screenW = w;
            screenH = h;
            for (int v = 0; v < vertexCount; v++) {
                sx[v] = (vertX[v] * w) / NORM;
                sy[v] = (vertY[v] * h) / NORM;
            }
        }
        int size = w * h;
        for (int i = 0; i < size; i++) fb[i] = 0xff000000;

        for (int t = 0; t < triangleCount; t++) {
            int a = triA[t];
            int b = triB[t];
            int c = triC[t];
            tx[0] = sx[a]; ty[0] = sy[a];
            tx[1] = sx[b]; ty[1] = sy[b];
            tx[2] = sx[c]; ty[2] = sy[c];
            fillTriangle(fb, w, h, triColour[t]);
        }
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
