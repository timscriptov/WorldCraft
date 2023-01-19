package com.solverlabs.droid.rugl.worldgenerator;

import com.solverlabs.droid.rugl.util.geom.Vector3i;
import com.solverlabs.worldcraft.chunk.Chunk;
import com.solverlabs.worldcraft.factories.BlockFactory;

import org.apache.commons.compress.archivers.cpio.CpioConstants;

import java.util.Arrays;
import java.util.Random;


public class StandardGenerationMethod extends BaseTerrainGenerator {
    private static final int COAL_ORE_PER_CHUNK = 4;
    private static final int DIAMOND_ORE_PER_CHUNK = 2;
    private static final int GOLD_ORE_PER_CHUNK = 2;
    private static final int IRON_ORE_PER_CHUNK = 3;
    private int generatedCoalCount;
    private int generatedDiamondCount;
    private int generatedGoldCount;
    private int generatedIronCount;
    private Random random = new Random();

    @Override
    public void generateTerrain(Chunk chunk, int noiseBlockOffset) {
        int chunkBlockX = chunk.chunkX * 16;
        int chunkBlockZ = chunk.chunkZ * 16;
        Arrays.fill(chunk.blockData, (byte) 0);
        this.generatedCoalCount = 0;
        this.generatedIronCount = 0;
        this.generatedGoldCount = 0;
        this.generatedDiamondCount = 0;
        for (int x = 0; x < 16; x++) {
            int blockX = x + chunkBlockX;
            for (int z = 0; z < 16; z++) {
                int blockZ = z + chunkBlockZ;
                generateStandardTerrain(chunk, x, z, blockX, blockZ, CpioConstants.C_IWUSR, noiseBlockOffset);
            }
        }
        chunk.wasChanged = true;
    }

    private void generateStandardTerrain(Chunk chunk, int x, int z, int blockX, int blockZ, int worldHeightInBlocks, int noiseBlockOffset) {
        int groundHeight = (int) getBlockNoise(blockX, blockZ);
        if (groundHeight < 1) {
            groundHeight = 1;
        } else if (groundHeight > 127) {
            groundHeight = 120;
        }
        boolean sunlit = true;
        setBlockType(chunk, x, 0, z, BlockFactory.Block.Bedrock.id);
        setBlockType(chunk, x, 1, z, BlockFactory.Block.Bedrock.id);
        byte blockType = 0;
        for (int y = worldHeightInBlocks - 1; y > 1; y--) {
            if (y > groundHeight) {
                if (y < 3) {
                    blockType = BlockFactory.Block.Water.id;
                } else {
                    blockType = 0;
                }
            } else if (y < groundHeight) {
                float octave1 = (PerlinSimplexNoise.noise(blockX * 0.005f, y * 0.005f, z * 0.05f) * 0.1f) + 0.1f;
                float initialNoise = (PerlinSimplexNoise.noise(blockX * 0.01f, y * 0.02f, z * 0.006f) * 0.3f) + 0.1f;
                if (initialNoise + (PerlinSimplexNoise.noise(blockX * 0.005f, y * 0.005f, z * 0.05f) * 0.1f) + 0.1f + (PerlinSimplexNoise.noise(blockX * 0.1f, y * 0.1f, z * 0.05f) * 0.05f) + 0.01f > 0.4f) {
                    if (y < 10) {
                        blockType = BlockFactory.Block.Water.id;
                    } else {
                        blockType = 0;
                    }
                } else if (sunlit) {
                    sunlit = false;
                    if (y > 100) {
                        blockType = BlockFactory.Block.SnowyGrass.id;
                    } else {
                        blockType = BlockFactory.Block.DirtWithGrass.id;
                        if (y < 80) {
                            blockType = BlockFactory.Block.Sand.id;
                        }
                        if (y < 69 && this.random.nextInt(10) == 0) {
                            blockType = BlockFactory.Block.ClayOre.id;
                        }
                    }
                    chunk.topLayer.add(new Vector3i(x, y, z));
                } else {
                    if (octave1 > 0.15f || y < this.random.nextInt(10) + 60) {
                        blockType = BlockFactory.Block.Stone.id;
                    } else {
                        blockType = BlockFactory.Block.Dirt.id;
                    }
                    generateOre(chunk, x, y, z);
                }
            }
            if (!isOreBlock(chunk, x, y, z)) {
                setBlockType(chunk, x, y, z, blockType);
                if (blockType == BlockFactory.Block.DirtWithGrass.id) {
                    int randomInt = this.random.nextInt(101);
                    if (randomInt < 1) {
                        setBlockType(chunk, x, y + 1, z, BlockFactory.FLOWER_ID);
                    } else if (randomInt < 20) {
                        setBlockType(chunk, x, y + 1, z, BlockFactory.GRASS_ID);
                    }
                }
            }
        }
    }

    private void generateOre(Chunk chunk, int x, int y, int z) {
        generateCoal(chunk, x, y, z);
        generateIron(chunk, x, y, z);
        generateGold(chunk, x, y, z);
        generateDiamod(chunk, x, y, z);
    }

    private void generateDiamod(Chunk chunk, int x, int y, int z) {
        if (canGenerateDiamond(y)) {
            this.generatedDiamondCount++;
            int count = this.random.nextInt(6) + 1;
            int oreX = x;
            int oreY = y;
            int oreZ = z;
            for (int i = 0; i < count; i++) {
                oreX += this.random.nextInt(2);
                oreY += this.random.nextInt(2);
                oreZ += this.random.nextInt(2);
                if (!isOreBlock(chunk, oreX, oreY, oreZ)) {
                    setBlockType(chunk, oreX, oreY, oreZ, BlockFactory.DIAMOND_ORE_ID);
                }
            }
        }
    }

    private void generateGold(Chunk chunk, int x, int y, int z) {
        if (canGenerateGold(y)) {
            this.generatedGoldCount++;
            int count = this.random.nextInt(5) + 1;
            int oreX = x;
            int oreY = y;
            int oreZ = z;
            for (int i = 0; i < count; i++) {
                oreX += this.random.nextInt(2);
                oreY += this.random.nextInt(2);
                oreZ += this.random.nextInt(2);
                if (!isOreBlock(chunk, oreX, oreY, oreZ)) {
                    setBlockType(chunk, oreX, oreY, oreZ, (byte) 14);
                }
            }
        }
    }

    private void generateIron(Chunk chunk, int x, int y, int z) {
        if (canGenerateIron(y)) {
            this.generatedIronCount++;
            int count = this.random.nextInt(5) + 1;
            int oreX = x;
            int oreY = y;
            int oreZ = z;
            for (int i = 0; i < count; i++) {
                oreX += this.random.nextInt(2);
                oreY += this.random.nextInt(2);
                oreZ += this.random.nextInt(2);
                if (!isOreBlock(chunk, oreX, oreY, oreZ)) {
                    setBlockType(chunk, oreX, oreY, oreZ, (byte) 15);
                }
            }
        }
    }

    private void generateCoal(Chunk chunk, int x, int y, int z) {
        if (canGenerateCoal(y)) {
            this.generatedCoalCount++;
            int count = this.random.nextInt(8) + 1;
            int oreX = x;
            int oreY = y;
            int oreZ = z;
            for (int i = 0; i < count; i++) {
                oreX += this.random.nextInt(2);
                oreY += this.random.nextInt(2);
                oreZ += this.random.nextInt(2);
                if (!isOreBlock(chunk, oreX, oreY, oreZ)) {
                    setBlockType(chunk, oreX, oreY, oreZ, BlockFactory.COAL_ORE_ID);
                }
            }
        }
    }

    private boolean canGenerateGold(int y) {
        int generatePercent = 0;
        if (this.generatedGoldCount >= 2) {
            return false;
        }
        if (y > 5 && y < 35) {
            generatePercent = this.random.nextInt(3) + 8;
        }
        return this.random.nextInt(101) < generatePercent;
    }

    private boolean canGenerateDiamond(int y) {
        int generatePercent = 0;
        if (this.generatedDiamondCount >= 2) {
            return false;
        }
        if (y > 2 && y < 20) {
            generatePercent = this.random.nextInt(3) + 7;
        }
        return this.random.nextInt(101) < generatePercent;
    }

    private boolean canGenerateCoal(int y) {
        int generatePercent = 0;
        if (this.generatedCoalCount >= 4) {
            return false;
        }
        if (y > 5 && y < 55) {
            generatePercent = this.random.nextInt(6) + 70;
        } else if (y >= 55 && y < 65) {
            generatePercent = this.random.nextInt(3) + 2;
        } else if (y >= 65 && y < 90) {
            generatePercent = this.random.nextInt(5);
        }
        return this.random.nextInt(101) < generatePercent;
    }

    private boolean canGenerateIron(int y) {
        int generatePercent = 0;
        if (this.generatedIronCount >= 3) {
            return false;
        }
        if (y < 55) {
            generatePercent = this.random.nextInt(3) + 38;
        } else if (y >= 55 && y < 65) {
            generatePercent = this.random.nextInt(3) + 2;
        }
        return this.random.nextInt(101) < generatePercent;
    }

    private float getBlockNoise(int blockX, int blockZ) {
        float mediumDetail = SimplexNoise.noise(blockX / 300.0f, 60.0f, blockZ / 300.0f);
        float fineDetail = SimplexNoise.noise(blockX / 80.0f, 80.0f, blockZ / 80.0f);
        float bigDetails = SimplexNoise.noise(blockX / 400.0f, 30.0f, blockZ / 400.0f);
        float noise = (64.0f * bigDetails) + (32.0f * mediumDetail) + (16.0f * fineDetail);
        return noise + 80.0f;
    }

    private boolean isOreBlock(Chunk chunk, int x, int y, int z) {
        return getBlockType(chunk, x, y, z) == 16 || getBlockType(chunk, x, y, z) == 15 || getBlockType(chunk, x, y, z) == 14 || getBlockType(chunk, x, y, z) == 56;
    }
}
