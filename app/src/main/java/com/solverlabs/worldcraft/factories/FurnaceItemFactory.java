package com.solverlabs.worldcraft.factories;

import androidx.annotation.NonNull;

import java.util.HashSet;
import java.util.Set;

public class FurnaceItemFactory {
    private static final long TIME_OFFSET = 100;

    @NonNull
    public static Set<Byte> getMaterialList() {
        Set<Byte> materials = new HashSet<>();
        FurnaceItem[] furnaceItems = FurnaceItem.values();
        for (FurnaceItem furnaceItem : furnaceItems) {
            if (furnaceItem.getCraftedItemId() != 0) {
                materials.add(furnaceItem.getId());
            }
        }
        return materials;
    }

    @NonNull
    public static Set<Byte> getFuelList() {
        Set<Byte> fuels = new HashSet<>();
        for (FurnaceItem furnaceItem : FurnaceItem.values()) {
            if (furnaceItem.getBurningTime() > 0) {
                fuels.add(furnaceItem.getId());
            }
        }
        return fuels;
    }

    public enum FurnaceItem {
        Coal(BlockFactory.COAL_ORE_ID, 80, BlockFactory.BARE_HANDS),
        Wood(BlockFactory.WOOD_ID, 15, BlockFactory.COAL_ORE_ID),
        IronOre(BlockFactory.IRON_ORE_ID, 0, BlockFactory.IRON_INGOT_ID),
        GoldOre(BlockFactory.GOLD_ORE_ID, 0, BlockFactory.GOLD_INGOT_ID),
        DiamondOre(BlockFactory.DIAMOND_ORE_ID, 0, BlockFactory.DIAMOND_INGOT_ID),
        Sand(BlockFactory.SAND_ID, 0, BlockFactory.GLASS_ID),
        WoodenPlanks(BlockFactory.WOODEN_PLANKS_ID, 15, BlockFactory.BARE_HANDS),
        Stick(BlockFactory.STICK_ID, 5, (byte) 0),
        RawBeef(BlockFactory.RAW_BEEF_ID, 0, BlockFactory.STEAK_ID),
        RawPorkchop(BlockFactory.RAW_PORKCHOP_ID, 0, BlockFactory.COOKED_PORKCHOP_ID),
        WorkBench(BlockFactory.CRAFTING_TABLE_ID, 15, BlockFactory.BARE_HANDS),
        Chest(BlockFactory.CHEST_ID, 15, BlockFactory.BARE_HANDS),
        Stone(BlockFactory.STONE_ID, 0, BlockFactory.COBBLE_ID),
        ClayOre(BlockFactory.CLAY_ORE_ID, 0, BlockFactory.BRICK_ID);

        private final int burningTime;
        private final byte craftedItemId;
        private final byte id;

        FurnaceItem(byte id, int burningTime, byte craftedItemId) {
            this.id = id;
            this.craftedItemId = craftedItemId;
            this.burningTime = burningTime;
        }

        public static int getBurningTime(byte id) {
            for (int i = 0; i < values().length; i++) {
                if (values()[i].id == id) {
                    return (int) ((values()[i].burningTime * 1000) + FurnaceItemFactory.TIME_OFFSET);
                }
            }
            return 0;
        }

        public static byte getCraftedItemId(byte id) {
            for (int i = 0; i < values().length; i++) {
                if (values()[i].id == id) {
                    return values()[i].craftedItemId;
                }
            }
            return (byte) 0;
        }

        public byte getId() {
            return id;
        }

        public int getBurningTime() {
            return burningTime;
        }

        public byte getCraftedItemId() {
            return craftedItemId;
        }
    }
}
