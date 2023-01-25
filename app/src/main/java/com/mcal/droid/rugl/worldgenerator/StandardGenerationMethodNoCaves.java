package com.mcal.droid.rugl.worldgenerator;

import com.mcal.worldcraft.chunk.Chunk;
import com.mcal.worldcraft.factories.BlockFactory;

import org.apache.commons.compress.archivers.cpio.CpioConstants;


public class StandardGenerationMethodNoCaves {
    private static final int CHUNK_BLOCK_DEPTH = 16;
    private static final int CHUNK_BLOCK_WIDTH = 16;

    private static void generateTerrain(Chunk chunk, int blockXInChunk, int blockZInChunk, int blockX, int blockZ, int worldDepthInBlocks) {
        int minimumGroundheight = worldDepthInBlocks / 4;
        int minimumGroundDepth = (int) (worldDepthInBlocks * 0.75f);
        float octave1 = PerlinSimplexNoise.noise(blockX * 1.0E-4f, blockZ * 1.0E-4f) * 0.5f;
        float octave2 = PerlinSimplexNoise.noise(blockX * 5.0E-4f, blockZ * 5.0E-4f) * 0.25f;
        float octave3 = PerlinSimplexNoise.noise(blockX * 0.005f, blockZ * 0.005f) * 0.12f;
        float octave4 = PerlinSimplexNoise.noise(blockX * 0.01f, blockZ * 0.01f) * 0.12f;
        float octave5 = PerlinSimplexNoise.noise(blockX * 0.03f, blockZ * 0.03f) * octave4;
        float lowerGroundHeight = octave1 + octave2 + octave3 + octave4 + octave5;
        float lowerGroundHeight2 = (minimumGroundDepth * lowerGroundHeight) + minimumGroundheight;
        boolean sunlit = true;
        byte blockType = 0;
        for (int y = worldDepthInBlocks - 1; y >= 0; y--) {
            if (y <= lowerGroundHeight2) {
                if (sunlit) {
                    blockType = BlockFactory.Block.Stone.id;
                    sunlit = false;
                } else {
                    blockType = BlockFactory.Block.Dirt.id;
                }
            }
            setBlockType(chunk, blockXInChunk, y, blockZInChunk, blockType);
        }
    }

    private static void setBlockType(Chunk chunk, int bx, int by, int bz, byte blockType) {
        if (by >= 0 && by < 128) {
            int index = (bz * CpioConstants.C_IWUSR) + by + (bx * 2048);
            chunk.blockData[index] = blockType;
        }
    }

    public void generateTerrain(Chunk chunk, int noiseBlockOffset) {
        for (int x = 0; x < 16; x++) {
            int blockX = (chunk.chunkX * 16) + x + noiseBlockOffset;
            for (int y = 0; y < 16; y++) {
                int blockZ = (chunk.chunkZ * 16) + y;
                generateTerrain(chunk, x, y, blockX, blockZ, CpioConstants.C_IWUSR);
            }
        }
    }
}
