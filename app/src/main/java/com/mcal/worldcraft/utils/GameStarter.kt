package com.mcal.worldcraft.utils

import android.app.Activity
import android.content.Intent
import com.mcal.droid.rugl.util.WorldUtils
import com.mcal.worldcraft.GameMode.isMultiplayerMode
import com.mcal.worldcraft.MyApplication
import com.mcal.worldcraft.activity.MainActivity
import com.mcal.worldcraft.activity.WorldCraftActivity
import com.mcal.worldcraft.factories.DescriptionFactory
import java.io.File

object GameStarter {
    private const val INTENT_EXTRA_GAME_MODE = "gameMode"
    private const val INTENT_EXTRA_IS_NEW_GAME = "isNewGame"
    private const val INTENT_EXTRA_WORLD = "world"

    @JvmStatic
    fun startGame(
        myApplication: MyApplication,
        activity: Activity,
        gameName: String?,
        isNewGame: Boolean,
        mapType: Int,
        gameMode: WorldGenerator.Mode?
    ) {
        var worldName = gameName
        if (myApplication.currentActivity == null) {
            val intent = Intent(activity, WorldCraftActivity::class.java)
            if (isMultiplayerMode) {
                val world = File(WorldUtils.WORLD_DIR, Properties.MULTIPLAYER_WORLD_NAME)
                intent.putExtra(INTENT_EXTRA_WORLD, world.absolutePath)
            } else {
                if (isNewGame) {
                    worldName = WorldGenerator.generateRandomMap(worldName, mapType, gameMode!!)
                }
                intent.putExtra(INTENT_EXTRA_WORLD, worldName)
                intent.putExtra(INTENT_EXTRA_IS_NEW_GAME, isNewGame)
                intent.putExtra(INTENT_EXTRA_GAME_MODE, gameMode)
            }
            activity.startActivity(intent)
        }
    }

    private fun getMapTypeName(mapType: Int): String {
        return when (mapType) {
            MainActivity.MAP_TYPE_RANDOM -> "random"
            MainActivity.MAP_TYPE_FLAT -> "flat"
            else -> DescriptionFactory.emptyText
        }
    }
}