package com.mcal.droid.rugl

import android.app.ProgressDialog
import android.content.DialogInterface
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mcal.droid.rugl.res.ResourceLoader
import com.mcal.worldcraft.Enemy
import com.mcal.worldcraft.GameMode.isMultiplayerMode
import com.mcal.worldcraft.R
import com.mcal.worldcraft.SoundManager
import com.mcal.worldcraft.activity.BaseActivity
import com.mcal.worldcraft.databinding.MenulayoutBinding
import com.mcal.worldcraft.factories.DescriptionFactory
import com.mcal.worldcraft.multiplayer.Multiplayer
import com.mcal.worldcraft.multiplayer.dialogs.PopupDialog
import com.mcal.worldcraft.ui.ChatBox
import com.mcal.worldcraft.utils.KeyboardUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

/**
 * Handy activity that can be simply subclassed. Just remember to call
 * [.start] in your
 * [.onCreate] or nothing will happen.
 * Handles starting the [ResourceLoader] and key input
 */
abstract class GameActivity : BaseActivity() {
    /**
     * The [Game]
     */
    protected var game: Game? = null
    private var loadingDialog: ProgressDialog? = null
    private var readOnlyMapNotificationDialog: AlertDialog? = null
    private var gameView: GameView? = null

    init {
        CoroutineScope(Dispatchers.IO).launch {
            while (isMultiplayerMode) {
                Multiplayer.pollPopupMessage()?.let { message ->
                    PopupDialog.showInUiThread(R.string.warning, message, this@GameActivity)
                }
                delay(1000L)
            }
        }
    }

    /**
     * Call this in your [.onCreate]
     * implementation
     *
     * @param game
     */
    fun start(game: Game) {
        this.game = game
        ResourceLoader.start(resources)
        setContentView(R.layout.activity_game)
        gameView = findViewById<GameView>(R.id.gameViewWithoutBanner).apply {
            init(game)
        }
    }

    /**
     * Displays a short message to the user
     *
     * @param message
     * @param longShow `true` for [Toast.LENGTH_LONG],
     * `false` for [Toast.LENGTH_SHORT]
     */
    fun showToast(message: String?, longShow: Boolean) {
        runOnUiThread {
            Toast.makeText(
                applicationContext,
                message,
                if (longShow) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun showGameMenuDialog() {
        val view = MenulayoutBinding.inflate(LayoutInflater.from(this))
        val playersList = view.playerListButton
        val dialog = MaterialAlertDialogBuilder(this).apply {
            setTitle("Game menu")
            setView(view.root)
        }.create()
        if (isMultiplayerMode) {
            playersList.visibility = View.VISIBLE
        } else {
            playersList.visibility = View.GONE
        }
        view.quitButton.setOnClickListener {
            if (isMultiplayerMode) {
                showLikeDialog()
            } else {
                completeCurrentPhase(true)
            }
            SoundManager.stopAllSounds()
            dialog.dismiss()
        }
        view.backButton.setOnClickListener { dialog.dismiss() }
        playersList.setOnClickListener {
            showPlayerList()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showPlayerList() {
        val view = View.inflate(this, R.layout.playerlist, null)
        val builder = MaterialAlertDialogBuilder(this)
        builder.setView(view)
        builder.setTitle("Player list       Room name:  " + Multiplayer.instance.roomName)
        val list = ArrayList<String>()
        list.add(Multiplayer.instance.playerName + "   (you)")
        val sortedEnemies: Set<Enemy> = TreeSet(Multiplayer.getEnemiesCopy())
        for (enemy in sortedEnemies) {
            list.add(enemy.name)
        }
        val dialog = builder.create()
        view.findViewById<ListView>(R.id.playerListView).apply {
            adapter = ArrayAdapter(
                this@GameActivity,
                R.layout.custom_list_content,
                R.id.list_content,
                list
            )
        }
        val cancelButton = view.findViewById<Button>(R.id.cancel)
        cancelButton.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showLoadingDialog(message: String) {
        try {
            runOnUiThread {
                if (!isFinishing) {
                    loadingDialog = ProgressDialog.show(
                        this@GameActivity,
                        DescriptionFactory.emptyText,
                        message,
                        true
                    ).apply {
                        setCancelable(false)
                        show()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun dismissLoadingDialog() {
        try {
            runOnUiThread {
                val dialog = loadingDialog
                if (dialog != null && !dialog.isShowing) {
                    dialog.dismiss()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveWorld(worldName: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                gameView?.game?.blockView?.saveWorld(worldName)
            } catch (th: Throwable) {
                th.printStackTrace()
            }
            try {
                completeCurrentPhase(true)
            } catch (th: Throwable) {
                th.printStackTrace()
            }
        }
    }

    fun completeCurrentPhase(needSaveWorld: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                gameView?.game?.blockView?.complete(needSaveWorld)
            } catch (th: Throwable) {
                th.printStackTrace()
            }
            try {
                Multiplayer.instance.shutdown()
                dismissLoadingDialog()
            } catch (th: Throwable) {
                th.printStackTrace()
            }
        }
    }

    fun showSaveWorldDialogOnConnectionLost() {
        showSaveWorldDialog(R.string.connection_lost_would_you_like_to_save_world)
    }

    fun showReadOnlyRoomModificationDialog() {
        showSaveWorldDialog(R.string.modification_read_only_world_warning, 17039360, false)
    }

    @JvmOverloads
    fun showSaveWorldDialog(
        stringId: Int = R.string.would_you_like_to_save_world,
        noButtonResourseId: Int = R.string.dont_save,
        completePhaseOnNoClick: Boolean = true
    ) {
        runOnUiThread {
            var dialog = readOnlyMapNotificationDialog
            if (dialog == null || !dialog.isShowing) {
                val input = EditText(this@GameActivity)
                input.setHint(R.string.world_name)
                input.setText(Multiplayer.instance.roomName)
                KeyboardUtils.hideKeyboardOnEnter(this@GameActivity, input)
                val builder = MaterialAlertDialogBuilder(this)
                builder.setTitle(R.string.save_world)
                builder.setMessage(stringId)
                builder.setView(input)
                builder.setCancelable(false)
                builder.setPositiveButton(R.string.save_world) { _: DialogInterface?, _: Int ->
                    val worldName = input.text.toString()
                    if (DescriptionFactory.emptyText != worldName && "null" != worldName) {
                        saveWorld(worldName)
                        hideKeyBoard(input)
                        return@setPositiveButton
                    }
                    Toast.makeText(this@GameActivity, R.string.wrong_world_name, Toast.LENGTH_LONG)
                        .show()
                }
                builder.setNeutralButton(noButtonResourseId) { _: DialogInterface?, _: Int ->
                    if (completePhaseOnNoClick) {
                        completeCurrentPhase(false)
                    }
                }
                dialog = builder.create().also {
                    readOnlyMapNotificationDialog = it
                }
                dialog.window?.setSoftInputMode(2)
                dialog.show()
            }
        }
    }

    private fun showLikeDialog() {
        MaterialAlertDialogBuilder(this).apply {
            setTitle(R.string.like_it)
            setMessage(R.string.did_you_like_this_world)
            setCancelable(false)
            setPositiveButton(R.string.like) { dialog: DialogInterface, id: Int ->
                Multiplayer.instance.likeWorld()
                dialog.dismiss()
                showSaveWorldDialog()
            }
            setNegativeButton(R.string.dislike) { dialog: DialogInterface, id: Int ->
                Multiplayer.instance.dislikeWorld()
                dialog.dismiss()
                showSaveWorldDialog()
            }
        }.show()
    }

    private fun sendChatMessage(msg: EditText) {
        val messageText = msg.text.toString()
        if (CHAT_COMMAND_HOME == messageText) {
            game?.blockView?.resetPlayerLocation()
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            Multiplayer.instance.gameClient?.let { gameClient ->
                if (msg.toString().isNotEmpty()) {
                    gameClient.chat(messageText)
                }
            }
        }
    }

    fun showChatDialog() {
        val msg = EditText(this).apply {
            filters = arrayOf<InputFilter>(LengthFilter(ChatBox.getMaxChatMessageLength()))
        }
        MaterialAlertDialogBuilder(this).apply {
            setTitle(R.string.your_message).setView(msg)
            setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                sendChatMessage(msg)
            }
            setNegativeButton(android.R.string.cancel) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
            create().apply {
                msg.setOnEditorActionListener { _: TextView?, _: Int, event: KeyEvent? ->
                    if (event == null || event.keyCode != 66) {
                        return@setOnEditorActionListener false
                    }
                    sendChatMessage(msg)
                    dismiss()
                    true
                }
            }
        }.show()
    }

    public override fun onPause() {
        super.onPause()
        dismissLoadingDialog()
        gameView?.onPause()
    }

    public override fun onResume() {
        super.onResume()
        gameView?.onResume()
        game?.resetTouches()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.repeatCount == 0) {
            gameView?.game?.blockView?.let { blockView ->
                if (keyCode == 4) {
                    showGameMenuDialog()
                } else {
                    blockView.onKeyDown(keyCode, event)
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        gameView?.game?.blockView?.onKeyUp(keyCode, event)
        return true
    }

    private fun hideKeyBoard(v: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(v.windowToken, 0)
    }

    companion object {
        private const val CHAT_COMMAND_HOME = "/home"
    }
}