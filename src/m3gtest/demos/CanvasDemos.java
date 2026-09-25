/*
 * M3G Tester - Canvas based visual demos for M3G
 */
package m3gtest.demos;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import javax.microedition.m3g.*;

import m3gtest.Ui;

public class CanvasDemos {

    private CanvasDemos() {
    }

    abstract static class Base extends Canvas {

        private final String heading;
        private String hint = "";

        Base(String heading) {
            this.heading = heading;
        }

        protected void hint(String text) {
            hint = text;
        }

        protected int bodyTop() {
            return Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL).getHeight() + 2;
        }

        protected void paint(Graphics g) {
            int w = getWidth();
            int h = getHeight();
            Font small = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
            g.setFont(small);
            g.setColor(0x000000);
            g.fillRect(0, 0, w, bodyTop());
            g.setColor(0xFFFFFF);
            g.drawString(heading, 2, 1, Graphics.TOP | Graphics.LEFT);
            int top = bodyTop();
            int bottom = h;
            if (hint.length() > 0) {
                bottom = h - small.getHeight();
                g.setColor(0x000000);
                g.drawString(hint, 2, bottom, Graphics.TOP | Graphics.LEFT);
            }
            g.setColor(0xFFFFFF);
            g.fillRect(0, top, w, bottom - top);
            g.setColor(0x000000);
            paintBody(g, 0, top, w, bottom - top);
        }

        protected abstract void paintBody(Graphics g, int x, int y, int w, int h);
    }

    /* ------------------------------------------------------------------ */
    /* M3G base that binds Graphics3D and handles errors                   */
    /* ------------------------------------------------------------------ */

    public abstract static class M3GBase extends Canvas {

        protected String status = "init";
        protected int frame = 0;
        protected Thread animator;
        protected boolean running;

        protected M3GBase(String title) {
            setTitle(title);
        }

        protected void showNotify() {
            running = true;
            if (animator == null) {
                animator = new Thread(new Runnable() {
                    public void run() {
                        while (running) {
                            frame++;
                            repaint();
                            try {
                                Thread.sleep(80);
                            } catch (InterruptedException e) {
                                return;
                            }
                        }
                    }
                });
                animator.start();
            }
        }

        protected void hideNotify() {
            running = false;
            animator = null;
        }

        protected void paint(Graphics g) {
            int w = getWidth();
            int h = getHeight();
            Font small = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
            g.setFont(small);
            try {
                Graphics3D g3d = Graphics3D.getInstance();
                g3d.bindTarget(g);
                try {
                    g3d.setViewport(0, 0, w, h);
                    Background bg = new Background();
                    bg.setColor(0x202040);
                    g3d.clear(bg);
                    paintM3G(g3d, w, h);
                } finally {
                    g3d.releaseTarget();
                }
                g.setColor(0xFFFFFF);
                g.drawString(status + " f=" + frame, 2, 2, Graphics.TOP | Graphics.LEFT);
            } catch (Throwable t) {
                g.setColor(0xFFFFFF);
                g.fillRect(0, 0, w, h);
                g.setColor(0xFF0000);
                g.drawString("M3G error: " + t, 2, 2, Graphics.TOP | Graphics.LEFT);
                g.drawString(t.toString(), 2, 2 + small.getHeight(), Graphics.TOP | Graphics.LEFT);
                status = t.toString();
            }
        }

        protected abstract void paintM3G(Graphics3D g3d, int w, int h) throws Exception;
    }

    /* ------------------------------------------------------------------ */
    /* Demos                                                               */
    /* ------------------------------------------------------------------ */

    public static class RotatingCube extends M3GBase {

        private Camera camera;
        private Light light;
        private Mesh cube;
        private Transform camTransform = new Transform();
        private Transform cubeTransform = new Transform();

        public RotatingCube() {
            super("Rotating cube");
            try {
                camera = new Camera();
                camera.setPerspective(60.0f, (float) getWidth() / getHeight(), 1.0f, 100.0f);
                camTransform.postTranslate(0, 0, 5);
                light = new Light();
                light.setMode(Light.DIRECTIONAL);
                light.setColor(0xFFFFFF);
                cube = createCube(1.0f);
                status = "cube created";
            } catch (Throwable t) {
                status = "init failed: " + t;
            }
        }

        protected void paintM3G(Graphics3D g3d, int w, int h) throws Exception {
            if (camera == null) return;
            camera.setPerspective(60.0f, (float) w / h, 1.0f, 100.0f);
            g3d.setCamera(camera, camTransform);
            g3d.addLight(light, new Transform());
            cubeTransform.setIdentity();
            cubeTransform.postRotate(frame * 2, 1, 1, 0);
            g3d.render(cube, cubeTransform);
            g3d.resetLights();
        }
    }

    public static class WorldDemo extends M3GBase {

        private World world;
        private Camera camera;

        public WorldDemo() {
            super("World demo");
            try {
                world = new World();
                camera = new Camera();
                camera.setPerspective(60.0f, 1.0f, 1.0f, 50.0f);
                Transform ct = new Transform();
                ct.postTranslate(0, 0, 8);
                camera.setTransform(ct);
                world.addChild(camera);
                world.setActiveCamera(camera);
                Background bg = new Background();
                bg.setColor(0x104010);
                world.setBackground(bg);
                Light light = new Light();
                light.setMode(Light.DIRECTIONAL);
                world.addChild(light);
                Mesh cube = createCube(1.5f);
                cube.setTransform(new Transform());
                world.addChild(cube);
                status = "world with cube";
            } catch (Throwable t) {
                status = "init failed: " + t;
            }
        }

        protected void paintM3G(Graphics3D g3d, int w, int h) throws Exception {
            if (world == null) return;
            if (world.getChildCount() > 2) {
                Node n = world.getChild(2);
                Transform t = new Transform();
                t.postRotate(frame, 0, 1, 0);
                n.setTransform(t);
            }
            g3d.render(world);
        }
    }

    public static class TexturedCube extends M3GBase {

        private Mesh cube;
        private Camera camera;
        private Transform camT = new Transform();
        private Transform cubeT = new Transform();

        public TexturedCube() {
            super("Textured cube");
            try {
                camera = new Camera();
                camera.setPerspective(60, 1.33f, 1, 100);
                camT.postTranslate(0, 0, 6);
                javax.microedition.lcdui.Image img = javax.microedition.lcdui.Image.createImage(64, 64);
                Graphics g = img.getGraphics();
                g.setColor(0xFFFFFF);
                g.fillRect(0, 0, 64, 64);
                g.setColor(0x000000);
                for (int y = 0; y < 64; y += 16) {
                    for (int x = 0; x < 64; x += 16) {
                        if (((x / 16) + (y / 16)) % 2 == 0) {
                            g.fillRect(x, y, 16, 16);
                        }
                    }
                }
                Image2D image2D = new Image2D(Image2D.RGB, img);
                Texture2D tex = new Texture2D(image2D);
                tex.setFiltering(Texture2D.FILTER_LINEAR, Texture2D.FILTER_LINEAR);
                tex.setWrapping(Texture2D.WRAP_REPEAT, Texture2D.WRAP_REPEAT);
                tex.setBlending(Texture2D.FUNC_MODULATE);

                Appearance ap = new Appearance();
                ap.setTexture(0, tex);
                Material mat = new Material();
                mat.setColor(Material.DIFFUSE, 0xFFFFFF);
                ap.setMaterial(mat);
                PolygonMode pm = new PolygonMode();
                pm.setShading(PolygonMode.SHADE_SMOOTH);
                ap.setPolygonMode(pm);

                cube = createCubeWithTexCoords(1.2f, ap);

                status = "textured cube";
            } catch (Throwable t) {
                status = "init failed: " + t;
            }
        }

        protected void paintM3G(Graphics3D g3d, int w, int h) throws Exception {
            if (cube == null) return;
            camera.setPerspective(60, (float) w / h, 1, 100);
            g3d.setCamera(camera, camT);
            Light light = new Light();
            light.setMode(Light.DIRECTIONAL);
            g3d.addLight(light, new Transform());
            cubeT.setIdentity();
            cubeT.postRotate(frame, 1, 1, 0);
            g3d.render(cube, cubeT);
            g3d.resetLights();
        }
    }

    public static class Sprite3DDemo extends M3GBase {

        private Sprite3D sprite;
        private Camera camera;
        private Transform camT = new Transform();
        private Transform spriteT = new Transform();

        public Sprite3DDemo() {
            super("Sprite3D");
            try {
                camera = new Camera();
                camera.setParallel(4, 4, 1, 100);
                camT.postTranslate(0, 0, 10);
                javax.microedition.lcdui.Image img = javax.microedition.lcdui.Image.createImage(32, 32);
                Graphics g = img.getGraphics();
                g.setColor(0xFF0000);
                g.fillRect(0, 0, 32, 32);
                g.setColor(0xFFFFFF);
                g.fillArc(4, 4, 24, 24, 0, 360);
                Image2D image2D = new Image2D(Image2D.RGBA, img);
                Appearance ap = new Appearance();
                sprite = new Sprite3D(false, image2D, ap);
                status = "sprite created";
            } catch (Throwable t) {
                status = "init failed: " + t;
            }
        }

        protected void paintM3G(Graphics3D g3d, int w, int h) throws Exception {
            if (sprite == null) return;
            g3d.setCamera(camera, camT);
            spriteT.setIdentity();
            spriteT.postTranslate((float) Math.sin(frame * 0.1f), (float) Math.cos(frame * 0.1f), 0);
            g3d.render(sprite, spriteT);
        }
    }

    public static class MorphingDemo extends M3GBase {

        private MorphingMesh morph;
        private Camera camera;
        private Transform camT = new Transform();

        public MorphingDemo() {
            super("Morphing");
            try {
                camera = new Camera();
                camera.setPerspective(60, 1, 1, 100);
                camT.postTranslate(0, 0, 5);
                VertexBuffer base = createTriangleVB(new float[]{0, 0, 0, 1, 0, 0, 0, 1, 0});
                VertexBuffer target = createTriangleVB(new float[]{0, 0, 0, 1, 0, 0, 0.5f, 1.5f, 0});
                IndexBuffer ib = new TriangleStripArray(0, new int[]{3});
                morph = new MorphingMesh(base, new VertexBuffer[]{target}, ib, new Appearance());
                status = "morph created";
            } catch (Throwable t) {
                status = "init failed: " + t;
            }
        }

        protected void paintM3G(Graphics3D g3d, int w, int h) throws Exception {
            if (morph == null) return;
            camera.setPerspective(60, (float) w / h, 1, 100);
            g3d.setCamera(camera, camT);
            float weight = (float) (Math.sin(frame * 0.1) * 0.5 + 0.5);
            morph.setWeights(new float[]{weight});
            g3d.render(morph, new Transform());
        }
    }

    public static class TransparencyDemo extends M3GBase {

        private Mesh cube1, cube2;
        private Camera camera;
        private Transform camT = new Transform();

        public TransparencyDemo() {
            super("Transparency");
            try {
                camera = new Camera();
                camera.setPerspective(60, 1, 1, 100);
                camT.postTranslate(0, 0, 6);
                Appearance ap1 = new Appearance();
                CompositingMode cm1 = new CompositingMode();
                cm1.setBlending(CompositingMode.ALPHA);
                ap1.setCompositingMode(cm1);
                Material m1 = new Material();
                m1.setColor(Material.DIFFUSE, 0xFF0000);
                ap1.setMaterial(m1);
                cube1 = createCubeWithAppearance(1.0f, ap1);

                Appearance ap2 = new Appearance();
                CompositingMode cm2 = new CompositingMode();
                cm2.setBlending(CompositingMode.ALPHA);
                cm2.setAlphaThreshold(0.2f);
                ap2.setCompositingMode(cm2);
                Material m2 = new Material();
                m2.setColor(Material.DIFFUSE, 0x0000FF);
                ap2.setMaterial(m2);
                cube2 = createCubeWithAppearance(0.7f, ap2);

                status = "two cubes alpha";
            } catch (Throwable t) {
                status = "init failed: " + t;
            }
        }

        protected void paintM3G(Graphics3D g3d, int w, int h) throws Exception {
            if (cube1 == null) return;
            camera.setPerspective(60, (float) w / h, 1, 100);
            g3d.setCamera(camera, camT);
            Transform t1 = new Transform();
            t1.postTranslate(-0.5f, 0, 0);
            t1.postRotate(frame, 0, 1, 0);
            g3d.render(cube1, t1);
            Transform t2 = new Transform();
            t2.postTranslate(0.5f, 0, 0);
            t2.postRotate(-frame, 0, 1, 0);
            g3d.render(cube2, t2);
        }
    }

    public static class PickingDemo extends M3GBase {

        private World world;
        private String pickInfo = "no pick";

        public PickingDemo() {
            super("Picking");
            try {
                world = new World();
                Camera cam = new Camera();
                cam.setPerspective(60, 1, 1, 50);
                Transform ct = new Transform();
                ct.postTranslate(0, 0, 5);
                cam.setTransform(ct);
                world.addChild(cam);
                world.setActiveCamera(cam);
                Background bg = new Background();
                bg.setColor(0x202020);
                world.setBackground(bg);
                Mesh cube = createCube(1.0f);
                cube.setScope(1);
                world.addChild(cube);
                status = "tap to pick";
            } catch (Throwable t) {
                status = "init failed: " + t;
            }
        }

        protected void pointerPressed(int x, int y) {
            if (world == null) return;
            try {
                RayIntersection ri = new RayIntersection();
                boolean hit = world.pick(1, 0, 0, 0, 0, 0, 1, ri);
                if (hit) {
                    pickInfo = "hit dist=" + ri.getDistance() + " obj=" + ri.getIntersected();
                } else {
                    pickInfo = "miss";
                }
                status = pickInfo;
            } catch (Throwable t) {
                pickInfo = "pick error " + t;
            }
        }

        protected void paintM3G(Graphics3D g3d, int w, int h) throws Exception {
            if (world == null) return;
            g3d.render(world);
        }
    }

    /* ------------------------------------------------------------------ */
    /* helpers to create cubes                                            */
    /* ------------------------------------------------------------------ */

    private static VertexBuffer createTriangleVB(float[] coords) {
        VertexArray pos = new VertexArray(3, 3, 2);
        short[] s = new short[9];
        for (int i = 0; i < 9; i++) {
            s[i] = (short) (coords[i] * 1000);
        }
        pos.set(0, 3, s);
        VertexBuffer vb = new VertexBuffer();
        vb.setPositions(pos, 0.001f, null);
        return vb;
    }

    private static Mesh createCube(float size) {
        return createCubeWithAppearance(size, new Appearance());
    }

    private static Mesh createCubeWithAppearance(float size, Appearance ap) {
        float hs = size / 2;
        float[] verts = new float[]{
            -hs, -hs, hs, hs, -hs, hs, hs, hs, hs, -hs, hs, hs,
            -hs, -hs, -hs, -hs, hs, -hs, hs, hs, -hs, hs, -hs, -hs
        };
        VertexArray pos = new VertexArray(8, 3, 2);
        short[] s = new short[24];
        for (int i = 0; i < 24; i++) {
            s[i] = (short) (verts[i] * 1000);
        }
        pos.set(0, 8, s);
        VertexBuffer vb = new VertexBuffer();
        vb.setPositions(pos, 0.001f, null);

        int[] indices = new int[]{
            0, 1, 2, 0, 2, 3,
            1, 5, 6, 1, 6, 2,
            5, 4, 7, 5, 7, 6,
            4, 0, 3, 4, 3, 7,
            3, 2, 6, 3, 6, 7,
            4, 5, 1, 4, 1, 0
        };
        IndexBuffer ib = new TriangleStripArray(indices, new int[]{3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3});
        return new Mesh(vb, ib, ap);
    }

    private static Mesh createCubeWithTexCoords(float size, Appearance ap) {
        float hs = size / 2;
        float[] positions = new float[24 * 3];
        float[] tex = new float[24 * 2];
        int v = 0;
        int t = 0;
        float[][] faces = new float[][]{
            {-hs, -hs, hs, hs, -hs, hs, hs, hs, hs, -hs, hs, hs},
            {hs, -hs, hs, hs, -hs, -hs, hs, hs, -hs, hs, hs, hs},
            {hs, -hs, -hs, -hs, -hs, -hs, -hs, hs, -hs, hs, hs, -hs},
            {-hs, -hs, -hs, -hs, -hs, hs, -hs, hs, hs, -hs, hs, -hs},
            {-hs, hs, hs, hs, hs, hs, hs, hs, -hs, -hs, hs, -hs},
            {-hs, -hs, -hs, hs, -hs, -hs, hs, -hs, hs, -hs, -hs, hs}
        };
        for (int f = 0; f < 6; f++) {
            for (int i = 0; i < 4; i++) {
                positions[v++] = faces[f][i * 3];
                positions[v++] = faces[f][i * 3 + 1];
                positions[v++] = faces[f][i * 3 + 2];
                tex[t++] = (i == 0 || i == 3) ? 0 : 1;
                tex[t++] = (i < 2) ? 0 : 1;
            }
        }
        VertexArray posArray = new VertexArray(24, 3, 2);
        short[] ps = new short[24 * 3];
        for (int i = 0; i < positions.length; i++) ps[i] = (short) (positions[i] * 1000);
        posArray.set(0, 24, ps);
        VertexArray texArray = new VertexArray(24, 2, 2);
        short[] ts = new short[24 * 2];
        for (int i = 0; i < tex.length; i++) ts[i] = (short) (tex[i] * 1000);
        texArray.set(0, 24, ts);
        VertexBuffer vb = new VertexBuffer();
        vb.setPositions(posArray, 0.001f, null);
        vb.setTexCoords(0, texArray, 0.001f, null);

        int[] indices = new int[36];
        int idx = 0;
        for (int f = 0; f < 6; f++) {
            int base = f * 4;
            indices[idx++] = base;
            indices[idx++] = base + 1;
            indices[idx++] = base + 2;
            indices[idx++] = base;
            indices[idx++] = base + 2;
            indices[idx++] = base + 3;
        }
        IndexBuffer ib = new TriangleStripArray(indices, new int[]{3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3});
        return new Mesh(vb, ib, ap);
    }
}
