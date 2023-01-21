package com.solverlabs.droid.rugl.worldgenerator;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.util.geom.Vector3i;
import com.solverlabs.worldcraft.chunk.Chunk;
import com.solverlabs.worldcraft.factories.BlockFactory;

import org.apache.commons.compress.archivers.cpio.CpioConstants;

import java.util.Random;


public class TreeDecorator {
    private final Random random = new Random();
    private Chunk currentChunk;

    public void decorate(int blockX, int blockY, int blockZ, @NonNull Chunk chunk) {
        this.currentChunk = chunk;
        if (chunk.blockType(blockX, blockY, blockZ) != 82 && isAValidLocationForDecoration(blockX, blockY, blockZ)) {
            if (chunk.blockType(blockX, blockY, blockZ) == 12) {
                createCactus(blockX, blockY, blockZ);
            } else {
                createTree(blockX, blockY, blockZ);
            }
        }
    }

    private boolean isAValidLocationForDecoration(int blockX, int blockY, int blockZ) {
        if (this.random.nextInt(100) >= 99 && blockY < 108) {
            return spaceAboveIsEmpty(blockX, blockY + 1, blockZ, 2, 2, 9);
        }
        return false;
    }

    private void createTree(int blockX, int blockY, int blockZ) {
        int trunkLength = this.random.nextInt(4) + 6;
        for (int y = blockY + 1; y <= blockY + trunkLength; y++) {
            createTrunkAt(blockX, y, blockZ);
        }
        createSphereAt(blockX, blockY + trunkLength, blockZ, this.random.nextInt(1) + 3);
    }

    private void createCactus(int blockX, int blockY, int blockZ) {
        int count = this.random.nextInt(3) + 3;
        for (int i = 1; i < count; i++) {
            setBlockType(this.currentChunk, blockX, blockY + i, blockZ, BlockFactory.CACTUS_ID);
        }
    }

    private void createSphereAt(int blockX, int blockY, int blockZ, int radius) {
        int radius2 = radius * radius;
        Vector3i vec1 = new Vector3i();
        Vector3i vec2 = new Vector3i();
        for (int x = blockX - radius2; x <= blockX + radius2; x++) {
            for (int y = blockY - radius2; y <= blockY + radius2; y++) {
                for (int z = blockZ - radius2; z <= blockZ + radius2; z++) {
                    vec1.set(x, y, z);
                    vec2.set(blockX, blockY, blockZ);
                    if (Vector3i.squaredDistance(vec1, vec2) <= radius2 && ((blockX != x || blockZ != z || y > blockY) && this.currentChunk != null)) {
                        setBlockType(this.currentChunk, x, y, z, (byte) 18);
                    }
                }
            }
        }
    }

    protected void setBlockType(Chunk chunk, int bx, int by, int bz, byte blockType) {
        if (by >= 0 && by < 128 && bx <= 15 && bx >= 0 && bz <= 15 && bz >= 0) {
            int index = (bz * CpioConstants.C_IWUSR) + by + (bx * 2048);
            chunk.blockData[index] = blockType;
        }
    }

    protected byte getBlockType(Chunk chunk, int bx, int by, int bz) {
        if (by < 0 || by >= 128 || bx > 15 || bx < 0 || bz > 15 || bz < 0) {
            return (byte) 0;
        }
        int index = (bz * CpioConstants.C_IWUSR) + by + (bx * 2048);
        return chunk.blockData[index];
    }

    private void createTrunkAt(int x, int y, int z) {
        if (this.currentChunk != null) {
            setBlockType(this.currentChunk, x, y, z, BlockFactory.Block.Wood.id);
        }
    }

    private boolean spaceAboveIsEmpty(int blockX, int blockY, int blockZ, int depthAbove, int widthAround, int heightAround) {
        for (int z = blockZ + 1; z <= blockZ + depthAbove; z++) {
            for (int x = blockX - widthAround; x <= blockX + widthAround; x++) {
                for (int y = blockY; y < blockY + heightAround; y++) {
                    if (this.currentChunk.blockType(x, y, z) != 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @NonNull
    public String toString() {
        return "Standard Tree";
    }
}
