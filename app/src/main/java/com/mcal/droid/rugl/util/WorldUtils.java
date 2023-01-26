package com.mcal.droid.rugl.util;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mcal.worldcraft.R;
import com.mcal.worldcraft.World;
import com.mcal.worldcraft.nbt.Tag;
import com.mcal.worldcraft.nbt.TagLoader;
import com.mcal.worldcraft.utils.Properties;
import com.mcal.worldcraft.utils.WorldGenerator;

import org.jetbrains.annotations.Contract;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Stack;
import java.util.TreeSet;

public class WorldUtils {
    private static final String WORLDS_HOME = "games/worldcraft/";
    private static final ArrayList<File> worlds = new ArrayList<>();
    private static final ArrayList<File> creativeModeWorlds = new ArrayList<>();
    private static final ArrayList<File> survivalModeWorlds = new ArrayList<>();
    private static final FileFilter DIR_FILTER = pathname -> pathname.isDirectory() && pathname.listFiles() != null;
    public static File WORLD_DIR = null;

    public static boolean isStorageAvailable(Context context) {
        try {
            searchSaves(context);
            return true;
        } catch (StorageNotFoundException e) {
            return false;
        }
    }

    public static void showStorageNotFoundDialog(final Activity activity) {
        if (activity == null) {
            Log.e("WorldCraft", "Activity is null in WorldUtils.showStorageNotFoundDialog() method");
        } else {
            activity.runOnUiThread(() -> {
                final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity);
                builder.setTitle(R.string.storage_not_found);
                builder.setNeutralButton(android.R.string.ok, null);
                builder.show();
            });
        }
    }

    private static File getWorldDir(@NonNull Context context) throws StorageNotFoundException {
        return context.getExternalFilesDir(null);
    }

    @Nullable
    @Contract(pure = true)
    public static File getWorld(int position) {
        return worlds.get(position);
    }

    public static void addWorld(File dir) {
        worlds.add(dir);
    }

    @NonNull
    public static ArrayList<File> getWorldList() {
        return new ArrayList<>(worlds);
    }

    public static ArrayList<File> getCreativeModeWorlds() {
        return creativeModeWorlds;
    }

    public static ArrayList<File> getSurvivalModeWorlds() {
        return survivalModeWorlds;
    }

    @NonNull
    public static Collection<WorldInfo> getWorldListSortedByLastModification(Context context) throws StorageNotFoundException {
        Collection<WorldInfo> result = new TreeSet<>((a, b) -> a.modifiedAt > b.modifiedAt ? -1 : 1);
        searchSaves(context);
        for (File file : getWorldList()) {
            result.add(getWorldInfo(file));
        }
        return result;
    }

    public static void searchSaves(Context context) throws StorageNotFoundException {
        worlds.clear();
        creativeModeWorlds.clear();
        survivalModeWorlds.clear();
        WORLD_DIR = getWorldDir(context);
        Stack<File> dirs = new Stack<>();
        File temp = WORLD_DIR;
        if (!temp.exists()) {
            temp.mkdirs();
        }
        dirs.add(temp);
        while (!dirs.isEmpty()) {
            File dir = dirs.pop();
            if (isWorld(dir)) {
                if (!Properties.MULTIPLAYER_WORLD_NAME.equals(dir.getName())) {
                    worlds.add(dir);
                    try {
                        WorldInfo worldInfo = getWorldInfo(dir);
                        if (worldInfo.isSurvival()) {
                            survivalModeWorlds.add(dir);
                        } else {
                            creativeModeWorlds.add(dir);
                        }
                    } catch (Throwable th) {
                        creativeModeWorlds.add(dir);
                    }
                }
            } else {
                File[] subDirs = dir.listFiles(DIR_FILTER);
                if (subDirs != null) {
                    Arrays.sort(subDirs);
                    dirs.addAll(Arrays.asList(subDirs));
                }
            }
        }
    }

    private static boolean isWorld(File dir) {
        try {
            for (File f : dir.listFiles()) {
                if (f != null && f.getName().equals(World.LEVEL_DAT_FILE_NAME)) {
                    return true;
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return false;
    }

    @NonNull
    private static WorldInfo getWorldInfo(final File file) {
        final WorldInfo worldInfo = new WorldInfo(file);
        TagLoader tagLoader = new TagLoader(new File(file, World.LEVEL_DAT_FILE_NAME)) {
            @Override
            public void complete() {
                Tag gameType;
                try {
                    if (resource != null && (gameType = resource.findTagByName(WorldGenerator.GAME_TYPE)) != null) {
                        worldInfo.isCreative = (Integer) gameType.getValue() == 1;
                    }
                    worldInfo.modifiedAt = WorldUtils.getLastModification(file);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        };
        try {
            tagLoader.load();
            tagLoader.complete();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return worldInfo;
    }

    public static long getLastModification(File file) {
        File regionDir = new File(file, World.REGION_DIR_NAME);
        long lastModified = 0;
        for (String fileName : regionDir.list()) {
            File mapFile = new File(regionDir, fileName);
            lastModified = Math.max(lastModified, mapFile.lastModified());
        }
        return lastModified;
    }

    public static class WorldInfo {
        public File file;
        public boolean isCreative = true;
        public long modifiedAt;
        public String name;

        public WorldInfo(@NonNull File file) {
            this.file = file;
            this.name = file.getName();
        }

        public boolean isCreative() {
            return this.isCreative;
        }

        public boolean isSurvival() {
            return !this.isCreative;
        }

        @NonNull
        public String toString() {
            return "[name: " + this.name + "; modified_at: " + this.modifiedAt + "; is_creative: " + this.isCreative;
        }
    }

    public static class StorageNotFoundException extends IOException {
        public StorageNotFoundException() {
            super("Storage not found");
        }
    }
}