package com.solverlabs.worldcraft.activity

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.solverlabs.droid.rugl.res.ResourceLoader
import com.solverlabs.worldcraft.GameMode
import com.solverlabs.worldcraft.Persistence
import com.solverlabs.worldcraft.R
import com.solverlabs.worldcraft.databinding.ActivityMainBinding
import com.solverlabs.worldcraft.factories.DescriptionFactory
import com.solverlabs.worldcraft.multiplayer.MultiplayerActivityHelper
import com.solverlabs.worldcraft.util.KeyboardUtils

class MainActivity : BaseActivity() {
    private var displayHeight = 0
    private var displayWidth = 0

    private lateinit var activityHelper: MultiplayerActivityHelper
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DisplayMetrics().also { displaymetrics ->
            displayWidth = displaymetrics.widthPixels
            displayHeight = displaymetrics.heightPixels
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        title = "WorldCraft"
        Persistence.initPersistence(this)
        activityHelper = MultiplayerActivityHelper(this)
        ResourceLoader.start(resources)
        initButtons()
        initVersion()
    }

    private fun initVersion() {
        binding.versionTextView.text = versionName
    }

    private val versionName: String
        get() = try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            version = pInfo.versionName
            getString(R.string.version, pInfo.versionName)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            DescriptionFactory.emptyText
        }

    private fun initButtons() {
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
            val i = Intent(this, OptionActivity::class.java)
            startActivity(i)
        }
    }

    private fun showChangeNameDialog() {
        val name = EditText(this)
        name.hint = Persistence.getInstance().playerName
        KeyboardUtils.hideKeyboardOnEnter(this, name)

        val dialog = MaterialAlertDialogBuilder(this)
        dialog.setTitle("Please enter your name for multiplayer! You can change your name in option menu")
        dialog.setView(name).setPositiveButton("OK") { _: DialogInterface?, _: Int ->
            Persistence.getInstance().apply {
                playerName = name.text.toString()
                isFirstTimeStarted = false
            }
            activityHelper.startMultiplayer()
        }
        dialog.show()
    }

    override fun onResume() {
        activityHelper.onResume(this)
        super.onResume()
    }

    override fun onPause() {
        if (!GameMode.isMultiplayerMode()) {
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