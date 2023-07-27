package com.mcal.worldcraft.activity

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import com.mcal.droid.rugl.Game
import com.mcal.droid.rugl.GameActivity
import com.mcal.droid.rugl.input.Touch
import com.mcal.droid.rugl.res.ResourceLoader
import com.mcal.droid.rugl.texture.TextureFactory
import com.mcal.droid.rugl.util.geom.Vector2f
import com.mcal.droid.rugl.util.geom.Vector3f
import com.mcal.worldcraft.*
import com.mcal.worldcraft.GameMode.isMultiplayerMode
import com.mcal.worldcraft.GameMode.isSurvivalMode
import com.mcal.worldcraft.GameMode.setGameMode
import com.mcal.worldcraft.dialog.DeathMenuDialog
import com.mcal.worldcraft.factories.BlockFactory
import com.mcal.worldcraft.multiplayer.Multiplayer
import com.mcal.worldcraft.multiplayer.dialogs.ReportAbuseDialog
import com.mcal.worldcraft.nbt.RegionFileCache
import com.mcal.worldcraft.nbt.Tag
import com.mcal.worldcraft.nbt.TagLoader
import com.mcal.worldcraft.ui.CustomProgressDialog
import com.mcal.worldcraft.utils.GameTime
import com.mcal.worldcraft.utils.WorldGenerator
import org.apache.commons.compress.archivers.cpio.CpioConstants
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class WorldCraftActivity : GameActivity() {
    private var bw: BlockView? = null
    private var isResumingGame = false
    private var loadingProgressDialog: ProgressDialog? = null
    private var resumeDialog: ProgressDialog? = null
    private var world: World? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(CpioConstants.C_IWUSR)

        intent.extras?.let { extras ->
            val isNewGame = extras.getBoolean("isNewGame")
            val gameMode = extras["gameMode"] as WorldGenerator.Mode

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

    override fun onResume() {
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
        var dialog = loadingProgressDialog
        if (dialog == null) {
            dialog = object : CustomProgressDialog(this) {
                @Deprecated("Deprecated in Java")
                override fun onBackPressed() {
                    world?.setCancel(true)
                    finish()
                }

                override fun buttonClick() {
                    world?.setCancel(true)
                    finish()
                }
            }.also { loadingProgressDialog = it }
        }
        dialog.show()
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
                    val rotationTag = playerTag.findTagByName("Rotation")
                    val rotation = Vector2f()
                    if (rotationTag != null) {
                        val tl2 = rotationTag.value as Array<Tag>
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
                                DeathMenuDialog(
                                    this@WorldCraftActivity,
                                    player
                                ).show()
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
                            start(Game(this@WorldCraftActivity, null, blockView))
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