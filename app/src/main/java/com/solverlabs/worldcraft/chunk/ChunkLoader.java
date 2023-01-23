package com.solverlabs.worldcraft.chunk;

import android.util.Log;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.Game;
import com.solverlabs.droid.rugl.res.ResourceLoader;
import com.solverlabs.worldcraft.GameMode;
import com.solverlabs.worldcraft.World;
import com.solverlabs.worldcraft.nbt.RegionFileCache;

import java.io.DataInputStream;

/**
 * This is packaged up like this so it can happen on the resource loading
 * thread, rather than the main render thread
 */
public abstract class ChunkLoader extends ResourceLoader.Loader<Chunk> {
    protected final World world;
    protected final int x;
    protected final int z;

    /**
     * @param w
     * @param x
     * @param z
     */
    public ChunkLoader(World w, int x, int z) {
        world = w;
        this.x = x;
        this.z = z;
    }

    @Override
    public void load() {
        try {
            DataInputStream is = RegionFileCache.getChunkDataInputStream(world.dir, x, z);
            if (is != null) {
                resource = new Chunk(world, is);
                if (resource != null && (resource.chunkX != x || resource.chunkZ != z)) {
                    Log.e(Game.RUGL_TAG, "expected " + this + ", got " + resource);
                }
            }
            if (resource == null && !GameMode.isMultiplayerMode() && world.getMapType() != -1 && GameMode.isSurvivalMode()) {
                resource = new Chunk(world, x, z);
                if (world.getMapType() == 0) {
                    world.generateTerrain(resource, false);
                }
                if (world.getMapType() == 1) {
                    world.generateTerrain(resource, true);
                }
            }
        } catch (Exception e) {
            Log.e(Game.RUGL_TAG, "Problem loading chunk (" + x + "," + z + ")", e);
            exception = e;
            resource = null;
        }
    }

    @NonNull
    public String toString() {
        return "chunk " + x + ", " + z;
    }
}
