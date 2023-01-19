package com.solverlabs.worldcraft.util

import android.app.Activity
import android.content.Intent
import com.solverlabs.droid.rugl.util.WorldUtils
import com.solverlabs.worldcraft.GameMode
import com.solverlabs.worldcraft.activity.WorldCraftActivity
import com.solverlabs.worldcraft.factories.DescriptionFactory
import java.io.File

object GameStarter {
    private const val INTENT_EXTRA_GAME_MODE = "gameMode"
    private const val INTENT_EXTRA_IS_NEW_GAME = "isNewGame"
    private const val INTENT_EXTRA_WORLD = "world"

    @JvmStatic
    fun startGame(
        activity: Activity,
        gameName: String,
        isNewGame: Boolean,
        mapType: Int,
        gameMode: WorldGenerator.Mode
    ) {
        var mapName = gameName
        val intent = Intent(activity, WorldCraftActivity::class.java)
        if (GameMode.isMultiplayerMode()) {
            val world = File(WorldUtils.WORLD_DIR, Properties.MULTIPLAYER_WORLD_NAME)
            intent.putExtra(INTENT_EXTRA_WORLD, world.absolutePath)
        } else {
            if (isNewGame) {
                mapName =
                    WorldGenerator.generateRandomMap(activity, gameName, mapType, gameMode)
            }
            intent.putExtra(INTENT_EXTRA_WORLD, mapName)
            intent.putExtra(INTENT_EXTRA_IS_NEW_GAME, isNewGame)
            intent.putExtra(INTENT_EXTRA_GAME_MODE, gameMode)
        }
        activity.startActivity(intent)
        activity.finish()
    }

    private fun getMapTypeName(mapType: Int): String {
        return when (mapType) {
            0 -> "random"
            1 -> "flat"
            else -> DescriptionFactory.emptyText
        }
    }
}