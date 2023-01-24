package com.solverlabs.droid.rugl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.solverlabs.droid.rugl.input.Touch;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;

@SuppressLint("ViewConstructor")
public final class GameView extends GLSurfaceView {
    /**
     * The game
     */
    public Game game;

    public GameView(Context context) {
        super(context);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(Game game) {
        setDebugFlags(DEBUG_CHECK_GL_ERROR);
        this.game = game;
        setEGLConfigChooser((egl, display) -> {
            // Ensure that we get a 16bit framebuffer. Otherwise,
            // we'll fall back to Pixelflinger on some devices (e.g.:
            // Samsung I7500)
            int[] attributes = new int[]{EGL10.EGL_DEPTH_SIZE, 16, EGL10.EGL_NONE};
            EGLConfig[] configs = new EGLConfig[1];
            int[] result = new int[1];
            egl.eglChooseConfig(display, attributes, configs, 1, result);
            return configs[0];
        });
        setRenderer((Renderer) game);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        Touch.onTouchEvent(event);

        return true;
    }
}