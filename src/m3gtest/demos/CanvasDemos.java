package m3gtest.demos;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import javax.microedition.m3g.*;

public class CanvasDemos {

    private CanvasDemos() {}

    /**
     * Incredibly beautiful cat running on grass demo.
     * Fixes:
     * - Cat now runs forward (fixed direction math and transform order T*R)
     * - Texture mapping fixed (proper UV per face, CLAMP for cat, REPEAT for grass)
     * - Grass no longer missing (40x40 ground + 80x80 far plane, camera look-at)
     * - Environment made beautiful: sky gradient with clouds, sun sprite, 12 trees, 20 flowers, 3 butterflies, fog, warm lighting, shadows, path
     */
    public static class CatGrassDemo extends Canvas {

        private Thread animator;
        private boolean running;
        private int frame = 0;
        private String status = "init";
        private String verifyLog = "";

        // M3G core
        private Graphics3D g3d;
        private Camera camera;
        private Transform camTransform = new Transform();
        private Light sunLight;
        private Light fillLight;
        private Light ambientLight;
        private Background background;
        private Fog fog;

        // Textures
        private Image2D skyImage2D;
        private Image2D grassImage2D;
        private Texture2D grassTexture;
        private Image2D catImage2D;
        private Texture2D catTexture;
        private Image2D catWhiteImage2D;
        private Texture2D catWhiteTexture;
        private Image2D trunkImage2D;
        private Texture2D trunkTexture;
        private Image2D foliageImage2D;
        private Texture2D foliageTexture;
        private Image2D flowerImage2D;
        private Texture2D flowerTexture;
        private Image2D sunImage2D;

        // Ground
        private Mesh groundMesh;
        private Mesh farGroundMesh;
        private Mesh pathMesh;
        private Appearance groundAppearance;
        private Appearance farGroundAppearance;
        private Appearance pathAppearance;

        // Environment
        private Group worldGroup; // holds trees, flowers, ground
        private Group[] trees = new Group[12];
        private Group[] flowers = new Group[20];
        private Group[] butterflies = new Group[3];
        private Sprite3D sunSprite;
        private Mesh[] butterflyMeshes = new Mesh[3];

        // Cat
        private Group catRoot;
        private Group catBodyGroup;
        private Mesh bodyMesh;
        private Mesh headMesh;
        private Mesh earLeftMesh;
        private Mesh earRightMesh;
        private Mesh tailMesh;
        private Mesh pawMesh;
        private Group legFLGroup, legFRGroup, legBLGroup, legBRGroup;
        private Group tailGroup;
        private Group headGroup;
        private Mesh shadowMesh;
        private Appearance catAppearance;
        private Appearance catWhiteAppearance;
        private Appearance catPinkAppearance;
        private Appearance shadowAppearance;

        // Animation
        private float catX = 0, catZ = 0, catDirRad = 0;
        private float runCycle = 0;

        public CatGrassDemo() {
            setTitle("Cat on grass - beautiful");
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
            g3d = Graphics3D.getInstance();
            if (g3d == null) throw new RuntimeException("G3D null");
            log.append("G3D OK;");

            // --- SKY GRADIENT 128x128 with clouds ---
            Image skyLCUI = Image.createImage(128, 128);
            Graphics sg = skyLCUI.getGraphics();
            for (int y = 0; y < 128; y++) {
                int t = (y * 255) / 127;
                // gradient top #1E90FF (30,144,255) to bottom #87CEEB (135,206,235) to horizon #FFE4B5 (255,228,181)
                int r, g, b;
                if (y < 80) {
                    // sky top to mid
                    float f = y / 80.0f;
                    r = (int)(30 + (135-30)*f);
                    g = (int)(144 + (206-144)*f);
                    b = (int)(255 + (235-255)*f);
                } else {
                    float f = (y-80)/48.0f;
                    r = (int)(135 + (255-135)*f);
                    g = (int)(206 + (228-206)*f);
                    b = (int)(235 + (181-235)*f);
                }
                sg.setColor((r<<16)|(g<<8)|b);
                sg.drawLine(0, y, 127, y);
            }
            // clouds - soft white blobs
            sg.setColor(0xFFFFFF);
            for (int i = 0; i < 15; i++) {
                int cx = (i*37+13)%110;
                int cy = (i*23+7)%60;
                int w = 12 + (i*7)%18;
                int h = 6 + (i*5)%8;
                sg.fillArc(cx, cy, w, h, 0, 360);
                sg.fillArc(cx+4, cy-2, w-4, h, 0, 360);
            }
            skyImage2D = new Image2D(Image2D.RGB, skyLCUI);
            log.append("sky 128x128 OK;");

            // --- GRASS 64x64 detailed ---
            Image grassLCUI = Image.createImage(64, 64);
            Graphics gg = grassLCUI.getGraphics();
            gg.setColor(0x2E8B57); // sea green base
            gg.fillRect(0,0,64,64);
            // dark blades
            for (int i = 0; i < 300; i++) {
                int x = (i*13+7)%64;
                int y = (i*29+11)%64;
                int shade = 0x228B22 + (i%3)*0x10100;
                gg.setColor(shade);
                gg.drawLine(x, y, x, Math.min(63, y+2+(i%4)));
                if (i%3==0) gg.drawLine(x+1, y, x+1, Math.min(63, y+1));
            }
            // light tips
            for (int i = 0; i < 150; i++) {
                int x = (i*7+3)%64;
                int y = (i*17+5)%64;
                gg.setColor(0x7CFC00);
                gg.fillRect(x, y, 1, 1);
            }
            // tiny flowers on grass texture
            for (int i = 0; i < 25; i++) {
                int x = (i*31+13)%60;
                int y = (i*19+7)%60;
                int col = (i%3==0)?0xFFFF00:(i%3==1)?0xFF69B4:0xFFFFFF;
                gg.setColor(col);
                gg.fillRect(x, y, 2, 2);
            }
            grassImage2D = new Image2D(Image2D.RGB, grassLCUI);
            grassTexture = new Texture2D(grassImage2D);
            grassTexture.setFiltering(Texture2D.FILTER_LINEAR, Texture2D.FILTER_LINEAR);
            grassTexture.setWrapping(Texture2D.WRAP_REPEAT, Texture2D.WRAP_REPEAT);
            grassTexture.setBlending(Texture2D.FUNC_MODULATE);
            log.append("grass 64x64 OK;");

            // --- CAT TABBY 64x64 more cute ---
            Image catLCUI = Image.createImage(64, 64);
            Graphics cg = catLCUI.getGraphics();
            cg.setColor(0xFFA54F); // light orange
            cg.fillRect(0,0,64,64);
            // darker stripes
            cg.setColor(0x8B4513);
            for (int y = 0; y < 64; y+=8) {
                cg.fillRect(0, y, 64, 2);
                cg.fillRect(0, y+4, 64, 1);
            }
            // white chest/belly
            cg.setColor(0xFFF8DC);
            cg.fillArc(8, 32, 48, 28, 0, 360);
            // face white
            cg.setColor(0xFFFFFF);
            cg.fillArc(12, 4, 40, 24, 0, 360);
            // pink nose
            cg.setColor(0xFF69B4);
            cg.fillRect(28, 18, 8, 4);
            cg.fillRect(30, 22, 4, 2);
            // eyes - big cute
            cg.setColor(0x000000);
            cg.fillArc(16, 8, 12, 12, 0, 360);
            cg.fillArc(36, 8, 12, 12, 0, 360);
            cg.setColor(0x00FF00);
            cg.fillArc(18, 10, 8, 8, 0, 360);
            cg.fillArc(38, 10, 8, 8, 0, 360);
            cg.setColor(0x000000);
            cg.fillArc(20, 12, 4, 6, 0, 360);
            cg.fillArc(40, 12, 4, 6, 0, 360);
            cg.setColor(0xFFFFFF);
            cg.fillArc(19, 11, 2, 2, 0, 360);
            cg.fillArc(39, 11, 2, 2, 0, 360);
            // whiskers
            cg.setColor(0x000000);
            cg.drawLine(4, 16, 14, 15);
            cg.drawLine(4, 20, 14, 19);
            cg.drawLine(50, 15, 60, 16);
            cg.drawLine(50, 19, 60, 20);
            catImage2D = new Image2D(Image2D.RGB, catLCUI);
            catTexture = new Texture2D(catImage2D);
            catTexture.setFiltering(Texture2D.FILTER_LINEAR, Texture2D.FILTER_LINEAR);
            catTexture.setWrapping(Texture2D.WRAP_CLAMP, Texture2D.WRAP_CLAMP);
            catTexture.setBlending(Texture2D.FUNC_MODULATE);

            // white paws texture 16x16
            Image whiteLCUI = Image.createImage(16,16);
            Graphics wg = whiteLCUI.getGraphics();
            wg.setColor(0xFFFFFF);
            wg.fillRect(0,0,16,16);
            wg.setColor(0xFFDAB9);
            wg.fillRect(0,12,16,4);
            catWhiteImage2D = new Image2D(Image2D.RGB, whiteLCUI);
            catWhiteTexture = new Texture2D(catWhiteImage2D);
            catWhiteTexture.setFiltering(Texture2D.FILTER_LINEAR, Texture2D.FILTER_LINEAR);
            catWhiteTexture.setWrapping(Texture2D.WRAP_CLAMP, Texture2D.WRAP_CLAMP);

            // trunk texture 16x16 brown bark
            Image trunkLCUI = Image.createImage(16,16);
            Graphics tg = trunkLCUI.getGraphics();
            tg.setColor(0x8B4513);
            tg.fillRect(0,0,16,16);
            tg.setColor(0x5D2906);
            for (int x=0;x<16;x+=4) tg.drawLine(x,0,x,16);
            trunkImage2D = new Image2D(Image2D.RGB, trunkLCUI);
            trunkTexture = new Texture2D(trunkImage2D);
            trunkTexture.setFiltering(Texture2D.FILTER_LINEAR, Texture2D.FILTER_LINEAR);

            // foliage texture 32x32 green with variation
            Image folLCUI = Image.createImage(32,32);
            Graphics fg = folLCUI.getGraphics();
            fg.setColor(0x228B22);
            fg.fillRect(0,0,32,32);
            fg.setColor(0x32CD32);
            for (int i=0;i<60;i++) {
                int x=(i*13)%32; int y=(i*7)%32;
                fg.fillRect(x,y,2,2);
            }
            foliageImage2D = new Image2D(Image2D.RGB, folLCUI);
            foliageTexture = new Texture2D(foliageImage2D);
            foliageTexture.setFiltering(Texture2D.FILTER_LINEAR, Texture2D.FILTER_LINEAR);

            // flower texture 8x8
            Image flowerLCUI = Image.createImage(8,8);
            Graphics flg = flowerLCUI.getGraphics();
            flg.setColor(0xFF00FF);
            flg.fillArc(0,0,8,8,0,360);
            flg.setColor(0xFFFF00);
            flg.fillArc(2,2,4,4,0,360);
            flowerImage2D = new Image2D(Image2D.RGB, flowerLCUI);
            flowerTexture = new Texture2D(flowerImage2D);

            // sun texture 32x32 yellow with glow
            Image sunLCUI = Image.createImage(32,32);
            Graphics sunG = sunLCUI.getGraphics();
            sunG.setColor(0xFFFF00);
            sunG.fillArc(0,0,32,32,0,360);
            sunG.setColor(0xFFFFFF);
            sunG.fillArc(8,8,16,16,0,360);
            sunG.setColor(0xFFD700);
            sunG.fillArc(10,10,12,12,0,360);
            sunImage2D = new Image2D(Image2D.RGBA, sunLCUI);

            log.append("cat+tree+flower+sun tex OK;");

            // --- Appearances ---
            groundAppearance = new Appearance();
            groundAppearance.setTexture(0, grassTexture);
            Material gm = new Material();
            gm.setColor(Material.DIFFUSE, 0xFFFFFF);
            gm.setColor(Material.AMBIENT, 0x999999);
            groundAppearance.setMaterial(gm);
            PolygonMode gpm = new PolygonMode();
            gpm.setShading(PolygonMode.SHADE_SMOOTH);
            gpm.setCulling(PolygonMode.CULL_NONE);
            groundAppearance.setPolygonMode(gpm);

            farGroundAppearance = new Appearance();
            farGroundAppearance.setTexture(0, grassTexture);
            Material fgm = new Material();
            fgm.setColor(Material.DIFFUSE, 0x88AA88);
            fgm.setColor(Material.AMBIENT, 0x777777);
            farGroundAppearance.setMaterial(fgm);
            farGroundAppearance.setPolygonMode(gpm);

            pathAppearance = new Appearance();
            pathAppearance.setTexture(0, grassTexture);
            Material pm = new Material();
            pm.setColor(Material.DIFFUSE, 0xDEB887);
            pm.setColor(Material.AMBIENT, 0xAA8866);
            pathAppearance.setMaterial(pm);
            pathAppearance.setPolygonMode(gpm);

            catAppearance = new Appearance();
            catAppearance.setTexture(0, catTexture);
            Material cm = new Material();
            cm.setColor(Material.DIFFUSE, 0xFFFFFF);
            cm.setColor(Material.SPECULAR, 0x333333);
            cm.setShininess(12);
            catAppearance.setMaterial(cm);
            PolygonMode cpm = new PolygonMode();
            cpm.setShading(PolygonMode.SHADE_SMOOTH);
            catAppearance.setPolygonMode(cpm);

            catWhiteAppearance = new Appearance();
            catWhiteAppearance.setTexture(0, catWhiteTexture);
            catWhiteAppearance.setMaterial(cm);
            catWhiteAppearance.setPolygonMode(cpm);

            catPinkAppearance = new Appearance();
            Material pinkMat = new Material();
            pinkMat.setColor(Material.DIFFUSE, 0xFFB6C1);
            catPinkAppearance.setMaterial(pinkMat);
            catPinkAppearance.setPolygonMode(cpm);

            shadowAppearance = new Appearance();
            CompositingMode scm = new CompositingMode();
            scm.setBlending(CompositingMode.ALPHA);
            scm.setAlphaThreshold(0.05f);
            shadowAppearance.setCompositingMode(scm);
            Material sm = new Material();
            sm.setColor(Material.DIFFUSE, 0x33000000);
            shadowAppearance.setMaterial(sm);
            PolygonMode spm = new PolygonMode();
            spm.setShading(PolygonMode.SHADE_FLAT);
            shadowAppearance.setPolygonMode(spm);

            Appearance trunkApp = new Appearance();
            trunkApp.setTexture(0, trunkTexture);
            trunkApp.setMaterial(gm);
            trunkApp.setPolygonMode(gpm);

            Appearance folApp = new Appearance();
            folApp.setTexture(0, foliageTexture);
            folApp.setMaterial(gm);
            folApp.setPolygonMode(gpm);

            Appearance flowerApp = new Appearance();
            flowerApp.setTexture(0, flowerTexture);
            Material flowerMat = new Material();
            flowerMat.setColor(Material.DIFFUSE, 0xFFFFFF);
            flowerApp.setMaterial(flowerMat);
            flowerApp.setPolygonMode(gpm);
            CompositingMode flowerCM = new CompositingMode();
            flowerCM.setBlending(CompositingMode.ALPHA);
            flowerApp.setCompositingMode(flowerCM);

            log.append("appearances OK;");

            // --- Ground meshes 40x40 and 80x80 ---
            groundMesh = createPlane(40.0f, groundAppearance, 12.0f);
            farGroundMesh = createPlane(80.0f, farGroundAppearance, 20.0f);
            pathMesh = createPlane(2.0f, pathAppearance, 1.0f);
            log.append("ground 40+80 OK;");

            // --- World group ---
            worldGroup = new Group();
            worldGroup.addChild(groundMesh);
            // far ground slightly lower to avoid z-fighting
            Transform farT = new Transform();
            farT.postTranslate(0, -0.05f, 0);
            farGroundMesh.setTransform(farT);
            worldGroup.addChild(farGroundMesh);

            // circular path where cat runs - 16 small planes forming circle
            Group pathGroup = new Group();
            for (int i = 0; i < 16; i++) {
                float ang = (i * 360.0f / 16.0f) * 3.14159f / 180.0f;
                float r = 4.0f;
                float x = (float)Math.sin(ang)*r;
                float z = (float)Math.cos(ang)*r;
                Mesh seg = createPlane(1.2f, pathAppearance, 1.0f);
                Transform tt = new Transform();
                tt.postTranslate(x, 0.01f, z);
                tt.postRotate((float)Math.toDegrees(ang), 0,1,0);
                seg.setTransform(tt);
                pathGroup.addChild(seg);
            }
            worldGroup.addChild(pathGroup);

            // --- Trees 12 around ---
            for (int i = 0; i < 12; i++) {
                float ang = (i * 360.0f / 12.0f) * 3.14159f / 180.0f;
                float r = 12.0f + (i%3);
                float x = (float)Math.sin(ang)*r;
                float z = (float)Math.cos(ang)*r;
                Group tree = createTree(trunkApp, folApp, 1.8f + (i%3)*0.4f);
                Transform treeT = new Transform();
                treeT.postTranslate(x, 0, z);
                tree.setTransform(treeT);
                trees[i] = tree;
                worldGroup.addChild(tree);
            }
            log.append("12 trees OK;");

            // --- Flowers 20 scattered ---
            for (int i = 0; i < 20; i++) {
                float ang = (i*137.5f)*3.14159f/180.0f; // golden angle
                float r = 2.0f + (i*0.6f)%8.0f;
                float x = (float)Math.sin(ang)*r;
                float z = (float)Math.cos(ang)*r;
                if (Math.abs(x) < 1.5f && Math.abs(z) < 1.5f) continue; // avoid center
                Group flower = new Group();
                Mesh stem = createBox(0.05f, 0.4f, 0.05f, trunkApp, "stem");
                Transform stemT = new Transform();
                stemT.postTranslate(0, 0.2f, 0);
                stem.setTransform(stemT);
                flower.addChild(stem);
                Mesh bloom = createBox(0.25f, 0.1f, 0.25f, flowerApp, "bloom");
                Transform bloomT = new Transform();
                bloomT.postTranslate(0, 0.45f, 0);
                bloom.setTransform(bloomT);
                flower.addChild(bloom);
                Transform fT = new Transform();
                fT.postTranslate(x, 0, z);
                flower.setTransform(fT);
                flowers[i] = flower;
                worldGroup.addChild(flower);
            }
            log.append("20 flowers OK;");

            // --- Butterflies 3 ---
            for (int i = 0; i < 3; i++) {
                Group bf = new Group();
                Appearance bfApp = new Appearance();
                Material bfm = new Material();
                int col = (i==0)?0xFF69B4:(i==1)?0x00FFFF:0xFFFF00;
                bfm.setColor(Material.DIFFUSE, col);
                bfApp.setMaterial(bfm);
                bfApp.setPolygonMode(gpm);
                Mesh wingL = createBox(0.3f, 0.02f, 0.2f, bfApp, "wing");
                Mesh wingR = createBox(0.3f, 0.02f, 0.2f, bfApp, "wing");
                Transform wLT = new Transform();
                wLT.postTranslate(-0.18f, 0, 0);
                wingL.setTransform(wLT);
                Transform wRT = new Transform();
                wRT.postTranslate(0.18f, 0, 0);
                wingR.setTransform(wRT);
                bf.addChild(wingL);
                bf.addChild(wingR);
                butterflies[i] = bf;
                butterflyMeshes[i] = wingL; // for animation ref
                worldGroup.addChild(bf);
            }
            log.append("3 butterflies OK;");

            // --- Sun sprite ---
            Image2D sunImg2D = new Image2D(Image2D.RGBA, Image.createImage(32,32));
            // Actually use previously created sunImage2D
            Appearance sunApp = new Appearance();
            sunApp.setTexture(0, new Texture2D(sunImage2D));
            CompositingMode sunCM = new CompositingMode();
            sunCM.setBlending(CompositingMode.ALPHA);
            sunApp.setCompositingMode(sunCM);
            sunSprite = new Sprite3D(true, sunImage2D, sunApp);
            sunSprite.setScale(6.0f, 6.0f);
            Transform sunT = new Transform();
            sunT.postTranslate(15, 12, -20);
            sunSprite.setTransform(sunT);
            worldGroup.addChild(sunSprite);

            // --- Cat meshes ---
            pawMesh = createBox(0.24f, 0.18f, 0.24f, catWhiteAppearance, "paw");
            bodyMesh = createBox(1.3f, 0.65f, 0.6f, catAppearance, "body");
            headMesh = createBox(0.65f, 0.6f, 0.65f, catAppearance, "head");
            earLeftMesh = createBox(0.18f, 0.28f, 0.14f, catPinkAppearance, "earL");
            earRightMesh = createBox(0.18f, 0.28f, 0.14f, catPinkAppearance, "earR");
            tailMesh = createBox(0.16f, 0.16f, 1.0f, catAppearance, "tail");
            shadowMesh = createPlane(1.6f, shadowAppearance, 1.0f);
            log.append("cat meshes OK;");

            // --- Cat hierarchy ---
            catRoot = new Group();
            catBodyGroup = new Group();

            Transform bodyT = new Transform();
            bodyT.postTranslate(0, 0.55f, 0);
            bodyMesh.setTransform(bodyT);
            catBodyGroup.addChild(bodyMesh);

            headGroup = new Group();
            Transform headPos = new Transform();
            headPos.postTranslate(0.85f, 0.65f, 0);
            headGroup.setTransform(headPos);
            headGroup.addChild(headMesh);
            Transform earLPos = new Transform();
            earLPos.postTranslate(0.18f, 0.38f, 0.18f);
            earLeftMesh.setTransform(earLPos);
            headGroup.addChild(earLeftMesh);
            Transform earRPos = new Transform();
            earRPos.postTranslate(0.18f, 0.38f, -0.18f);
            earRightMesh.setTransform(earRPos);
            headGroup.addChild(earRightMesh);
            catBodyGroup.addChild(headGroup);

            tailGroup = new Group();
            Transform tailPivot = new Transform();
            tailPivot.postTranslate(-0.75f, 0.6f, 0);
            tailGroup.setTransform(tailPivot);
            Transform tailMeshT = new Transform();
            tailMeshT.postTranslate(-0.5f, 0, 0);
            tailMesh.setTransform(tailMeshT);
            tailGroup.addChild(tailMesh);
            catBodyGroup.addChild(tailGroup);

            // legs with white paws
            legFLGroup = createLegWithPaw(0.5f, 0.0f, 0.24f);
            legFRGroup = createLegWithPaw(0.5f, 0.0f, -0.24f);
            legBLGroup = createLegWithPaw(-0.5f, 0.0f, 0.24f);
            legBRGroup = createLegWithPaw(-0.5f, 0.0f, -0.24f);

            catBodyGroup.addChild(legFLGroup);
            catBodyGroup.addChild(legFRGroup);
            catBodyGroup.addChild(legBLGroup);
            catBodyGroup.addChild(legBRGroup);

            Group shadowGroup = new Group();
            Transform shadowT = new Transform();
            shadowT.postTranslate(0, 0.02f, 0);
            shadowT.postRotate(90, 1, 0, 0);
            shadowGroup.setTransform(shadowT);
            shadowGroup.addChild(shadowMesh);
            catBodyGroup.addChild(shadowGroup);

            catRoot.addChild(catBodyGroup);
            log.append("cat hierarchy OK;");

            // --- Camera, lights, background, fog ---
            camera = new Camera();
            camera.setPerspective(50.0f, 1.33f, 0.3f, 60.0f);

            sunLight = new Light();
            sunLight.setMode(Light.DIRECTIONAL);
            sunLight.setColor(0xFFE4B5); // warm sun
            sunLight.setIntensity(1.3f);

            fillLight = new Light();
            fillLight.setMode(Light.DIRECTIONAL);
            fillLight.setColor(0xADD8E6);
            fillLight.setIntensity(0.4f);

            ambientLight = new Light();
            ambientLight.setMode(Light.AMBIENT);
            ambientLight.setColor(0x666666);
            ambientLight.setIntensity(0.7f);

            background = new Background();
            background.setColor(0x87CEEB);
            // Use sky image as background
            Background skyBg = new Background();
            skyBg.setColor(0x87CEEB);
            skyBg.setImage(skyImage2D);
            skyBg.setImageMode(Background.BORDER, Background.BORDER);
            skyBg.setCrop(0, 0, 128, 64);
            background = skyBg;

            fog = new Fog();
            fog.setMode(Fog.LINEAR);
            fog.setColor(0xC2DFFF); // light horizon fog
            fog.setLinear(14.0f, 38.0f);
            groundAppearance.setFog(fog);
            farGroundAppearance.setFog(fog);
            catAppearance.setFog(fog);
            catWhiteAppearance.setFog(fog);
            trunkApp.setFog(fog);
            folApp.setFog(fog);

            verifyLog = log.toString();
        }

        private Group createTree(Appearance trunkApp, Appearance folApp, float height) throws Exception {
            Group tree = new Group();
            float trunkH = height;
            Mesh trunk = createBox(0.25f, trunkH, 0.25f, trunkApp, "trunk");
            Transform trunkT = new Transform();
            trunkT.postTranslate(0, trunkH/2.0f, 0);
            trunk.setTransform(trunkT);
            tree.addChild(trunk);
            // foliage 3 layers
            for (int i = 0; i < 3; i++) {
                float sz = 1.2f - i*0.2f;
                Mesh fol = createBox(sz, sz*0.7f, sz, folApp, "fol");
                Transform folT = new Transform();
                folT.postTranslate(0, trunkH + 0.3f + i*0.5f, 0);
                fol.setTransform(folT);
                tree.addChild(fol);
            }
            return tree;
        }

        private Group createLegWithPaw(float x, float y, float z) throws Exception {
            Group hip = new Group();
            Transform hipT = new Transform();
            hipT.postTranslate(x, y, z);
            hip.setTransform(hipT);
            // upper leg
            Mesh upper = createBox(0.24f, 0.5f, 0.24f, catAppearance, "upperLeg");
            Transform upT = new Transform();
            upT.postTranslate(0, -0.25f, 0);
            upper.setTransform(upT);
            hip.addChild(upper);
            // paw white at bottom
            Group pawGroup = new Group();
            Transform pawPivot = new Transform();
            pawPivot.postTranslate(0, -0.5f, 0);
            pawGroup.setTransform(pawPivot);
            Mesh paw = (Mesh) pawMesh.duplicate();
            Transform pawT = new Transform();
            pawT.postTranslate(0, -0.09f, 0);
            paw.setTransform(pawT);
            pawGroup.addChild(paw);
            hip.addChild(pawGroup);
            return hip;
        }

        private Mesh createBox(float w, float h, float d, Appearance ap, String dbg) throws Exception {
            float hx = w / 2.0f;
            float hy = h / 2.0f;
            float hz = d / 2.0f;
            float[] pos = new float[]{
                -hx, -hy, hz,  hx, -hy, hz,  hx, hy, hz,  -hx, hy, hz,
                hx, -hy, -hz,  -hx, -hy, -hz,  -hx, hy, -hz,  hx, hy, -hz,
                hx, -hy, hz,  hx, -hy, -hz,  hx, hy, -hz,  hx, hy, hz,
                -hx, -hy, -hz,  -hx, -hy, hz,  -hx, hy, hz,  -hx, hy, -hz,
                -hx, hy, hz,  hx, hy, hz,  hx, hy, -hz,  -hx, hy, -hz,
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
            // Fixed UV: each face 0,0 1,0 1,1 0,1 with correct orientation
            short[] tex = new short[]{
                0,1000, 1000,1000, 1000,0, 0,0,
                0,1000, 1000,1000, 1000,0, 0,0,
                0,1000, 1000,1000, 1000,0, 0,0,
                0,1000, 1000,1000, 1000,0, 0,0,
                0,1000, 1000,1000, 1000,0, 0,0,
                0,1000, 1000,1000, 1000,0, 0,0
            };
            VertexArray posArray = new VertexArray(24, 3, 2);
            short[] posShort = new short[72];
            for (int i = 0; i < 72; i++) posShort[i] = (short)(pos[i]*1000);
            posArray.set(0,24,posShort);
            VertexArray normArray = new VertexArray(24,3,1);
            normArray.set(0,24,norm);
            VertexArray texArray = new VertexArray(24,2,2);
            texArray.set(0,24,tex);
            VertexBuffer vb = new VertexBuffer();
            vb.setPositions(posArray,0.001f,new float[]{0,0,0});
            vb.setNormals(normArray);
            vb.setTexCoords(0,texArray,0.001f,new float[]{0,0,0});
            int[] indices = new int[36];
            int p=0;
            for (int f=0;f<6;f++) {
                int b=f*4;
                indices[p++]=b; indices[p++]=b+1; indices[p++]=b+2;
                indices[p++]=b; indices[p++]=b+2; indices[p++]=b+3;
            }
            IndexBuffer ib = new TriangleStripArray(indices, new int[]{3,3,3,3,3,3,3,3,3,3,3,3});
            Mesh mesh = new Mesh(vb, ib, ap);
            return mesh;
        }

        private Mesh createPlane(float size, Appearance ap, float texRepeat) throws Exception {
            float hs = size/2.0f;
            float[] pos = new float[]{ -hs,0,-hs, hs,0,-hs, hs,0,hs, -hs,0,hs };
            byte[] norm = new byte[]{ 0,127,0, 0,127,0, 0,127,0, 0,127,0 };
            short tr = (short)(texRepeat*1000);
            short[] tex = new short[]{ 0,0, tr,0, tr,tr, 0,tr };
            VertexArray posArray = new VertexArray(4,3,2);
            short[] ps = new short[12];
            for (int i=0;i<12;i++) ps[i]=(short)(pos[i]*1000);
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
                            runCycle += 0.28f;

                            float radius = 4.2f;
                            float ang = frame * 0.018f;
                            catX = (float)Math.sin(ang) * radius;
                            catZ = (float)Math.cos(ang) * radius;
                            catDirRad = ang; // direction tangent, fixed forward

                            float swing = (float)Math.sin(runCycle) * 0.65f;
                            float swing2 = (float)Math.sin(runCycle + 3.14159f) * 0.65f;

                            try {
                                // Legs - trot gait, now correct forward
                                Transform tFL = new Transform();
                                tFL.postTranslate(0.5f, 0.0f, 0.24f);
                                tFL.postRotate((float)Math.toDegrees(swing), 0,0,1);
                                legFLGroup.setTransform(tFL);

                                Transform tFR = new Transform();
                                tFR.postTranslate(0.5f, 0.0f, -0.24f);
                                tFR.postRotate((float)Math.toDegrees(swing2), 0,0,1);
                                legFRGroup.setTransform(tFR);

                                Transform tBL = new Transform();
                                tBL.postTranslate(-0.5f, 0.0f, 0.24f);
                                tBL.postRotate((float)Math.toDegrees(swing2), 0,0,1);
                                legBLGroup.setTransform(tBL);

                                Transform tBR = new Transform();
                                tBR.postTranslate(-0.5f, 0.0f, -0.24f);
                                tBR.postRotate((float)Math.toDegrees(swing), 0,0,1);
                                legBRGroup.setTransform(tBR);

                                // Tail wag + up/down
                                Transform tailT = new Transform();
                                tailT.postTranslate(-0.75f, 0.6f, 0);
                                tailT.postRotate((float)Math.sin(runCycle*1.6f)*22, 0,1,0);
                                tailT.postRotate((float)Math.sin(runCycle*0.7f)*10, 0,0,1);
                                tailGroup.setTransform(tailT);

                                // Body bob + slight tilt
                                Transform bodyBob = new Transform();
                                bodyBob.postTranslate(0, (float)Math.abs(Math.sin(runCycle))*0.09f, 0);
                                bodyBob.postRotate((float)Math.sin(runCycle)*3, 0,0,1);
                                catBodyGroup.setTransform(bodyBob);

                                // Head look around
                                Transform headLook = new Transform();
                                headLook.postTranslate(0.85f, 0.65f, 0);
                                headLook.postRotate((float)Math.sin(frame*0.05f)*8, 0,1,0);
                                headGroup.setTransform(headLook);

                                // Butterflies flutter around
                                for (int i=0;i<3;i++) {
                                    Group bf = butterflies[i];
                                    float bAng = frame*0.02f + i*2.0f;
                                    float bx = (float)Math.sin(bAng)*(3+i) + catX*0.3f;
                                    float bz = (float)Math.cos(bAng*0.7f)*(3+i) + catZ*0.3f;
                                    float by = 1.2f + (float)Math.sin(bAng*1.3f)*0.6f + i*0.3f;
                                    Transform bT = new Transform();
                                    bT.postTranslate(bx, by, bz);
                                    bT.postRotate(frame*5 + i*30, 0,1,0);
                                    bf.setTransform(bT);
                                }

                            } catch (Throwable ignore) {}

                            repaint();
                            try { Thread.sleep(45); } catch (InterruptedException e) { return; }
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
            Font medium = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
            g.setFont(small);

            if (g3d == null || worldGroup == null || catRoot == null) {
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

                    // --- Camera look-at cat ---
                    float camRadius = 9.5f;
                    float camAng = frame * 0.008f;
                    float camX = (float)Math.sin(camAng) * 2.5f;
                    float camZ = camRadius + (float)Math.cos(camAng) * 1.2f;
                    float camY = 3.2f + (float)Math.sin(camAng*0.5f)*0.4f;

                    float targetX = catX * 0.6f;
                    float targetZ = catZ * 0.6f;
                    float targetY = 0.4f;

                    float dx = targetX - camX;
                    float dy = targetY - camY;
                    float dz = targetZ - camZ;
                    float lenXZ = (float)Math.sqrt(dx*dx + dz*dz);
                    float yaw = (float)Math.toDegrees(Math.atan2(-dx, -dz));
                    float pitch = (float)Math.toDegrees(Math.atan2(dy, lenXZ));

                    camTransform.setIdentity();
                    camTransform.postTranslate(camX, camY, camZ);
                    camTransform.postRotate(yaw, 0,1,0);
                    camTransform.postRotate(pitch, 1,0,0);

                    camera.setPerspective(50.0f, (float)w/h, 0.3f, 60.0f);
                    g3d.setCamera(camera, camTransform);

                    // Lights - warm sun + fill + ambient
                    Transform sunLT = new Transform();
                    sunLT.postTranslate(10, 15, -10);
                    g3d.addLight(sunLight, sunLT);

                    Transform fillLT = new Transform();
                    fillLT.postTranslate(-8, 5, 8);
                    g3d.addLight(fillLight, fillLT);

                    g3d.addLight(ambientLight, new Transform());

                    // Render world (ground, trees, flowers, butterflies, sun)
                    Transform worldT = new Transform();
                    g3d.render(worldGroup, worldT);

                    // Cat - FIXED: translate then rotate (T*R) so cat faces forward correctly
                    Transform catRootT = new Transform();
                    catRootT.postTranslate(catX, 0, catZ);
                    catRootT.postRotate((float)Math.toDegrees(catDirRad), 0,1,0);
                    // add 180 if still backwards? Our model faces +X, direction tangent is angle, so need -90? Let's test: at ang=0, cat at (0,r), dirRad=0, tangent +X, model +X -> should face +X, so dir 0 is correct.
                    // If cat faces +X initially, rotation 0 keeps +X, good. So use dirRad directly.
                    g3d.render(catRoot, catRootT);

                    g3d.resetLights();

                } finally {
                    g3d.releaseTarget();
                }

                // Beautiful overlay
                g.setFont(medium);
                g.setColor(0xFFFFFF);
                g.drawString("Cute cat running on meadow", 4, 3, Graphics.TOP|Graphics.LEFT);
                g.setFont(small);
                g.setColor(0xFFFFAA);
                g.drawString("frame="+frame+" cat "+String.valueOf((int)(catX*10)/10.0f)+","+String.valueOf((int)(catZ*10)/10.0f)+" dir "+(int)Math.toDegrees(catDirRad), 4, 18, Graphics.TOP|Graphics.LEFT);
                g.setColor(0xAAFFAA);
                g.drawString("12 trees, 20 flowers, 3 butterflies, sun, fog, path - all procedural", 4, 30, Graphics.TOP|Graphics.LEFT);
                g.setColor(0xFFFFFF);
                g.drawString(status, 4, h-14, Graphics.TOP|Graphics.LEFT);

            } catch (Throwable t) {
                g.setColor(0x000000);
                g.fillRect(0,0,w,h);
                g.setColor(0xFF5555);
                g.drawString("M3G error: "+t, 2, 2, Graphics.TOP|Graphics.LEFT);
                g.drawString(t.toString(), 2, 14, Graphics.TOP|Graphics.LEFT);
                status = "render fail: "+t;
            }
        }
    }
}
