package com.solverlabs.worldcraft.activity

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.solverlabs.droid.rugl.util.WorldUtils
import com.solverlabs.droid.rugl.util.WorldUtils.WorldInfo
import com.solverlabs.worldcraft.MyApplication
import com.solverlabs.worldcraft.R
import com.solverlabs.worldcraft.adapters.WorldListAdapter
import com.solverlabs.worldcraft.databinding.SingleplayerBinding
import com.solverlabs.worldcraft.util.GameStarter
import com.solverlabs.worldcraft.util.WorldGenerator
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == RESULT_FIRST_USER && resultCode == RESULT_OK && data.getBooleanExtra(
                SHOULD_FINISH,
                false
            )
        ) {
            finish()
        }
    }

    private fun onItemClick(worldInfo: WorldInfo) {
        GameStarter.startGame(
            (application as MyApplication),
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