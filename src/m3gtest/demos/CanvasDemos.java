package m3gtest.demos;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import javax.microedition.m3g.*;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import java.io.InputStream;

public class CanvasDemos {

    private CanvasDemos() {}

    /**
     * Fixed beautiful meadow with hills, river, far river, normal trees, light ground, no text, fixed cat mapping
     */
    public static class CatGrassDemo extends Canvas {

        private Thread animator;
        private boolean running;
        private int frame = 0;

        // M3G
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
        private Image2D waterImage2D;
        private Texture2D waterTexture;
        private Image2D faceImage2D;
        private Texture2D faceTexture;
        private Image2D trunkImage2D;
        private Texture2D trunkTexture;
        private Image2D foliageImage2D;
        private Texture2D foliageTexture;

        // Ground
        private Mesh groundHillyMesh;
        private Mesh farGroundMesh;
        private Mesh riverMesh;
        private Mesh farRiverMesh;
        private Appearance groundAppearance;
        private Appearance farGroundAppearance;
        private Appearance waterAppearance;
        private Appearance farWaterAppearance;

        // World
        private Group worldGroup;
        private Group[] trees = new Group[16];
        private Group[] flowers = new Group[24];
        private Group[] butterflies = new Group[3];
        private Sprite3D sunSprite;

        // Cat - fixed mapping with solid colors, no horror texture
        private Group catRoot;
        private Group catBodyGroup;
        private Mesh bodyMesh;
        private Mesh headMesh;
        private Mesh earLeftMesh;
        private Mesh earRightMesh;
        private Mesh tailBaseMesh;
        private Mesh tailTipMesh;
        private Mesh pawMesh;
        private Mesh faceDecalMesh;
        private Group legFLGroup, legFRGroup, legBLGroup, legBRGroup;
        private Group tailGroup;
        private Group tailTipGroup;
        private Group headGroup;
        private Mesh shadowMesh;
        private Appearance catBodyAppearance; // solid orange, no texture - fixes mapping horror
        private Appearance catWhiteAppearance;
        private Appearance catPinkAppearance;
        private Appearance faceDecalAppearance;
        private Appearance shadowAppearance;

        private float catX = 0, catZ = 0, catDirRad = 0;
        private float runCycle = 0;

        private Player midiPlayer;

        public CatGrassDemo() {
            setTitle("Cat meadow");
            try {
                verifyAndCreateAllAssets();
            } catch (Throwable t) {
                t.printStackTrace();
            }
            tryStartMidi();
        }

        private void tryStartMidi() {
            try {
                InputStream is = null;
                try { is = getClass().getResourceAsStream("/eon.mid"); } catch (Throwable t) {}
                if (is == null) try { is = getClass().getResourceAsStream("/res/eon.mid"); } catch (Throwable t) {}
                if (is != null) {
                    Player p = Manager.createPlayer(is, "audio/midi");
                    p.setLoopCount(-1);
                    try {
                        javax.microedition.media.control.VolumeControl vc = (javax.microedition.media.control.VolumeControl) p.getControl("VolumeControl");
                        if (vc != null) vc.setLevel(75);
                    } catch (Throwable ignore) {}
                    p.start();
                    midiPlayer = p;
                }
            } catch (Throwable t) {}
        }

        private void verifyAndCreateAllAssets() throws Exception {
            g3d = Graphics3D.getInstance();

            // --- SKY fixed 256x128 beautiful gradient ---
            Image skyLCUI = Image.createImage(256, 128);
            Graphics sg = skyLCUI.getGraphics();
            for (int y = 0; y < 128; y++) {
                int r,g,b;
                if (y < 50) {
                    // top deep blue to mid blue
                    float f = y / 50.0f;
                    r = (int)(20 + (70-20)*f);
                    g = (int)(40 + (130-40)*f);
                    b = (int)(120 + (220-120)*f);
                } else if (y < 90) {
                    float f = (y-50)/40.0f;
                    r = (int)(70 + (135-70)*f);
                    g = (int)(130 + (206-130)*f);
                    b = (int)(220 + (235-220)*f);
                } else {
                    float f = (y-90)/38.0f;
                    r = (int)(135 + (255-135)*f);
                    g = (int)(206 + (235-206)*f);
                    b = (int)(235 + (210-235)*f);
                }
                sg.setColor((r<<16)|(g<<8)|b);
                sg.drawLine(0, y, 255, y);
            }
            // soft clouds
            sg.setColor(0xFFFFFF);
            for (int i = 0; i < 22; i++) {
                int cx = (i*41+7)%230;
                int cy = (i*29+11)%70;
                int w = 18 + (i*11)%28;
                int h = 8 + (i*7)%10;
                sg.fillArc(cx, cy, w, h, 0, 360);
                sg.fillArc(cx+6, cy-3, w-6, h, 0, 360);
                sg.fillArc(cx-4, cy+2, w-8, h, 0, 360);
            }
            // sun glow near top
            sg.setColor(0xFFFFAA);
            sg.fillArc(180, 8, 28, 28, 0, 360);
            sg.setColor(0xFFFFFF);
            sg.fillArc(186, 14, 16, 16, 0, 360);
            skyImage2D = new Image2D(Image2D.RGB, skyLCUI);

            // --- GRASS light, not dark ---
            Image grassLCUI = Image.createImage(64, 64);
            Graphics gg = grassLCUI.getGraphics();
            gg.setColor(0x7CFC00); // lawn green light, was dark 2E8B57
            gg.fillRect(0,0,64,64);
            // lighter blades
            for (int i = 0; i < 200; i++) {
                int x = (i*13+7)%64;
                int y = (i*29+11)%64;
                gg.setColor(0x90EE90);
                gg.drawLine(x, y, x, Math.min(63, y+2));
            }
            // darker variation subtle
            for (int i = 0; i < 100; i++) {
                int x = (i*7+3)%64;
                int y = (i*17+5)%64;
                gg.setColor(0x6B8E23);
                gg.fillRect(x, y, 1, 1);
            }
            // tiny flowers
            for (int i = 0; i < 18; i++) {
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

            // --- WATER texture 64x64 blue with waves ---
            Image waterLCUI = Image.createImage(64, 64);
            Graphics wg = waterLCUI.getGraphics();
            wg.setColor(0x1E90FF);
            wg.fillRect(0,0,64,64);
            wg.setColor(0x87CEEB);
            for (int y=0;y<64;y+=4) {
                for (int x=0;x<64;x+=8) {
                    int off = (int)(Math.sin((x+y+frame*0.1f)*0.2f)*2);
                    wg.drawLine(x, y+off, x+4, y+off);
                }
            }
            wg.setColor(0xFFFFFF);
            for (int i=0;i<30;i++) {
                int x=(i*17)%64; int y=(i*23)%64;
                wg.fillRect(x,y,2,1);
            }
            waterImage2D = new Image2D(Image2D.RGB, waterLCUI);
            waterTexture = new Texture2D(waterImage2D);
            waterTexture.setFiltering(Texture2D.FILTER_LINEAR, Texture2D.FILTER_LINEAR);
            waterTexture.setWrapping(Texture2D.WRAP_REPEAT, Texture2D.WRAP_REPEAT);
            waterTexture.setBlending(Texture2D.FUNC_MODULATE);

            // --- FACE decal 32x32 ---
            Image faceLCUI = Image.createImage(32, 32);
            Graphics fcg = faceLCUI.getGraphics();
            fcg.setColor(0xFFFFFF);
            fcg.fillRect(0,0,32,32);
            fcg.setColor(0x000000);
            fcg.fillArc(2, 2, 12, 12, 0, 360);
            fcg.fillArc(18, 2, 12, 12, 0, 360);
            fcg.setColor(0x33CC33);
            fcg.fillArc(4, 4, 8, 8, 0, 360);
            fcg.fillArc(20, 4, 8, 8, 0, 360);
            fcg.setColor(0x000000);
            fcg.fillArc(6, 6, 4, 5, 0, 360);
            fcg.fillArc(22, 6, 4, 5, 0, 360);
            fcg.setColor(0xFFFFFF);
            fcg.fillArc(7, 7, 2, 2, 0, 360);
            fcg.fillArc(23, 7, 2, 2, 0, 360);
            fcg.setColor(0xFF69B4);
            fcg.fillRect(13, 16, 6, 3);
            fcg.setColor(0x000000);
            fcg.drawLine(0, 14, 8, 14);
            fcg.drawLine(0, 18, 8, 17);
            fcg.drawLine(24, 14, 32, 14);
            fcg.drawLine(24, 17, 32, 18);
            faceImage2D = new Image2D(Image2D.RGB, faceLCUI);
            faceTexture = new Texture2D(faceImage2D);
            faceTexture.setFiltering(Texture2D.FILTER_LINEAR, Texture2D.FILTER_LINEAR);

            // trunk bark 16x16
            Image trunkLCUI = Image.createImage(16,16);
            Graphics tg = trunkLCUI.getGraphics();
            tg.setColor(0x8B4513);
            tg.fillRect(0,0,16,16);
            tg.setColor(0x5D2906);
            for (int x=0;x<16;x+=4) tg.drawLine(x,0,x,16);
            trunkImage2D = new Image2D(Image2D.RGB, trunkLCUI);
            trunkTexture = new Texture2D(trunkImage2D);

            // foliage 32x32
            Image folLCUI = Image.createImage(32,32);
            Graphics flg = folLCUI.getGraphics();
            flg.setColor(0x228B22);
            flg.fillRect(0,0,32,32);
            flg.setColor(0x32CD32);
            for (int i=0;i<60;i++) {
                int x=(i*13)%32; int y=(i*7)%32;
                flg.fillRect(x,y,2,2);
            }
            foliageImage2D = new Image2D(Image2D.RGB, folLCUI);
            foliageTexture = new Texture2D(foliageImage2D);

            // --- Appearances - LIGHT GROUND ---
            groundAppearance = new Appearance();
            groundAppearance.setTexture(0, grassTexture);
            Material gm = new Material();
            gm.setColor(Material.DIFFUSE, 0xFFFFFF); // light, was dark
            gm.setColor(Material.AMBIENT, 0xCCCCCC);
            groundAppearance.setMaterial(gm);
            PolygonMode gpm = new PolygonMode();
            gpm.setShading(PolygonMode.SHADE_SMOOTH);
            gpm.setCulling(PolygonMode.CULL_NONE);
            groundAppearance.setPolygonMode(gpm);

            farGroundAppearance = new Appearance();
            farGroundAppearance.setTexture(0, grassTexture);
            Material fgm = new Material();
            fgm.setColor(Material.DIFFUSE, 0xFFFFFF);
            fgm.setColor(Material.AMBIENT, 0xBBBBBB);
            farGroundAppearance.setMaterial(fgm);
            farGroundAppearance.setPolygonMode(gpm);

            waterAppearance = new Appearance();
            waterAppearance.setTexture(0, waterTexture);
            Material wm = new Material();
            wm.setColor(Material.DIFFUSE, 0xFFFFFF);
            wm.setColor(Material.AMBIENT, 0xAAAAAA);
            waterAppearance.setMaterial(wm);
            waterAppearance.setPolygonMode(gpm);
            CompositingMode wcm = new CompositingMode();
            wcm.setBlending(CompositingMode.ALPHA);
            wcm.setAlphaThreshold(0.1f);
            waterAppearance.setCompositingMode(wcm);

            farWaterAppearance = new Appearance();
            farWaterAppearance.setTexture(0, waterTexture);
            Material fwm = new Material();
            fwm.setColor(Material.DIFFUSE, 0xAACCEE);
            farWaterAppearance.setMaterial(fwm);
            farWaterAppearance.setPolygonMode(gpm);
            farWaterAppearance.setCompositingMode(wcm);

            // CAT - solid colors, no horror texture mapping
            catBodyAppearance = new Appearance();
            Material catMat = new Material();
            catMat.setColor(Material.DIFFUSE, 0xFFA54F); // orange
            catMat.setColor(Material.AMBIENT, 0xFFA54F);
            catMat.setColor(Material.SPECULAR, 0x444444);
            catMat.setShininess(10);
            catBodyAppearance.setMaterial(catMat);
            PolygonMode cpm = new PolygonMode();
            cpm.setShading(PolygonMode.SHADE_SMOOTH);
            catBodyAppearance.setPolygonMode(cpm);

            faceDecalAppearance = new Appearance();
            faceDecalAppearance.setTexture(0, faceTexture);
            Material faceMat = new Material();
            faceMat.setColor(Material.DIFFUSE, 0xFFFFFF);
            faceDecalAppearance.setMaterial(faceMat);
            faceDecalAppearance.setPolygonMode(cpm);
            CompositingMode faceCM = new CompositingMode();
            faceCM.setBlending(CompositingMode.ALPHA);
            faceDecalAppearance.setCompositingMode(faceCM);

            catWhiteAppearance = new Appearance();
            Material whiteMat = new Material();
            whiteMat.setColor(Material.DIFFUSE, 0xFFFFFF);
            whiteMat.setColor(Material.AMBIENT, 0xFFFFFF);
            catWhiteAppearance.setMaterial(whiteMat);
            catWhiteAppearance.setPolygonMode(cpm);

            catPinkAppearance = new Appearance();
            Material pinkMat = new Material();
            pinkMat.setColor(Material.DIFFUSE, 0xFFB6C1);
            pinkMat.setColor(Material.AMBIENT, 0xFFB6C1);
            catPinkAppearance.setMaterial(pinkMat);
            catPinkAppearance.setPolygonMode(cpm);

            shadowAppearance = new Appearance();
            CompositingMode scm = new CompositingMode();
            scm.setBlending(CompositingMode.ALPHA);
            shadowAppearance.setCompositingMode(scm);
            Material sm = new Material();
            sm.setColor(Material.DIFFUSE, 0x33000000);
            shadowAppearance.setMaterial(sm);
            shadowAppearance.setPolygonMode(cpm);

            Appearance trunkApp = new Appearance();
            trunkApp.setTexture(0, trunkTexture);
            trunkApp.setMaterial(gm);
            trunkApp.setPolygonMode(gpm);

            Appearance folApp = new Appearance();
            folApp.setTexture(0, foliageTexture);
            folApp.setMaterial(gm);
            folApp.setPolygonMode(gpm);

            // --- HILLY TERRAIN 40x40 with 10x10 segments ---
            groundHillyMesh = createHillyTerrain(40.0f, 10, 1.8f, groundAppearance);
            farGroundMesh = createHillyTerrain(80.0f, 8, 2.5f, farGroundAppearance);
            Transform farT = new Transform();
            farT.postTranslate(0, -0.3f, 0);
            farGroundMesh.setTransform(farT);

            // --- RIVER winding through meadow ---
            riverMesh = createRiver(22.0f, 14, 2.2f, 0.0f, waterAppearance);
            Transform riverT = new Transform();
            riverT.postTranslate(0, -0.15f, 0);
            riverMesh.setTransform(riverT);

            // Far river in distance
            farRiverMesh = createRiver(30.0f, 10, 4.0f, -18.0f, farWaterAppearance);
            Transform farRiverT = new Transform();
            farRiverT.postTranslate(0, -0.4f, -12.0f);
            farRiverMesh.setTransform(farRiverT);

            worldGroup = new Group();
            worldGroup.addChild(groundHillyMesh);
            worldGroup.addChild(farGroundMesh);
            worldGroup.addChild(riverMesh);
            worldGroup.addChild(farRiverMesh);

            // --- TREES normally placed - not circle, natural clusters ---
            // Use pseudo-random but avoid river (x near 0)
            int treeCount = 0;
            float[][] treePos = {
                {-8, -6}, {-10, -2}, {-9, 3}, {-7, 8}, {-5, 12},
                {6, 10}, {9, 6}, {11, 1}, {10, -4}, {8, -8},
                {-3, -10}, {3, -11}, {-12, 5}, {12, 9}, {-4, 14}, {5, 13}
            };
            for (int i = 0; i < treePos.length && treeCount < trees.length; i++) {
                float x = treePos[i][0];
                float z = treePos[i][1];
                // avoid river center: river is around x = sin(z*0.2)*3, width 2.2
                float riverX = (float)Math.sin(z*0.2f)*3.0f;
                if (Math.abs(x - riverX) < 2.5f) continue; // skip too close to river
                // height from hilly terrain approx
                float y = getTerrainHeight(x, z, 1.8f);
                Group tree = createTree(trunkApp, folApp, 1.6f + (i%4)*0.5f);
                Transform tt = new Transform();
                tt.postTranslate(x, y, z);
                tree.setTransform(tt);
                trees[treeCount++] = tree;
                worldGroup.addChild(tree);
            }

            // Flowers near river and meadow
            for (int i = 0; i < flowers.length; i++) {
                float ang = (i*137.5f)*3.14159f/180.0f;
                float r = 2.5f + (i*0.7f)%9.0f;
                float x = (float)Math.sin(ang)*r;
                float z = (float)Math.cos(ang)*r;
                if (Math.abs(x) < 1.2f && Math.abs(z) < 1.2f) continue;
                float riverX = (float)Math.sin(z*0.2f)*3.0f;
                if (Math.abs(x - riverX) < 1.0f) continue; // not in river
                float y = getTerrainHeight(x, z, 1.8f);
                Group flower = new Group();
                Mesh stem = createBox(0.04f, 0.35f, 0.04f, trunkApp, "stem");
                Transform stemT = new Transform();
                stemT.postTranslate(0, 0.18f, 0);
                stem.setTransform(stemT);
                flower.addChild(stem);
                Appearance flowerApp = new Appearance();
                Material fm = new Material();
                int col = (i%4==0)?0xFF69B4:(i%4==1)?0xFFFF00:(i%4==2)?0xFF00FF:0xFFFFFF;
                fm.setColor(Material.DIFFUSE, col);
                flowerApp.setMaterial(fm);
                flowerApp.setPolygonMode(gpm);
                Mesh bloom = createBox(0.22f, 0.08f, 0.22f, flowerApp, "bloom");
                Transform bloomT = new Transform();
                bloomT.postTranslate(0, 0.38f, 0);
                bloom.setTransform(bloomT);
                flower.addChild(bloom);
                Transform fT = new Transform();
                fT.postTranslate(x, y, z);
                flower.setTransform(fT);
                flowers[i] = flower;
                worldGroup.addChild(flower);
            }

            // Butterflies
            for (int i = 0; i < 3; i++) {
                Group bf = new Group();
                Appearance bfApp = new Appearance();
                Material bfm = new Material();
                int col = (i==0)?0xFF69B4:(i==1)?0x00FFFF:0xFFFF00;
                bfm.setColor(Material.DIFFUSE, col);
                bfApp.setMaterial(bfm);
                bfApp.setPolygonMode(gpm);
                Mesh wingL = createBox(0.28f, 0.02f, 0.18f, bfApp, "wing");
                Mesh wingR = createBox(0.28f, 0.02f, 0.18f, bfApp, "wing");
                Transform wLT = new Transform();
                wLT.postTranslate(-0.16f, 0, 0);
                wingL.setTransform(wLT);
                Transform wRT = new Transform();
                wRT.postTranslate(0.16f, 0, 0);
                wingR.setTransform(wRT);
                bf.addChild(wingL);
                bf.addChild(wingR);
                butterflies[i] = bf;
                worldGroup.addChild(bf);
            }

            // Sun sprite - fixed
            Image sunLCUI = Image.createImage(32,32);
            Graphics sunG = sunLCUI.getGraphics();
            sunG.setColor(0xFFFF00);
            sunG.fillArc(0,0,32,32,0,360);
            sunG.setColor(0xFFFFFF);
            sunG.fillArc(8,8,16,16,0,360);
            sunG.setColor(0xFFD700);
            sunG.fillArc(10,10,12,12,0,360);
            sunImage2D = new Image2D(Image2D.RGBA, sunLCUI);
            Texture2D sunTex = new Texture2D(sunImage2D);
            sunTex.setFiltering(Texture2D.FILTER_LINEAR, Texture2D.FILTER_LINEAR);
            Appearance sunApp = new Appearance();
            sunApp.setTexture(0, sunTex);
            CompositingMode sunCM = new CompositingMode();
            sunCM.setBlending(CompositingMode.ALPHA);
            sunApp.setCompositingMode(sunCM);
            sunSprite = new Sprite3D(true, sunImage2D, sunApp);
            sunSprite.setScale(6.0f, 6.0f, 6.0f);
            Transform sunT = new Transform();
            sunT.postTranslate(15, 12, -20);
            sunSprite.setTransform(sunT);
            worldGroup.addChild(sunSprite);

            // --- CAT with fixed mapping solid colors ---
            pawMesh = createBox(0.22f, 0.16f, 0.22f, catWhiteAppearance, "paw");
            bodyMesh = createBox(1.25f, 0.6f, 0.55f, catBodyAppearance, "body");
            headMesh = createBox(0.6f, 0.55f, 0.6f, catBodyAppearance, "head");
            earLeftMesh = createBox(0.16f, 0.26f, 0.12f, catPinkAppearance, "earL");
            earRightMesh = createBox(0.16f, 0.26f, 0.12f, catPinkAppearance, "earR");
            tailBaseMesh = createBox(0.16f, 0.16f, 0.55f, catBodyAppearance, "tailBase");
            tailTipMesh = createBox(0.12f, 0.12f, 0.45f, catBodyAppearance, "tailTip");
            shadowMesh = createPlane(1.5f, shadowAppearance, 1.0f);
            faceDecalMesh = createPlaneWithUV(0.45f, faceDecalAppearance, 0, 0, 1, 1);

            catRoot = new Group();
            catBodyGroup = new Group();

            Transform bodyT = new Transform();
            bodyT.postTranslate(0, 0.55f, 0);
            bodyMesh.setTransform(bodyT);
            catBodyGroup.addChild(bodyMesh);

            headGroup = new Group();
            Transform headPos = new Transform();
            headPos.postTranslate(0.82f, 0.62f, 0);
            headGroup.setTransform(headPos);
            headGroup.addChild(headMesh);
            Transform faceT = new Transform();
            faceT.postTranslate(0.31f, 0.05f, 0);
            faceT.postRotate(90, 0,1,0);
            faceDecalMesh.setTransform(faceT);
            headGroup.addChild(faceDecalMesh);
            Transform earLPos = new Transform();
            earLPos.postTranslate(0.1f, 0.36f, 0.16f);
            earLeftMesh.setTransform(earLPos);
            headGroup.addChild(earLeftMesh);
            Transform earRPos = new Transform();
            earRPos.postTranslate(0.1f, 0.36f, -0.16f);
            earRightMesh.setTransform(earRPos);
            headGroup.addChild(earRightMesh);
            catBodyGroup.addChild(headGroup);

            tailGroup = new Group();
            Transform tailPivot = new Transform();
            tailPivot.postTranslate(-0.62f, 0.52f, 0);
            tailGroup.setTransform(tailPivot);
            Transform tailBaseT = new Transform();
            tailBaseT.postTranslate(-0.27f, 0, 0);
            tailBaseMesh.setTransform(tailBaseT);
            tailGroup.addChild(tailBaseMesh);

            tailTipGroup = new Group();
            Transform tipPivot = new Transform();
            tipPivot.postTranslate(-0.55f, 0, 0);
            tailTipGroup.setTransform(tipPivot);
            Transform tipT = new Transform();
            tipT.postTranslate(-0.22f, 0, 0);
            tailTipMesh.setTransform(tipT);
            tailTipGroup.addChild(tailTipMesh);
            tailGroup.addChild(tailTipGroup);
            catBodyGroup.addChild(tailGroup);

            legFLGroup = createLegWithPaw(0.48f, 0.0f, 0.22f);
            legFRGroup = createLegWithPaw(0.48f, 0.0f, -0.22f);
            legBLGroup = createLegWithPaw(-0.48f, 0.0f, 0.22f);
            legBRGroup = createLegWithPaw(-0.48f, 0.0f, -0.22f);

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

            camera = new Camera();
            camera.setPerspective(52.0f, 1.33f, 0.3f, 70.0f);

            sunLight = new Light();
            sunLight.setMode(Light.DIRECTIONAL);
            sunLight.setColor(0xFFE4B5);
            sunLight.setIntensity(1.4f);

            fillLight = new Light();
            fillLight.setMode(Light.DIRECTIONAL);
            fillLight.setColor(0xADD8E6);
            fillLight.setIntensity(0.5f);

            ambientLight = new Light();
            ambientLight.setMode(Light.AMBIENT);
            ambientLight.setColor(0x888888);
            ambientLight.setIntensity(0.8f);

            background = new Background();
            background.setColor(0x87CEEB);
            Background skyBg = new Background();
            skyBg.setColor(0x87CEEB);
            skyBg.setImage(skyImage2D);
            skyBg.setImageMode(Background.BORDER, Background.BORDER);
            skyBg.setCrop(0, 0, 256, 90);
            background = skyBg;

            fog = new Fog();
            fog.setMode(Fog.LINEAR);
            fog.setColor(0xC8E6FF);
            fog.setLinear(18.0f, 45.0f);
            groundAppearance.setFog(fog);
            farGroundAppearance.setFog(fog);
            waterAppearance.setFog(fog);
            farWaterAppearance.setFog(fog);
            catBodyAppearance.setFog(fog);
            catWhiteAppearance.setFog(fog);
        }

        private float getTerrainHeight(float x, float z, float scale) {
            // same formula as hilly terrain
            return (float)(Math.sin(x*0.2f)*Math.cos(z*0.2f)*scale*0.5f + Math.sin(x*0.1f)*Math.cos(z*0.15f)*scale*0.3f);
        }

        private Group createTree(Appearance trunkApp, Appearance folApp, float height) throws Exception {
            Group tree = new Group();
            float trunkH = height;
            Mesh trunk = createBox(0.22f, trunkH, 0.22f, trunkApp, "trunk");
            Transform trunkT = new Transform();
            trunkT.postTranslate(0, trunkH/2.0f, 0);
            trunk.setTransform(trunkT);
            tree.addChild(trunk);
            for (int i = 0; i < 3; i++) {
                float sz = 1.1f - i*0.18f;
                Mesh fol = createBox(sz, sz*0.65f, sz, folApp, "fol");
                Transform folT = new Transform();
                folT.postTranslate(0, trunkH + 0.25f + i*0.45f, 0);
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
            Mesh upper = createBox(0.22f, 0.45f, 0.22f, catBodyAppearance, "upperLeg");
            Transform upT = new Transform();
            upT.postTranslate(0, -0.22f, 0);
            upper.setTransform(upT);
            hip.addChild(upper);
            Group pawGroup = new Group();
            Transform pawPivot = new Transform();
            pawPivot.postTranslate(0, -0.45f, 0);
            pawGroup.setTransform(pawPivot);
            Mesh paw = (Mesh) pawMesh.duplicate();
            Transform pawT = new Transform();
            pawT.postTranslate(0, -0.08f, 0);
            paw.setTransform(pawT);
            pawGroup.addChild(paw);
            hip.addChild(pawGroup);
            return hip;
        }

        private Mesh createHillyTerrain(float size, int segments, float heightScale, Appearance ap) throws Exception {
            int vertsX = segments + 1;
            int vertsZ = segments + 1;
            int vertCount = vertsX * vertsZ;
            float[] pos = new float[vertCount * 3];
            byte[] norm = new byte[vertCount * 3];
            short[] tex = new short[vertCount * 2];
            int idx = 0;
            int tIdx = 0;
            float half = size / 2.0f;
            float step = size / segments;
            for (int z = 0; z < vertsZ; z++) {
                for (int x = 0; x < vertsX; x++) {
                    float fx = -half + x * step;
                    float fz = -half + z * step;
                    float fy = getTerrainHeight(fx, fz, heightScale);
                    pos[idx++] = fx;
                    pos[idx++] = fy;
                    pos[idx++] = fz;
                    // normal up, will be smoothed
                    norm[(z*vertsX + x)*3] = 0;
                    norm[(z*vertsX + x)*3+1] = 127;
                    norm[(z*vertsX + x)*3+2] = 0;
                    // tex repeat
                    float u = (x / (float)segments) * 8.0f;
                    float v = (z / (float)segments) * 8.0f;
                    tex[tIdx++] = (short)(u*1000);
                    tex[tIdx++] = (short)(v*1000);
                }
            }
            VertexArray posArray = new VertexArray(vertCount, 3, 2);
            short[] ps = new short[vertCount*3];
            for (int i=0;i<vertCount*3;i++) ps[i]=(short)(pos[i]*1000);
            posArray.set(0, vertCount, ps);
            VertexArray normArray = new VertexArray(vertCount, 3, 1);
            normArray.set(0, vertCount, norm);
            VertexArray texArray = new VertexArray(vertCount, 2, 2);
            texArray.set(0, vertCount, tex);
            VertexBuffer vb = new VertexBuffer();
            vb.setPositions(posArray, 0.001f, new float[]{0,0,0});
            vb.setNormals(normArray);
            vb.setTexCoords(0, texArray, 0.001f, new float[]{0,0,0});

            int quadCount = segments * segments;
            int[] indices = new int[quadCount * 6];
            int p = 0;
            for (int z = 0; z < segments; z++) {
                for (int x = 0; x < segments; x++) {
                    int v0 = z * vertsX + x;
                    int v1 = v0 + 1;
                    int v2 = v0 + vertsX;
                    int v3 = v2 + 1;
                    indices[p++] = v0; indices[p++] = v2; indices[p++] = v1;
                    indices[p++] = v1; indices[p++] = v2; indices[p++] = v3;
                }
            }
            IndexBuffer ib = new TriangleStripArray(indices, new int[quadCount*2]);
            for (int i=0;i<quadCount*2;i++) ((TriangleStripArray)ib).getClass(); // dummy to avoid warning
            // Actually need strip lengths array: each quad 2 triangles = 2 strips of 3? Use 3 per triangle
            int[] strips = new int[quadCount*2];
            for (int i=0;i<strips.length;i++) strips[i]=3;
            ib = new TriangleStripArray(indices, strips);
            return new Mesh(vb, ib, ap);
        }

        private Mesh createRiver(float length, int segments, float width, float zOffset, Appearance ap) throws Exception {
            int verts = (segments+1)*2;
            float[] pos = new float[verts*3];
            byte[] norm = new byte[verts*3];
            short[] tex = new short[verts*2];
            int pIdx=0, tIdx=0;
            float segLen = length / segments;
            float halfLen = length/2.0f;
            for (int i=0;i<=segments;i++) {
                float fz = -halfLen + i*segLen + zOffset;
                float fxCenter = (float)Math.sin(fz*0.2f)*3.0f;
                float fxLeft = fxCenter - width*0.5f;
                float fxRight = fxCenter + width*0.5f;
                float fy = -0.12f; // slightly below ground
                // left
                pos[pIdx++] = fxLeft; pos[pIdx++] = fy; pos[pIdx++] = fz;
                // right
                pos[pIdx++] = fxRight; pos[pIdx++] = fy; pos[pIdx++] = fz;

                norm[(i*2)*3+1]=127;
                norm[(i*2+1)*3+1]=127;

                float v = (i/(float)segments)*6.0f;
                tex[tIdx++] = 0; tex[tIdx++] = (short)(v*1000);
                tex[tIdx++] = (short)(4*1000); tex[tIdx++] = (short)(v*1000);
            }
            VertexArray posA = new VertexArray(verts,3,2);
            short[] ps = new short[verts*3];
            for (int i=0;i<verts*3;i++) ps[i]=(short)(pos[i]*1000);
            posA.set(0, verts, ps);
            VertexArray normA = new VertexArray(verts,3,1);
            normA.set(0, verts, norm);
            VertexArray texA = new VertexArray(verts,2,2);
            texA.set(0, verts, tex);
            VertexBuffer vb = new VertexBuffer();
            vb.setPositions(posA,0.001f,new float[]{0,0,0});
            vb.setNormals(normA);
            vb.setTexCoords(0,texA,0.001f,new float[]{0,0,0});

            int[] indices = new int[segments*6];
            int ip=0;
            for (int i=0;i<segments;i++) {
                int v0=i*2, v1=v0+1, v2=v0+2, v3=v0+3;
                indices[ip++]=v0; indices[ip++]=v2; indices[ip++]=v1;
                indices[ip++]=v1; indices[ip++]=v2; indices[ip++]=v3;
            }
            int[] strips = new int[segments*2];
            for (int i=0;i<strips.length;i++) strips[i]=3;
            IndexBuffer ib = new TriangleStripArray(indices, strips);
            return new Mesh(vb, ib, ap);
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
            return new Mesh(vb, ib, ap);
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

        private Mesh createPlaneWithUV(float size, Appearance ap, float u0, float v0, float u1, float v1) throws Exception {
            float hs = size/2.0f;
            float[] pos = new float[]{ -hs,0,-hs, hs,0,-hs, hs,0,hs, -hs,0,hs };
            byte[] norm = new byte[]{ 0,127,0, 0,127,0, 0,127,0, 0,127,0 };
            short[] tex = new short[]{
                (short)(u0*1000), (short)(v0*1000),
                (short)(u1*1000), (short)(v0*1000),
                (short)(u1*1000), (short)(v1*1000),
                (short)(u0*1000), (short)(v1*1000)
            };
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
                            float radius = 4.5f;
                            float ang = frame * 0.016f;
                            catX = (float)Math.sin(ang) * radius;
                            catZ = (float)Math.cos(ang) * radius;
                            catDirRad = ang;
                            float swing = (float)Math.sin(runCycle) * 0.62f;
                            float swing2 = (float)Math.sin(runCycle + 3.14159f) * 0.62f;
                            try {
                                Transform tFL = new Transform();
                                tFL.postTranslate(0.48f, 0.0f, 0.22f);
                                tFL.postRotate((float)Math.toDegrees(swing), 0,0,1);
                                legFLGroup.setTransform(tFL);
                                Transform tFR = new Transform();
                                tFR.postTranslate(0.48f, 0.0f, -0.22f);
                                tFR.postRotate((float)Math.toDegrees(swing2), 0,0,1);
                                legFRGroup.setTransform(tFR);
                                Transform tBL = new Transform();
                                tBL.postTranslate(-0.48f, 0.0f, 0.22f);
                                tBL.postRotate((float)Math.toDegrees(swing2), 0,0,1);
                                legBLGroup.setTransform(tBL);
                                Transform tBR = new Transform();
                                tBR.postTranslate(-0.48f, 0.0f, -0.22f);
                                tBR.postRotate((float)Math.toDegrees(swing), 0,0,1);
                                legBRGroup.setTransform(tBR);

                                Transform tailT = new Transform();
                                tailT.postTranslate(-0.62f, 0.52f, 0);
                                tailT.postRotate((float)Math.sin(runCycle*1.6f)*20, 0,1,0);
                                tailT.postRotate((float)Math.sin(runCycle*0.7f)*8, 0,0,1);
                                tailGroup.setTransform(tailT);

                                Transform tipT = new Transform();
                                tipT.postTranslate(-0.55f, 0, 0);
                                tipT.postRotate((float)Math.sin(runCycle*1.6f+0.5f)*16, 0,1,0);
                                tailTipGroup.setTransform(tipT);

                                Transform bodyBob = new Transform();
                                bodyBob.postTranslate(0, (float)Math.abs(Math.sin(runCycle))*0.08f, 0);
                                bodyBob.postRotate((float)Math.sin(runCycle)*2.5f, 0,0,1);
                                catBodyGroup.setTransform(bodyBob);

                                Transform headLook = new Transform();
                                headLook.postTranslate(0.82f, 0.62f, 0);
                                headLook.postRotate((float)Math.sin(frame*0.04f)*7, 0,1,0);
                                headGroup.setTransform(headLook);

                                for (int i=0;i<3;i++) {
                                    Group bf = butterflies[i];
                                    float bAng = frame*0.018f + i*2.1f;
                                    float bx = (float)Math.sin(bAng)*(3.5f+i) + catX*0.25f;
                                    float bz = (float)Math.cos(bAng*0.7f)*(3.5f+i) + catZ*0.25f;
                                    float by = 1.3f + (float)Math.sin(bAng*1.2f)*0.7f + i*0.25f;
                                    Transform bT = new Transform();
                                    bT.postTranslate(bx, by, bz);
                                    bT.postRotate(frame*4 + i*28, 0,1,0);
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
            try { if (midiPlayer != null) midiPlayer.stop(); } catch (Throwable t) {}
        }

        protected void paint(Graphics g) {
            int w = getWidth();
            int h = getHeight();

            if (g3d == null || worldGroup == null || catRoot == null) {
                g.setColor(0x000000);
                g.fillRect(0,0,w,h);
                return;
            }

            try {
                g3d.bindTarget(g);
                try {
                    g3d.setViewport(0, 0, w, h);
                    g3d.clear(background);

                    float camRadius = 10.0f;
                    float camAng = frame * 0.007f;
                    float camX = (float)Math.sin(camAng) * 3.0f;
                    float camZ = camRadius + (float)Math.cos(camAng) * 1.5f;
                    float camY = 3.6f + (float)Math.sin(camAng*0.5f)*0.5f;

                    float targetX = catX * 0.5f;
                    float targetZ = catZ * 0.5f;
                    float targetY = 0.35f;

                    float dx = targetX - camX;
                    float dy = targetY - camY;
                    float dz = targetZ - camZ;
                    float lenXZ = (float)Math.sqrt(dx*dx + dz*dz);
                    float yaw = toDegrees(atan2(-dx, -dz));
                    float pitch = toDegrees(atan2(dy, lenXZ));

                    camTransform.setIdentity();
                    camTransform.postTranslate(camX, camY, camZ);
                    camTransform.postRotate(yaw, 0,1,0);
                    camTransform.postRotate(pitch, 1,0,0);

                    camera.setPerspective(52.0f, (float)w/h, 0.3f, 70.0f);
                    g3d.setCamera(camera, camTransform);

                    Transform sunLT = new Transform();
                    sunLT.postTranslate(10, 15, -10);
                    g3d.addLight(sunLight, sunLT);

                    Transform fillLT = new Transform();
                    fillLT.postTranslate(-8, 5, 8);
                    g3d.addLight(fillLight, fillLT);

                    g3d.addLight(ambientLight, new Transform());

                    Transform worldT = new Transform();
                    g3d.render(worldGroup, worldT);

                    Transform catRootT = new Transform();
                    // cat follows terrain height
                    float terrainY = getTerrainHeight(catX, catZ, 1.8f);
                    catRootT.postTranslate(catX, terrainY, catZ);
                    catRootT.postRotate((float)Math.toDegrees(catDirRad), 0,1,0);
                    g3d.render(catRoot, catRootT);

                    g3d.resetLights();

                } finally {
                    g3d.releaseTarget();
                }
                // No text overlay - user requested remove

            } catch (Throwable t) {
                g.setColor(0x000000);
                g.fillRect(0,0,w,h);
            }
        }

        private static final float PI = 3.1415926535f;
        private static final float PI_2 = PI * 0.5f;
        private static final float PI_4 = PI * 0.25f;

        private static float toDegrees(float rad) {
            return rad * 57.2957795f;
        }

        private static float atan(float x) {
            boolean neg = x < 0;
            float ax = neg ? -x : x;
            float r;
            if (ax <= 1.0f) {
                r = PI_4 * ax - ax * (ax - 1.0f) * (0.2447f + 0.0663f * ax);
            } else {
                float inv = 1.0f / ax;
                float atanInv = PI_4 * inv - inv * (inv - 1.0f) * (0.2447f + 0.0663f * inv);
                r = PI_2 - atanInv;
            }
            return neg ? -r : r;
        }

        private static float atan2(float y, float x) {
            if (x == 0.0f) {
                if (y > 0.0f) return PI_2;
                if (y < 0.0f) return -PI_2;
                return 0.0f;
            }
            float a = atan(y / x);
            if (x < 0.0f) {
                if (y >= 0.0f) a += PI;
                else a -= PI;
            }
            return a;
        }
    }
}
