package com.solverlabs.worldcraft;

import com.solverlabs.worldcraft.multiplayer.Multiplayer;


public class GameMode {
    public static final int CREATIVE_GAME_MODE = 1;
    public static final int SURVIVAL_GAME_MODE = 0;
    private static int currentGameMode = 1;

    public static int currentGameMode() {
        return currentGameMode;
    }

    public static void setGameMode(int gameMode) {
        currentGameMode = gameMode;
    }

    public static boolean isSurvivalMode() {
        return currentGameMode == 0;
    }

    public static boolean isCreativeMode() {
        return currentGameMode == 1;
    }

    public static boolean isMultiplayerMode() {
        return Multiplayer.instance.isInMultiplayerMode;
    }
}
