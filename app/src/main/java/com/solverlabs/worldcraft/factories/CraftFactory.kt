package com.solverlabs.worldcraft.factories

object CraftFactory {
    private const val BLOCK_GROUP = 2
    private const val ITEM_GROUP = 3
    private const val TOOL_GROUP = 1

    enum class CraftItem(
        val iD: Byte,
        count: Int,
        group: Int,
        needWorkBanch: Boolean,
        vararg material: ByteArray
    ) {
        Stick(
            BlockFactory.STICK_ID,
            4,
            TOOL_GROUP,
            byteArrayOf(BlockFactory.WOODEN_PLANKS_ID, 2)
        ),
        Torch(
            BlockFactory.TORCH_ID,
            4,
            TOOL_GROUP,
            byteArrayOf(BlockFactory.STICK_ID, 1),
            byteArrayOf(BlockFactory.COAL_ORE_ID, 1)
        ),
        WoodenPlanks(
            BlockFactory.WOODEN_PLANKS_ID,
            4,
            BLOCK_GROUP,
            byteArrayOf(BlockFactory.WOOD_ID, 1)
        ),
        WorkBench(
            BlockFactory.CRAFTING_TABLE_ID,
            1,
            BLOCK_GROUP,
            byteArrayOf(BlockFactory.WOODEN_PLANKS_ID, 4)
        ),
        Furnace(
            BlockFactory.FURNACE_ID,
            1,
            BLOCK_GROUP,
            true,
            byteArrayOf(BlockFactory.STONE_ID, 8)
        ),
        Bed(
            BlockFactory.BED_ID,
            1,
            BLOCK_GROUP,
            true,
            byteArrayOf(BlockFactory.WOODEN_PLANKS_ID, 3),
            byteArrayOf(35, 3)
        ),
        Door(
            BlockFactory.CLOSED_WOOD_DOOR_ID,
            1,
            BLOCK_GROUP,
            true,
            byteArrayOf(BlockFactory.WOODEN_PLANKS_ID, 6)
        ),
        IronDoor(
            BlockFactory.CLOSED_IRON_DOOR_ID,
            1,
            BLOCK_GROUP,
            true,
            byteArrayOf(BlockFactory.IRON_INGOT_ID, 6)
        ),
        Ladder(
            BlockFactory.LADDER_ID,
            3,
            BLOCK_GROUP,
            true,
            byteArrayOf(BlockFactory.STICK_ID, 7)
        ),
        Chest(
            BlockFactory.CHEST_ID,
            TOOL_GROUP,
            BLOCK_GROUP,
            true,
            byteArrayOf(BlockFactory.WOODEN_PLANKS_ID, 8)
        ),
        StoneBrick(
            BlockFactory.STONE_BRICK_ID,
            4,
            BLOCK_GROUP,
            true,
            byteArrayOf(BlockFactory.STONE_ID, 4)
        ),
        MossyStoneBrick(
            BlockFactory.STONE_BRICK_MOSSY_ID,
            4,
            BLOCK_GROUP,
            true,
            byteArrayOf(BlockFactory.MOSS_STONE_ID, 4)
        ),
        BrickBlock(
            BlockFactory.BRICK_BLOCK_ID,
            1,
            BLOCK_GROUP,
            true,
            byteArrayOf(BlockFactory.BRICK_ID, 4)
        ),
        SandStone(
            BlockFactory.SAND_STONE_ID,
            1,
            BLOCK_GROUP,
            true,
            byteArrayOf(BlockFactory.SAND_ID, 4)
        ),
        Shears(
            BlockFactory.SHEARS_ID,
            1,
            TOOL_GROUP,
            true,
            byteArrayOf(BlockFactory.IRON_INGOT_ID, 2)
        ),
        WoodShovel(
            BlockFactory.WOOD_SHOVEL_ID,
            1,
            TOOL_GROUP,
            true,
            byteArrayOf(BlockFactory.STICK_ID, 2),
            byteArrayOf(BlockFactory.WOODEN_PLANKS_ID, 1)
        ),
        WoodPickaxe(
            BlockFactory.WOOD_PICK_ID,
            1,
            TOOL_GROUP,
            true,
            byteArrayOf(BlockFactory.STICK_ID, 2),
            byteArrayOf(BlockFactory.WOODEN_PLANKS_ID, 3)
        ),
        WoodAxe(
            BlockFactory.WOOD_AXE_ID,
            1,
            TOOL_GROUP,
            true,
            byteArrayOf(BlockFactory.STICK_ID, 2),
            byteArrayOf(BlockFactory.WOODEN_PLANKS_ID, 3)
        ),
        WoodSword(
            BlockFactory.WOOD_SWORD_ID,
            1,
            TOOL_GROUP,
            true,
            byteArrayOf(BlockFactory.STICK_ID, 1),
            byteArrayOf(BlockFactory.WOODEN_PLANKS_ID, 2)
        ),
        StoneShovel(
            BlockFactory.STONE_SHOVEL_ID,
            1,
            TOOL_GROUP,
            true,
            byteArrayOf(BlockFactory.STICK_ID, 2),
            byteArrayOf(1, 1)
        ),
        StonePickaxe(
            BlockFactory.STONE_PICK_ID,
            1,
            TOOL_GROUP,
            true,
            byteArrayOf(BlockFactory.STICK_ID, 2),
            byteArrayOf(1, 3)
        ),
        StoneAxe(
            BlockFactory.STONE_AXE_ID,
            1,
            TOOL_GROUP,
            true,
            byteArrayOf(BlockFactory.STICK_ID, 2),
            byteArrayOf(1, 3)
        ),
        StoneSword(
            BlockFactory.STONE_SWORD_ID,
            1,
            TOOL_GROUP,
            true,
            byteArrayOf(BlockFactory.STICK_ID, 1),
            byteArrayOf(1, 2)
        ),
        IronShovel(
            BlockFactory.IRON_SHOVEL_ID,
            1,
            TOOL_GROUP,
            true,
            byteArrayOf(BlockFactory.STICK_ID, 2),
            byteArrayOf(BlockFactory.IRON_INGOT_ID, 1)
        ),
        IronPickaxe(
            BlockFactory.IRON_PICK_ID,
            1,
            TOOL_GROUP,
            true,
            byteArrayOf(BlockFactory.STICK_ID, 2),
            byteArrayOf(BlockFactory.IRON_INGOT_ID, 3)
        ),
        IronAxe(
            BlockFactory.IRON_AXE_ID,
            1,
            TOOL_GROUP,
            true,
            byteArrayOf(BlockFactory.STICK_ID, 2),
            byteArrayOf(BlockFactory.IRON_INGOT_ID, 3)
        ),
        IronSword(
            BlockFactory.IRON_SWORD_ID,
            1,
            TOOL_GROUP,
            true,
            byteArrayOf(BlockFactory.STICK_ID, 1),
            byteArrayOf(BlockFactory.IRON_INGOT_ID, 2)
        ),
        GoldShovel(
            BlockFactory.GOLD_SHOVEL_ID,
            1,
            TOOL_GROUP,
            true,
            byteArrayOf(BlockFactory.STICK_ID, 2),
            byteArrayOf(BlockFactory.GOLD_INGOT_ID, 1)
        ),
        GoldPickaxe(
            BlockFactory.GOLD_PICK_ID,
            1,
            TOOL_GROUP,
            true,
            byteArrayOf(BlockFactory.STICK_ID, 2),
            byteArrayOf(BlockFactory.GOLD_INGOT_ID, 3)
        ),
        GoldAxe(
            BlockFactory.GOLD_AXE_ID,
            1,
            TOOL_GROUP,
            true,
            byteArrayOf(BlockFactory.STICK_ID, 2),
            byteArrayOf(BlockFactory.GOLD_INGOT_ID, 3)
        ),
        GoldSword(
            BlockFactory.GOLD_SWORD_ID,
            1,
            TOOL_GROUP,
            true,
            byteArrayOf(BlockFactory.STICK_ID, 1),
            byteArrayOf(BlockFactory.GOLD_INGOT_ID, 2)
        ),
        DiamondShovel(
            BlockFactory.DIAMOND_SHOVEL_ID,
            1,
            TOOL_GROUP,
            true,
            byteArrayOf(BlockFactory.STICK_ID, 2),
            byteArrayOf(BlockFactory.DIAMOND_INGOT_ID, 1)
        ),
        DiamondPickaxe(
            BlockFactory.DIAMOND_PICK_ID,
            1,
            TOOL_GROUP,
            true,
            byteArrayOf(BlockFactory.STICK_ID, 2),
            byteArrayOf(BlockFactory.DIAMOND_INGOT_ID, 3)
        ),
        DiamondAxe(
            BlockFactory.DIAMOND_AXE_ID,
            1,
            TOOL_GROUP,
            true,
            byteArrayOf(BlockFactory.STICK_ID, 2),
            byteArrayOf(BlockFactory.DIAMOND_INGOT_ID, 3)
        ),
        DiamondSword(
            BlockFactory.DIAMOND_SWORD_ID,
            1,
            TOOL_GROUP,
            true,
            byteArrayOf(BlockFactory.STICK_ID, 1),
            byteArrayOf(BlockFactory.DIAMOND_INGOT_ID, 2)
        ),
        BlockOfDiamond(
            BlockFactory.DIAMOND_BLOCK_ID,
            1,
            ITEM_GROUP,
            true,
            byteArrayOf(BlockFactory.DIAMOND_INGOT_ID, 9)
        ),
        BlockOfGold(
            BlockFactory.GOLD_BLOCK_ID,
            1,
            ITEM_GROUP,
            true,
            byteArrayOf(BlockFactory.GOLD_INGOT_ID, 9)
        ),
        BlockOfIron(
            BlockFactory.IRON_BLOCK_ID,
            1,
            ITEM_GROUP,
            true,
            byteArrayOf(BlockFactory.IRON_INGOT_ID, 9)
        ),
        Diamond(
            BlockFactory.DIAMOND_INGOT_ID,
            9,
            ITEM_GROUP,
            false,
            byteArrayOf(BlockFactory.DIAMOND_BLOCK_ID, 1)
        ),
        GoldIngot(
            BlockFactory.GOLD_INGOT_ID,
            9,
            ITEM_GROUP,
            false,
            byteArrayOf(BlockFactory.GOLD_BLOCK_ID, 1)
        ),
        IronIngot(
            BlockFactory.IRON_INGOT_ID,
            9,
            ITEM_GROUP,
            false,
            byteArrayOf(BlockFactory.IRON_BLOCK_ID, 1)
        );

        val count: Int
        val group: Int
        val material: Array<ByteArray>
        val isNeedWorkBanch: Boolean

        init {
            this.material = material as Array<ByteArray>
            this.count = count
            this.group = group
            isNeedWorkBanch = needWorkBanch
        }

        constructor(craftedItemId: Byte, count: Int, group: Int, vararg material: ByteArray) : this(
            craftedItemId,
            count,
            group,
            false,
            *material
        ) {
        }
    }
}