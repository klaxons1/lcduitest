import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

/**
 * Desktop preview of the J2ME "Lotus" film: renders frames to PNG so the look can be checked
 * without a phone or emulator. Not part of the MIDlet.
 *
 * Usage: java -cp <classes>:res LotusPreview outDir width height frameIndex...
 * (frameIndex values are film frames; use -1 to render frames at evenly spaced times instead)
 */
public class LotusPreview {
    public static void main(String[] args) throws Exception {
        File out = new File(args[0]);
        out.mkdirs();
        int w = Integer.parseInt(args[1]);
        int h = Integer.parseInt(args[2]);

        LotusFilm film = new LotusFilm(LotusFilm.class);
        System.out.println("film: " + film.frames + " frames, " + film.cols + "x" + film.rows
                + " cells, " + film.fps + " fps, " + film.colours + " colours");

        int[] fb = new int[w * h];
        for (int a = 3; a < args.length; a++) {
            int index = Integer.parseInt(args[a]);
            film.seek(index);
            long t0 = System.nanoTime();
            film.render(fb, w, h);
            long us = (System.nanoTime() - t0) / 1000;
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            img.setRGB(0, 0, w, h, fb, 0, w);
            ImageIO.write(img, "png", new File(out, String.format("f%04d.png", index)));
            System.out.println("frame " + index + " render " + us + " us");
        }
    }
}
