package com.solverlabs.worldcraft;

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
    public Player player;
    public World world;
    public FPSCamera cam = new FPSCamera();
    public int skyColour = Colour.packFloat(0.7f, 0.7f, 0.9f, 1.0f);
    public CharactersPainter charactersPainter = new CharactersPainter();
    private TextShape fpsShape;
    private long fpsVar;
    private long lastDrawAt;

    public BlockView(World world) {
        this.world = world;
        this.player = new Player(world);
    }

    public void init(Game game) {
        Game.logicAdvance = 0.016666668f;
        BlockFactory.loadTexture();
        ItemFactory.loadTexture();
        SkinFactory.loadTexture();
        Hand.loadTexture();
        MobPainter.loadTexture(this.world);
        DroppableItem.init();
        BlockEntityPainter.init();
        BlockParticle.init();
        this.mobAggregator = new MobPainter();
        this.entityPainter = new BlockEntityPainter();
        this.gui = new GUI(this.player, this.world, this.cam, this.mobAggregator, this.entityPainter, game);
        this.player.init(this.world.getLevelTag());
        this.world.init(this.player, this.entityPainter);
        Clouds.getInstance().init(this.world);
        BlockFactory.state.fog.setFA(this.skyColour);
    }

    public void setCamRotation(@NonNull Vector2f rot) {
        this.cam.setHeading(rot.x);
        this.cam.setElevation(rot.y);
    }

    public void openGLinit() {
        GLES10.glClearColor(Colour.redf(this.skyColour), Colour.greenf(this.skyColour), Colour.bluef(this.skyColour), Colour.alphaf(this.skyColour));
    }

    public void setSkyColour(int skyColour) {
        this.skyColour = skyColour;
        GLES10.glClearColor(Colour.redf(skyColour), Colour.greenf(skyColour), Colour.bluef(skyColour), Colour.alphaf(skyColour));
        BlockFactory.state.fog.setFA(skyColour);
    }

    public void advance(float delta) {
        this.gui.advance(delta, this.cam);
        if (this.world.isChunksInited()) {
            this.player.advance(delta, this.cam, this.gui);
        }
        this.charactersPainter.advance(delta, this.world.loadradius, this.cam);
        this.charactersPainter.advance(delta, this.world.loadradius, this.cam);
        this.world.advance();
        int currentSkyColour = this.world.getSkyColour();
        if (this.skyColour != currentSkyColour) {
            setSkyColour(currentSkyColour);
        }
        this.mobAggregator.advance(delta, this.world, this.cam, this.player);
        this.entityPainter.advance(delta, this.world, this.cam, this.player);
    }

    public void draw() {
        try {
            long timeAfterDraw = System.currentTimeMillis() - this.lastDrawAt;
            long timeToSleep = TIME_BETWEEN_FRAMES - timeAfterDraw;
            if (timeToSleep > 0) {
                try {
                    Thread.sleep(timeToSleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            GLES10.glClear(16640);
            this.cam.setPosition(this.player.position.x, this.player.position.y, this.player.position.z);
            this.world.draw(this.player.position, this.cam);
            this.charactersPainter.draw(this.player.position, this.world.loadradius, this.cam);
            this.entityPainter.draw(this.player.position, this.world.loadradius, this.cam);
            this.mobAggregator.draw(this.player.position, this.world.loadradius, this.cam);
            this.gui.draw();
            this.lastDrawAt = System.currentTimeMillis();
        } catch (OutOfMemoryError e2) {
            this.complete = true;
            System.runFinalization();
            Runtime.getRuntime().gc();
            System.gc();
        }
    }

    private long fpsAvg() {
        long sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += this.lastFpss[i];
        }
        return sum / 10;
    }

    private void setLastFps(long fpsVar) {
        for (int i = 0; i < 9; i++) {
            this.lastFpss[i] = this.lastFpss[i + 1];
        }
        this.lastFpss[9] = fpsVar;
    }

    private void drawFPS() {
        long drawDuration = System.currentTimeMillis() - this.lastDrawAt;
        this.fpsVar = drawDuration == 0 ? 99L : 1000 / drawDuration;
        setLastFps(this.fpsVar);
        this.fpsShape = GUI.getFont().buildTextShape("fps:  " + fpsAvg(), Colour.packFloat(1.0f, 1.0f, 1.0f, 1.0f));
        this.fpsShape.translate(100.0f, 100.0f, 0.0f);
        this.fpsShape.render(this.r);
        this.r.render();
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
            this.world.save();
        }
        this.complete = true;
    }

    public void unload() {
        this.world.clearCache();
    }

    public void saveWorld(String worldName) {
        this.world.saveAs(worldName);
    }

    public void resetPlayerLocation() {
        if (this.player != null) {
            this.player.resetSavedPosition();
        }
    }

    public void destroyWorld() {
        this.world.destroy();
        this.world = null;
        this.gui = null;
        this.cam = null;
        this.player = null;
        this.charactersPainter = null;
    }

    public boolean isWorldReady() {
        return this.world != null && this.world.isReady();
    }
}
