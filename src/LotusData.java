import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Reads res/lotus.bin, produced by tools/make_lotus_data.py from the film frames.
 *
 * Layout (big-endian):
 *   4 bytes  magic "LOT1"
 *   u8       version (1)
 *   u8       palette size
 *   u8       bands per frame
 *   u8       sectors per band
 *   u16      frame count
 *   palette  size * (R, G, B)
 *   cells    frames * bands * sectors palette indices (u8)
 */
final class LotusData {
    static final String RESOURCE = "/lotus.bin";

    final int colours;
    final int bands;
    final int sectors;
    final int frames;
    /** Palette as 0xRRGGBB. */
    final int[] palette;
    /** Palette index per cell, stored in row-major order: frame, band, sector. */
    final byte[] cells;

    private LotusData(int colours, int bands, int sectors, int frames,
                      int[] palette, byte[] cells) {
        this.colours = colours;
        this.bands = bands;
        this.sectors = sectors;
        this.frames = frames;
        this.palette = palette;
        this.cells = cells;
    }

    /** Total number of bands in the film strip (one band = one row of cells). */
    int totalBands() {
        return frames * bands;
    }

    static LotusData load(InputStream raw) throws IOException {
        DataInputStream in = new DataInputStream(raw);
        byte[] magic = new byte[4];
        in.readFully(magic);
        if (magic[0] != 'L' || magic[1] != 'O' || magic[2] != 'T' || magic[3] != '1') {
            throw new IOException("lotus.bin: bad magic");
        }
        int version = in.readUnsignedByte();
        if (version != 1) {
            throw new IOException("lotus.bin: unsupported version " + version);
        }
        int colours = in.readUnsignedByte();
        int bands = in.readUnsignedByte();
        int sectors = in.readUnsignedByte();
        int frames = in.readUnsignedShort();

        int[] palette = new int[colours];
        for (int i = 0; i < colours; i++) {
            int r = in.readUnsignedByte();
            int g = in.readUnsignedByte();
            int b = in.readUnsignedByte();
            palette[i] = (r << 16) | (g << 8) | b;
        }

        byte[] cells = new byte[frames * bands * sectors];
        in.readFully(cells);
        return new LotusData(colours, bands, sectors, frames, palette, cells);
    }
}
