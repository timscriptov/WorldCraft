package com.solverlabs.droid.rugl.util;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.solverlabs.worldcraft.R;
import com.solverlabs.worldcraft.World;
import com.solverlabs.worldcraft.nbt.Tag;
import com.solverlabs.worldcraft.nbt.TagLoader;
import com.solverlabs.worldcraft.util.Properties;
import com.solverlabs.worldcraft.util.WorldGenerator;

import org.jetbrains.annotations.Contract;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Stack;
import java.util.TreeSet;


public class WorldUtils {
    private static final String WORLDS_HOME = "games/worldcraft/";
    private static final ArrayList<File> mWorlds = new ArrayList<>();
    private static final ArrayList<File> mCreativeModeWorlds = new ArrayList<>();
    private static final ArrayList<File> mSurvivalModeWorlds = new ArrayList<>();
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
                MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(activity);
                materialAlertDialogBuilder.setTitle(R.string.storage_not_found);
                materialAlertDialogBuilder.setNeutralButton(android.R.string.ok, null);
                materialAlertDialogBuilder.show();
            });
        }
    }

    private static File getWorldDir(@NonNull Context context) {
        return context.getExternalFilesDir(null);
    }

    private static File getInternalStorage(Context context) {
        ContextWrapper contextWrapper = new ContextWrapper(context);
        return contextWrapper.getDir("games", 0);
    }

    @Nullable
    @Contract(pure = true)
    public static File getWorld(int position) {
        return mWorlds.get(position);
    }

    public static void addWorld(File dir) {
        mWorlds.add(dir);
    }

    @NonNull
    public static ArrayList<File> getWorldList() {
        ArrayList<File> result = new ArrayList<>();
        result.addAll(mWorlds);
        return result;
    }

    public static ArrayList<File> getmCreativeModeWorlds() {
        return mCreativeModeWorlds;
    }

    public static ArrayList<File> getmSurvivalModeWorlds() {
        return mSurvivalModeWorlds;
    }

    @NonNull
    public static Collection<WorldInfo> getWorldListSortedByLastModification(Context context) throws StorageNotFoundException {
        Collection<WorldInfo> result = new TreeSet<>((a, b) -> a.mModifiedAt > b.mModifiedAt ? -1 : 1);
        searchSaves(context);
        for (File file : getWorldList()) {
            result.add(getWorldInfo(file));
        }
        return result;
    }

    public static void searchSaves(Context context) throws StorageNotFoundException {
        mWorlds.clear();
        mCreativeModeWorlds.clear();
        mSurvivalModeWorlds.clear();
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
                    mWorlds.add(dir);
                    try {
                        WorldInfo worldInfo = getWorldInfo(dir);
                        if (worldInfo.isSurvival()) {
                            mSurvivalModeWorlds.add(dir);
                        } else {
                            mCreativeModeWorlds.add(dir);
                        }
                    } catch (Throwable th) {
                        mCreativeModeWorlds.add(dir);
                    }
                }
            } else {
                File[] subDirs = dir.listFiles(DIR_FILTER);
                if (subDirs != null) {
                    Arrays.sort(subDirs);
                    Collections.addAll(dirs, subDirs);
                }
            }
        }
    }

    private static boolean isWorld(File dir) {
        try {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f != null && f.getName().equals(World.LEVEL_DAT_FILE_NAME)) {
                        return true;
                    }
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
                    if (this.resource != null && (gameType = ((Tag) this.resource).findTagByName(WorldGenerator.GAME_TYPE)) != null) {
                        worldInfo.mIsCreative = (Integer) gameType.getValue() == 1;
                    }
                    worldInfo.mModifiedAt = WorldUtils.getLastModification(file);
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
        String[] files = regionDir.list();
        if (files != null) {
            for (String fileName : files) {
                File mapFile = new File(regionDir, fileName);
                lastModified = Math.max(lastModified, mapFile.lastModified());
            }
        }
        return lastModified;
    }


    public static class WorldInfo {
        public File mFile;
        public boolean mIsCreative = true;
        public long mModifiedAt;
        public String mName;

        public WorldInfo(@NonNull File file) {
            this.mFile = file;
            this.mName = file.getName();
        }

        public boolean ismIsCreative() {
            return this.mIsCreative;
        }

        public boolean isSurvival() {
            return !this.mIsCreative;
        }

        @NonNull
        public String toString() {
            return "[name: " + this.mName + "; modified_at: " + this.mModifiedAt + "; is_creative: " + this.mIsCreative;
        }
    }


    public static class StorageNotFoundException extends IOException {
        public StorageNotFoundException() {
            super("Storage not found");
        }
    }
}
