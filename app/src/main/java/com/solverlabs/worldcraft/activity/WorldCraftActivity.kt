package com.solverlabs.worldcraft.activity

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import com.solverlabs.droid.rugl.Game
import com.solverlabs.droid.rugl.GameActivity
import com.solverlabs.droid.rugl.input.Touch
import com.solverlabs.droid.rugl.res.ResourceLoader
import com.solverlabs.droid.rugl.texture.TextureFactory
import com.solverlabs.droid.rugl.util.geom.Vector2f
import com.solverlabs.droid.rugl.util.geom.Vector3f
import com.solverlabs.worldcraft.*
import com.solverlabs.worldcraft.GameMode.isMultiplayerMode
import com.solverlabs.worldcraft.GameMode.isSurvivalMode
import com.solverlabs.worldcraft.GameMode.setGameMode
import com.solverlabs.worldcraft.dialog.DeathMenuDialog
import com.solverlabs.worldcraft.factories.BlockFactory
import com.solverlabs.worldcraft.multiplayer.Multiplayer
import com.solverlabs.worldcraft.multiplayer.dialogs.ReportAbuseDialog
import com.solverlabs.worldcraft.nbt.RegionFileCache
import com.solverlabs.worldcraft.nbt.Tag
import com.solverlabs.worldcraft.nbt.TagLoader
import com.solverlabs.worldcraft.ui.CustomProgressDialog
import com.solverlabs.worldcraft.util.GameTime
import com.solverlabs.worldcraft.util.WorldGenerator
import org.apache.commons.compress.archivers.cpio.CpioConstants
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class WorldCraftActivity : GameActivity() {
    private lateinit var application: MyApplication
    private var bw: BlockView? = null
    private var deathMenuDialog: DeathMenuDialog? = null
    private var isResumingGame = false
    private var loadingProgressDialog: ProgressDialog? = null
    private var resumeDialog: ProgressDialog? = null
    private var world: World? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        application = applicationContext as MyApplication
        window.addFlags(CpioConstants.C_IWUSR)

        intent.extras?.let { extras ->
            val isNewGame = extras.getBoolean("isNewGame")
            val gameMode = extras["gameMode"] as WorldGenerator.Mode?

            Touch.resetTouch()
            initSoundManager()

            extras.getString("world")?.let { worldName ->
                showProgressDialog()
                val dir = File(worldName)
                TagLoad(File(dir, World.LEVEL_DAT_FILE_NAME), dir, isNewGame, gameMode).apply {
                    selfCompleting = true
                }.also { ResourceLoader.load(it) }
            }
        }
    }

    private fun initSoundManager() {
        SoundManager.initSounds(this)
        SoundManager.loadSounds()
    }

    override fun onPause() {
        clearReferences()
        world?.save()
        isResumingGame = true
        if (loadingProgressDialog != null) {
            loadingProgressDialog!!.dismiss()
        }
        if (isMultiplayerMode) {
            Multiplayer.instance.shutdown()
            finish()
        }
        super.onPause()
    }

    private fun clearReferences() {
        if (application.isCurrentActivity(this)) {
            application.currentActivity = null
        }
    }

    override fun onResume() {
        application.currentActivity = this
        if (isResumingGame) {
            isResumingGame = false
            if (isMultiplayerMode) {
                super.finish()
                return
            }
            showResumeDialog()
        }
        super.onResume()
    }

    public override fun onDestroy() {
        TextureFactory.removeListener()
        bw?.let { blockView ->
            blockView.destroyWorld()
            bw = null
            TextureFactory.deleteAllTextures()
        }
        RegionFileCache.clear()
        System.runFinalization()
        Runtime.getRuntime().gc()
        super.onDestroy()
    }

    override fun finish() {
        world?.let { world ->
            if (world.isNewGame) {
                world.dir.deleteRecursively()
            }
        }
        super.finish()
    }

    private fun showResumeDialog() {
        if (resumeDialog == null) {
            resumeDialog = ProgressDialog(this).apply {
                setTitle("Please wait")
                setMessage("Resuming")
            }
        }
        runOnUiThread { resumeDialog?.show() }
    }

    private fun showProgressDialog() {
        if (loadingProgressDialog == null) {
            loadingProgressDialog = object : CustomProgressDialog(this) {
                override fun onBackPressed() {
                    world?.setCancel(true)
                    finish()
                }

                override fun buttonClick() {
                    world?.setCancel(true)
                    finish()
                }
            }
        }
        loadingProgressDialog!!.show()
    }

    fun dismissProgressDialog() {
        loadingProgressDialog!!.dismiss()
    }

    fun dismissResumeDialog() {
        resumeDialog?.dismiss()
    }

    fun dismissAllLoadingDialogs() {
        loadingProgressDialog?.let { dialog ->
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }
        resumeDialog?.let { dialog ->
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }
    }

    inner class TagLoad internal constructor(
        dat: File?,
        val dir: File,
        val isNewGame: Boolean,
        val gameMode: WorldGenerator.Mode?
    ) : TagLoader(dat) {
        override fun complete() {
            runOnUiThread(TagLoaderRunnable())
        }

        inner class TagLoaderRunnable : Runnable {
            override fun run() {
                val mapType: Int
                if (resource == null) {
                    showToast(
                        "Could not load world level.dat ${exception.javaClass.simpleName}: ${exception.message}",
                        true
                    )
                    finish()
                    return
                }
                try {
                    val time = resource.findTagByName(WorldGenerator.LAST_PLAYED)
                    GameTime.initTime((time.value as Long))
                    val playerTag = resource.findTagByName("Player")
                    val pos = playerTag.findTagByName("Pos")
                    val tl = pos.value as Array<Tag>
                    val p = Vector3f()
                    p.x = (tl[0].value as Double).toFloat()
                    p.y = (tl[1].value as Double).toFloat()
                    p.z = (tl[2].value as Double).toFloat()
                    val rotaionTag = playerTag.findTagByName("Rotation")
                    val rotation = Vector2f()
                    if (rotaionTag != null) {
                        val tl2 = rotaionTag.value as Array<Tag>
                        rotation.x = (tl2[0].value as Float)
                        rotation.y = (tl2[1].value as Float)
                        if (rotation.y > 1.5707964f || rotation.y < -1.5707964f) {
                            rotation.y = 0.0f
                        }
                    }
                    val mapTypeTag = resource.findTagByName(WorldGenerator.MAP_TYPE)
                    mapType = if (mapTypeTag != null) {
                        mapTypeTag.value as Int
                    } else {
                        -1
                    }
                    world = object : World(dir, p, resource) {
                        override fun isLoadingDialogVisible(): Boolean {
                            return loadingProgressDialog?.isShowing ?: false
                        }

                        override fun incLoadingProgressStatus(diff: Int) {
                            loadingProgressDialog?.incrementProgressBy(diff)
                        }

                        override fun setLoadingProgressStatus(progress: Int, max: Int) {
                            loadingProgressDialog?.apply {
                                this.max = max
                                this.progress = progress
                            }
                        }

                        override fun showGameMenu() {
                            runOnUiThread { showGameMenuDialog() }
                        }

                        override fun dismissLoadingDialog() {
                            loadingProgressDialog?.dismiss()
                        }

                        override fun dismissLoadingDialogAndWait() {
                            loadingProgressDialog?.let { dialog ->
                                if (dialog.isShowing) {
                                    val latch = CountDownLatch(1)
                                    dialog.setOnDismissListener { latch.countDown() }
                                    dialog.dismiss()
                                    try {
                                        latch.await(2L, TimeUnit.SECONDS)
                                    } catch (e: InterruptedException) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        }

                        override fun showChat() {
                            runOnUiThread { showChatDialog() }
                        }

                        override fun showReportAbuse() {
                            runOnUiThread {
                                val reportAbuseDialog = ReportAbuseDialog(this@WorldCraftActivity)
                                reportAbuseDialog.show()
                            }
                        }

                        override fun showDeathMenu(player: Player) {
                            runOnUiThread {
                                val dialog = deathMenuDialog
                                if (dialog == null || !dialog.isVisible) {
                                    deathMenuDialog = DeathMenuDialog(
                                        this@WorldCraftActivity,
                                        player
                                    ).apply { show() }
                                }
                            }
                        }
                    }.also { world ->
                        world.setNewGame(isNewGame)
                        world.mapType = mapType
                        (loadingProgressDialog as? CustomProgressDialog)?.updateMax(
                            World.getLoadingLimit(
                                isNewGame
                            )
                        )
                        bw = BlockView(world).also { blockView ->
                            if (isMultiplayerMode) {
                                Multiplayer.instance.blockView = blockView
                            }
                            blockView.setCamRotation(rotation)
                            blockView.cam.invert = Persistence.getInstance().isInvertY
                            setGameMode(if (isMultiplayerMode || WorldGenerator.Mode.SURVIVAL != gameMode) 1 else 0)
                            if (isSurvivalMode && !isMultiplayerMode) {
                                world.initSunLight()
                            }
                            initFog()
                            val game = Game(this@WorldCraftActivity, null, blockView)
                            start(game)
                        }
                    }

                } catch (e: Exception) {
                    showToast("Problem parsing level.dat - Maybe a corrupt file?", true)
                    Log.e(Game.RUGL_TAG, "Level.dat corrupted?", e)
                    finish()
                }
            }

            private fun initFog() {
                var fogDistance = Persistence.getInstance().fogDistance
                if (fogDistance < 0.0f) {
                    fogDistance = 0.0f
                }
                BlockFactory.state.fog.start = fogDistance
                BlockFactory.state.fog.end = 10.0f + fogDistance
            }
        }
    }
}