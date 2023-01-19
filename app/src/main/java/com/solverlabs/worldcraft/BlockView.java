package com.solverlabs.worldcraft;

import static android.opengl.GLES10.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES10.GL_DEPTH_BUFFER_BIT;

import android.opengl.GLES10;
import android.view.KeyEvent;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.Game;
import com.solverlabs.droid.rugl.gl.StackedRenderer;
import com.solverlabs.droid.rugl.text.TextShape;
import com.solverlabs.droid.rugl.util.Colour;
import com.solverlabs.droid.rugl.util.FPSCamera;
import com.solverlabs.droid.rugl.util.geom.Vector2f;
import com.solverlabs.worldcraft.blockentity.BlockEntityPainter;
import com.solverlabs.worldcraft.factories.BlockFactory;
import com.solverlabs.worldcraft.factories.ItemFactory;
import com.solverlabs.worldcraft.mob.MobPainter;
import com.solverlabs.worldcraft.ui.GUI;
import com.solverlabs.worldcraft.ui.Hand;


public class BlockView {
    public static final boolean DRAW_FPS = false;
    public static final long FPS = 80;
    private static final int FPS_COUNT = 10;
    private static final long TIME_BETWEEN_FRAMES = 12;
    private final StackedRenderer r = new StackedRenderer();
    private final long[] lastFpss = new long[10];
    public boolean complete;
    public BlockEntityPainter entityPainter;
    public GUI gui;
    public MobPainter mobAggregator;
    public Player mPlayer;
    public World mWorld;
    public FPSCamera mCam = new FPSCamera();
    public int mSkyColour = Colour.packFloat(0.7f, 0.7f, 0.9f, 1.0f);
    public CharactersPainter charactersPainter = new CharactersPainter();
    private TextShape fpsShape;
    private long fpsVar;
    private long lastDrawAt;

    public BlockView(World world) {
        mWorld = world;
        mPlayer = new Player(world);
    }

    public void init(Game game) {
        Game.mLogicAdvance = 0.016666668f;
        BlockFactory.loadTexture();
        ItemFactory.loadTexture();
        SkinFactory.loadTexture();
        Hand.loadTexture();
        MobPainter.loadTexture(mWorld);
        DroppableItem.init();
        BlockEntityPainter.init();
        BlockParticle.init();
        mobAggregator = new MobPainter();
        entityPainter = new BlockEntityPainter();
        gui = new GUI(mPlayer, mWorld, mCam, mobAggregator, entityPainter, game);
        mPlayer.init(mWorld.getmLevelTag());
        mWorld.init(mPlayer, entityPainter);
        Clouds.getInstance().init(mWorld);
        BlockFactory.state.fog.setFA(mSkyColour);
    }

    public void setCamRotation(@NonNull Vector2f rot) {
        mCam.setHeading(rot.x);
        mCam.setElevation(rot.y);
    }

    public void openGLinit() {
        int skyColour = mSkyColour;
        GLES10.glClearColor(Colour.redf(skyColour), Colour.greenf(skyColour), Colour.bluef(skyColour), Colour.alphaf(skyColour));
    }

    public void setmSkyColour(int skyColour) {
        mSkyColour = skyColour;
        GLES10.glClearColor(Colour.redf(skyColour), Colour.greenf(skyColour), Colour.bluef(skyColour), Colour.alphaf(skyColour));
        BlockFactory.state.fog.setFA(skyColour);
    }

    public void advance(float delta) {
        final FPSCamera cam = mCam;
        // steering
        gui.advance(delta, cam);
        final World world = mWorld;
        final Player player = mPlayer;
        if (world.isChunksInited()) {
            // movement
            player.advance(delta, cam, gui);
        }
        charactersPainter.advance(delta, world.loadradius, cam);
        charactersPainter.advance(delta, world.loadradius, cam);
        // chunk loading
        world.advance();
        int currentSkyColour = world.getSkyColour();
        if (mSkyColour != currentSkyColour) {
            setmSkyColour(currentSkyColour);
        }
        mobAggregator.advance(delta, world, cam, player);
        entityPainter.advance(delta, world, cam, player);
    }

    public void draw() {
        try {
            long timeAfterDraw = System.currentTimeMillis() - lastDrawAt;
            long timeToSleep = TIME_BETWEEN_FRAMES - timeAfterDraw;
            if (timeToSleep > 0) {
                try {
                    Thread.sleep(timeToSleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            GLES10.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            mCam.setPosition(mPlayer.position.x, mPlayer.position.y, mPlayer.position.z);
            mWorld.draw(mPlayer.position, mCam);
            charactersPainter.draw(mPlayer.position, mWorld.loadradius, mCam);
            entityPainter.draw(mPlayer.position, mWorld.loadradius, mCam);
            mobAggregator.draw(mPlayer.position, mWorld.loadradius, mCam);
            gui.draw();
            lastDrawAt = System.currentTimeMillis();
        } catch (OutOfMemoryError e2) {
            complete = true;
            System.runFinalization();
            Runtime.getRuntime().gc();
            System.gc();
        }
    }

    private long fpsAvg() {
        long sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += lastFpss[i];
        }
        return sum / 10;
    }

    private void setLastFps(long fpsVar) {
        for (int i = 0; i < 9; i++) {
            lastFpss[i] = lastFpss[i + 1];
        }
        lastFpss[9] = fpsVar;
    }

    private void drawFPS() {
        long drawDuration = System.currentTimeMillis() - lastDrawAt;
        fpsVar = drawDuration == 0 ? 99L : 1000 / drawDuration;
        setLastFps(fpsVar);
        fpsShape = GUI.getFont().buildTextShape("fps:  " + fpsAvg(), Colour.packFloat(1.0f, 1.0f, 1.0f, 1.0f));
        fpsShape.translate(100.0f, 100.0f, 0.0f);
        fpsShape.render(r);
        r.render();
    }

    public BlockView next() {
        return null;
    }

    public void onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4) {
        }
    }

    public void onKeyUp(int keyCode, KeyEvent event) {
    }

    public void complete(boolean neewSave) {
        if (neewSave) {
            mWorld.save();
        }
        complete = true;
    }

    public void unload() {
        mWorld.clearCache();
    }

    public void saveWorld(String worldName) {
        mWorld.saveAs(worldName);
    }

    public void resetPlayerLocation() {
        if (mPlayer != null) {
            mPlayer.resetSavedPosition();
        }
    }

    public void destroyWorld() {
        mWorld.destroy();
        mWorld = null;
        gui = null;
        mCam = null;
        mPlayer = null;
        charactersPainter = null;
    }

    public boolean isWorldReady() {
        return mWorld != null && mWorld.isReady();
    }
}
