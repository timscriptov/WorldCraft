package com.mcal.droid.rugl

import android.content.Context
import android.hardware.SensorManager
import android.opengl.GLES10
import android.opengl.GLSurfaceView
import android.util.Log
import com.mcal.droid.rugl.gl.GLUtil
import com.mcal.droid.rugl.gl.GLVersion
import com.mcal.droid.rugl.input.Touch
import com.mcal.droid.rugl.res.ResourceLoader
import com.mcal.droid.rugl.util.ExceptionHandler
import com.mcal.worldcraft.BlockView
import com.mcal.worldcraft.GameMode
import com.mcal.worldcraft.activity.WorldCraftActivity
import com.mcal.worldcraft.multiplayer.Multiplayer
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * A convenient [BlockView]-based game model
 *
 * @author ryanm
 */
class Game(
    val gameActivity: GameActivity,
    private val requiredVersion: GLVersion?,
    /**
     * @return the current phase
     */
    var blockView: BlockView?
) : GLSurfaceView.Renderer {
    private var resetTouches = true
    private var phaseInited = false
    private var lastLogic = System.currentTimeMillis()

    /**
     * Call this when we may have lost track of touchscreen activity e.g.: when
     * we were in another activity, etc
     */
    fun resetTouches() {
        resetTouches = true
    }

    /**
     * Gets the sensor manager
     *
     * @return the [SensorManager]
     */
    val sensorManager: SensorManager
        get() = gameActivity.getSystemService(Context.SENSOR_SERVICE) as SensorManager


    init {
        Multiplayer.instance.gameActivity = gameActivity
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        Log.i(RUGL_TAG, "Surface created at " + Date())
        val glVersionString = GLES10.glGetString(GLES10.GL_VERSION)
        val extensionsString = GLES10.glGetString(GLES10.GL_EXTENSIONS)
        val buff = StringBuilder()
        buff.append("\tVendor = ").append(
            GLES10.glGetString(GLES10.GL_VENDOR)
        )
        buff.append("\n\tRenderer = ").append(
            GLES10.glGetString(GLES10.GL_RENDERER)
        )
        buff.append("\n\tVersion = ").append(glVersionString)
        buff.append("\n\tExtensions")
        if (extensionsString != null) {
            for (ex in extensionsString.split(" ".toRegex()).toTypedArray()) {
                buff.append("\n\t\t").append(ex)
            }
        } else {
            buff.append("null")
        }
        ExceptionHandler.addLogInfo("GLInfo", buff.toString())
        Log.i(RUGL_TAG, buff.toString())
        glVersion = GLVersion.findVersion(glVersionString)
        Log.i(RUGL_TAG, "Detected $glVersion")
        if (requiredVersion != null
            && requiredVersion.ordinal > glVersion!!.ordinal
        ) {
            // requirements fail!
            gameActivity.showToast(
                "Required OpenGLES version " + requiredVersion
                        + " but found version " + glVersion, true
            )
            gameActivity.finish()
        }
        GLUtil.enableVertexArrays()
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f)
        gl.glShadeModel(GL10.GL_SMOOTH)
        gl.glClearDepthf(1.0f)
        gl.glEnable(GL10.GL_DEPTH_TEST)
        gl.glDepthFunc(GL10.GL_LEQUAL)
        phaseInited = false
        lastLogic = System.currentTimeMillis()
        GLUtil.checkGLError()
        for (i in surfaceListeners.indices) {
            surfaceListeners[i].onSurfaceCreated()
        }
    }

    /**
     * Default implementation is to set up a 1:1 orthographic projection. Touches
     * are scaled are similarly scaled 1:1
     */
    override fun onSurfaceChanged(
        gl: GL10, width: Int,
        height: Int
    ) {
        screenWidth = width
        screenHeight = height
        gameWidth = width.toFloat()
        gameHeight = height.toFloat()
        GLUtil.scaledOrtho(gameWidth, gameHeight, screenWidth, screenHeight, -1f, 1f)
        Log.i(RUGL_TAG, "Surface changed $width x $height")
        GLUtil.checkGLError()
        for (i in surfaceListeners.indices) {
            surfaceListeners[i].onSurfaceChanged(width, height)
        }
        Touch.setScreenSize(gameWidth, gameHeight, screenWidth, screenHeight)
    }

    override fun onDrawFrame(gl: GL10) {
        blockView?.let { view ->
            if (!phaseInited) {
                Log.i(RUGL_TAG, "BlockView $view initing")
                view.apply {
                    openGLinit()
                    init(this@Game)
                }
                phaseInited = true
            }
            if (resetTouches) {
                Touch.reset()
                resetTouches = false
            }
            ResourceLoader.checkCompletion()
            Touch.processTouches()
            val now = System.currentTimeMillis()
            val dur = now - lastLogic
            if (logicAdvance > 0.0f) {
                while (lastLogic < now) {
                    view.advance(logicAdvance)
                    lastLogic += (logicAdvance * 1000.0f).toLong()
                }
            } else {
                view.advance(dur.toFloat() / 1000.0f)
            }
            lastLogic = now
            view.draw()
            if (view.isWorldReady) {
                if (!GameMode.isMultiplayerMode || Multiplayer.instance.isWorldReady) {
                    if (view.world.isChunksInited) {
                        (gameActivity as WorldCraftActivity).dismissAllLoadingDialogs()
                    }
                    Multiplayer.instance.isWorldShowing = true
                    if (view.complete) {
                        Log.i(RUGL_TAG, "BlockView $view complete")
                        view.unload()
                        blockView = view.next()
                        phaseInited = false
                    }
                    GLUtil.checkGLError()
                }
            }
        } ?: run {
            // time to quit
            Log.i(RUGL_TAG, "Exiting")
            gameActivity.finish()
            return
        }
    }

    abstract class SurfaceListener {
        /**
         * Called when the surface is created
         */
        open fun onSurfaceCreated() {}

        /**
         * Called when the surface is changed
         *
         * @param width
         * @param height
         */
        fun onSurfaceChanged(width: Int, height: Int) {}
    }

    companion object {
        /**
         * Info logging tag
         */
        const val RUGL_TAG = "RUGL"

        /**
         * OpenGLES version
         */
        @JvmField
        var glVersion: GLVersion? = null

        /**
         * Screen height
         */
        @JvmField
        var screenHeight = 0

        /**
         * Screen width
         */
        @JvmField
        var screenWidth = 0

        /**
         * Desired width of screen for rendering and input purposes
         */
        @JvmField
        var gameWidth = 800.0f

        /**
         * Desired height of screen for rendering and input purposes
         */
        @JvmField
        var gameHeight = 480.0f
        private val surfaceListeners = ArrayList<SurfaceListener>()

        /**
         * The desired logic advance, in seconds, or -1 to disable fixed interval
         * advances. Be careful when setting a fixed logic advance - things will not
         * go well if executing the logic code takes longer than the logic advance
         * value.
         */
        @JvmField
        var logicAdvance = -1.0f

        /**
         * @param sl
         */
        @JvmStatic
        fun addSurfaceLIstener(sl: SurfaceListener) {
            surfaceListeners.add(sl)
        }

        /**
         * @param sl
         */
        @JvmStatic
        fun removeSurfaceListener(sl: SurfaceListener) {
            surfaceListeners.remove(sl)
        }

        @JvmStatic
        fun removeAllSurfaceListeners() {
            surfaceListeners.clear()
        }
    }
}