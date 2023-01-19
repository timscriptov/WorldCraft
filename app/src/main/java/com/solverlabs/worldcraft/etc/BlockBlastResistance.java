package com.solverlabs.worldcraft.etc;

import com.solverlabs.worldcraft.factories.BlockFactory;

import java.util.HashMap;
import java.util.Map;


public class BlockBlastResistance {
    private static final float DEFAULT_BLAST_RESISTANCE = 5.0f;
    private static final Map<Byte, Float> speedTable = new HashMap<>();

    static {
        speedTable.put((byte) 7, 1.8E7f);
        speedTable.put((byte) 49, 6000.0f);
        speedTable.put((byte) 10, 500.0f);
        speedTable.put((byte) 11, 500.0f);
        speedTable.put((byte) 8, 500.0f);
        speedTable.put((byte) 9, 500.0f);
        speedTable.put((byte) 120, 30.0f);
        speedTable.put((byte) 45, 30.0f);
        speedTable.put((byte) BlockFactory.NETHER_BRICK_ID, 30.0f);
        speedTable.put((byte) BlockFactory.STONE_BRICK_ID, 30.0f);
        speedTable.put((byte) BlockFactory.STONE_BRICK_MOSSY_ID, 30.0f);
        speedTable.put((byte) 4, 30.0f);
        speedTable.put((byte) BlockFactory.DIAMOND_BLOCK_ID, 30.0f);
        speedTable.put((byte) BlockFactory.GOLD_BLOCK_ID, 30.0f);
        speedTable.put((byte) BlockFactory.IRON_BLOCK_ID, 30.0f);
        speedTable.put((byte) BlockFactory.JUKEBOX_ID, 30.0f);
        speedTable.put((byte) BlockFactory.DOUBLE_SLAB_ID, 30.0f);
        speedTable.put((byte) 44, 30.0f);
        speedTable.put((byte) 1, 30.0f);
        speedTable.put((byte) BlockFactory.CLOSED_IRON_DOOR_ID, 25.0f);
        speedTable.put((byte) BlockFactory.DISPENSER_ID, 17.5f);
        speedTable.put((byte) 61, 6000.0f);
        speedTable.put((byte) 14, 15.0f);
        speedTable.put((byte) 15, 15.0f);
        speedTable.put((byte) BlockFactory.COAL_ORE_ID, 15.0f);
        speedTable.put((byte) 21, 15.0f);
        speedTable.put((byte) BlockFactory.DIAMOND_ORE_ID, 15.0f);
        speedTable.put((byte) BlockFactory.CLAY_ORE_ID, 15.0f);
        speedTable.put((byte) BlockFactory.CLOSED_WOOD_DOOR_ID, 15.0f);
        speedTable.put((byte) 22, 15.0f);
        speedTable.put((byte) 5, 15.0f);
        speedTable.put((byte) BlockFactory.WOOD_PLANK_PINE_ID, 15.0f);
        speedTable.put((byte) BlockFactory.WOOD_PLANK_JUNGLE_ID, 15.0f);
        speedTable.put((byte) 54, 12.5f);
        speedTable.put((byte) 58, 12.5f);
        speedTable.put((byte) BlockFactory.WOOD_ID, 10.0f);
        speedTable.put((byte) 47, 7.5f);
        speedTable.put((byte) BlockFactory.MELON_ID, (float) DEFAULT_BLAST_RESISTANCE);
        speedTable.put((byte) BlockFactory.PUMPKIN_ID, (float) DEFAULT_BLAST_RESISTANCE);
        speedTable.put((byte) 25, 4.0f);
        speedTable.put((byte) 24, 4.0f);
        speedTable.put((byte) BlockFactory.SANDSTONE2_ID, 4.0f);
        speedTable.put((byte) 35, 4.0f);
        speedTable.put((byte) BlockFactory.FARMLAND_ID, 3.0f);
        speedTable.put((byte) 13, 3.0f);
        speedTable.put((byte) 19, 3.0f);
        speedTable.put((byte) BlockFactory.GRASS_ID, 3.0f);
        speedTable.put((byte) BlockFactory.SNOWY_GRASS_ID, 3.0f);
        speedTable.put((byte) 2, 2.5f);
        speedTable.put((byte) 3, 2.5f);
        speedTable.put((byte) BlockFactory.ICE_ID, 2.5f);
        speedTable.put((byte) 12, 2.5f);
        speedTable.put((byte) 88, 2.5f);
        speedTable.put((byte) BlockFactory.CACTUS_ID, 2.0f);
        speedTable.put((byte) 76, 2.0f);
        speedTable.put((byte) BlockFactory.NETHERRACK_ID, 2.0f);
        speedTable.put((byte) BlockFactory.GLASS_ID, 1.5f);
        speedTable.put((byte) BlockFactory.GLOW_STONE_ID, 1.5f);
        speedTable.put((byte) BlockFactory.BED_ID, 1.0f);
        speedTable.put((byte) 18, 1.0f);
        speedTable.put((byte) BlockFactory.SNOW_ID, 0.5f);
        speedTable.put((byte) BlockFactory.FLOWER_ID, 0.0f);
        speedTable.put((byte) 46, 0.0f);
        speedTable.put((byte) BlockFactory.TORCH_ID, 0.0f);
    }

    public static float getBlastResistance(byte blockType) {
        Float value = speedTable.get(blockType);
        return value == null ? DEFAULT_BLAST_RESISTANCE : value;
    }
}
