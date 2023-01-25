package com.mcal.droid.rugl.worldgenerator;

import androidx.annotation.NonNull;

import com.mcal.droid.rugl.util.geom.Vector3i;
import com.mcal.worldcraft.chunk.Chunk;
import com.mcal.worldcraft.factories.BlockFactory;

import org.apache.commons.compress.archivers.cpio.CpioConstants;

import java.util.Random;


public class DualLayerTerrainWithMediumValleys extends BaseTerrainGenerator {
    private final Random random = new Random();
    private final int r = this.random.nextInt(9) + 1;

    @Override
    public void generateTerrain(@NonNull Chunk chunk, int noiseBlockOffset) {
        byte blockType;
        int chunkBlockX = chunk.chunkX * 16;
        int chunkBlockZ = chunk.chunkZ * 16;
        for (int x = 0; x < 16; x++) {
            int blockX = chunkBlockX + x;
            for (int z = 0; z < 16; z++) {
                int blockZ = chunkBlockZ + z;
                float lowerGroundHeight = getLowerGroundHeight(chunk, blockX, blockZ, x, z, CpioConstants.C_IWUSR, noiseBlockOffset);
                int groundHeight = getUpperGroundHeight(blockX, blockZ, lowerGroundHeight, noiseBlockOffset);
                boolean sunlit = true;
                for (int y = 127; y >= 0; y--) {
                    if (y > groundHeight) {
                        blockType = 0;
                    } else if (y > lowerGroundHeight) {
                        float octave1 = (PerlinSimplexNoise.noise(blockX * 0.01f, blockZ * 0.01f, y * 0.01f) * 0.015f * y) + 0.1f;
                        float caveNoise = (PerlinSimplexNoise.noise(blockX * 0.01f, blockZ * 0.01f, y * 0.1f) * 0.06f) + octave1 + 0.1f + (PerlinSimplexNoise.noise(blockX * 0.2f, blockZ * 0.2f, y * 0.2f) * 0.03f) + 0.01f;
                        if (caveNoise > 0.2f) {
                            blockType = 0;
                        } else if (sunlit) {
                            blockType = BlockFactory.Block.DirtWithGrass.id;
                            chunk.topLayer.add(new Vector3i(x, y, z));
                            sunlit = false;
                        } else {
                            blockType = BlockFactory.Block.Dirt.id;
                            if (caveNoise < 0.2f) {
                                blockType = BlockFactory.Block.Stone.id;
                            }
                        }
                    } else if (sunlit) {
                        blockType = BlockFactory.Block.DirtWithGrass.id;
                        chunk.topLayer.add(new Vector3i(x, y, z));
                        sunlit = false;
                    } else {
                        blockType = BlockFactory.Block.Dirt.id;
                    }
                    setBlockType(chunk, x, y, z, blockType);
                }
            }
        }
    }

    private int getUpperGroundHeight(int blockX, int blockY, float lowerGroundHeight, int noiseBlockOffset) {
        int noiseX = blockX + noiseBlockOffset;
        float octave1 = PerlinSimplexNoise.noise(noiseX * 0.001f, blockY * 0.001f) * 0.5f;
        float octave2 = PerlinSimplexNoise.noise((noiseX + 100) * 0.002f, blockY * 0.002f) * 0.25f;
        float octave3 = PerlinSimplexNoise.noise((noiseX + 100) * 0.01f, blockY * 0.01f) * 0.25f;
        float octaveSum = octave1 + octave2 + octave3;
        return ((int) (64.0f * octaveSum)) + ((int) lowerGroundHeight);
    }

    private float getLowerGroundHeight(Chunk chunk, int blockX, int blockZ, int blockXInChunk, int blockZInChunk, int worldDepthInBlocks, int noiseBlockOffset) {
        int minimumGroundheight = worldDepthInBlocks / 4;
        int minimumGroundDepth = (int) (worldDepthInBlocks * 0.5f);
        int noiseX = blockX + noiseBlockOffset;
        float octave1 = PerlinSimplexNoise.noise(noiseX * 1.0E-4f * this.r, blockZ * 1.0E-4f * this.r) * 0.5f;
        float octave2 = PerlinSimplexNoise.noise(noiseX * 1.0E-4f * this.r, blockZ * 1.0E-4f * this.r) * 0.35f;
        float octave3 = PerlinSimplexNoise.noise(noiseX * 0.01f * this.r, blockZ * 0.01f * this.r) * 0.15f;
        float lowerGroundHeight = (minimumGroundDepth * (octave1 + octave2 + octave3)) + minimumGroundheight;
        for (int y = (int) lowerGroundHeight; y >= 0; y--) {
            setBlockType(chunk, blockXInChunk, y, blockZInChunk, BlockFactory.Block.Dirt.id);
        }
        return lowerGroundHeight;
    }
}
