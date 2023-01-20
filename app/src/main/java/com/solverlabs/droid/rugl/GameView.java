package com.solverlabs.droid.rugl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.solverlabs.droid.rugl.input.Touch;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

public final class GameView extends GLSurfaceView {
    public Game game;

    public GameView(Context context) {
        super(context);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(Game game) {
        setDebugFlags(1);
        this.game = game;
        setEGLConfigChooser((egl, display) -> {
            int[] attributes = {12325, 16, 12344};
            EGLConfig[] configs = new EGLConfig[1];
            int[] result = new int[1];
            egl.eglChooseConfig(display, attributes, configs, 1, result);
            return configs[0];
        });
        setRenderer(game);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Touch.onTouchEvent(event);
        return true;
    }
}
