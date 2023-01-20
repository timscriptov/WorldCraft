package com.solverlabs.worldcraft.factories;

public class CraftFactory {
    private static final int BLOCK_GROUP = 2;
    private static final int ITEM_GROUP = 3;
    private static final int TOOL_GROUP = 1;

    public enum CraftItem {
        Stick(BlockFactory.STICK_ID, 4, 1, new byte[]{5, 2}),
        Torch(BlockFactory.TORCH_ID, 4, 1, new byte[]{BlockFactory.STICK_ID, 1}, new byte[]{BlockFactory.COAL_ORE_ID, 1}),
        WoodenPlanks((byte) 5, 4, 2, new byte[]{BlockFactory.WOOD_ID, 1}),
        WorkBench((byte) 58, 1, 2, new byte[]{5, 4}),
        Furnace((byte) 61, 1, 2, true, new byte[]{1, 8}),
        Bed(BlockFactory.BED_ID, 1, 2, true, new byte[]{5, 3}, new byte[]{35, 3}),
        Door(BlockFactory.CLOSED_WOOD_DOOR_ID, 1, 2, true, new byte[]{5, 6}),
        IronDoor(BlockFactory.CLOSED_IRON_DOOR_ID, 1, 2, true, new byte[]{BlockFactory.IRON_INGOT_ID, 6}),
        Ladder((byte) 76, 3, 2, true, new byte[]{BlockFactory.STICK_ID, 7}),
        Chest((byte) 54, 1, 2, true, new byte[]{5, 8}),
        StoneBrick(BlockFactory.STONE_BRICK_ID, 4, 2, true, new byte[]{1, 4}),
        MossyStoneBrick(BlockFactory.STONE_BRICK_MOSSY_ID, 4, 2, true, new byte[]{48, 4}),
        BrickBlock((byte) 45, 1, 2, true, new byte[]{120, 4}),
        SandStone((byte) 24, 1, 2, true, new byte[]{12, 4}),
        Shears(BlockFactory.SHEARS_ID, 1, 1, true, new byte[]{BlockFactory.IRON_INGOT_ID, 2}),
        WoodShovel((byte) 27, 1, 1, true, new byte[]{BlockFactory.STICK_ID, 2}, new byte[]{5, 1}),
        WoodPickaxe((byte) 28, 1, 1, true, new byte[]{BlockFactory.STICK_ID, 2}, new byte[]{5, 3}),
        WoodAxe((byte) 29, 1, 1, true, new byte[]{BlockFactory.STICK_ID, 2}, new byte[]{5, 3}),
        WoodSword(BlockFactory.WOOD_SWORD_ID, 1, 1, true, new byte[]{BlockFactory.STICK_ID, 1}, new byte[]{5, 2}),
        StoneShovel((byte) 31, 1, 1, true, new byte[]{BlockFactory.STICK_ID, 2}, new byte[]{1, 1}),
        StonePickaxe((byte) 32, 1, 1, true, new byte[]{BlockFactory.STICK_ID, 2}, new byte[]{1, 3}),
        StoneAxe((byte) 33, 1, 1, true, new byte[]{BlockFactory.STICK_ID, 2}, new byte[]{1, 3}),
        StoneSword((byte) 30, 1, 1, true, new byte[]{BlockFactory.STICK_ID, 1}, new byte[]{1, 2}),
        IronShovel((byte) 36, 1, 1, true, new byte[]{BlockFactory.STICK_ID, 2}, new byte[]{BlockFactory.IRON_INGOT_ID, 1}),
        IronPickaxe((byte) 37, 1, 1, true, new byte[]{BlockFactory.STICK_ID, 2}, new byte[]{BlockFactory.IRON_INGOT_ID, 3}),
        IronAxe((byte) 38, 1, 1, true, new byte[]{BlockFactory.STICK_ID, 2}, new byte[]{BlockFactory.IRON_INGOT_ID, 3}),
        IronSword((byte) 34, 1, 1, true, new byte[]{BlockFactory.STICK_ID, 1}, new byte[]{BlockFactory.IRON_INGOT_ID, 2}),
        GoldShovel((byte) 40, 1, 1, true, new byte[]{BlockFactory.STICK_ID, 2}, new byte[]{BlockFactory.GOLD_INGOT_ID, 1}),
        GoldPickaxe((byte) 50, 1, 1, true, new byte[]{BlockFactory.STICK_ID, 2}, new byte[]{BlockFactory.GOLD_INGOT_ID, 3}),
        GoldAxe((byte) 51, 1, 1, true, new byte[]{BlockFactory.STICK_ID, 2}, new byte[]{BlockFactory.GOLD_INGOT_ID, 3}),
        GoldSword(BlockFactory.GOLD_SWORD_ID, 1, 1, true, new byte[]{BlockFactory.STICK_ID, 1}, new byte[]{BlockFactory.GOLD_INGOT_ID, 2}),
        DiamondShovel((byte) 53, 1, 1, true, new byte[]{BlockFactory.STICK_ID, 2}, new byte[]{BlockFactory.DIAMOND_INGOT_ID, 1}),
        DiamondPickaxe((byte) 55, 1, 1, true, new byte[]{BlockFactory.STICK_ID, 2}, new byte[]{BlockFactory.DIAMOND_INGOT_ID, 3}),
        DiamondAxe((byte) 59, 1, 1, true, new byte[]{BlockFactory.STICK_ID, 2}, new byte[]{BlockFactory.DIAMOND_INGOT_ID, 3}),
        DiamondSword((byte) 52, 1, 1, true, new byte[]{BlockFactory.STICK_ID, 1}, new byte[]{BlockFactory.DIAMOND_INGOT_ID, 2}),
        BlockOfDiamond(BlockFactory.DIAMOND_BLOCK_ID, 1, 3, true, new byte[]{BlockFactory.DIAMOND_INGOT_ID, 9}),
        BlockOfGold(BlockFactory.GOLD_BLOCK_ID, 1, 3, true, new byte[]{BlockFactory.GOLD_INGOT_ID, 9}),
        BlockOfIron(BlockFactory.IRON_BLOCK_ID, 1, 3, true, new byte[]{BlockFactory.IRON_INGOT_ID, 9}),
        Diamond(BlockFactory.DIAMOND_INGOT_ID, 9, 3, false, new byte[]{BlockFactory.DIAMOND_BLOCK_ID, 1}),
        GoldIgnot(BlockFactory.GOLD_INGOT_ID, 9, 3, false, new byte[]{BlockFactory.GOLD_BLOCK_ID, 1}),
        IronIgnot(BlockFactory.IRON_INGOT_ID, 9, 3, false, new byte[]{BlockFactory.IRON_BLOCK_ID, 1});
        
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
