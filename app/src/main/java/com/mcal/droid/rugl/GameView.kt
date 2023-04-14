package com.mcal.droid.rugl

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import com.mcal.droid.rugl.input.Touch
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLDisplay

@SuppressLint("ViewConstructor")
class GameView : GLSurfaceView {
    /**
     * The game
     */
    @JvmField
    var game: Game? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    fun init(game: Game) {
        debugFlags = DEBUG_CHECK_GL_ERROR
        this.game = game.also {
            setEGLConfigChooser { egl: EGL10, display: EGLDisplay? ->
                // Ensure that we get a 16bit framebuffer. Otherwise,
                // we'll fall back to Pixelflinger on some devices (e.g.:
                // Samsung I7500)
                val attributes = intArrayOf(EGL10.EGL_DEPTH_SIZE, 16, EGL10.EGL_NONE)
                val configs = arrayOfNulls<EGLConfig>(1)
                val result = IntArray(1)
                egl.eglChooseConfig(display, attributes, configs, 1, result)
                configs[0]
            }
            setRenderer(it as Renderer)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        Touch.onTouchEvent(event)
        return true
    }
}