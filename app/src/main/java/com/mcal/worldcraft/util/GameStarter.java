package com.mcal.worldcraft.util;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.mcal.droid.rugl.util.WorldUtils;
import com.mcal.worldcraft.GameMode;
import com.mcal.worldcraft.MyApplication;
import com.mcal.worldcraft.activity.MainActivity;
import com.mcal.worldcraft.activity.WorldCraftActivity;
import com.mcal.worldcraft.factories.DescriptionFactory;

import java.io.File;

public class GameStarter {
    private static final String INTENT_EXTRA_GAME_MODE = "gameMode";
    private static final String INTENT_EXTRA_IS_NEW_GAME = "isNewGame";
    private static final String INTENT_EXTRA_WORLD = "world";

    public static void startGame(@NonNull MyApplication myApplication, Activity activity, String gameName, boolean isNewGame, int mapType, WorldGenerator.Mode gameMode) {
        if (myApplication.getCurrentActivity() == null) {
            Intent intent = new Intent(activity, WorldCraftActivity.class);
            if (GameMode.isMultiplayerMode()) {
                File world = new File(WorldUtils.WORLD_DIR, Properties.MULTIPLAYER_WORLD_NAME);
                intent.putExtra(INTENT_EXTRA_WORLD, world.getAbsolutePath());
            } else {
                if (isNewGame) {
                    gameName = WorldGenerator.generateRandomMap(gameName, mapType, gameMode);
                }
                intent.putExtra(INTENT_EXTRA_WORLD, gameName);
                intent.putExtra(INTENT_EXTRA_IS_NEW_GAME, isNewGame);
                intent.putExtra(INTENT_EXTRA_GAME_MODE, gameMode);
            }
            activity.startActivity(intent);
        }
    }

    private static String getMapTypeName(int mapType) {
        switch (mapType) {
            case MainActivity.MAP_TYPE_RANDOM:
                return "random";
            case MainActivity.MAP_TYPE_FLAT:
                return "flat";
            default: // MAP_TYPE_PREDEFINED
                return DescriptionFactory.emptyText;
        }
    }
}
