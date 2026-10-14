import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Reads the film. Every frame has two layers of flat-coloured triangles:
 *
 *  1. Environment: a uniform grid of cols x rows cells over the screen. Each cell is two
 *     triangles (TL-BR diagonal), each with its own colour from a 16-entry palette (4 bits
 *     per triangle, one byte per cell).
 *  2. People: an adaptive quadtree over the people regions, marked by hand. Each leaf is cut
 *     into two triangles along the diagonal that fits the picture best. Drawn on top.
 *
 * The data (res/lotus.bin, made by tools/make_lotus_data.py) is format "LOT7". This class only
 * decodes the tree and keeps the payload; it does not depend on the MIDP UI, so the desktop
 * preview can use it too.
 */
final class LotusFilm {
    static final String RESOURCE = "/lotus.bin";

    final int fps;
    final int frames;
    final int srcW;
    final int srcH;
    final int minCell;
    final int maxLeaves;
    final int cols;
    final int rows;
    /** Environment palette entries as 0xRRGGBB (up to 16). */
    final int[] envPalette;
    /** People palette entries as 0xRRGGBB. */
    final int[] palette;

    private final Class owner;
    private final int headerBytes;
    private final byte[] payload;

    // Frame layout inside payload: tree bits, then one byte per environment cell, then leaves.
    private int envBase;
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

    // Bit reader over the payload.
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
        if (magic[0] != 'L' || magic[1] != 'O' || magic[2] != 'T' || magic[3] != '7') {
            throw new IOException("lotus.bin: bad magic");
        }
        int version = d.readUnsignedByte();
        if (version != 7) throw new IOException("lotus.bin: unsupported version " + version);
        fps = d.readUnsignedByte();
        frames = d.readUnsignedShort();
        srcW = d.readUnsignedShort();
        srcH = d.readUnsignedShort();
        minCell = d.readUnsignedByte();
        maxLeaves = d.readUnsignedShort();
        cols = d.readUnsignedByte();
        rows = d.readUnsignedByte();
        int envCount = d.readUnsignedByte();
        int peopleCount = d.readUnsignedByte();
        int maxPayload = d.readUnsignedShort();
        if (fps == 0 || frames == 0 || maxLeaves == 0 || srcW == 0 || srcH == 0 || minCell < 2
                || cols == 0 || rows == 0 || envCount == 0 || peopleCount == 0 || maxPayload == 0) {
            throw new IOException("lotus.bin: empty film");
        }

        envPalette = readPalette(d, envCount);
        palette = readPalette(d, peopleCount);
        // magic 4, fixed fields 17, palettes 3 per colour
        headerBytes = 4 + 17 + 3 * (envCount + peopleCount);

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

    /** Makes frame `index` the current one: reads its payload and decodes the people tree. */
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
        decodePeople();
        current = index;
    }

    private void skipFrame() throws IOException {
        int len = in.readUnsignedShort();
        if (len > payload.length) throw new IOException("lotus.bin: frame too large");
        in.readFully(payload, 0, len);
    }

    private void readFrame() throws IOException {
        int len = in.readUnsignedShort();
        if (len > payload.length) throw new IOException("lotus.bin: frame too large");
        in.readFully(payload, 0, len);
        int bitBytes = ((payload[0] & 0xff) << 8) | (payload[1] & 0xff);
        if (2 + bitBytes + cols * rows > len) throw new IOException("lotus.bin: bad frame");
        bitPos = 16;
        bitEnd = 16 + bitBytes * 8;
        envBase = 2 + bitBytes;
        peopleBase = envBase + cols * rows;
        leafCount = 0;
    }

    /**
     * Colour of one environment triangle: tri 0 is the upper-right half of the cell, tri 1 the
     * lower-left half. Returns 0xRRGGBB.
     */
    int envColour(int cell, int tri) {
        int b = payload[envBase + cell] & 0xff;
        int idx = tri == 0 ? (b >> 4) : (b & 0x0f);
        if (idx >= envPalette.length) idx = envPalette.length - 1;
        return envPalette[idx];
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
