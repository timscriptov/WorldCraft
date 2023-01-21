package com.solverlabs.worldcraft.nbt;

import androidx.annotation.NonNull;

import com.solverlabs.worldcraft.World;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.util.Hashtable;

public class RegionFileCache {
    private static final int MAX_CACHE_SIZE = 256;
    private static final Hashtable<File, RegionFile> cache = new Hashtable<>();

    private RegionFileCache() {
    }

    @NonNull
    public static RegionFile getRegionFile(File basePath, int chunkX, int chunkZ) {
        File regionDir = new File(basePath, World.REGION_DIR_NAME);
        File file = new File(regionDir, "r." + (chunkX >> 5) + "." + (chunkZ >> 5) + ".mcr");
        RegionFile reg = cache.get(file);
        if (reg != null) {
            return reg;
        }
        if (!regionDir.exists()) {
            regionDir.mkdirs();
        }
        if (cache.size() >= MAX_CACHE_SIZE) {
            clear();
        }
        RegionFile reg2 = new RegionFile(file);
        cache.put(file, reg2);
        return reg2;
    }

    public static void clear() {
        cache.clear();
    }

    public static int getSizeDelta(File basePath, int chunkX, int chunkZ) {
        RegionFile r = getRegionFile(basePath, chunkX, chunkZ);
        return r.getSizeDelta();
    }

    public static DataInputStream getChunkDataInputStream(File basePath, int chunkX, int chunkZ) {
        RegionFile r = getRegionFile(basePath, chunkX, chunkZ);
        return r.getChunkDataInputStream(chunkX & 31, chunkZ & 31);
    }

    public static DataOutputStream getChunkDataOutputStream(File basePath, int chunkX, int chunkZ) {
        RegionFile r = getRegionFile(basePath, chunkX, chunkZ);
        return r.getChunkDataOutputStream(chunkX & 31, chunkZ & 31);
    }
}
