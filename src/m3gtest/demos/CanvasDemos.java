package m3gtest.demos;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import javax.microedition.m3g.*;

public class CanvasDemos {

    private CanvasDemos() {}

    /**
     * Cat running on grass - single demo that verifies all assets.
     * Features:
     * - Procedurally generated grass texture (64x64) with verification
     * - Procedurally generated cat texture (32x32 tabby) with verification
     * - Ground plane 20x20 with REPEAT wrapping and fog
     * - Low-poly cat built from 9 boxes: body, head, 2 ears, 4 legs, tail
     * - Each leg is a Group with pivot for running animation
     * - Cat runs in circle on grass, body bobs, tail wags, legs swing
     * - Directional sun light + ambient, sky background, linear fog
     * - Shadow quad under cat
     * - Full asset verification with status overlay
     */
    public static class CatGrassDemo extends Canvas {

        private Thread animator;
        private boolean running;
        private int frame = 0;
        private String status = "init";
        private String verifyLog = "";

        // M3G objects
        private Graphics3D g3d;
        private Camera camera;
        private Transform camTransform = new Transform();
        private Light sunLight;
        private Light ambientLight;
        private Background background;
        private Fog fog;

        // Textures
        private Image2D grassImage2D;
        private Texture2D grassTexture;
        private Image2D catImage2D;
        private Texture2D catTexture;

        // Ground
        private Mesh groundMesh;
        private Appearance groundAppearance;

        // Cat
        private Group catRoot; // root that moves on grass
        private Group catBodyGroup; // body + head + tail + ears
        private Mesh bodyMesh;
        private Mesh headMesh;
        private Mesh earLeftMesh;
        private Mesh earRightMesh;
        private Mesh tailMesh;
        private Group legFLGroup, legFRGroup, legBLGroup, legBRGroup;
        private Group tailGroup;
        private Mesh legMesh; // shared leg mesh (reused)
        private Mesh shadowMesh;

        // Appearances
        private Appearance catAppearance;
        private Appearance catHeadAppearance;
        private Appearance shadowAppearance;

        // Animation state
        private float catX = 0, catZ = 0, catDir = 0;
        private float runCycle = 0;

        public CatGrassDemo() {
            setTitle("Cat on grass");
            try {
                verifyAndCreateAllAssets();
                status = "assets OK - " + verifyLog;
            } catch (Throwable t) {
                status = "asset fail: " + t + " - " + verifyLog;
                t.printStackTrace();
            }
        }

        private void verifyAndCreateAllAssets() throws Exception {
            StringBuffer log = new StringBuffer();

            // --- Step 1: Graphics3D ---
            g3d = Graphics3D.getInstance();
            if (g3d == null) throw new RuntimeException("Graphics3D null");
            log.append("G3D OK;");

            // --- Step 2: Grass texture generation + verification ---
            Image grassLCUI = Image.createImage(64, 64);
            if (grassLCUI == null) throw new RuntimeException("grass LCUI null");
            if (grassLCUI.getWidth() != 64 || grassLCUI.getHeight() != 64) throw new RuntimeException("grass size wrong");
            Graphics gg = grassLCUI.getGraphics();
            gg.setColor(0x33AA33);
            gg.fillRect(0, 0, 64, 64);
            // darker blades
            for (int i = 0; i < 200; i++) {
                int x = (i * 13 + 7) % 64;
                int y = (i * 29 + 11) % 64;
                gg.setColor(0x228B22);
                gg.drawLine(x, y, x, Math.min(63, y + 2 + (i % 3)));
            }
            // lighter spots
            for (int i = 0; i < 120; i++) {
                int x = (i * 7 + 3) % 64;
                int y = (i * 17 + 5) % 64;
                gg.setColor(0x66CC66);
                gg.fillRect(x, y, 1, 1);
            }
            // tiny flowers
            for (int i = 0; i < 20; i++) {
                int x = (i * 31 + 13) % 60;
                int y = (i * 19 + 7) % 60;
                gg.setColor((i % 2 == 0) ? 0xFFFF00 : 0xFFFFFF);
                gg.fillRect(x, y, 2, 2);
            }
            if (grassLCUI.getWidth() != 64) throw new RuntimeException("grass LCUI width check");
            grassImage2D = new Image2D(Image2D.RGB, grassLCUI);
            if (grassImage2D == null) throw new RuntimeException("grass Image2D null");
            if (grassImage2D.getWidth() != 64 || grassImage2D.getHeight() != 64) throw new RuntimeException("grass Image2D size");
            if (!grassImage2D.isMutable()) log.append("grass immutable OK;");
            grassTexture = new Texture2D(grassImage2D);
            if (grassTexture == null) throw new RuntimeException("grass Texture2D null");
            grassTexture.setFiltering(Texture2D.FILTER_LINEAR, Texture2D.FILTER_LINEAR);
            grassTexture.setWrapping(Texture2D.WRAP_REPEAT, Texture2D.WRAP_REPEAT);
            grassTexture.setBlending(Texture2D.FUNC_MODULATE);
            if (grassTexture.getImage() != grassImage2D) throw new RuntimeException("grass texture image mismatch");
            log.append("grass tex OK;");

            // --- Step 3: Cat texture generation + verification ---
            Image catLCUI = Image.createImage(32, 32);
            if (catLCUI == null) throw new RuntimeException("cat LCUI null");
            Graphics cg = catLCUI.getGraphics();
            cg.setColor(0xFF8C00); // orange tabby base
            cg.fillRect(0, 0, 32, 32);
            cg.setColor(0xFFFFFF);
            cg.fillRect(4, 18, 24, 10); // white belly/chest
            cg.setColor(0x000000);
            for (int y = 0; y < 32; y += 6) {
                cg.drawLine(0, y, 32, y); // stripes
            }
            cg.setColor(0xFF69B4); // pink nose
            cg.fillRect(14, 12, 4, 2);
            cg.setColor(0x000000);
            cg.fillRect(8, 6, 4, 4); // left eye
            cg.fillRect(20, 6, 4, 4); // right eye
            cg.setColor(0xFFFFFF);
            cg.fillRect(9, 7, 1, 1);
            cg.fillRect(21, 7, 1, 1);
            catImage2D = new Image2D(Image2D.RGB, catLCUI);
            if (catImage2D == null) throw new RuntimeException("cat Image2D null");
            if (catImage2D.getWidth() != 32) throw new RuntimeException("cat Image2D width");
            catTexture = new Texture2D(catImage2D);
            catTexture.setFiltering(Texture2D.FILTER_LINEAR, Texture2D.FILTER_LINEAR);
            catTexture.setWrapping(Texture2D.WRAP_CLAMP, Texture2D.WRAP_CLAMP);
            catTexture.setBlending(Texture2D.FUNC_MODULATE);
            log.append("cat tex OK;");

            // --- Step 4: Appearances ---
            // Ground appearance
            groundAppearance = new Appearance();
            if (groundAppearance == null) throw new RuntimeException("groundAppearance null");
            groundAppearance.setTexture(0, grassTexture);
            Material groundMat = new Material();
            groundMat.setColor(Material.DIFFUSE, 0xFFFFFF);
            groundMat.setColor(Material.AMBIENT, 0xAAAAAA);
            groundAppearance.setMaterial(groundMat);
            PolygonMode groundPM = new PolygonMode();
            groundPM.setShading(PolygonMode.SHADE_SMOOTH);
            groundPM.setCulling(PolygonMode.CULL_NONE);
            groundAppearance.setPolygonMode(groundPM);
            if (groundAppearance.getTexture(0) != grassTexture) throw new RuntimeException("ground tex not set");

            // Cat appearance
            catAppearance = new Appearance();
            catAppearance.setTexture(0, catTexture);
            Material catMat = new Material();
            catMat.setColor(Material.DIFFUSE, 0xFFFFFF);
            catMat.setColor(Material.SPECULAR, 0x222222);
            catMat.setShininess(8);
            catAppearance.setMaterial(catMat);
            PolygonMode catPM = new PolygonMode();
            catPM.setShading(PolygonMode.SHADE_SMOOTH);
            catAppearance.setPolygonMode(catPM);

            catHeadAppearance = new Appearance();
            catHeadAppearance.setTexture(0, catTexture);
            catHeadAppearance.setMaterial(catMat);
            catHeadAppearance.setPolygonMode(catPM);

            // Shadow appearance - semi-transparent dark
            shadowAppearance = new Appearance();
            CompositingMode shadowCM = new CompositingMode();
            shadowCM.setBlending(CompositingMode.ALPHA);
            shadowCM.setAlphaThreshold(0.1f);
            shadowAppearance.setCompositingMode(shadowCM);
            Material shadowMat = new Material();
            shadowMat.setColor(Material.DIFFUSE, 0x44000000);
            shadowAppearance.setMaterial(shadowMat);
            PolygonMode shadowPM = new PolygonMode();
            shadowPM.setShading(PolygonMode.SHADE_FLAT);
            shadowAppearance.setPolygonMode(shadowPM);
            log.append("appearances OK;");

            // --- Step 5: Ground mesh (20x20 plane) ---
            groundMesh = createPlane(20.0f, groundAppearance, 5.0f); // 5x repeat
            if (groundMesh == null) throw new RuntimeException("groundMesh null");
            if (groundMesh.getVertexBuffer() == null) throw new RuntimeException("ground VB null");
            if (groundMesh.getIndexBuffer() == null) throw new RuntimeException("ground IB null");
            log.append("ground mesh OK;");

            // --- Step 6: Cat body parts (boxes) ---
            // Shared leg mesh 0.22 x 0.7 x 0.22
            legMesh = createBox(0.22f, 0.7f, 0.22f, catAppearance, "leg");
            if (legMesh == null) throw new RuntimeException("legMesh null");

            bodyMesh = createBox(1.2f, 0.6f, 0.55f, catAppearance, "body");
            headMesh = createBox(0.6f, 0.55f, 0.6f, catHeadAppearance, "head");
            earLeftMesh = createBox(0.18f, 0.25f, 0.12f, catAppearance, "earL");
            earRightMesh = createBox(0.18f, 0.25f, 0.12f, catAppearance, "earR");
            tailMesh = createBox(0.15f, 0.15f, 0.9f, catAppearance, "tail");
            shadowMesh = createPlane(1.4f, shadowAppearance, 1.0f);

            if (bodyMesh.getVertexBuffer().getVertexCount() != 24) throw new RuntimeException("body vertex count not 24");
            log.append("cat meshes OK;");

            // --- Step 7: Assemble cat hierarchy with pivot groups ---
            catRoot = new Group();
            catBodyGroup = new Group();

            // Body at origin of catBodyGroup
            Transform bodyT = new Transform();
            bodyT.postTranslate(0, 0.5f, 0);
            bodyMesh.setTransform(bodyT);
            catBodyGroup.addChild(bodyMesh);

            // Head at front
            Group headGroup = new Group();
            Transform headPos = new Transform();
            headPos.postTranslate(0.75f, 0.6f, 0);
            headGroup.setTransform(headPos);
            headGroup.addChild(headMesh);
            // Ears on head
            Transform earLPos = new Transform();
            earLPos.postTranslate(0.15f, 0.35f, 0.15f);
            earLeftMesh.setTransform(earLPos);
            headGroup.addChild(earLeftMesh);
            Transform earRPos = new Transform();
            earRPos.postTranslate(0.15f, 0.35f, -0.15f);
            earRightMesh.setTransform(earRPos);
            headGroup.addChild(earRightMesh);
            catBodyGroup.addChild(headGroup);

            // Tail group with wag pivot
            tailGroup = new Group();
            Transform tailPivot = new Transform();
            tailPivot.postTranslate(-0.7f, 0.55f, 0);
            tailGroup.setTransform(tailPivot);
            Transform tailMeshT = new Transform();
            tailMeshT.postTranslate(-0.45f, 0, 0); // offset so pivot at base
            tailMesh.setTransform(tailMeshT);
            tailGroup.addChild(tailMesh);
            catBodyGroup.addChild(tailGroup);

            // Legs - each leg is Group (hip) + mesh translated down
            legFLGroup = createLegGroup(0.45f, 0.0f, 0.22f); // front left
            legFRGroup = createLegGroup(0.45f, 0.0f, -0.22f); // front right
            legBLGroup = createLegGroup(-0.45f, 0.0f, 0.22f); // back left
            legBRGroup = createLegGroup(-0.45f, 0.0f, -0.22f); // back right

            catBodyGroup.addChild(legFLGroup);
            catBodyGroup.addChild(legFRGroup);
            catBodyGroup.addChild(legBLGroup);
            catBodyGroup.addChild(legBRGroup);

            // Shadow under cat
            Group shadowGroup = new Group();
            Transform shadowT = new Transform();
            shadowT.postTranslate(0, 0.02f, 0);
            shadowT.postRotate(90, 1, 0, 0);
            shadowGroup.setTransform(shadowT);
            shadowGroup.addChild(shadowMesh);
            catBodyGroup.addChild(shadowGroup);

            catRoot.addChild(catBodyGroup);

            if (catRoot.getChildCount() != 1) throw new RuntimeException("catRoot child count");
            if (catBodyGroup.getChildCount() != 8) throw new RuntimeException("catBody child count expected 8 got " + catBodyGroup.getChildCount());
            log.append("cat hierarchy OK;");

            // --- Step 8: Camera, lights, background, fog ---
            camera = new Camera();
            if (camera == null) throw new RuntimeException("camera null");
            camera.setPerspective(45.0f, 1.33f, 0.5f, 50.0f);

            sunLight = new Light();
            sunLight.setMode(Light.DIRECTIONAL);
            sunLight.setColor(0xFFFFFF);
            sunLight.setIntensity(1.0f);

            ambientLight = new Light();
            ambientLight.setMode(Light.AMBIENT);
            ambientLight.setColor(0x444444);
            ambientLight.setIntensity(0.6f);

            background = new Background();
            background.setColor(0x87CEEB); // sky blue
            if (background.getColor() != 0x87CEEB) throw new RuntimeException("bg color mismatch");

            fog = new Fog();
            fog.setMode(Fog.LINEAR);
            fog.setColor(0x87CEEB);
            fog.setLinear(12.0f, 28.0f);
            groundAppearance.setFog(fog);
            catAppearance.setFog(fog);
            catHeadAppearance.setFog(fog);
            log.append("camera/light/bg/fog OK;");

            verifyLog = log.toString();
        }

        private Group createLegGroup(float x, float y, float z) {
            Group hip = new Group();
            Transform hipT = new Transform();
            hipT.postTranslate(x, y, z);
            hip.setTransform(hipT);
            // leg mesh centered at origin, we need it hanging down
            Mesh legInstance = (Mesh) legMesh.duplicate();
            Transform legT = new Transform();
            legT.postTranslate(0, -0.35f, 0); // pivot at top
            legInstance.setTransform(legT);
            hip.addChild(legInstance);
            return hip;
        }

        private Mesh createBox(float w, float h, float d, Appearance ap, String dbg) throws Exception {
            float hx = w / 2.0f;
            float hy = h / 2.0f;
            float hz = d / 2.0f;
            // 6 faces *4 verts =24
            float[] pos = new float[]{
                // front +Z
                -hx, -hy, hz,  hx, -hy, hz,  hx, hy, hz,  -hx, hy, hz,
                // back -Z
                hx, -hy, -hz,  -hx, -hy, -hz,  -hx, hy, -hz,  hx, hy, -hz,
                // right +X
                hx, -hy, hz,  hx, -hy, -hz,  hx, hy, -hz,  hx, hy, hz,
                // left -X
                -hx, -hy, -hz,  -hx, -hy, hz,  -hx, hy, hz,  -hx, hy, -hz,
                // top +Y
                -hx, hy, hz,  hx, hy, hz,  hx, hy, -hz,  -hx, hy, -hz,
                // bottom -Y
                -hx, -hy, -hz,  hx, -hy, -hz,  hx, -hy, hz,  -hx, -hy, hz
            };
            byte[] norm = new byte[]{
                0,0,127, 0,0,127, 0,0,127, 0,0,127,
                0,0,-128, 0,0,-128, 0,0,-128, 0,0,-128,
                127,0,0, 127,0,0, 127,0,0, 127,0,0,
                -128,0,0, -128,0,0, -128,0,0, -128,0,0,
                0,127,0, 0,127,0, 0,127,0, 0,127,0,
                0,-128,0, 0,-128,0, 0,-128,0, 0,-128,0
            };
            short[] tex = new short[]{
                0,0, 1000,0, 1000,1000, 0,1000,
                0,0, 1000,0, 1000,1000, 0,1000,
                0,0, 1000,0, 1000,1000, 0,1000,
                0,0, 1000,0, 1000,1000, 0,1000,
                0,0, 1000,0, 1000,1000, 0,1000,
                0,0, 1000,0, 1000,1000, 0,1000
            };
            VertexArray posArray = new VertexArray(24, 3, 2);
            short[] posShort = new short[72];
            for (int i = 0; i < 72; i++) {
                posShort[i] = (short) (pos[i] * 1000);
            }
            posArray.set(0, 24, posShort);

            VertexArray normArray = new VertexArray(24, 3, 1);
            normArray.set(0, 24, norm);

            VertexArray texArray = new VertexArray(24, 2, 2);
            texArray.set(0, 24, tex);

            VertexBuffer vb = new VertexBuffer();
            vb.setPositions(posArray, 0.001f, new float[]{0,0,0});
            vb.setNormals(normArray);
            vb.setTexCoords(0, texArray, 0.001f, new float[]{0,0,0});

            int[] indices = new int[36];
            int p = 0;
            for (int f = 0; f < 6; f++) {
                int b = f * 4;
                indices[p++] = b; indices[p++] = b+1; indices[p++] = b+2;
                indices[p++] = b; indices[p++] = b+2; indices[p++] = b+3;
            }
            IndexBuffer ib = new TriangleStripArray(indices, new int[]{3,3,3,3,3,3,3,3,3,3,3,3});

            Mesh mesh = new Mesh(vb, ib, ap);
            if (mesh.getVertexBuffer().getVertexCount() != 24) throw new RuntimeException(dbg+" vertex count");
            return mesh;
        }

        private Mesh createPlane(float size, Appearance ap, float texRepeat) throws Exception {
            float hs = size / 2.0f;
            float[] pos = new float[]{
                -hs, 0, -hs,
                hs, 0, -hs,
                hs, 0, hs,
                -hs, 0, hs
            };
            byte[] norm = new byte[]{
                0,127,0, 0,127,0, 0,127,0, 0,127,0
            };
            float tr = texRepeat;
            short[] tex = new short[]{
                0,0,
                (short)(tr*1000),0,
                (short)(tr*1000),(short)(tr*1000),
                0,(short)(tr*1000)
            };
            VertexArray posArray = new VertexArray(4, 3, 2);
            short[] ps = new short[12];
            for (int i = 0; i < 12; i++) ps[i] = (short)(pos[i]*1000);
            posArray.set(0,4,ps);
            VertexArray normArray = new VertexArray(4,3,1);
            normArray.set(0,4,norm);
            VertexArray texArray = new VertexArray(4,2,2);
            texArray.set(0,4,tex);
            VertexBuffer vb = new VertexBuffer();
            vb.setPositions(posArray,0.001f,new float[]{0,0,0});
            vb.setNormals(normArray);
            vb.setTexCoords(0,texArray,0.001f,new float[]{0,0,0});
            IndexBuffer ib = new TriangleStripArray(new int[]{0,1,2,3}, new int[]{4});
            return new Mesh(vb, ib, ap);
        }

        protected void showNotify() {
            running = true;
            if (animator == null) {
                animator = new Thread(new Runnable() {
                    public void run() {
                        while (running) {
                            frame++;
                            runCycle += 0.25f;
                            // cat runs in circle radius 4
                            float radius = 4.0f;
                            float angle = frame * 0.02f;
                            catX = (float)Math.sin(angle) * radius;
                            catZ = (float)Math.cos(angle) * radius;
                            catDir = angle + 1.5708f; // +90 deg

                            // leg swing - running gait
                            float swing = (float)Math.sin(runCycle) * 0.6f; // rad
                            float swing2 = (float)Math.sin(runCycle + 3.14159f) * 0.6f;

                            try {
                                // front legs opposite to back legs for trot
                                Transform tFL = new Transform();
                                tFL.postTranslate(0.45f, 0.0f, 0.22f);
                                tFL.postRotate((float)Math.toDegrees(swing), 0, 0, 1);
                                legFLGroup.setTransform(tFL);

                                Transform tFR = new Transform();
                                tFR.postTranslate(0.45f, 0.0f, -0.22f);
                                tFR.postRotate((float)Math.toDegrees(swing2), 0, 0, 1);
                                legFRGroup.setTransform(tFR);

                                Transform tBL = new Transform();
                                tBL.postTranslate(-0.45f, 0.0f, 0.22f);
                                tBL.postRotate((float)Math.toDegrees(swing2), 0, 0, 1);
                                legBLGroup.setTransform(tBL);

                                Transform tBR = new Transform();
                                tBR.postTranslate(-0.45f, 0.0f, -0.22f);
                                tBR.postRotate((float)Math.toDegrees(swing), 0, 0, 1);
                                legBRGroup.setTransform(tBR);

                                // tail wag
                                Transform tailT = new Transform();
                                tailT.postTranslate(-0.7f, 0.55f, 0);
                                tailT.postRotate((float)Math.sin(runCycle*1.5f)*20, 0, 1, 0);
                                tailGroup.setTransform(tailT);

                                // body bob
                                Transform bodyBob = new Transform();
                                bodyBob.postTranslate(0, (float)Math.abs(Math.sin(runCycle))*0.08f, 0);
                                catBodyGroup.setTransform(bodyBob);

                            } catch (Throwable ignore) {}

                            repaint();
                            try { Thread.sleep(50); } catch (InterruptedException e) { return; }
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

            if (g3d == null || groundMesh == null || catRoot == null) {
                g.setColor(0x000000);
                g.fillRect(0,0,w,h);
                g.setColor(0xFFFFFF);
                g.drawString(status, 2, 2, Graphics.TOP|Graphics.LEFT);
                return;
            }

            try {
                g3d.bindTarget(g);
                try {
                    g3d.setViewport(0, 0, w, h);
                    g3d.clear(background);

                    // Camera - orbit slightly to follow cat
                    float camDist = 9.0f;
                    float camHeight = 3.5f;
                    float camAngle = frame * 0.01f;
                    float camX = (float)Math.sin(camAngle) * 2.0f;
                    float camZ = camDist + (float)Math.cos(camAngle) * 1.0f;
                    camTransform.setIdentity();
                    camTransform.postTranslate(camX, camHeight, camZ);
                    // look at cat
                    // Simple look-at via translate then rotate: we use setOrientation? We'll approximate
                    // For simplicity, camera always looks at origin, cat moves around origin
                    // So we rotate camera to look at center
                    camera.setPerspective(45.0f, (float)w/h, 0.5f, 50.0f);
                    g3d.setCamera(camera, camTransform);

                    // Lights
                    Transform sunT = new Transform();
                    sunT.postTranslate(5, 10, 5);
                    g3d.addLight(sunLight, sunT);
                    g3d.addLight(ambientLight, new Transform());

                    // Ground
                    Transform groundT = new Transform();
                    g3d.render(groundMesh, groundT);

                    // Cat root - move on grass circle
                    Transform catRootT = new Transform();
                    catRootT.postRotate((float)Math.toDegrees(catDir), 0, 1, 0);
                    catRootT.postTranslate(catX, 0, catZ);
                    g3d.render(catRoot, catRootT);

                    g3d.resetLights();

                } finally {
                    g3d.releaseTarget();
                }

                // Overlay UI - asset verification
                g.setColor(0xFFFFFF);
                g.drawString("Cat running on grass", 2, 2, Graphics.TOP|Graphics.LEFT);
                g.drawString("frame="+frame+" cat=("+ (int)(catX*10)/10.0f +","+ (int)(catZ*10)/10.0f +")", 2, 2+small.getHeight(), Graphics.TOP|Graphics.LEFT);
                g.setColor(0xFFFF00);
                g.drawString(status, 2, h-2-small.getHeight()*2, Graphics.TOP|Graphics.LEFT);
                g.drawString("grass 64x64 RGB REPEAT, cat 32x32 RGB CLAMP, 9 meshes, 4 legs anim", 2, h-2-small.getHeight(), Graphics.TOP|Graphics.LEFT);

            } catch (Throwable t) {
                g.setColor(0x000000);
                g.fillRect(0,0,w,h);
                g.setColor(0xFF0000);
                g.drawString("M3G error: "+t, 2, 2, Graphics.TOP|Graphics.LEFT);
                g.drawString(t.toString(), 2, 2+small.getHeight(), Graphics.TOP|Graphics.LEFT);
                status = "render fail: "+t;
            }
        }
    }
}
