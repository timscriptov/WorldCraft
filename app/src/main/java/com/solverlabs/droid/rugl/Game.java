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
    /**
     * Info logging tag
     */
    public static final String RUGL_TAG = "RUGL";
    private static final ArrayList<SurfaceListener> mSurfaceListeners = new ArrayList<>();
    public static GLVersion mGlVersion;
    /**
     * Screen height
     */
    public static int mScreenHeight;
    /**
     * Screen width
     */
    public static int mScreenWidth;
    /**
     * Desired width of screen for rendering and input purposes
     */
    public static float mGameWidth = 800.0f;
    /**
     * Desired height of screen for rendering and input purposes
     */
    public static float mGameHeight = 480.0f;
    /**
     * The desired logic advance, in seconds, or -1 to disable fixed interval
     * advances. Be careful when setting a fixed logic advance - things will not
     * go well if executing the logic code takes longer than the logic advance
     * value.
     */
    public static float mLogicAdvance = -1.0f;
    private final GameActivity mGa;
    private final GLVersion mRequiredVersion;
    private BlockView mCurrentBlockView;
    private boolean mResetTouches = true;
    private boolean mPhaseInited = false;
    private long mLastLogic = System.currentTimeMillis();

    public Game(GameActivity ga, GLVersion requiredVersion, BlockView phase) {
        this.mGa = ga;
        this.mRequiredVersion = requiredVersion;
        this.mCurrentBlockView = phase;
        Multiplayer.instance.gameActivity = ga;
    }

    public static void addSurfaceLIstener(SurfaceListener sl) {
        mSurfaceListeners.add(sl);
    }

    public static void removeSurfaceListener(SurfaceListener sl) {
        mSurfaceListeners.remove(sl);
    }

    public static void removeAllSurfaceListeners() {
        mSurfaceListeners.clear();
    }

    /**
     * Call this when we may have lost track of touchscreen activity e.g.: when
     * we were in another activity, etc
     */
    public void resetTouches() {
        this.mResetTouches = true;
    }

    /**
     * Gets the sensor manager
     *
     * @return the {@link SensorManager}
     */
    public SensorManager getSensorManager() {
        return (SensorManager) mGa.getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.i(RUGL_TAG, "Surface created at " + new Date());
        String glVersionString = GLES10.glGetString(GLES10.GL_VERSION);
        String extensionsString = GLES10.glGetString(GLES10.GL_EXTENSIONS);
        StringBuilder buff = new StringBuilder();
        buff.append("\tVendor = ").append(GLES10.glGetString(GLES10.GL_VENDOR));
        buff.append("\n\tRenderer = ").append(GLES10.glGetString(GLES10.GL_RENDERER));
        buff.append("\n\tVersion = ").append(glVersionString);
        buff.append("\n\tExtensions");
        if (extensionsString != null) {
            String[] extensions = extensionsString.split(" ");
            for (String ex : extensions) {
                buff.append("\n\t\t").append(ex);
            }
        } else {
            buff.append("null");
        }
        ExceptionHandler.addLogInfo("GLInfo", buff.toString());
        Log.i(RUGL_TAG, buff.toString());
        mGlVersion = GLVersion.findVersion(glVersionString);
        Log.i(RUGL_TAG, "Detected " + mGlVersion);
        if (this.mRequiredVersion != null && this.mRequiredVersion.ordinal() > mGlVersion.ordinal()) {
            this.mGa.showToast("Required OpenGLES version " + this.mRequiredVersion + " but found version " + mGlVersion, true);
            this.mGa.finish();
        }
        GLUtil.enableVertexArrays();
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
        gl.glShadeModel(7425);
        gl.glClearDepthf(1.0f);
        gl.glEnable(2929);
        gl.glDepthFunc(515);
        this.mPhaseInited = false;
        this.mLastLogic = System.currentTimeMillis();
        GLUtil.checkGLError();
        for (int i = 0; i < mSurfaceListeners.size(); i++) {
            mSurfaceListeners.get(i).onSurfaceCreated();
        }
    }

    /**
     * Default implementation is to set up a 1:1 orthographic projection. Touches
     * are scaled are similarly scaled 1:1
     */
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mScreenWidth = width;
        mScreenHeight = height;
        GLUtil.scaledOrtho(mGameWidth, mGameHeight, mScreenWidth, mScreenHeight, -1.0f, 1.0f);
        Log.i(RUGL_TAG, "Surface changed " + width + " x " + height);
        GLUtil.checkGLError();
        for (int i = 0; i < mSurfaceListeners.size(); i++) {
            mSurfaceListeners.get(i).onSurfaceChanged(width, height);
        }
        Touch.setScreenSize(mGameWidth, mGameHeight, mScreenWidth, mScreenHeight);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (this.mCurrentBlockView == null) {
            Log.i(RUGL_TAG, "Exiting");
            this.mGa.finish();
            return;
        }
        if (!this.mPhaseInited) {
            Log.i(RUGL_TAG, "BlockView " + this.mCurrentBlockView + " initing");
            this.mCurrentBlockView.openGLinit();
            this.mCurrentBlockView.init(this);
            this.mPhaseInited = true;
        }
        if (this.mResetTouches) {
            Touch.reset();
            this.mResetTouches = false;
        }
        ResourceLoader.checkCompletion();
        Touch.processTouches();
        long now = System.currentTimeMillis();
        long dur = now - this.mLastLogic;
        if (mLogicAdvance > 0.0f) {
            while (this.mLastLogic < now) {
                this.mCurrentBlockView.advance(mLogicAdvance);
                this.mLastLogic += mLogicAdvance * 1000.0f;
            }
        } else {
            this.mCurrentBlockView.advance(((float) dur) / 1000.0f);
        }
        this.mLastLogic = now;
        this.mCurrentBlockView.draw();
        if (this.mCurrentBlockView.isWorldReady()) {
            if (!GameMode.isMultiplayerMode() || Multiplayer.instance.isWorldReady) {
                if (this.mCurrentBlockView.mWorld.isChunksInited()) {
                    ((WorldCraftActivity) this.mGa).dismissAllLoadingDialogs();
                }
                Multiplayer.instance.isWorldShowing = true;
                if (this.mCurrentBlockView.complete) {
                    Log.i(RUGL_TAG, "BlockView " + this.mCurrentBlockView + " complete");
                    getBlockView().unload();
                    this.mCurrentBlockView = this.mCurrentBlockView.next();
                    this.mPhaseInited = false;
                }
                GLUtil.checkGLError();
            }
        }
    }

    public BlockView getBlockView() {
        return this.mCurrentBlockView;
    }

    public GameActivity getGameActivity() {
        return this.mGa;
    }

    public static abstract class SurfaceListener {
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
        public void onSurfaceChanged(int width, int height) {
        }
    }
}
