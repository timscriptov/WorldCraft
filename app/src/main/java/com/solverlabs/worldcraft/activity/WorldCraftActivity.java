package com.solverlabs.worldcraft.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;

import com.solverlabs.droid.rugl.Game;
import com.solverlabs.droid.rugl.GameActivity;
import com.solverlabs.droid.rugl.input.Touch;
import com.solverlabs.droid.rugl.res.ResourceLoader;
import com.solverlabs.droid.rugl.texture.TextureFactory;
import com.solverlabs.droid.rugl.util.geom.Vector2f;
import com.solverlabs.droid.rugl.util.geom.Vector3f;
import com.solverlabs.worldcraft.BlockView;
import com.solverlabs.worldcraft.GameMode;
import com.solverlabs.worldcraft.MyApplication;
import com.solverlabs.worldcraft.Persistence;
import com.solverlabs.worldcraft.Player;
import com.solverlabs.worldcraft.SoundManager;
import com.solverlabs.worldcraft.World;
import com.solverlabs.worldcraft.dialog.DeathMenuDialog;
import com.solverlabs.worldcraft.factories.BlockFactory;
import com.solverlabs.worldcraft.multiplayer.Multiplayer;
import com.solverlabs.worldcraft.multiplayer.dialogs.ReportAbuseDialog;
import com.solverlabs.worldcraft.nbt.RegionFileCache;
import com.solverlabs.worldcraft.nbt.Tag;
import com.solverlabs.worldcraft.nbt.TagLoader;
import com.solverlabs.worldcraft.ui.CustomProgressDialog;
import com.solverlabs.worldcraft.util.GameTime;
import com.solverlabs.worldcraft.util.WorldGenerator;

import org.apache.commons.compress.archivers.cpio.CpioConstants;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WorldCraftActivity extends GameActivity {
    protected MyApplication application;
    private BlockView bw;
    private DeathMenuDialog deathMenuDialog;
    private boolean isResumingGame = false;
    private ProgressDialog loadingProgressDialog;
    private ProgressDialog resumeDialog;
    private World world;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.application = (MyApplication) getApplicationContext();
        getWindow().addFlags(CpioConstants.C_IWUSR);
        String worldFileName = getIntent().getExtras().getString("world");
        boolean isNewGame = getIntent().getExtras().getBoolean("isNewGame");
        WorldGenerator.Mode gameMode = (WorldGenerator.Mode) getIntent().getExtras().get("gameMode");
        Touch.resetTouch();
        initSoundManager();
        Log.e(Game.RUGL_TAG, "loading " + worldFileName);
        File dir = new File(worldFileName);
        showProgressDialog();
        TagLoader tl = new TagLoad(new File(dir, World.LEVEL_DAT_FILE_NAME), dir, isNewGame, gameMode);
        tl.selfCompleting = true;
        ResourceLoader.load(tl);
    }

    private void initSoundManager() {
        SoundManager.initSounds(this);
        SoundManager.loadSounds();
    }

    @Override
    public void onPause() {
        clearReferences();
        if (world != null) {
            world.save();
        }
        isResumingGame = true;
        if (loadingProgressDialog != null) {
            loadingProgressDialog.dismiss();
        }
        if (GameMode.isMultiplayerMode()) {
            Multiplayer.instance.shutdown();
            finish();
        }
        super.onPause();
    }

    private void clearReferences() {
        if (application.isCurrentActivity(this)) {
            application.setCurrentActivity(null);
        }
    }

    @Override
    public void onResume() {
        application.setCurrentActivity(this);
        if (isResumingGame) {
            isResumingGame = false;
            if (GameMode.isMultiplayerMode()) {
                super.finish();
                return;
            }
            showResumeDialog();
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        TextureFactory.removeListener();
        if (bw != null) {
            bw.destroyWorld();
            bw = null;
            TextureFactory.deleteAllTextures();
        }
        RegionFileCache.clear();
        System.runFinalization();
        Runtime.getRuntime().gc();
        super.onDestroy();
    }

    @Override
    public void finish() {
        if (this.world != null && world.isNewGame) {
            deleteSave(world.dir.getAbsolutePath());
        }
        super.finish();
    }

    private void showResumeDialog() {
        if (resumeDialog == null) {
            resumeDialog = new ProgressDialog(this);
            resumeDialog.setTitle("Please wait");
            resumeDialog.setMessage("Resuming");
        }
        runOnUiThread(() -> resumeDialog.show());
    }

    private void showProgressDialog() {
        if (loadingProgressDialog == null) {
            loadingProgressDialog = new CustomProgressDialog(this) {
                @Override
                public void onBackPressed() {
                    if (world != null) {
                        world.setCancel(true);
                    }
                    finish();
                }

                @Override
                public void buttonClick() {
                    if (world != null) {
                        world.setCancel(true);
                    }
                    finish();
                }
            };
        }
        loadingProgressDialog.show();
    }

    public void dismissProgresDialog() {
        loadingProgressDialog.dismiss();
    }

    public void dismissResumeDialog() {
        resumeDialog.dismiss();
    }

    public void dismissAllLoadingDialogs() {
        if (loadingProgressDialog != null && loadingProgressDialog.isShowing()) {
            loadingProgressDialog.dismiss();
        }
        if (resumeDialog != null && resumeDialog.isShowing()) {
            resumeDialog.dismiss();
        }
    }

    private void deleteSave(String absolutePath) {
        File dir = new File(absolutePath);
        String[] list = dir.list();
        if (list != null) {
            for (String str : list) {
                File file = new File(dir, str);
                if (file.isFile()) {
                    Log.d("del_file", "  " + file.delete());
                }
                if (file.isDirectory()) {
                    deleteSave(file.getAbsolutePath());
                }
            }
            if (dir.isDirectory() && list.length == 0) {
                Log.d("del_dir", "  " + dir.delete());
            }
        }
    }

    public class TagLoad extends TagLoader {
        final File dir;
        final WorldGenerator.Mode gameMode;
        final boolean isNewGame;

        TagLoad(File dat, File file, boolean z, WorldGenerator.Mode mode) {
            super(dat);
            this.dir = file;
            this.isNewGame = z;
            this.gameMode = mode;
        }

        @Override
        public void complete() {
            runOnUiThread(new TagLoaderRunnable());
        }

        public class TagLoaderRunnable implements Runnable {
            @Override
            public void run() {
                int mapType;
                if (resource == null) {
                    showToast("Could not load world level.dat\n" + exception.getClass().getSimpleName() + ":" + exception.getMessage(), true);
                    finish();
                    return;
                }
                try {
                    Tag time = resource.findTagByName(WorldGenerator.LAST_PLAYED);
                    GameTime.initTime((Long) time.getValue());
                    Tag playerTag = resource.findTagByName("Player");
                    Tag pos = playerTag.findTagByName("Pos");
                    Tag[] tl = (Tag[]) pos.getValue();
                    Vector3f p = new Vector3f();
                    p.x = ((Double) tl[0].getValue()).floatValue();
                    p.y = ((Double) tl[1].getValue()).floatValue();
                    p.z = ((Double) tl[2].getValue()).floatValue();
                    Tag rotaionTag = playerTag.findTagByName("Rotation");
                    Vector2f rotation = new Vector2f();
                    if (rotaionTag != null) {
                        Tag[] tl2 = (Tag[]) rotaionTag.getValue();
                        rotation.x = (Float) tl2[0].getValue();
                        rotation.y = (Float) tl2[1].getValue();
                        if (rotation.y > 1.5707964f || rotation.y < -1.5707964f) {
                            rotation.y = 0.0f;
                        }
                    }
                    Tag mapTypeTag = resource.findTagByName(WorldGenerator.MAP_TYPE);
                    if (mapTypeTag != null) {
                        mapType = (Integer) mapTypeTag.getValue();
                    } else {
                        mapType = -1;
                    }
                    world = new World(dir, p, resource) {
                        @Override
                        public boolean isLoadingDialogVisible() {
                            return loadingProgressDialog != null && loadingProgressDialog.isShowing();
                        }

                        @Override
                        public void incLoadingProgressStatus(int diff) {
                            loadingProgressDialog.incrementProgressBy(diff);
                        }

                        @Override
                        public void setLoadingProgressStatus(int progress, int max) {
                            loadingProgressDialog.setMax(max);
                            loadingProgressDialog.setProgress(progress);
                        }

                        @Override
                        public void showGameMenu() {
                            runOnUiThread(WorldCraftActivity.this::showGameMenuDialog);
                        }

                        @Override
                        public void dismissLoadingDialog() {
                            if (loadingProgressDialog != null) {
                                loadingProgressDialog.dismiss();
                            }
                        }

                        @Override
                        public void dismissLoadingDialogAndWait() {
                            if (loadingProgressDialog != null && loadingProgressDialog.isShowing()) {
                                final CountDownLatch latch = new CountDownLatch(1);
                                loadingProgressDialog.setOnDismissListener(dialog -> latch.countDown());
                                loadingProgressDialog.dismiss();
                                try {
                                    latch.await(2L, TimeUnit.SECONDS);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void showChat() {
                            runOnUiThread(WorldCraftActivity.this::showChatDialog);
                        }

                        @Override
                        public void showReportAbuse() {
                            runOnUiThread(() -> {
                                ReportAbuseDialog reportAbuseDialog = new ReportAbuseDialog(WorldCraftActivity.this);
                                reportAbuseDialog.show();
                            });
                        }

                        @Override
                        public void showDeathMenu(final Player player) {
                            runOnUiThread(() -> {
                                if (deathMenuDialog == null || !deathMenuDialog.isVisible()) {
                                    deathMenuDialog = new DeathMenuDialog(WorldCraftActivity.this, player);
                                    deathMenuDialog.show();
                                }
                            });
                        }
                    };
                    world.setNewGame(isNewGame);
                    world.setMapType(mapType);
                    ((CustomProgressDialog) loadingProgressDialog).updateMax(World.getLoadingLimit(isNewGame));
                    bw = new BlockView(world);
                    if (GameMode.isMultiplayerMode()) {
                        Multiplayer.instance.blockView = bw;
                    }
                    bw.setCamRotation(rotation);
                    bw.cam.invert = Persistence.getInstance().isInvertY();
                    GameMode.setGameMode((GameMode.isMultiplayerMode() || !WorldGenerator.Mode.SURVIVAL.equals(gameMode)) ? 1 : 0);
                    if (GameMode.isSurvivalMode() && !GameMode.isMultiplayerMode()) {
                        world.initSunLight();
                    }
                    initFog();
                    Game game = new Game(WorldCraftActivity.this, null, bw);
                    start(game);
                } catch (Exception e) {
                    showToast("Problem parsing level.dat - Maybe a corrupt file?", true);
                    Log.e(Game.RUGL_TAG, "Level.dat corrupted?", e);
                    finish();
                }
            }

            private void initFog() {
                float fogDistance = Persistence.getInstance().getFogDistance();
                if (fogDistance < 0.0f) {
                    fogDistance = 0.0f;
                }
                BlockFactory.state.fog.start = fogDistance;
                BlockFactory.state.fog.end = 10.0f + fogDistance;
            }
        }
    }
}
