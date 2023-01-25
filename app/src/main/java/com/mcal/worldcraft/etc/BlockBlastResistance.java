package com.mcal.worldcraft.etc;

import com.mcal.worldcraft.factories.BlockFactory;

import java.util.HashMap;
import java.util.Map;

public class BlockBlastResistance {
    private static final float DEFAULT_BLAST_RESISTANCE = 5.0f;
    private static final Map<Byte, Float> speedTable = new HashMap<>();

    static {
        speedTable.put(BlockFactory.BEDROCK_ID, 1.8E7f);
        speedTable.put(BlockFactory.OBSIDIAN_ID, 6000.0f);
        speedTable.put(BlockFactory.LAVA_ID, 500.0f);
        speedTable.put(BlockFactory.STILL_LAVA_ID, 500.0f);
        speedTable.put(BlockFactory.WATER_ID, 500.0f);
        speedTable.put(BlockFactory.STILL_WATER_ID, 500.0f);
        speedTable.put(BlockFactory.BRICK_ID, 30.0f);
        speedTable.put(BlockFactory.BRICK_BLOCK_ID, 30.0f);
        speedTable.put(BlockFactory.NETHER_BRICK_ID, 30.0f);
        speedTable.put(BlockFactory.STONE_BRICK_ID, 30.0f);
        speedTable.put(BlockFactory.STONE_BRICK_MOSSY_ID, 30.0f);
        speedTable.put(BlockFactory.COBBLE_ID, 30.0f);
        speedTable.put(BlockFactory.DIAMOND_BLOCK_ID, 30.0f);
        speedTable.put(BlockFactory.GOLD_BLOCK_ID, 30.0f);
        speedTable.put(BlockFactory.IRON_BLOCK_ID, 30.0f);
        speedTable.put(BlockFactory.JUKEBOX_ID, 30.0f);
        speedTable.put(BlockFactory.DOUBLE_SLAB_ID, 30.0f);
        speedTable.put(BlockFactory.SLAB_ID, 30.0f);
        speedTable.put(BlockFactory.STONE_ID, 30.0f);
        speedTable.put(BlockFactory.CLOSED_IRON_DOOR_ID, 25.0f);
        speedTable.put(BlockFactory.DISPENSER_ID, 17.5f);
        speedTable.put(BlockFactory.FURNACE_ID, 6000.0f);
        speedTable.put(BlockFactory.GOLD_ORE_ID, 15.0f);
        speedTable.put(BlockFactory.IRON_ORE_ID, 15.0f);
        speedTable.put(BlockFactory.COAL_ORE_ID, 15.0f);
        speedTable.put(BlockFactory.LAPIS_LAZULI_ORE_ID, 15.0f);
        speedTable.put(BlockFactory.DIAMOND_ORE_ID, 15.0f);
        speedTable.put(BlockFactory.CLAY_ORE_ID, 15.0f);
        speedTable.put(BlockFactory.CLOSED_WOOD_DOOR_ID, 15.0f);
        speedTable.put(BlockFactory.LAPIS_LAZULI_BLOCK_ID, 15.0f);
        speedTable.put(BlockFactory.WOODEN_PLANKS_ID, 15.0f);
        speedTable.put(BlockFactory.WOOD_PLANK_PINE_ID, 15.0f);
        speedTable.put(BlockFactory.WOOD_PLANK_JUNGLE_ID, 15.0f);
        speedTable.put(BlockFactory.CHEST_ID, 12.5f);
        speedTable.put(BlockFactory.CRAFTING_TABLE_ID, 12.5f);
        speedTable.put(BlockFactory.WOOD_ID, 10.0f);
        speedTable.put(BlockFactory.BOOKSHELF_ID, 7.5f);
        speedTable.put(BlockFactory.MELON_ID, DEFAULT_BLAST_RESISTANCE);
        speedTable.put(BlockFactory.PUMPKIN_ID, DEFAULT_BLAST_RESISTANCE);
        speedTable.put(BlockFactory.NOTE_BLOCK_ID, 4.0f);
        speedTable.put(BlockFactory.SAND_STONE_ID, 4.0f);
        speedTable.put(BlockFactory.SANDSTONE2_ID, 4.0f);
        speedTable.put(BlockFactory.WOOL_ID, 4.0f);
        speedTable.put(BlockFactory.FARMLAND_ID, 3.0f);
        speedTable.put(BlockFactory.GRAVEL_ID, 3.0f);
        speedTable.put(BlockFactory.SPONGE_ID, 3.0f);
        speedTable.put(BlockFactory.GRASS_ID, 3.0f);
        speedTable.put(BlockFactory.SNOWY_GRASS_ID, 3.0f);
        speedTable.put(BlockFactory.DIRT_WITH_GRASS_ID, 2.5f);
        speedTable.put(BlockFactory.DIRT_ID, 2.5f);
        speedTable.put(BlockFactory.ICE_ID, 2.5f);
        speedTable.put(BlockFactory.SAND_ID, 2.5f);
        speedTable.put(BlockFactory.SOUL_SAND_ID, 2.5f);
        speedTable.put(BlockFactory.CACTUS_ID, 2.0f);
        speedTable.put(BlockFactory.LADDER_ID, 2.0f);
        speedTable.put(BlockFactory.NETHERRACK_ID, 2.0f);
        speedTable.put(BlockFactory.GLASS_ID, 1.5f);
        speedTable.put(BlockFactory.GLOW_STONE_ID, 1.5f);
        speedTable.put(BlockFactory.BED_ID, 1.0f);
        speedTable.put(BlockFactory.LEAVES_ID, 1.0f);
        speedTable.put(BlockFactory.SNOW_ID, 0.5f);
        speedTable.put(BlockFactory.FLOWER_ID, 0.0f);
        speedTable.put(BlockFactory.TNT_ID, 0.0f);
        speedTable.put(BlockFactory.TORCH_ID, 0.0f);
    }

    public static float getBlastResistance(byte blockType) {
        Float value = speedTable.get(blockType);
        return value == null ? DEFAULT_BLAST_RESISTANCE : value;
    }
}
