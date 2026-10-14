import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;

/**
 * Desktop preview of the J2ME "Lotus" tunnel: renders frames to PNG so the look can be
 * checked without a phone or emulator. Not part of the MIDlet.
 *
 * Usage: java -cp <classes> LotusPreview res/lotus.bin outDir [width height] [ms...]
 */
public class LotusPreview {
    public static void main(String[] args) throws Exception {
        InputStream in = new FileInputStream(args[0]);
        LotusData data = LotusData.load(in);
        in.close();

        File out = new File(args[1]);
        out.mkdirs();
        int w = args.length > 3 ? Integer.parseInt(args[2]) : 176;
        int h = args.length > 3 ? Integer.parseInt(args[3]) : 208;
        LotusTunnel tunnel = new LotusTunnel(data);
        int[] fb = new int[w * h];

        long[] times = { 0, 2500, 5000, 7500, 10000, 15000, 20000, 25000 };
        if (args.length > 4) {
            times = new long[args.length - 4];
            for (int i = 4; i < args.length; i++) times[i - 4] = Long.parseLong(args[i]);
        }
        for (int i = 0; i < times.length; i++) {
            long t0 = System.nanoTime();
            tunnel.render(fb, w, h, times[i]);
            long us = (System.nanoTime() - t0) / 1000;
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            img.setRGB(0, 0, w, h, fb, 0, w);
            ImageIO.write(img, "png", new File(out, String.format("t%02d_%dms.png", i, times[i])));
            System.out.println("t=" + times[i] + "ms render " + us + " us");
        }
    }
}
