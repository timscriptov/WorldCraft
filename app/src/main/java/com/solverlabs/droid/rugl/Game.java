package com.solverlabs.droid.rugl;

import android.content.Context;
import android.hardware.SensorManager;
import android.opengl.GLES10;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.solverlabs.droid.rugl.gl.GLUtil;
import com.solverlabs.droid.rugl.gl.GLVersion;
import com.solverlabs.droid.rugl.input.Touch;
import com.solverlabs.droid.rugl.res.ResourceLoader;
import com.solverlabs.droid.rugl.util.ExceptionHandler;
import com.solverlabs.worldcraft.BlockView;
import com.solverlabs.worldcraft.GameMode;
import com.solverlabs.worldcraft.activity.WorldCraftActivity;
import com.solverlabs.worldcraft.multiplayer.Multiplayer;

import java.util.ArrayList;
import java.util.Date;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class Game implements GLSurfaceView.Renderer {
    public static final String RUGL_TAG = "RUGL";
    public static GLVersion glVersion;
    public static int screenHeight;
    public static int screenWidth;
    public static float gameWidth = 800.0f;
    public static float gameHeight = 480.0f;
    public static float logicAdvance = -1.0f;
    private static final ArrayList<SurfaceListener> surfaceListeners = new ArrayList<>();
    private final GameActivity ga;
    private final GLVersion requiredVersion;
    private BlockView currentBlockView;
    private int fps;
    private boolean resetTouches = true;
    private boolean phaseInited = false;
    private long lastLogic = System.currentTimeMillis();

    public Game(GameActivity ga, GLVersion requiredVersion, BlockView phase) {
        this.ga = ga;
        this.requiredVersion = requiredVersion;
        this.currentBlockView = phase;
        Multiplayer.instance.gameActivity = ga;
    }

    public static void addSurfaceLIstener(SurfaceListener sl) {
        surfaceListeners.add(sl);
    }

    public static void removeSurfaceListener(SurfaceListener sl) {
        surfaceListeners.remove(sl);
    }

    public static void removeAllSurfaceListeners() {
        surfaceListeners.clear();
    }

    public void resetTouches() {
        this.resetTouches = true;
    }

    public SensorManager getSensorManager() {
        return (SensorManager) this.ga.getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.i(RUGL_TAG, "Surface created at " + new Date());
        String glVersionString = GLES10.glGetString(7938);
        String extensionsString = GLES10.glGetString(7939);
        StringBuilder buff = new StringBuilder();
        buff.append("\tVendor = ").append(GLES10.glGetString(7936));
        buff.append("\n\tRenderer = ").append(GLES10.glGetString(7937));
        buff.append("\n\tVersion = ").append(glVersionString);
        buff.append("\n\tExtensions");
        if (extensionsString != null) {
            String[] arr$ = extensionsString.split(" ");
            for (String ex : arr$) {
                buff.append("\n\t\t" + ex);
            }
        } else {
            buff.append("null");
        }
        ExceptionHandler.addLogInfo("GLInfo", buff.toString());
        Log.i(RUGL_TAG, buff.toString());
        glVersion = GLVersion.findVersion(glVersionString);
        Log.i(RUGL_TAG, "Detected " + glVersion);
        if (this.requiredVersion != null && this.requiredVersion.ordinal() > glVersion.ordinal()) {
            this.ga.showToast("Required OpenGLES version " + this.requiredVersion + " but found version " + glVersion, true);
            this.ga.finish();
        }
        GLUtil.enableVertexArrays();
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
        gl.glShadeModel(7425);
        gl.glClearDepthf(1.0f);
        gl.glEnable(2929);
        gl.glDepthFunc(515);
        this.phaseInited = false;
        this.lastLogic = System.currentTimeMillis();
        GLUtil.checkGLError();
        for (int i = 0; i < surfaceListeners.size(); i++) {
            surfaceListeners.get(i).onSurfaceCreated();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        screenWidth = width;
        screenHeight = height;
        GLUtil.scaledOrtho(gameWidth, gameHeight, screenWidth, screenHeight, -1.0f, 1.0f);
        Log.i(RUGL_TAG, "Surface changed " + width + " x " + height);
        GLUtil.checkGLError();
        for (int i = 0; i < surfaceListeners.size(); i++) {
            surfaceListeners.get(i).onSurfaceChanged(width, height);
        }
        Touch.setScreenSize(gameWidth, gameHeight, screenWidth, screenHeight);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (this.currentBlockView == null) {
            Log.i(RUGL_TAG, "Exiting");
            this.ga.finish();
            return;
        }
        if (!this.phaseInited) {
            Log.i(RUGL_TAG, "BlockView " + this.currentBlockView + " initing");
            this.currentBlockView.openGLinit();
            this.currentBlockView.init(this);
            this.phaseInited = true;
        }
        if (this.resetTouches) {
            Touch.reset();
            this.resetTouches = false;
        }
        ResourceLoader.checkCompletion();
        Touch.processTouches();
        long now = System.currentTimeMillis();
        long dur = now - this.lastLogic;
        if (logicAdvance > 0.0f) {
            while (this.lastLogic < now) {
                this.currentBlockView.advance(logicAdvance);
                this.lastLogic += logicAdvance * 1000.0f;
            }
        } else {
            this.currentBlockView.advance(((float) dur) / 1000.0f);
        }
        this.lastLogic = now;
        this.currentBlockView.draw();
        if (this.currentBlockView.isWorldReady()) {
            if (!GameMode.isMultiplayerMode() || Multiplayer.instance.isWorldReady) {
                if (this.currentBlockView.world.isChunksInited()) {
                    ((WorldCraftActivity) this.ga).dismissAllLoadingDialogs();
                }
                Multiplayer.instance.isWorldShowing = true;
                if (this.currentBlockView.complete) {
                    Log.i(RUGL_TAG, "BlockView " + this.currentBlockView + " complete");
                    getBlockView().unload();
                    this.currentBlockView = this.currentBlockView.next();
                    this.phaseInited = false;
                }
                GLUtil.checkGLError();
            }
        }
    }

    public BlockView getBlockView() {
        return this.currentBlockView;
    }

    public GameActivity getGameActivity() {
        return this.ga;
    }


    public static abstract class SurfaceListener {
        public void onSurfaceCreated() {
        }

        public void onSurfaceChanged(int width, int height) {
        }
    }
}
