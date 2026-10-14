public class KvmCheck {
    public static void main(String[] args) throws Exception {
        LotusFilm f = new LotusFilm(KvmCheck.class);
        int[] order = {0, 1, 2, 50, 49, 3, f.frames - 1, 0, 500, 500, 499, f.frames - 1};
        long acc = 0;
        for (int i = 0; i < order.length; i++) {
            f.seek(order[i]);
            acc = acc * 31 + checksum(f);
        }
        System.out.println("frames=" + f.frames + " grid=" + f.cols + "x" + f.rows
                + " fps=" + f.fps + " checksum=" + acc);

        // Decode time: every frame in order, as playback does it (state carried frame to frame).
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < f.frames; i++) {
            f.seek(i);
        }
        System.out.println("decode_all_ms=" + (System.currentTimeMillis() - t0));

        // Hash of the environment colours of every frame, to compare with the encoder's data.
        long envHash = 0;
        for (int i = 0; i < f.frames; i++) {
            f.seek(i);
            for (int c = 0; c < f.cols * f.rows; c++) {
                envHash = envHash * 31 + f.envColour(c, 0);
                envHash = envHash * 31 + f.envColour(c, 1);
            }
        }
        System.out.println("env_hash=" + envHash);
    }

    static long checksum(LotusFilm f) {
        long s = 0;
        for (int c = 0; c < f.cols * f.rows; c++) {
            s = s * 31 + f.envColour(c, 0);
            s = s * 31 + f.envColour(c, 1);
        }
        s = s * 31 + f.leafCount;
        for (int k = 0; k < f.leafCount; k++) {
            s = s * 31 + f.lx0[k];
            s = s * 31 + f.ly0[k];
            s = s * 31 + f.lx1[k];
            s = s * 31 + f.ly1[k];
            s = s * 31 + f.ldiag[k];
            s = s * 31 + f.lcolA[k];
            s = s * 31 + f.lcolB[k];
        }
        return s;
    }
}
