package com.solverlabs.droid.rugl.worldgenerator;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.util.geom.Vector3i;
import com.solverlabs.worldcraft.chunk.Chunk;
import com.solverlabs.worldcraft.factories.BlockFactory;

import org.apache.commons.compress.archivers.cpio.CpioConstants;

import java.util.Arrays;
import java.util.Random;


public abstract class BaseTerrainGenerator {
    protected static final int CHUNK_BLOCK_DEPTH = 16;
    protected static final int CHUNK_BLOCK_WIDTH = 16;
    protected PerlinNoise perlinNoise = new PerlinNoise(new Random().nextInt(1249984534));

    public abstract void generateTerrain(Chunk chunk, int i);

    public void setBlockType(Chunk chunk, int bx, int by, int bz, byte blockType) {
        if (by >= 0 && by < 128 && bx <= 15 && bx >= 0 && bz <= 15 && bz >= 0) {
            int index = (bz * CpioConstants.C_IWUSR) + by + (bx * 2048);
            chunk.blockData[index] = blockType;
        }
    }
    public byte getBlockType(Chunk chunk, int bx, int by, int bz) {
        if (by < 0 || by >= 128 || bx > 15 || bx < 0 || bz > 15 || bz < 0) {
            return (byte) 0;
        }
        int index = (bz * CpioConstants.C_IWUSR) + by + (bx * 2048);
        return chunk.blockData[index];
    }

    public void generateFlatTerrain(@NonNull Chunk chunk) {
        byte blockType;
        Arrays.fill(chunk.blockData, (byte) 0);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 50; y++) {
                    if (y < 10) {
                        blockType = BlockFactory.Block.Bedrock.id;
                    } else if (y < 30) {
                        blockType = BlockFactory.Block.Stone.id;
                    } else if (y < 49) {
                        blockType = BlockFactory.Block.Dirt.id;
                    } else {
                        blockType = BlockFactory.Block.DirtWithGrass.id;
                        chunk.topLayer.add(new Vector3i(x, y, z));
                    }
                    setBlockType(chunk, x, y, z, blockType);
                }
            }
        }
        chunk.wasChanged = true;
    }
}
