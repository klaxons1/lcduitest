import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Reads the film. Every frame has two layers of flat-coloured triangles:
 *
 *  1. Environment: a uniform grid of cols x rows cells over the screen, two triangles per cell
 *     (TL-BR diagonal). Each frame has a palette of up to 16 colours, and each triangle picks one
 *     palette entry. The palette is absolute on key frames and a delta from the previous frame
 *     otherwise (often all zero).
 *  2. People: an adaptive quadtree over the people regions, marked by hand. Each leaf is cut
 *     into two triangles along the diagonal that fits the picture best. Drawn on top.
 *
 * The data (res/lotus.bin, made by tools/make_lotus_data.py) is format "LOT8". The palette carries
 * from frame to frame, so skipped frames are still read (their palette is applied) and a seek walks
 * forward from the last key frame. This class does not depend on the MIDP UI, so the desktop
 * preview can use it too.
 */
final class LotusFilm {
    static final String RESOURCE = "/lotus.bin";
    static final int VERSION = 8;

    final int fps;
    final int frames;
    final int srcW;
    final int srcH;
    final int minCell;
    final int maxLeaves;
    final int cols;
    final int rows;
    /** Environment palette entries per frame (<= 16). */
    final int envPaletteSize;
    /** Environment palette of the current frame, 0xRRGGBB. */
    final int[] envPalette;
    /** People palette entries as 0xRRGGBB. */
    final int[] palette;

    private final Class owner;
    private final int headerBytes;
    private final byte[] payload;
    private final int tris;
    // Position of the environment indices (two 4-bit indices per byte) in the payload of the last frame.
    private int envBase;

    // Position of the people colour bytes inside the payload of the last frame read.
    private int peopleBase;

    // Decoded people leaves of the current frame, in traversal order.
    final int[] lx0;
    final int[] ly0;
    final int[] lx1;
    final int[] ly1;
    final int[] ldiag;
    final int[] lcolA;                // 0xRRGGBB
    final int[] lcolB;
    int leafCount;

    private DataInputStream in;       // positioned at frame `pos`
    private int pos;                  // index of the next frame the stream yields
    private int current = -1;         // frame held in the arrays, -1 = none

    // Bit reader over the tree of the current frame.
    private int bitPos;
    private int bitEnd;

    /**
     * @param owner class whose resource loader is used to open RESOURCE (and reopen it on rewind)
     */
    LotusFilm(Class owner) throws IOException {
        this.owner = owner;
        DataInputStream d = new DataInputStream(open());
        byte[] magic = new byte[4];
        d.readFully(magic);
        if (magic[0] != 'L' || magic[1] != 'O' || magic[2] != 'T' || magic[3] != '8') {
            throw new IOException("lotus.bin: bad magic");
        }
        int version = d.readUnsignedByte();
        if (version != VERSION) throw new IOException("lotus.bin: unsupported version " + version);
        fps = d.readUnsignedByte();
        frames = d.readUnsignedShort();
        srcW = d.readUnsignedShort();
        srcH = d.readUnsignedShort();
        minCell = d.readUnsignedByte();
        maxLeaves = d.readUnsignedShort();
        cols = d.readUnsignedByte();
        rows = d.readUnsignedByte();
        envPaletteSize = d.readUnsignedByte();
        int peopleCount = d.readUnsignedByte();
        int maxPayload = d.readUnsignedShort();
        if (fps == 0 || frames == 0 || maxLeaves == 0 || srcW == 0 || srcH == 0 || minCell < 2
                || cols == 0 || rows == 0 || envPaletteSize == 0 || peopleCount == 0
                || maxPayload == 0) {
            throw new IOException("lotus.bin: empty film");
        }

        palette = readPalette(d, peopleCount);
        // magic 4, fixed fields 17, people palette 3 per colour
        headerBytes = 4 + 17 + 3 * peopleCount;

        tris = 2 * cols * rows;
        envPalette = new int[envPaletteSize];
        payload = new byte[maxPayload];

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

    private static int[] readPalette(DataInputStream d, int count) throws IOException {
        int[] p = new int[count];
        for (int i = 0; i < count; i++) {
            int r = d.readUnsignedByte();
            int g = d.readUnsignedByte();
            int b = d.readUnsignedByte();
            p[i] = (r << 16) | (g << 8) | b;
        }
        return p;
    }

    private InputStream open() throws IOException {
        InputStream s = owner.getResourceAsStream(RESOURCE);
        if (s == null) throw new IOException("missing " + RESOURCE);
        return s;
    }

    /** Makes frame `index` the current one: applies the frames up to it and decodes the people. */
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
            readFrame();
            pos++;
        }
        readFrame();
        pos++;
        decodePeople();
        current = index;
    }

    /**
     * Reads the next frame from the stream into the payload buffer and applies its environment
     * part (palette and triangle indices) to the state. The tree stays undecoded until needed.
     */
    private void readFrame() throws IOException {
        int len = in.readUnsignedShort();
        if (len > payload.length) throw new IOException("lotus.bin: frame too large");
        in.readFully(payload, 0, len);
        byte[] b = payload;
        boolean key = (b[0] & 1) != 0;
        int p = 1;

        // Palette: absolute on key frames, signed deltas from the previous palette otherwise.
        for (int i = 0; i < envPaletteSize; i++) {
            int old = envPalette[i];
            int rgb = 0;
            for (int shift = 16; shift >= 0; shift -= 8) {
                int v = key ? (b[p] & 0xff) : ((old >> shift) & 0xff) + b[p];
                if (v < 0) v = 0;
                if (v > 255) v = 255;
                rgb |= v << shift;
                p++;
            }
            envPalette[i] = rgb;
        }

        if (p + 2 > len) throw new IOException("lotus.bin: bad frame");
        int treeBytes = ((b[p] & 0xff) << 8) | (b[p + 1] & 0xff);
        p += 2;
        bitPos = p * 8;
        bitEnd = (p + treeBytes) * 8;
        p += treeBytes;

        // Triangles: one 4-bit index each, two per byte (triangle 2k in the high nibble). The
        // indices are read from the payload when they are drawn, so only the palette is carried.
        envBase = p;
        if (p + (tris >> 1) > len) throw new IOException("lotus.bin: bad frame");
        p += tris >> 1;
        peopleBase = p;
        if (peopleBase > len) throw new IOException("lotus.bin: bad frame");
        leafCount = 0;
    }

    /**
     * Colour of one environment triangle: tri 0 is the upper-right half of the cell, tri 1 the
     * lower-left half. Returns 0xRRGGBB.
     */
    int envColour(int cell, int tri) {
        int t = 2 * cell + tri;
        int v = payload[envBase + (t >> 1)] & 0xff;
        int i = (t & 1) == 0 ? (v >> 4) : (v & 0x0f);
        if (i >= envPaletteSize) i = envPaletteSize - 1;
        return envPalette[i];
    }

    /** Frame index for a playback clock in milliseconds (loops forever). */
    int frameAt(long ms) {
        return (int) ((ms * fps / 1000L) % frames);
    }

    private int readBit() throws IOException {
        if (bitPos >= bitEnd) throw new IOException("lotus.bin: tree overrun");
        int bit = (payload[bitPos >> 3] >> (7 - (bitPos & 7))) & 1;
        bitPos++;
        return bit;
    }

    private void decodePeople() throws IOException {
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
        int ia = payload[peopleBase + 2 * k] & 0xff;
        int ib = payload[peopleBase + 2 * k + 1] & 0xff;
        if (ia >= palette.length) ia = palette.length - 1;
        if (ib >= palette.length) ib = palette.length - 1;
        lcolA[k] = palette[ia];
        lcolB[k] = palette[ib];
    }
}
