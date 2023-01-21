package com.solverlabs.worldcraft.chunk;

import android.util.Log;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.Game;
import com.solverlabs.droid.rugl.res.ResourceLoader;
import com.solverlabs.worldcraft.GameMode;
import com.solverlabs.worldcraft.World;
import com.solverlabs.worldcraft.nbt.RegionFileCache;

import java.io.DataInputStream;

public abstract class ChunkLoader extends ResourceLoader.Loader<Chunk> {
    protected final World world;
    protected final int x;
    protected final int z;

    public ChunkLoader(World w, int x, int z) {
        this.world = w;
        this.x = x;
        this.z = z;
    }

    @Override
    public void load() {
        try {
            DataInputStream is = RegionFileCache.getChunkDataInputStream(this.world.dir, this.x, this.z);
            if (is != null) {
                this.resource = new Chunk(this.world, is);
                if (this.resource != null && (this.resource.chunkX != this.x || this.resource.chunkZ != this.z)) {
                    Log.e(Game.RUGL_TAG, "expected " + this + ", got " + this.resource);
                }
            }
            if (this.resource == null && !GameMode.isMultiplayerMode() && this.world.getMapType() != -1 && GameMode.isSurvivalMode()) {
                this.resource = new Chunk(this.world, this.x, this.z);
                if (this.world.getMapType() == 0) {
                    this.world.generateTerrain(this.resource, false);
                }
                if (this.world.getMapType() == 1) {
                    this.world.generateTerrain(this.resource, true);
                }
            }
        } catch (Exception e) {
            Log.e(Game.RUGL_TAG, "Problem loading chunk (" + this.x + "," + this.z + ")", e);
            this.exception = e;
            this.resource = null;
        }
    }

    @NonNull
    public String toString() {
        return "chunk " + this.x + ", " + this.z;
    }
}
