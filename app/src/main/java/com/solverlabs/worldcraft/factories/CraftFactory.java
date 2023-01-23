package com.solverlabs.worldcraft.factories;

public class CraftFactory {
    private static final int BLOCK_GROUP = 2;
    private static final int ITEM_GROUP = 3;
    private static final int TOOL_GROUP = 1;

    public enum CraftItem {
        Stick(BlockFactory.STICK_ID, 4, TOOL_GROUP, new byte[]{BlockFactory.WOODEN_PLANKS_ID, 2}),
        Torch(BlockFactory.TORCH_ID, 4, TOOL_GROUP, new byte[]{BlockFactory.STICK_ID, 1}, new byte[]{BlockFactory.COAL_ORE_ID, 1}),
        WoodenPlanks(BlockFactory.WOODEN_PLANKS_ID, 4, BLOCK_GROUP, new byte[]{BlockFactory.WOOD_ID, 1}),
        WorkBench(BlockFactory.CRAFTING_TABLE_ID, 1, BLOCK_GROUP, new byte[]{BlockFactory.WOODEN_PLANKS_ID, 4}),
        Furnace(BlockFactory.FURNACE_ID, 1, BLOCK_GROUP, true, new byte[]{BlockFactory.STONE_ID, 8}),
        Bed(BlockFactory.BED_ID, 1, BLOCK_GROUP, true, new byte[]{BlockFactory.WOODEN_PLANKS_ID, 3}, new byte[]{35, 3}),
        Door(BlockFactory.CLOSED_WOOD_DOOR_ID, 1, BLOCK_GROUP, true, new byte[]{BlockFactory.WOODEN_PLANKS_ID, 6}),
        IronDoor(BlockFactory.CLOSED_IRON_DOOR_ID, 1, BLOCK_GROUP, true, new byte[]{BlockFactory.IRON_INGOT_ID, 6}),
        Ladder(BlockFactory.LADDER_ID, 3, BLOCK_GROUP, true, new byte[]{BlockFactory.STICK_ID, 7}),
        Chest(BlockFactory.CHEST_ID, TOOL_GROUP, BLOCK_GROUP, true, new byte[]{BlockFactory.WOODEN_PLANKS_ID, 8}),
        StoneBrick(BlockFactory.STONE_BRICK_ID, 4, BLOCK_GROUP, true, new byte[]{BlockFactory.STONE_ID, 4}),
        MossyStoneBrick(BlockFactory.STONE_BRICK_MOSSY_ID, 4, BLOCK_GROUP, true, new byte[]{BlockFactory.MOSS_STONE_ID, 4}),
        BrickBlock(BlockFactory.BRICK_BLOCK_ID, 1, BLOCK_GROUP, true, new byte[]{BlockFactory.BRICK_ID, 4}),
        SandStone(BlockFactory.SAND_STONE_ID, 1, BLOCK_GROUP, true, new byte[]{BlockFactory.SAND_ID, 4}),
        Shears(BlockFactory.SHEARS_ID, 1, TOOL_GROUP, true, new byte[]{BlockFactory.IRON_INGOT_ID, 2}),
        WoodShovel(BlockFactory.WOOD_SHOVEL_ID, 1, TOOL_GROUP, true, new byte[]{BlockFactory.STICK_ID, 2}, new byte[]{BlockFactory.WOODEN_PLANKS_ID, 1}),
        WoodPickaxe(BlockFactory.WOOD_PICK_ID, 1, TOOL_GROUP, true, new byte[]{BlockFactory.STICK_ID, 2}, new byte[]{BlockFactory.WOODEN_PLANKS_ID, 3}),
        WoodAxe(BlockFactory.WOOD_AXE_ID, 1, TOOL_GROUP, true, new byte[]{BlockFactory.STICK_ID, 2}, new byte[]{BlockFactory.WOODEN_PLANKS_ID, 3}),
        WoodSword(BlockFactory.WOOD_SWORD_ID, 1, TOOL_GROUP, true, new byte[]{BlockFactory.STICK_ID, 1}, new byte[]{BlockFactory.WOODEN_PLANKS_ID, 2}),
        StoneShovel(BlockFactory.STONE_SHOVEL_ID, 1, TOOL_GROUP, true, new byte[]{BlockFactory.STICK_ID, 2}, new byte[]{1, 1}),
        StonePickaxe(BlockFactory.STONE_PICK_ID, 1, TOOL_GROUP, true, new byte[]{BlockFactory.STICK_ID, 2}, new byte[]{1, 3}),
        StoneAxe(BlockFactory.STONE_AXE_ID, 1, TOOL_GROUP, true, new byte[]{BlockFactory.STICK_ID, 2}, new byte[]{1, 3}),
        StoneSword(BlockFactory.STONE_SWORD_ID, 1, TOOL_GROUP, true, new byte[]{BlockFactory.STICK_ID, 1}, new byte[]{1, 2}),
        IronShovel(BlockFactory.IRON_SHOVEL_ID, 1, TOOL_GROUP, true, new byte[]{BlockFactory.STICK_ID, 2}, new byte[]{BlockFactory.IRON_INGOT_ID, 1}),
        IronPickaxe(BlockFactory.IRON_PICK_ID, 1, TOOL_GROUP, true, new byte[]{BlockFactory.STICK_ID, 2}, new byte[]{BlockFactory.IRON_INGOT_ID, 3}),
        IronAxe(BlockFactory.IRON_AXE_ID, 1, TOOL_GROUP, true, new byte[]{BlockFactory.STICK_ID, 2}, new byte[]{BlockFactory.IRON_INGOT_ID, 3}),
        IronSword(BlockFactory.IRON_SWORD_ID, 1, TOOL_GROUP, true, new byte[]{BlockFactory.STICK_ID, 1}, new byte[]{BlockFactory.IRON_INGOT_ID, 2}),
        GoldShovel(BlockFactory.GOLD_SHOVEL_ID, 1, TOOL_GROUP, true, new byte[]{BlockFactory.STICK_ID, 2}, new byte[]{BlockFactory.GOLD_INGOT_ID, 1}),
        GoldPickaxe(BlockFactory.GOLD_PICK_ID, 1, TOOL_GROUP, true, new byte[]{BlockFactory.STICK_ID, 2}, new byte[]{BlockFactory.GOLD_INGOT_ID, 3}),
        GoldAxe(BlockFactory.GOLD_AXE_ID, 1, TOOL_GROUP, true, new byte[]{BlockFactory.STICK_ID, 2}, new byte[]{BlockFactory.GOLD_INGOT_ID, 3}),
        GoldSword(BlockFactory.GOLD_SWORD_ID, 1, TOOL_GROUP, true, new byte[]{BlockFactory.STICK_ID, 1}, new byte[]{BlockFactory.GOLD_INGOT_ID, 2}),
        DiamondShovel(BlockFactory.DIAMOND_SHOVEL_ID, 1, TOOL_GROUP, true, new byte[]{BlockFactory.STICK_ID, 2}, new byte[]{BlockFactory.DIAMOND_INGOT_ID, 1}),
        DiamondPickaxe(BlockFactory.DIAMOND_PICK_ID, 1, TOOL_GROUP, true, new byte[]{BlockFactory.STICK_ID, 2}, new byte[]{BlockFactory.DIAMOND_INGOT_ID, 3}),
        DiamondAxe(BlockFactory.DIAMOND_AXE_ID, 1, TOOL_GROUP, true, new byte[]{BlockFactory.STICK_ID, 2}, new byte[]{BlockFactory.DIAMOND_INGOT_ID, 3}),
        DiamondSword(BlockFactory.DIAMOND_SWORD_ID, 1, TOOL_GROUP, true, new byte[]{BlockFactory.STICK_ID, 1}, new byte[]{BlockFactory.DIAMOND_INGOT_ID, 2}),
        BlockOfDiamond(BlockFactory.DIAMOND_BLOCK_ID, 1, ITEM_GROUP, true, new byte[]{BlockFactory.DIAMOND_INGOT_ID, 9}),
        BlockOfGold(BlockFactory.GOLD_BLOCK_ID, 1, ITEM_GROUP, true, new byte[]{BlockFactory.GOLD_INGOT_ID, 9}),
        BlockOfIron(BlockFactory.IRON_BLOCK_ID, 1, ITEM_GROUP, true, new byte[]{BlockFactory.IRON_INGOT_ID, 9}),
        Diamond(BlockFactory.DIAMOND_INGOT_ID, 9, ITEM_GROUP, false, new byte[]{BlockFactory.DIAMOND_BLOCK_ID, 1}),
        GoldIgnot(BlockFactory.GOLD_INGOT_ID, 9, ITEM_GROUP, false, new byte[]{BlockFactory.GOLD_BLOCK_ID, 1}),
        IronIgnot(BlockFactory.IRON_INGOT_ID, 9, ITEM_GROUP, false, new byte[]{BlockFactory.IRON_BLOCK_ID, 1});

        private final int count;
        private final int group;
        private final byte id;
        private final byte[][] material;
        private final boolean needWorkBanch;

        CraftItem(byte crafteItemId, int count, int group, boolean needWorkBanch, byte[]... material) {
            this.id = crafteItemId;
            this.material = material;
            this.count = count;
            this.group = group;
            this.needWorkBanch = needWorkBanch;
        }

        CraftItem(byte crafteItemId, int count, int group, byte[]... material) {
            this(crafteItemId, count, group, false, material);
        }

        public byte getID() {
            return this.id;
        }

        public byte[][] getMaterial() {
            return this.material;
        }

        public int getCount() {
            return this.count;
        }

        public boolean isNeedWorkBanch() {
            return this.needWorkBanch;
        }

        public int getGroup() {
            return this.group;
        }
    }
}
