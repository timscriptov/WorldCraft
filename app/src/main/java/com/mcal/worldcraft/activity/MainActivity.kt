package com.mcal.worldcraft.activity

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.EditText
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mcal.droid.rugl.res.ResourceLoader
import com.mcal.worldcraft.GameMode
import com.mcal.worldcraft.Persistence
import com.mcal.worldcraft.databinding.ActivityMainBinding
import com.mcal.worldcraft.factories.DescriptionFactory
import com.mcal.worldcraft.multiplayer.MultiplayerActivityHelper
import com.mcal.worldcraft.utils.KeyboardUtils

class MainActivity : BaseActivity() {
    private lateinit var activityHelper: MultiplayerActivityHelper
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        title = "WorldCraft"
        Persistence.initPersistence(this)
        activityHelper = MultiplayerActivityHelper(this)
        ResourceLoader.start(resources)
        binding.singlePlyerButton.setOnClickListener {
            val intent = Intent(this@MainActivity, SinglePlayerActivity::class.java)
            startActivity(intent)
        }
        binding.multiplayerButton.setOnClickListener {
            if (Persistence.getInstance().isFirstTimeStarted) {
                showChangeNameDialog()
            } else {
                activityHelper.startMultiplayer()
            }
        }
        binding.optionButton.setOnClickListener {
            Persistence.getInstance().isFirstTimeStarted = false
            val i = Intent(this, SettingsActivity::class.java)
            startActivity(i)
        }
    }

    private fun showChangeNameDialog() {
        val name = EditText(this).apply {
            hint = Persistence.getInstance().playerName
        }
        KeyboardUtils.hideKeyboardOnEnter(this, name)

        MaterialAlertDialogBuilder(this).apply {
            setTitle("Please enter your name for multiplayer! You can change your name in option menu")
            setView(name).setPositiveButton("OK") { _: DialogInterface?, _: Int ->
                Persistence.getInstance().apply {
                    playerName = name.text.toString()
                    isFirstTimeStarted = false
                }
                activityHelper.startMultiplayer()
            }
        }.show()
    }

    override fun onResume() {
        activityHelper.onResume(this)
        super.onResume()
    }

    override fun onPause() {
        if (!GameMode.isMultiplayerMode) {
            activityHelper.onPause()
        }
        super.onPause()
    }

    companion object {
        const val MAP_TYPE_FLAT = 1
        const val MAP_TYPE_PREDEFINED = 2
        const val MAP_TYPE_RANDOM = 0

        @JvmField
        var version = DescriptionFactory.emptyText
    }
}