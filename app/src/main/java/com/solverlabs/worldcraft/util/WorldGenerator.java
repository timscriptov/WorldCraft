package com.solverlabs.worldcraft.util;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.solverlabs.droid.rugl.util.WorldUtils;
import com.solverlabs.worldcraft.World;
import com.solverlabs.worldcraft.activity.MainMenuActivity;
import com.solverlabs.worldcraft.factories.DescriptionFactory;
import com.solverlabs.worldcraft.nbt.Tag;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class WorldGenerator {
    public static final String BUILD_VERSION = "BuildVersion";
    public static final String GAME_TYPE = "GameType";
    public static final String LAST_PLAYED = "LastPlayed";
    public static final String MAP_TYPE = "MapType";

    @Nullable
    public static String generateRandomMap(String worldName, int mapType, @NonNull Mode mode) {
        int i = 0;
        String path = DescriptionFactory.emptyText;
        File worldDir = new File(WorldUtils.WORLD_DIR, worldName);
        if (worldDir.exists()) {
            do {
                worldDir = new File(WorldUtils.WORLD_DIR, worldName + i);
                i++;
            } while (worldDir.exists());
        }
        try {
            worldDir.mkdirs();
            WorldUtils.addWorld(worldDir);
            final File levelDatFile = new File(worldDir, World.LEVEL_DAT_FILE_NAME);
            levelDatFile.createNewFile();
            path = levelDatFile.getParent();
            createLevelTag(mode, mapType).writeTo(new FileOutputStream(levelDatFile), true);
            final File regionDir = new File(worldDir, World.REGION_DIR_NAME);
            regionDir.mkdir();
            new File(regionDir, "r.0.0.mcr").createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }

    @NonNull
    private static Tag createLevelTag(Mode mode, int mapType) {
        Tag[] tags = new Tag[9];
        tags[0] = new Tag(Tag.Type.TAG_Long, LAST_PLAYED, 0L);
        tags[1] = new Tag(Tag.Type.TAG_Int, "SpawnX", 50);
        tags[2] = new Tag(Tag.Type.TAG_Int, "SpawnY", 80);
        tags[3] = new Tag(Tag.Type.TAG_Int, "SpawnZ", 50);
        tags[4] = createPlayerTag();
        tags[5] = new Tag(Tag.Type.TAG_Int, GAME_TYPE, mode == Mode.CREATIVE ? 1 : 0);
        tags[6] = new Tag(Tag.Type.TAG_Int, MAP_TYPE, mapType);
        tags[7] = new Tag(Tag.Type.TAG_String, BUILD_VERSION, MainMenuActivity.version + "_J");
        tags[8] = new Tag(Tag.Type.TAG_End, null, null);
        Tag dataTag = new Tag(Tag.Type.TAG_Compound, "Data", tags);
        return new Tag(Tag.Type.TAG_Compound, DescriptionFactory.emptyText,
                new Tag[]{dataTag, new Tag(Tag.Type.TAG_End, null, null)}
        );
    }

    @NonNull
    private static Tag createPlayerTag() {
        Tag[] tags = {
                new Tag("Pos", Tag.Type.TAG_Double),
                new Tag("Rotation", Tag.Type.TAG_Float),
                new Tag(Tag.Type.TAG_Short, "Health", (short) 20),
                new Tag("Inventory", Tag.Type.TAG_Compound),
                new Tag(Tag.Type.TAG_End, null, null)};
        tags[0].addTag(new Tag(Tag.Type.TAG_Double, "x", 50.0d));
        tags[0].addTag(new Tag(Tag.Type.TAG_Double, "y", 80.0d));
        tags[0].addTag(new Tag(Tag.Type.TAG_Double, "z", 50.0d));
        tags[1].addTag(new Tag(Tag.Type.TAG_Float, "heading", 0.0f));
        tags[1].addTag(new Tag(Tag.Type.TAG_Float, "elevation", 0.0f));
        return new Tag(Tag.Type.TAG_Compound, "Player", tags);
    }

    public enum Mode {
        SURVIVAL,
        CREATIVE
    }
}
