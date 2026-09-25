/*
 * M3G Tester - Canvas based visual demos for M3G
 */
package m3gtest.demos;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import javax.microedition.m3g.*;

public class CanvasDemos {

    private CanvasDemos() {
    }

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
        public WorldDemo() {
            super("World demo");
            try {
                world = new World();
                Camera camera = new Camera();
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

    // ---- NEW VISUAL DEMOS ----

    public static class FogDemo extends M3GBase {
        private World world;
        private Fog fog;
        public FogDemo() {
            super("Fog");
            try {
                world = new World();
                Camera cam = new Camera();
                cam.setPerspective(60, 1, 1, 50);
                Transform ct = new Transform();
                ct.postTranslate(0, 0, 10);
                cam.setTransform(ct);
                world.addChild(cam);
                world.setActiveCamera(cam);
                Background bg = new Background();
                bg.setColor(0x8080A0);
                world.setBackground(bg);
                fog = new Fog();
                fog.setColor(0x8080A0);
                fog.setMode(Fog.LINEAR);
                fog.setLinear(5, 20);
                for (int i = 0; i < 5; i++) {
                    Mesh cube = createCube(1.0f);
                    Transform t = new Transform();
                    t.postTranslate(0, 0, -i * 3);
                    cube.setTransform(t);
                    Appearance ap = new Appearance();
                    ap.setFog(fog);
                    cube.setAppearance(0, ap);
                    world.addChild(cube);
                }
                status = "fog linear 5-20";
            } catch (Throwable t) {
                status = "init failed: " + t;
            }
        }
        protected void paintM3G(Graphics3D g3d, int w, int h) throws Exception {
            if (world == null) return;
            float near = 5 + (float) Math.sin(frame * 0.05f) * 2;
            fog.setLinear(near, 20);
            g3d.render(world);
        }
    }

    public static class LightingDemo extends M3GBase {
        private Mesh cube;
        private Camera camera;
        private Transform camT = new Transform();
        private Light[] lights = new Light[3];
        private Transform[] lightT = new Transform[3];
        public LightingDemo() {
            super("Multi-light");
            try {
                camera = new Camera();
                camera.setPerspective(60, 1, 1, 100);
                camT.postTranslate(0, 0, 6);
                for (int i = 0; i < 3; i++) {
                    lights[i] = new Light();
                    lights[i].setMode(i == 0 ? Light.DIRECTIONAL : (i == 1 ? Light.OMNI : Light.SPOT));
                    lights[i].setColor(i == 0 ? 0xFF0000 : (i == 1 ? 0x00FF00 : 0x0000FF));
                    lights[i].setIntensity(1.0f);
                    lightT[i] = new Transform();
                }
                Appearance ap = new Appearance();
                Material mat = new Material();
                mat.setColor(Material.DIFFUSE, 0xFFFFFF);
                mat.setColor(Material.SPECULAR, 0xFFFFFF);
                mat.setShininess(20);
                ap.setMaterial(mat);
                cube = createCubeWithAppearance(1.5f, ap);
                status = "3 lights RGB";
            } catch (Throwable t) {
                status = "init failed: " + t;
            }
        }
        protected void paintM3G(Graphics3D g3d, int w, int h) throws Exception {
            if (cube == null) return;
            camera.setPerspective(60, (float) w / h, 1, 100);
            g3d.setCamera(camera, camT);
            for (int i = 0; i < 3; i++) {
                lightT[i].setIdentity();
                float angle = frame * (i + 1) * 0.05f;
                lightT[i].postTranslate((float) Math.sin(angle) * 3, (float) Math.cos(angle) * 3, 2);
                g3d.addLight(lights[i], lightT[i]);
            }
            Transform cubeT = new Transform();
            cubeT.postRotate(frame, 0, 1, 0);
            g3d.render(cube, cubeT);
            g3d.resetLights();
        }
    }

    public static class CompositingDemo extends M3GBase {
        private Mesh[] cubes = new Mesh[4];
        private Camera camera;
        private Transform camT = new Transform();
        public CompositingDemo() {
            super("Blending modes");
            try {
                camera = new Camera();
                camera.setPerspective(60, 1, 1, 100);
                camT.postTranslate(0, 0, 8);
                int[] modes = new int[]{CompositingMode.REPLACE, CompositingMode.ALPHA, CompositingMode.ALPHA_ADD, CompositingMode.MODULATE};
                for (int i = 0; i < 4; i++) {
                    Appearance ap = new Appearance();
                    CompositingMode cm = new CompositingMode();
                    cm.setBlending(modes[i]);
                    ap.setCompositingMode(cm);
                    Material mat = new Material();
                    mat.setColor(Material.DIFFUSE, 0xFFFFFF);
                    ap.setMaterial(mat);
                    cubes[i] = createCubeWithAppearance(0.8f, ap);
                }
                status = "REPLACE,ALPHA,ADD,MODULATE";
            } catch (Throwable t) {
                status = "init failed: " + t;
            }
        }
        protected void paintM3G(Graphics3D g3d, int w, int h) throws Exception {
            camera.setPerspective(60, (float) w / h, 1, 100);
            g3d.setCamera(camera, camT);
            Light light = new Light();
            light.setMode(Light.DIRECTIONAL);
            g3d.addLight(light, new Transform());
            for (int i = 0; i < 4; i++) {
                Transform t = new Transform();
                t.postTranslate((i - 1.5f) * 1.2f, (float) Math.sin(frame * 0.05f + i) * 0.5f, 0);
                t.postRotate(frame + i * 20, 0, 1, 0);
                g3d.render(cubes[i], t);
            }
            g3d.resetLights();
        }
    }

    public static class BackgroundScrollDemo extends M3GBase {
        private Background bg;
        private Image2D image;
        public BackgroundScrollDemo() {
            super("Background scroll");
            try {
                javax.microedition.lcdui.Image img = javax.microedition.lcdui.Image.createImage(64, 64);
                Graphics g = img.getGraphics();
                for (int y = 0; y < 64; y++) {
                    g.setColor((y * 4) << 16 | 0x00FF00);
                    g.drawLine(0, y, 64, y);
                }
                image = new Image2D(Image2D.RGB, img);
                bg = new Background();
                bg.setColor(0x000000);
                bg.setImage(image);
                bg.setImageMode(Background.REPEAT, Background.REPEAT);
                bg.setCrop(0, 0, 32, 32);
                status = "scrolling bg";
            } catch (Throwable t) {
                status = "init failed: " + t;
            }
        }
        protected void paintM3G(Graphics3D g3d, int w, int h) throws Exception {
            if (bg == null) return;
            int cx = (int) (frame % 64);
            int cy = (int) ((frame * 0.7f) % 64);
            bg.setCrop(cx, cy, 32 + (frame % 10), 32 + (frame % 10));
            g3d.clear(bg);
            // also render a small cube to test clear + render
            Camera cam = new Camera();
            cam.setParallel(2, 2, 1, 10);
            Transform camT = new Transform();
            camT.postTranslate(0, 0, 5);
            g3d.setCamera(cam, camT);
            Mesh cube = createCube(0.5f);
            Transform t = new Transform();
            t.postRotate(frame, 1, 1, 0);
            g3d.render(cube, t);
        }
    }

    public static class AnimationDemo extends M3GBase {
        private Group group;
        private Camera camera;
        private Transform camT = new Transform();
        private AnimationController ctrl;
        private long startTime = System.currentTimeMillis();
        public AnimationDemo() {
            super("Animation");
            try {
                camera = new Camera();
                camera.setPerspective(60, 1, 1, 100);
                camT.postTranslate(0, 0, 8);
                group = new Group();
                Mesh cube = createCube(1.0f);
                group.addChild(cube);
                // Create simple translation animation
                KeyframeSequence ks = new KeyframeSequence(3, 3, KeyframeSequence.LINEAR);
                ks.setKeyframe(0, 0, new float[]{-2, 0, 0});
                ks.setKeyframe(1, 1000, new float[]{2, 0, 0});
                ks.setKeyframe(2, 2000, new float[]{-2, 0, 0});
                ks.setDuration(2000);
                ks.setRepeatMode(KeyframeSequence.LOOP);
                AnimationTrack track = new AnimationTrack(ks, AnimationTrack.TRANSLATION);
                ctrl = new AnimationController();
                ctrl.setSpeed(1.0f, 0);
                ctrl.setActiveInterval(0, 10000);
                ctrl.setPosition(0, 0);
                track.setController(ctrl);
                group.addAnimationTrack(track);
                status = "animated translation";
            } catch (Throwable t) {
                status = "init failed: " + t;
            }
        }
        protected void paintM3G(Graphics3D g3d, int w, int h) throws Exception {
            if (group == null) return;
            camera.setPerspective(60, (float) w / h, 1, 100);
            g3d.setCamera(camera, camT);
            Light light = new Light();
            light.setMode(Light.DIRECTIONAL);
            g3d.addLight(light, new Transform());
            int time = (int) (System.currentTimeMillis() - startTime);
            group.animate(time);
            g3d.render(group, new Transform());
            g3d.resetLights();
        }
    }

    public static class VertexColorDemo extends M3GBase {
        private Mesh mesh;
        private Camera camera;
        private Transform camT = new Transform();
        public VertexColorDemo() {
            super("Vertex colors");
            try {
                camera = new Camera();
                camera.setPerspective(60, 1, 1, 100);
                camT.postTranslate(0, 0, 4);
                VertexArray pos = new VertexArray(4, 3, 2);
                short[] ps = new short[]{-1000,-1000,0, 1000,-1000,0, 1000,1000,0, -1000,1000,0};
                pos.set(0, 4, ps);
                VertexArray colors = new VertexArray(4, 3, 1);
                byte[] cs = new byte[]{(byte)255,0,0, 0,(byte)255,0, 0,0,(byte)255, (byte)255,(byte)255,0};
                colors.set(0, 4, cs);
                VertexBuffer vb = new VertexBuffer();
                vb.setPositions(pos, 0.001f, new float[]{0,0,0});
                vb.setColors(colors);
                IndexBuffer ib = new TriangleStripArray(new int[]{0,1,2,3}, new int[]{4});
                Appearance ap = new Appearance();
                PolygonMode pm = new PolygonMode();
                pm.setShading(PolygonMode.SHADE_SMOOTH);
                ap.setPolygonMode(pm);
                mesh = new Mesh(vb, ib, ap);
                status = "4 colored verts";
            } catch (Throwable t) {
                status = "init failed: " + t;
            }
        }
        protected void paintM3G(Graphics3D g3d, int w, int h) throws Exception {
            if (mesh == null) return;
            camera.setPerspective(60, (float) w / h, 1, 100);
            g3d.setCamera(camera, camT);
            Transform t = new Transform();
            t.postRotate(frame, 0, 0, 1);
            g3d.render(mesh, t);
        }
    }

    public static class MultipleViewportsDemo extends M3GBase {
        private Mesh cube;
        private Camera camera;
        public MultipleViewportsDemo() {
            super("Viewports");
            try {
                camera = new Camera();
                camera.setPerspective(60, 1, 1, 100);
                cube = createCube(1.0f);
                status = "2 viewports";
            } catch (Throwable t) {
                status = "init failed: " + t;
            }
        }
        protected void paintM3G(Graphics3D g3d, int w, int h) throws Exception {
            if (cube == null) return;
            // First viewport: left half
            g3d.setViewport(0, 0, w/2, h);
            Background bg1 = new Background();
            bg1.setColor(0xFF0000);
            g3d.clear(bg1);
            Transform camT1 = new Transform();
            camT1.postTranslate(0, 0, 5);
            camera.setPerspective(60, (float)(w/2)/h, 1, 100);
            g3d.setCamera(camera, camT1);
            Transform t1 = new Transform();
            t1.postRotate(frame, 1, 0, 0);
            g3d.render(cube, t1);

            // Second viewport: right half
            g3d.setViewport(w/2, 0, w/2, h);
            Background bg2 = new Background();
            bg2.setColor(0x0000FF);
            g3d.clear(bg2);
            Transform camT2 = new Transform();
            camT2.postTranslate(0, 0, 5);
            g3d.setCamera(camera, camT2);
            Transform t2 = new Transform();
            t2.postRotate(-frame, 0, 1, 0);
            g3d.render(cube, t2);

            // Reset viewport
            g3d.setViewport(0, 0, w, h);
        }
    }

    private static VertexBuffer createTriangleVB(float[] coords) {
        VertexArray pos = new VertexArray(3, 3, 2);
        short[] s = new short[9];
        for (int i = 0; i < 9; i++) {
            s[i] = (short) (coords[i] * 1000);
        }
        pos.set(0, 3, s);
        VertexBuffer vb = new VertexBuffer();
        vb.setPositions(pos, 0.001f, new float[]{0,0,0});
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
        vb.setPositions(pos, 0.001f, new float[]{0,0,0});
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
        vb.setPositions(posArray, 0.001f, new float[]{0,0,0});
        vb.setTexCoords(0, texArray, 0.001f, new float[]{0,0,0});
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
