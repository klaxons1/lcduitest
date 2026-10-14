import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;

import javax.imageio.ImageIO;

/**
 * Desktop preview of the J2ME "Lotus" film: renders frames to PNG so the look can be checked
 * without a phone or emulator. Not part of the MIDlet. It draws the same two layers as the
 * device: the background picture, then the people triangles on top.
 *
 * Usage: java -cp <classes>:res LotusPreview outDir width height frameIndex...
 */
public class LotusPreview {
    public static void main(String[] args) throws Exception {
        File out = new File(args[0]);
        out.mkdirs();
        int w = Integer.parseInt(args[1]);
        int h = Integer.parseInt(args[2]);

        LotusFilm film = new LotusFilm(LotusFilm.class);
        System.out.println("film: " + film.frames + " frames, " + film.fps + " fps, "
                + film.srcW + "x" + film.srcH + " source, up to " + film.maxLeaves + " people leaves");

        for (int a = 3; a < args.length; a++) {
            int index = Integer.parseInt(args[a]);
            film.seek(index);

            BufferedImage bg = ImageIO.read(new ByteArrayInputStream(
                    film.payload(), film.backgroundOffset(), film.backgroundLength()));
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            g.drawImage(bg, 0, 0, w, h, null);

            for (int k = 0; k < film.leafCount; k++) {
                int x0 = (film.lx0[k] * w) / film.srcW;
                int y0 = (film.ly0[k] * h) / film.srcH;
                int x1 = (film.lx1[k] * w) / film.srcW;
                int y1 = (film.ly1[k] * h) / film.srcH;
                if (film.ldiag[k] == 0) {
                    fill(g, film.lcolA[k], x0, y0, x1, y0, x1, y1);
                    fill(g, film.lcolB[k], x0, y0, x1, y1, x0, y1);
                } else {
                    fill(g, film.lcolA[k], x0, y0, x1, y0, x0, y1);
                    fill(g, film.lcolB[k], x1, y0, x1, y1, x0, y1);
                }
            }
            g.dispose();
            ImageIO.write(img, "png", new File(out, String.format("f%04d.png", index)));
            System.out.println("frame " + index + " people leaves " + film.leafCount);
        }
    }

    private static void fill(Graphics2D g, int rgb, int ax, int ay, int bx, int by, int cx, int cy) {
        g.setColor(new java.awt.Color(rgb));
        g.fillPolygon(new int[] {ax, bx, cx}, new int[] {ay, by, cy}, 3);
    }
}
