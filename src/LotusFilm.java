import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Reads the film: a background picture per frame and the people polygons of the frame.
 *
 *  - Background: each frame's picture is a complete PNG (240 x 320, palette). It is not split
 *    into polygons. The canvas turns it into an Image and draws it.
 *  - People: an adaptive quadtree over the people regions, marked by hand. Each leaf is cut into
 *    two flat-coloured triangles along the diagonal that fits the picture best. The canvas draws
 *    them on top of the picture with Graphics.fillTriangle.
 *
 * The data (res/lotus.bin, made by tools/make_lotus_data.py) is format "LOT6". This class only
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
    /** People palette entries as 0xRRGGBB. */
    final int[] palette;

    private final Class owner;
    private final int headerBytes;
    private final byte[] payload;

    // Frame state: where the background PNG sits in `payload`.
    private int bgOffset;
    private int bgLength;

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
    private int colourBase;

    /**
     * @param owner class whose resource loader is used to open RESOURCE (and reopen it on rewind)
     */
    LotusFilm(Class owner) throws IOException {
        this.owner = owner;
        DataInputStream d = new DataInputStream(open());
        byte[] magic = new byte[4];
        d.readFully(magic);
        if (magic[0] != 'L' || magic[1] != 'O' || magic[2] != 'T' || magic[3] != '6') {
            throw new IOException("lotus.bin: bad magic");
        }
        int version = d.readUnsignedByte();
        if (version != 6) throw new IOException("lotus.bin: unsupported version " + version);
        fps = d.readUnsignedByte();
        frames = d.readUnsignedShort();
        srcW = d.readUnsignedShort();
        srcH = d.readUnsignedShort();
        minCell = d.readUnsignedByte();
        maxLeaves = d.readUnsignedShort();
        int colours = d.readUnsignedByte();
        int maxPayload = d.readUnsignedShort();
        if (fps == 0 || frames == 0 || maxLeaves == 0 || srcW == 0 || srcH == 0 || minCell < 2
                || maxPayload == 0 || colours == 0) {
            throw new IOException("lotus.bin: empty film");
        }

        palette = new int[colours];
        for (int i = 0; i < colours; i++) {
            int r = d.readUnsignedByte();
            int g = d.readUnsignedByte();
            int b = d.readUnsignedByte();
            palette[i] = (r << 16) | (g << 8) | b;
        }
        // magic 4, fixed fields 14, palette 3 per colour
        headerBytes = 4 + 14 + 3 * colours;

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
        int pngLen = u16(0);
        if (2 + pngLen + 2 > len) throw new IOException("lotus.bin: bad frame");
        bgOffset = 2;
        bgLength = pngLen;
        int bitBytes = u16(2 + pngLen);
        int bitStart = 4 + pngLen;
        if (bitStart + bitBytes > len) throw new IOException("lotus.bin: bad frame");
        bitPos = bitStart * 8;
        bitEnd = (bitStart + bitBytes) * 8;
        colourBase = bitStart + bitBytes;
        leafCount = 0;
    }

    private int u16(int at) {
        return ((payload[at] & 0xff) << 8) | (payload[at + 1] & 0xff);
    }

    /** The current frame's background PNG is payload[bgOffset .. bgOffset + bgLength). */
    byte[] payload() {
        return payload;
    }

    int backgroundOffset() {
        return bgOffset;
    }

    int backgroundLength() {
        return bgLength;
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
        int ia = payload[colourBase + 2 * k] & 0xff;
        int ib = payload[colourBase + 2 * k + 1] & 0xff;
        if (ia >= palette.length) ia = palette.length - 1;
        if (ib >= palette.length) ib = palette.length - 1;
        lcolA[k] = palette[ia];
        lcolB[k] = palette[ib];
    }
}
