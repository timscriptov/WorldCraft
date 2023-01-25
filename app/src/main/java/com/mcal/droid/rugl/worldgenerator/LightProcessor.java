package com.mcal.droid.rugl.worldgenerator;

import android.util.Log;

import androidx.annotation.NonNull;

import com.mcal.worldcraft.World;
import com.mcal.worldcraft.chunk.Chunk;
import com.mcal.worldcraft.chunk.Chunklet;
import com.mcal.worldcraft.factories.BlockFactory;

import java.util.ArrayList;
import java.util.Arrays;


public class LightProcessor {
    private static final byte FURNACELIGHT = 13;
    private static final byte SUNLIGHT = 15;
    private static final byte TORCHLIGHT = 14;
    private final ArrayList<Chunklet> dirtyChunklet = new ArrayList<>();
    private final World world;

    public LightProcessor(World world) {
        this.world = world;
    }

    public void lightChunk(Chunk chunk) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 127; y >= 0; y--) {
                    if (isSunlitBlock(chunk, x, y, z)) {
                        setLightingAroundBlock(x, y, z, 15, chunk);
                    }
                }
            }
        }
    }

    public void setLightingAroundBlock(int x, int y, int z, int lightValue, Chunk chunk) {
        setSkyLightAroundBlockRecursive(x - 1, y, z, lightValue, chunk);
        setSkyLightAroundBlockRecursive(x + 1, y, z, lightValue, chunk);
        setSkyLightAroundBlockRecursive(x, y, z + 1, lightValue, chunk);
        setSkyLightAroundBlockRecursive(x, y, z - 1, lightValue, chunk);
        setSkyLightAroundBlockRecursive(x, y + 1, z, lightValue, chunk);
        setSkyLightAroundBlockRecursive(x, y - 1, z, lightValue, chunk);
    }

    private void setSkyLightAroundBlockRecursive(int x, int y, int z, int lightValue, Chunk chunk) {
        if (y < 128 && y >= 0 && x >= 0 && x <= 15 && z >= 0 && z <= 15 && lightValue != 0 && chunk != null) {
            byte blockType = chunk.blockType(x, y, z);
            if (!BlockFactory.opaque(blockType) && chunk.skyLight(x, y, z) < lightValue - 1) {
                chunk.setSkyLightForBlock(x, y, z, lightValue);
                setSkyLightAroundBlockRecursive(x - 1, y, z, lightValue, chunk);
                setSkyLightAroundBlockRecursive(x + 1, y, z, lightValue, chunk);
                setSkyLightAroundBlockRecursive(x, y, z + 1, lightValue, chunk);
                setSkyLightAroundBlockRecursive(x, y, z - 1, lightValue, chunk);
                setSkyLightAroundBlockRecursive(x, y + 1, z, lightValue, chunk);
                setSkyLightAroundBlockRecursive(x, y - 1, z, lightValue, chunk);
            }
        }
    }

    public void lightSunlitBlocksInChunk(@NonNull Chunk chunk) {
        Arrays.fill(chunk.skylight, (byte) 0);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int y = 127;
                byte blockType = chunk.blockType(x, 127, z);
                while (y >= 0 && (blockType == 0 || !BlockFactory.getBlock(blockType).opaque)) {
                    chunk.setSkyLightForBlock(x, y, z, 15);
                    y--;
                    blockType = chunk.blockType(x, y, z);
                }
            }
        }
    }

    public void recalculateSkyLightingAround(@NonNull Chunk chunk, int x, int y, int z) {
        int light = 0;
        try {
            BlockFactory.Block b = BlockFactory.getBlock(chunk.blockType(x, y, z));
            if ((chunk.blockType(x, y, z) == 0 || (b != null && !b.opaque)) && (light = Math.max(Math.max(Math.max(Math.max(Math.max(chunk.skyLight(x + 1, y, z) - 1, chunk.skyLight(x - 1, y, z) - 1), chunk.skyLight(x, y, z + 1) - 1), chunk.skyLight(x, y, z - 1) - 1), chunk.skyLight(x, y + 1, z)), chunk.skyLight(x, y - 1, z) - 1)) < 0) {
                light = 0;
            }
            chunk.setSkyLightForBlock(x, y, z, light);
            recalculateSkyLightRecursive(chunk, x + 1, y, z);
            recalculateSkyLightRecursive(chunk, x - 1, y, z);
            recalculateSkyLightRecursive(chunk, x, y, z + 1);
            recalculateSkyLightRecursive(chunk, x, y, z - 1);
            recalculateSkyLightRecursive(chunk, x, y + 1, z);
            recalculateSkyLightRecursive(chunk, x, y - 1, z);
        } catch (StackOverflowError e) {
            Log.d("WRLD", "StackOverflowError   " + Thread.currentThread().countStackFrames());
        }
    }

    private void recalculateSkyLightRecursive(@NonNull Chunk chunk, int x, int y, int z) {
        int wx = (chunk.chunkX * 16) + x;
        int wz = (chunk.chunkZ * 16) + z;
        Chunklet dirty = this.world.getChunklet(wx, y, wz);
        BlockFactory.Block b = BlockFactory.getBlock(chunk.blockType(x, y, z));
        if ((dirty != null && chunk.blockType(x, y, z) == 0) || (b != null && !b.opaque)) {
            int light = Math.max(Math.max(Math.max(Math.max(Math.max(chunk.skyLight(x + 1, y, z) - 1, chunk.skyLight(x - 1, y, z) - 1), chunk.skyLight(x, y, z + 1) - 1), chunk.skyLight(x, y, z - 1) - 1), chunk.skyLight(x, y + 1, z)), chunk.skyLight(x, y - 1, z) - 1);
            if (light <= 0) {
                chunk.setSkyLightForBlock(x, y, z, 0);
            } else if (chunk.skyLight(x, y, z) != light && chunk.blockLight(x, y, z) <= light) {
                chunk.setSkyLightForBlock(x, y, z, light);
                if (!this.dirtyChunklet.contains(dirty)) {
                    this.dirtyChunklet.add(dirty);
                }
                recalculateSkyLightRecursive(chunk, x + 1, y, z);
                recalculateSkyLightRecursive(chunk, x - 1, y, z);
                recalculateSkyLightRecursive(chunk, x, y, z + 1);
                recalculateSkyLightRecursive(chunk, x, y, z - 1);
                recalculateSkyLightRecursive(chunk, x, y + 1, z);
                recalculateSkyLightRecursive(chunk, x, y - 1, z);
            }
        }
    }

    private boolean isSunlitBlock(@NonNull Chunk chunk, int x, int y, int z) {
        return chunk.skyLight(x, y + 1, z) == 15 && chunk.blockType(x, y + 1, z) == 0;
    }

    public void recalculateBlockLightAround(@NonNull Chunk chunk, int x, int y, int z) {
        try {
            int wx = (chunk.chunkX * 16) + x;
            int wz = (chunk.chunkZ * 16) + z;
            if (chunk.blockType(x, y, z) == BlockFactory.Block.Torch.id) {
                chunk.setLightForBlock(x, y, z, 14);
                recalculateBlockLightAround(wx, y, wz, 14);
            } else if (chunk.blockType(x, y, z) == 119) {
                chunk.setLightForBlock(x, y, z, 13);
                recalculateBlockLightAround(wx, y, wz, 13);
            } else {
                int light = Math.max(Math.max(Math.max(Math.max(Math.max(chunk.blockLight(x + 1, y, z), chunk.blockLight(x - 1, y, z)), chunk.blockLight(x, y, z + 1)), chunk.blockLight(x, y, z - 1)), chunk.blockLight(x, y + 1, z)), chunk.blockLight(x, y - 1, z)) - 1;
                if (light <= 0) {
                    light = 0;
                }
                chunk.setLightForBlock(x, y, z, light);
                setBlockLightRecursive(chunk, x + 1, y, z);
                setBlockLightRecursive(chunk, x - 1, y, z);
                setBlockLightRecursive(chunk, x, y, z + 1);
                setBlockLightRecursive(chunk, x, y, z - 1);
                setBlockLightRecursive(chunk, x, y + 1, z);
                setBlockLightRecursive(chunk, x, y - 1, z);
            }
        } catch (StackOverflowError e) {
            Log.d("BLOCKLIGHT", "StackOverflowError");
        }
    }

    private void setBlockLightRecursive(@NonNull Chunk chunk, int x, int y, int z) {
        int wx = (chunk.chunkX * 16) + x;
        int wz = (chunk.chunkZ * 16) + z;
        Chunklet dirty = this.world.getChunklet(wx, y, wz);
        if (dirty != null) {
            if (chunk.blockType(x, y, z) == 0 || chunk.blockType(x, y, z) == 61) {
                int light = Math.max(Math.max(Math.max(Math.max(Math.max(chunk.blockLight(x + 1, y, z), chunk.blockLight(x - 1, y, z)), chunk.blockLight(x, y, z + 1)), chunk.blockLight(x, y, z - 1)), chunk.blockLight(x, y + 1, z)), chunk.blockLight(x, y - 1, z)) - 1;
                if (light <= 0) {
                    light = 0;
                }
                if (chunk.blockLight(x, y, z) != light) {
                    chunk.setLightForBlock(x, y, z, light);
                    if (!this.dirtyChunklet.contains(dirty)) {
                        this.dirtyChunklet.add(dirty);
                    }
                    setBlockLightRecursive(chunk, x + 1, y, z);
                    setBlockLightRecursive(chunk, x - 1, y, z);
                    setBlockLightRecursive(chunk, x, y, z + 1);
                    setBlockLightRecursive(chunk, x, y, z - 1);
                    setBlockLightRecursive(chunk, x, y - 1, z);
                    setBlockLightRecursive(chunk, x, y + 1, z);
                }
            }
        }
    }

    public void recalculateBlockLightAround(int x, int y, int z, int lightValue) {
        setBlockLightAroundBlockRecursive(x - 1, y, z, lightValue);
        setBlockLightAroundBlockRecursive(x + 1, y, z, lightValue);
        setBlockLightAroundBlockRecursive(x, y, z + 1, lightValue);
        setBlockLightAroundBlockRecursive(x, y, z - 1, lightValue);
        setBlockLightAroundBlockRecursive(x, y + 1, z, lightValue);
        setBlockLightAroundBlockRecursive(x, y - 1, z, lightValue);
    }

    private void setBlockLightAroundBlockRecursive(int x, int y, int z, int lightValue) {
        Chunk chunk;
        if (y < 128 && y >= 0 && lightValue != 0 && (chunk = this.world.getChunk((int) Math.floor(x / 16), (int) Math.floor(z / 16))) != null) {
            int blockX = x % 16;
            int blockZ = z % 16;
            byte blockType = chunk.blockType(blockX, y, blockZ);
            if (!BlockFactory.opaque(blockType) && chunk.blockLight(blockX, y, blockZ) < lightValue - 1) {
                chunk.setLightForBlock(blockX, y, blockZ, lightValue);
                Chunklet dirty = this.world.getChunklet(x, y, z);
                if (!this.dirtyChunklet.contains(dirty)) {
                    this.dirtyChunklet.add(dirty);
                }
                setBlockLightAroundBlockRecursive(x - 1, y, z, lightValue);
                setBlockLightAroundBlockRecursive(x + 1, y, z, lightValue);
                setBlockLightAroundBlockRecursive(x, y, z + 1, lightValue);
                setBlockLightAroundBlockRecursive(x, y, z - 1, lightValue);
                setBlockLightAroundBlockRecursive(x, y + 1, z, lightValue);
                setBlockLightAroundBlockRecursive(x, y - 1, z, lightValue);
            }
        }
    }

    public ArrayList<Chunklet> getDirtyChunklet() {
        return this.dirtyChunklet;
    }
}
