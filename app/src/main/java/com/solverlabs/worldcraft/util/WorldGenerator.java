package com.solverlabs.worldcraft.util;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.util.WorldUtils;
import com.solverlabs.worldcraft.World;
import com.solverlabs.worldcraft.activity.MainMenuActivity;
import com.solverlabs.worldcraft.factories.DescriptionFactory;
import com.solverlabs.worldcraft.nbt.Tag;

import org.jetbrains.annotations.Contract;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class WorldGenerator {
    public static final String BUILD_VERSION = "BuildVersion";
    public static final String GAME_TYPE = "GameType";
    public static final String LAST_PLAYED = "LastPlayed";
    public static final String MAP_TYPE = "MapType";

    public static String generateRandomMap(Activity activity, String worldName, int mapType, @NonNull Mode mode) {
        if (mode.equals(Mode.SURVIVAL)) {
            mapType = 0;
        }
        int i = 0;
        String path = DescriptionFactory.emptyText;
        File dir = new File(WorldUtils.WORLD_DIR, worldName);
        try {
            if (dir.exists()) {
                do {
                    dir = new File(WorldUtils.WORLD_DIR, worldName + i);
                    i++;
                } while (dir.exists());
                File dir2 = dir;

                dir2.mkdirs();
                WorldUtils.addWorld(dir2);
                File file = new File(dir2, World.LEVEL_DAT_FILE_NAME);
                file.createNewFile();
                path = file.getParent();
                OutputStream os = new FileOutputStream(file);

                Tag levelTag = createLevelTag(mode, mapType);
                levelTag.writeTo(os, true);
                File dir3 = new File(dir2, World.REGION_DIR_NAME);

                dir3.mkdir();
                new File(dir3, "r.0.0.mcr").createNewFile();

                return path;
            }
            File dir22 = dir;
            dir22.mkdirs();
            WorldUtils.addWorld(dir22);
            File file2 = new File(dir22, World.LEVEL_DAT_FILE_NAME);
            file2.createNewFile();
            path = file2.getParent();
            OutputStream os2 = new FileOutputStream(file2);
            Tag levelTag2 = createLevelTag(mode, mapType);
            levelTag2.writeTo(os2, true);
            File dir32 = new File(dir22, World.REGION_DIR_NAME);
            dir32.mkdir();
            new File(dir32, "r.0.0.mcr").createNewFile();
            return path;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }

    @NonNull
    @Contract("_, _ -> new")
    private static Tag createLevelTag(Mode mode, int mapType) {
        Tag[] tags = new Tag[9];
        tags[0] = new Tag(Tag.Type.TAG_Long, LAST_PLAYED, (Object) 0L);
        tags[1] = new Tag(Tag.Type.TAG_Int, "SpawnX", (Object) 50);
        tags[2] = new Tag(Tag.Type.TAG_Int, "SpawnY", (Object) 80);
        tags[3] = new Tag(Tag.Type.TAG_Int, "SpawnZ", (Object) 50);
        tags[4] = createPlayerTag();
        tags[5] = new Tag(Tag.Type.TAG_Int, GAME_TYPE, mode == Mode.CREATIVE ? 1 : 0);
        tags[6] = new Tag(Tag.Type.TAG_Int, MAP_TYPE, mapType);
        tags[7] = new Tag(Tag.Type.TAG_String, BUILD_VERSION, MainMenuActivity.version + "_J");
        tags[8] = new Tag(Tag.Type.TAG_End, (String) null, (Tag[]) null);
        Tag dataTag = new Tag(Tag.Type.TAG_Compound, "Data", tags);
        return new Tag(Tag.Type.TAG_Compound, DescriptionFactory.emptyText, new Tag[]{dataTag, new Tag(Tag.Type.TAG_End, (String) null, (Tag[]) null)});
    }

    @NonNull
    private static Tag createPlayerTag() {
        Tag[] tags = {new Tag("Pos", Tag.Type.TAG_Double), new Tag("Rotation", Tag.Type.TAG_Float), new Tag(Tag.Type.TAG_Short, "Health", (Object) (short) 20), new Tag("Inventory", Tag.Type.TAG_Compound), new Tag(Tag.Type.TAG_End, (String) null, (Tag[]) null)};
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
