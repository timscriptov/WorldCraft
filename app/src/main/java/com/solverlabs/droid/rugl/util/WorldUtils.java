package com.solverlabs.droid.rugl.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.Log;

import com.solverlabs.worldcraft.R;
import com.solverlabs.worldcraft.World;
import com.solverlabs.worldcraft.nbt.Tag;
import com.solverlabs.worldcraft.nbt.TagLoader;
import com.solverlabs.worldcraft.util.Properties;
import com.solverlabs.worldcraft.util.WorldGenerator;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Stack;
import java.util.TreeSet;

/* loaded from: classes.dex */
public class WorldUtils {
    private static final String WORLDS_HOME = "games/worldcraft/";
    public static File WORLD_DIR = null;
    private static ArrayList<File> worlds = new ArrayList<>();
    private static ArrayList<File> creativeModeWorlds = new ArrayList<>();
    private static ArrayList<File> survivalModeWorlds = new ArrayList<>();
    private static FileFilter DIR_FILTER = new FileFilter() { // from class: com.solverlabs.droid.rugl.util.WorldUtils.1
        @Override // java.io.FileFilter
        public boolean accept(File pathname) {
            return pathname.isDirectory() && pathname.listFiles() != null;
        }
    };

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
            activity.runOnUiThread(new Runnable() { // from class: com.solverlabs.droid.rugl.util.WorldUtils.2
                @Override // java.lang.Runnable
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle(R.string.storage_not_found).setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() { // from class: com.solverlabs.droid.rugl.util.WorldUtils.2.1
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });
        }
    }

    private static File getWorldDir(Context context) throws StorageNotFoundException {
        return context.getExternalFilesDir(null);
    }

    private static void testDir(File worldDir) throws IOException {
        File testFile = new File(worldDir, "test.file");
        testFile.createNewFile();
        testFile.delete();
    }

    private static File getExternalStorage() {
        return new File(Environment.getExternalStorageDirectory() + "/" + WORLDS_HOME);
    }

    private static File getInternalStorage(Context context) {
        ContextWrapper contextWrapper = new ContextWrapper(context);
        return contextWrapper.getDir("games", 0);
    }

    public static File getWorld(int position) {
        if (worlds != null) {
            return worlds.get(position);
        }
        return null;
    }

    public static void addWorld(File dir) {
        if (worlds != null) {
            worlds.add(dir);
        }
    }

    public static ArrayList<File> getWorldList() {
        ArrayList<File> result = new ArrayList<>();
        if (worlds != null) {
            result.addAll(worlds);
        }
        return result;
    }

    public static ArrayList<File> getCreativeModeWorlds() {
        return creativeModeWorlds;
    }

    public static ArrayList<File> getSurvivalModeWorlds() {
        return survivalModeWorlds;
    }

    public static Collection<WorldInfo> getWorldListSortedByLastModification(Context context) throws StorageNotFoundException {
        Collection<WorldInfo> result = new TreeSet<>(new Comparator<WorldInfo>() { // from class: com.solverlabs.droid.rugl.util.WorldUtils.3
            @Override // java.util.Comparator
            public int compare(WorldInfo a, WorldInfo b) {
                return a.modifiedAt > b.modifiedAt ? -1 : 1;
            }
        });
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
                    for (File subDir : subDirs) {
                        dirs.add(subDir);
                    }
                }
            }
        }
    }

    private static boolean isWorld(File dir) {
        try {
            File[] arr$ = dir.listFiles();
            for (File f : arr$) {
                if (f != null && f.getName().equals(World.LEVEL_DAT_FILE_NAME)) {
                    return true;
                }
            }
        } catch (Throwable th) {
        }
        return false;
    }

    private static WorldInfo getWorldInfo(final File file) {
        final WorldInfo worldInfo = new WorldInfo(file);
        TagLoader tagLoader = new TagLoader(new File(file, World.LEVEL_DAT_FILE_NAME)) { // from class: com.solverlabs.droid.rugl.util.WorldUtils.4
            @Override // com.solverlabs.droid.rugl.res.ResourceLoader.Loader
            public void complete() {
                Tag gameType;
                try {
                    if (this.resource != null && (gameType = ((Tag) this.resource).findTagByName(WorldGenerator.GAME_TYPE)) != null) {
                        worldInfo.isCreative = ((Integer) gameType.getValue()).intValue() == 1;
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
        String[] arr$ = regionDir.list();
        for (String fileName : arr$) {
            File mapFile = new File(regionDir, fileName);
            lastModified = Math.max(lastModified, mapFile.lastModified());
        }
        return lastModified;
    }

    /* loaded from: classes.dex */
    public static class WorldInfo {
        public File file;
        public boolean isCreative = true;
        public long modifiedAt;
        public String name;

        public WorldInfo(File file) {
            this.file = file;
            this.name = file.getName();
        }

        public boolean isCreative() {
            return this.isCreative;
        }

        public boolean isSurvival() {
            return !this.isCreative;
        }

        public String toString() {
            return "[name: " + this.name + "; modified_at: " + this.modifiedAt + "; is_creative: " + this.isCreative;
        }
    }

    /* loaded from: classes.dex */
    public static class StorageNotFoundException extends IOException {
        public StorageNotFoundException() {
            super("Storage not found");
        }
    }
}