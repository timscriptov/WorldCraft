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

/**
 * A convenient {@link BlockView}-based game model
 *
 * @author ryanm
 */
public class Game implements GLSurfaceView.Renderer {
    /**
     * Info logging tag
     */
    public static final String RUGL_TAG = "RUGL";
    /**
     * OpenGLES version
     */
    public static GLVersion glVersion;
    /**
     * Screen height
     */
    public static int screenHeight;
    /**
     * Screen width
     */
    public static int screenWidth;
    private BlockView currentBlockView;
    private int fps;
    private final GameActivity ga;
    private final GLVersion requiredVersion;
    /**
     * Desired width of screen for rendering and input purposes
     */
    public static float gameWidth = 800.0f;
    /**
     * Desired height of screen for rendering and input purposes
     */
    public static float gameHeight = 480.0f;
    private static final ArrayList<SurfaceListener> surfaceListeners = new ArrayList<>();
    /**
     * The desired logic advance, in seconds, or -1 to disable fixed interval
     * advances. Be careful when setting a fixed logic advance - things will not
     * go well if executing the logic code takes longer than the logic advance
     * value.
     */
    public static float logicAdvance = -1.0f;
    private boolean resetTouches = true;
    private boolean phaseInited = false;
    private long lastLogic = System.currentTimeMillis();

    /**
     * @param sl
     */
    public static void addSurfaceLIstener(final SurfaceListener sl) {
        surfaceListeners.add(sl);
    }

    /**
     * @param sl
     */
    public static void removeSurfaceListener(final SurfaceListener sl) {
        surfaceListeners.remove(sl);
    }

    public static void removeAllSurfaceListeners() {
        surfaceListeners.clear();
    }

    /**
     * Call this when we may have lost track of touchscreen activity e.g.: when
     * we were in another activity, etc
     */
    public void resetTouches() {
        resetTouches = true;
    }

    /**
     * Gets the sensor manager
     *
     * @return the {@link SensorManager}
     */
    public SensorManager getSensorManager() {
        return (SensorManager) ga.getSystemService(Context.SENSOR_SERVICE);
    }

    /**
     * @param ga              used solely to quit when we run out of phases
     * @param requiredVersion The {@link GLVersion} that will be required in the game, or
     *                        <code>null</code> not to bother checking
     * @param phase           The initial phase
     */
    public Game(GameActivity ga, GLVersion requiredVersion, BlockView phase) {
        this.ga = ga;
        this.requiredVersion = requiredVersion;
        this.currentBlockView = phase;
        Multiplayer.instance.gameActivity = ga;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.i(RUGL_TAG, "Surface created at " + new Date());

        final String glVersionString = GLES10.glGetString(GLES10.GL_VERSION);
        final String extensionsString = GLES10.glGetString(GLES10.GL_EXTENSIONS);

        final StringBuilder buff = new StringBuilder();
        buff.append("\tVendor = ").append(
                GLES10.glGetString(GLES10.GL_VENDOR));
        buff.append("\n\tRenderer = ").append(
                GLES10.glGetString(GLES10.GL_RENDERER));
        buff.append("\n\tVersion = ").append(glVersionString);
        buff.append("\n\tExtensions");
        if (extensionsString != null) {
            for (final String ex : extensionsString.split(" ")) {
                buff.append("\n\t\t" + ex);
            }
        } else {
            buff.append("null");
        }
        ExceptionHandler.addLogInfo("GLInfo", buff.toString());
        Log.i(RUGL_TAG, buff.toString());
        glVersion = GLVersion.findVersion(glVersionString);
        Log.i(RUGL_TAG, "Detected " + glVersion);
        if (requiredVersion != null
                && requiredVersion.ordinal() > glVersion.ordinal()) {
            // requirements fail!
            ga.showToast("Required OpenGLES version " + requiredVersion
                    + " but found version " + glVersion, true);
            ga.finish();
        }

        GLUtil.enableVertexArrays();
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glClearDepthf(1.0f);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LEQUAL);
        this.phaseInited = false;
        this.lastLogic = System.currentTimeMillis();
        GLUtil.checkGLError();
        for (int i = 0; i < surfaceListeners.size(); i++) {
            surfaceListeners.get(i).onSurfaceCreated();
        }
    }

    /**
     * Default implementation is to set up a 1:1 orthographic projection. Touches
     * are scaled are similarly scaled 1:1
     */
    @Override
    public void onSurfaceChanged(final GL10 gl, final int width,
                                 final int height) {
        Game.screenWidth = width;
        Game.screenHeight = height;

        GLUtil.scaledOrtho(gameWidth, gameHeight, screenWidth, screenHeight, -1, 1);

        Log.i(RUGL_TAG, "Surface changed " + width + " x " + height);

        GLUtil.checkGLError();

        for (int i = 0; i < surfaceListeners.size(); i++) {
            surfaceListeners.get(i).onSurfaceChanged(width, height);
        }

        Touch.setScreenSize(gameWidth, Game.gameHeight, Game.screenWidth, Game.screenHeight);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // time to quit
        if (currentBlockView == null) {
            Log.i(RUGL_TAG, "Exiting");
            ga.finish();
            return;
        }
        if (!phaseInited) {
            Log.i(RUGL_TAG, "BlockView " + currentBlockView + " initing");
            currentBlockView.openGLinit();
            currentBlockView.init(this);
            phaseInited = true;
        }
        if (resetTouches) {
            Touch.reset();
            resetTouches = false;
        }
        ResourceLoader.checkCompletion();
        Touch.processTouches();

        final long now = System.currentTimeMillis();
        final long dur = now - lastLogic;

        if (logicAdvance > 0.0f) {
            while (lastLogic < now) {
                currentBlockView.advance(logicAdvance);
                lastLogic += logicAdvance * 1000.0f;
            }
        } else {
            currentBlockView.advance(((float) dur) / 1000.0f);
        }
        lastLogic = now;
        currentBlockView.draw();
        if (currentBlockView.isWorldReady()) {
            if (!GameMode.isMultiplayerMode() || Multiplayer.instance.isWorldReady) {
                if (currentBlockView.world.isChunksInited()) {
                    ((WorldCraftActivity) this.ga).dismissAllLoadingDialogs();
                }
                Multiplayer.instance.isWorldShowing = true;
                if (currentBlockView.complete) {
                    Log.i(RUGL_TAG, "BlockView " + currentBlockView + " complete");
                    getBlockView().unload();
                    currentBlockView = currentBlockView.next();
                    phaseInited = false;
                }
                GLUtil.checkGLError();
            }
        }
    }

    /**
     * @return the current phase
     */
    public BlockView getBlockView() {
        return currentBlockView;
    }

    public GameActivity getGameActivity() {
        return this.ga;
    }

    public abstract static class SurfaceListener {
        /**
         * Called when the surface is created
         */
        public void onSurfaceCreated() {
        }

        /**
         * Called when the surface is changed
         *
         * @param width
         * @param height
         */
        public void onSurfaceChanged(final int width, final int height) {
        }
    }
}