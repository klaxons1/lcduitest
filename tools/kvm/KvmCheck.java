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
