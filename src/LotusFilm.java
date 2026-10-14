import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Plays the video as a film: every frame is a grid of cells, and every cell is drawn as one
 * single-colour quad polygon.
 *
 * The frame data (res/lotus.bin, made by tools/make_lotus_data.py) is read as a stream, so only
 * one packed frame is in memory at a time. Playback is sequential; a seek only rewinds when it
 * goes backwards.
 *
 * Format "LOT2" (big-endian): magic, u8 version, u8 bits (4|8), u8 cols, u8 rows,
 * u8 palette size, u8 fps, u16 frame count, palette (R,G,B per entry), then packed frames.
 */
final class LotusFilm {
    static final String RESOURCE = "/lotus.bin";

    final int bits;
    final int cols;
    final int rows;
    final int colours;
    final int fps;
    final int frames;
    /** Palette entries as 0xRRGGBB. */
    final int[] palette;

    private final Class owner;
    private final int frameBytes;
    private final byte[] packed;
    private final int[] cellColour;   // ARGB colour per cell of the current frame
    private DataInputStream in;       // positioned at frame `pos`
    private int pos;                  // index of the next frame the stream yields
    private int current = -1;         // index of the frame in cellColour, -1 = none
    private final int[] qx = new int[4];
    private final int[] qy = new int[4];

    /**
     * @param owner class whose resource loader is used to open RESOURCE (and reopen it on rewind)
     */
    LotusFilm(Class owner) throws IOException {
        this.owner = owner;
        InputStream first = open();
        DataInputStream d = new DataInputStream(first);
        byte[] magic = new byte[4];
        d.readFully(magic);
        if (magic[0] != 'L' || magic[1] != 'O' || magic[2] != 'T' || magic[3] != '2') {
            throw new IOException("lotus.bin: bad magic");
        }
        int version = d.readUnsignedByte();
        if (version != 2) throw new IOException("lotus.bin: unsupported version " + version);
        bits = d.readUnsignedByte();
        if (bits != 4 && bits != 8) throw new IOException("lotus.bin: bad bit depth " + bits);
        cols = d.readUnsignedByte();
        rows = d.readUnsignedByte();
        colours = d.readUnsignedByte();
        fps = d.readUnsignedByte();
        frames = d.readUnsignedShort();
        if (cols == 0 || rows == 0 || frames == 0 || fps == 0) {
            throw new IOException("lotus.bin: empty film");
        }

        palette = new int[colours];
        for (int i = 0; i < colours; i++) {
            int r = d.readUnsignedByte();
            int g = d.readUnsignedByte();
            int b = d.readUnsignedByte();
            palette[i] = (r << 16) | (g << 8) | b;
        }

        int cells = cols * rows;
        frameBytes = bits == 4 ? (cells + 1) / 2 : cells;
        packed = new byte[frameBytes];
        cellColour = new int[cells];
        in = d;
        pos = 0;
    }

    private InputStream open() throws IOException {
        InputStream s = owner.getResourceAsStream(RESOURCE);
        if (s == null) throw new IOException("missing " + RESOURCE);
        return s;
    }

    /** Makes frame `index` the current one, decoding its cell colours. */
    void seek(int index) throws IOException {
        if (index == current) return;
        if (index < 0 || index >= frames) throw new IOException("frame " + index + " out of range");
        if (index < pos) {
            // Rewind: reopen the resource and skip the header again.
            in.close();
            in = new DataInputStream(open());
            skipHeader(in);
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

    private void skipHeader(DataInputStream d) throws IOException {
        // magic(4) + version,bits,cols,rows,colours,fps (6) + frames (2) + palette.
        // readFully rather than skipBytes: skipBytes may skip less on a jar stream.
        d.readFully(new byte[4 + 6 + 2 + colours * 3]);
    }

    private void decode() {
        int cells = cols * rows;
        for (int c = 0; c < cells; c++) {
            int idx;
            if (bits == 8) {
                idx = packed[c] & 0xff;
            } else {
                int b = packed[c >> 1] & 0xff;
                idx = (c & 1) == 0 ? (b >> 4) : (b & 15);
            }
            if (idx >= colours) idx = colours - 1;
            cellColour[c] = 0xff000000 | palette[idx];
        }
    }

    /** Frame index for a playback clock in milliseconds (loops forever). */
    int frameAt(long ms) {
        return (int) ((ms * fps / 1000L) % frames);
    }

    /**
     * Draws the current frame into fb (ARGB, w*h). The film keeps its aspect ratio and is
     * centred, with black bars around it. Every cell is a quad with one flat colour.
     */
    void render(int[] fb, int w, int h) {
        int size = w * h;
        for (int i = 0; i < size; i++) fb[i] = 0xff000000;

        int fw;
        int fh;
        if (w * rows <= h * cols) {
            fw = w;
            fh = (w * rows) / cols;
        } else {
            fh = h;
            fw = (h * cols) / rows;
        }
        int ox = (w - fw) / 2;
        int oy = (h - fh) / 2;

        for (int r = 0; r < rows; r++) {
            int y0 = oy + (r * fh) / rows;
            int y1 = oy + ((r + 1) * fh) / rows;
            for (int c = 0; c < cols; c++) {
                int x0 = ox + (c * fw) / cols;
                int x1 = ox + ((c + 1) * fw) / cols;
                qx[0] = x0; qy[0] = y0;
                qx[1] = x1; qy[1] = y0;
                qx[2] = x1; qy[2] = y1;
                qx[3] = x0; qy[3] = y1;
                fillQuad(fb, w, h, cellColour[r * cols + c]);
            }
        }
    }

    /**
     * Fills the convex quad (qx, qy) with one flat colour. Scanline based: for each row the four
     * edges are intersected and the span between the extremes is painted.
     */
    private void fillQuad(int[] fb, int w, int h, int col) {
        int ymin = qy[0];
        int ymax = qy[0];
        for (int i = 1; i < 4; i++) {
            if (qy[i] < ymin) ymin = qy[i];
            if (qy[i] > ymax) ymax = qy[i];
        }
        if (ymax < 0 || ymin >= h) return;
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
