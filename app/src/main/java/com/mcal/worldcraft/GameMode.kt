package com.mcal.worldcraft

import com.mcal.worldcraft.multiplayer.Multiplayer

object GameMode {
    private const val CREATIVE_GAME_MODE = 1
    private const val SURVIVAL_GAME_MODE = 0
    private var currentGameMode = CREATIVE_GAME_MODE

    @JvmStatic
    fun currentGameMode(): Int {
        return currentGameMode
    }

    @JvmStatic
    fun setGameMode(gameMode: Int) {
        currentGameMode = gameMode
    }

    @JvmStatic
    val isSurvivalMode: Boolean
        get() = currentGameMode == SURVIVAL_GAME_MODE

    @JvmStatic
    val isCreativeMode: Boolean
        get() = currentGameMode == CREATIVE_GAME_MODE

    @JvmStatic
    val isMultiplayerMode: Boolean
        get() = Multiplayer.instance.isInMultiplayerMode
}