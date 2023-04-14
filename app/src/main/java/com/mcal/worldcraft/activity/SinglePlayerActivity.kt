package com.mcal.worldcraft.activity

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mcal.droid.rugl.util.WorldUtils
import com.mcal.droid.rugl.util.WorldUtils.WorldInfo
import com.mcal.worldcraft.R
import com.mcal.worldcraft.adapters.WorldListAdapter
import com.mcal.worldcraft.databinding.SingleplayerBinding
import com.mcal.worldcraft.utils.GameStarter
import com.mcal.worldcraft.utils.WorldGenerator
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class SinglePlayerActivity : BaseActivity(), RemoveMapListener {
    private lateinit var binding: SingleplayerBinding
    private lateinit var worldListAdapter: ItemAdapter<WorldListAdapter>
    private lateinit var fastApkAdapter: FastAdapter<WorldListAdapter>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SingleplayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        initWorldList()
        binding.backButton.setOnClickListener { finish() }
        binding.createButton.setOnClickListener {
            val intent = Intent(this@SinglePlayerActivity, NewGameSingleplayerActivity::class.java)
            startActivity(intent)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESULT_FIRST_USER && resultCode == RESULT_OK && data?.getBooleanExtra(
                SHOULD_FINISH,
                false
            ) == true
        ) {
            finish()
        }
    }

    private fun onItemClick(worldInfo: WorldInfo) {
        GameStarter.startGame(
            this,
            worldInfo.file.absolutePath,
            false,
            0,
            if (worldInfo.isCreative) WorldGenerator.Mode.CREATIVE else WorldGenerator.Mode.SURVIVAL
        )
        finish()
    }

    private fun initWorldList() {
        worldListAdapter = ItemAdapter()
        fastApkAdapter = FastAdapter.with(worldListAdapter)

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@SinglePlayerActivity, 2)
            adapter = fastApkAdapter
        }

        WorldUtils.getWorldListSortedByLastModification(this)
            .forEach { world ->
                println(world.toString())
                worldListAdapter.add(
                    WorldListAdapter()
                        .withId(world.modifiedAt)
                        .withListener(this)
                        .withIcon(if (world.isCreative) R.drawable.world_creative else R.drawable.world_survival)
                        .withWorldName(world.name)
                        .withGameTime(
                            DateFormat.format("MM/dd/yyyy hh:mmaa", world.modifiedAt).toString()
                        )
                        .withGameMode(if (world.isCreative) "Creative" else "Survival")
                        .withWorldInfo(world)
                )
            }

        fastApkAdapter.onClickListener =
            { _: View?, _: IAdapter<WorldListAdapter>, mainMenuItem: WorldListAdapter, _: Int ->
                mainMenuItem.worldInfo?.let { world ->
                    onItemClick(world)
                }
                true
            }
    }

    override fun removeWorld(position: Int, worldInfo: WorldInfo) {
        val dialog = MaterialAlertDialogBuilder(this)
        dialog.setTitle(
            getString(
                R.string.are_you_realy_want_to_delete_map,
                worldInfo.name
            )
        )
        dialog.setPositiveButton(R.string.yes) { _: DialogInterface?, _: Int ->
            worldInfo.file?.path?.takeIf { File(it).exists() }?.let {
                worldListAdapter.remove(position)
                fastApkAdapter.notifyItemRemoved(position)
                fastApkAdapter.notifyItemRangeChanged(position, fastApkAdapter.itemCount)
                CoroutineScope(Dispatchers.IO).launch {
                    File(it).deleteRecursively()
                }
            }
        }
        dialog.setNegativeButton(R.string.no, null)
        dialog.show()
    }

    companion object {
        const val SHOULD_FINISH = "shouldFinish"
    }
}