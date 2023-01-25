package com.mcal.worldcraft.inventory;

import androidx.annotation.NonNull;

import com.mcal.droid.rugl.geom.BedBlock;
import com.mcal.droid.rugl.geom.DoorBlock;
import com.mcal.droid.rugl.geom.TexturedShape;
import com.mcal.worldcraft.GameMode;
import com.mcal.worldcraft.etc.Food;
import com.mcal.worldcraft.factories.BlockFactory;
import com.mcal.worldcraft.factories.ItemFactory;
import com.mcal.worldcraft.material.Material;

public class InventoryItem {
    private final int maxCount;
    private final int maxDurability;
    private final int slot;
    public boolean isInHotbar = false;
    private BlockFactory.Block block;
    private int count = 0;
    private int currentDurability;
    private ItemFactory.Item item;

    public InventoryItem(@NonNull ItemFactory.Item item, int slot) {
        this.item = item;
        maxCount = item.maxCountInStack;
        this.slot = slot;
        maxDurability = item.durability;
        currentDurability = maxDurability;
    }

    public InventoryItem(@NonNull ItemFactory.Item item, int slot, int durability) {
        this.item = item;
        maxCount = item.maxCountInStack;
        this.slot = slot;
        maxDurability = item.durability;
        currentDurability = durability;
    }

    public InventoryItem(BlockFactory.Block block, int slot) {
        this.block = block;
        maxCount = 99;
        this.slot = slot;
        maxDurability = 1;
        currentDurability = maxDurability;
    }

    public InventoryItem(byte itemId, int slot, int damage, int count, boolean isInHotbar) {
        item = ItemFactory.Item.getItemByID(itemId);
        this.slot = slot;
        currentDurability = damage;
        maxDurability = item.durability;
        this.count = count;
        maxCount = item.maxCountInStack;
        this.isInHotbar = isInHotbar;
    }

    public InventoryItem clone() {
        return item != null ? new InventoryItem(item, slot, currentDurability) : new InventoryItem(block, slot);
    }

    public int getCount() {
        return count;
    }

    public void incCount() {
        count++;
    }

    public void decCount() {
        count--;
    }

    public void decCount(int count) {
        if (this.count >= count) {
            this.count -= count;
        }
    }

    public void decDurability() {
        currentDurability--;
    }

    public boolean isTool() {
        return block == null && item.isTool();
    }

    public float getDurabilityRatio() {
        return (currentDurability * 1.0f) / maxDurability;
    }

    public ItemFactory.Item getItem() {
        return item;
    }

    public BlockFactory.Block getBlock() {
        if (block == null) {
            return item.block;
        }
        return block;
    }

    public Material getMaterial() {
        return getBlock() != null ? getBlock().material : Material.UNKNOWN;
    }

    public TexturedShape getItemShape() {
        item = getItem();
        if (item != null) {
            return item.itemShape;
        }
        if (block != null) {
            if (DoorBlock.isDoor(block) || BedBlock.isBed(block)) {
                block.blockItemShape.state = ItemFactory.itemState;
            }
            return block.blockItemShape;
        }
        return null;
    }

    public boolean isFull() {
        return count >= maxCount;
    }

    public boolean isEmpty() {
        if (GameMode.isCreativeMode()) {
            return false;
        }
        return count <= 0 || currentDurability <= 0;
    }

    public boolean isUseAsFuel() {
        return item.isUseAsFuel();
    }

    public boolean isUseAsMaterial() {
        return item.isUseAsMaterials();
    }

    public int getSlot() {
        return slot;
    }

    public byte getItemID() {
        if (item != null) {
            return item.id;
        }
        if (block != null) {
            return block.id;
        }
        return (byte) 0;
    }

    public int getCurrentDurability() {
        return currentDurability;
    }

    public boolean isFood() {
        return ItemFactory.FOOD_ID_LIST.containsKey(getItemID());
    }

    public Food getFood() {
        return ItemFactory.FOOD_ID_LIST.get(getItemID());
    }

    public int getDamage() {
        Integer result = ItemFactory.WEAPON_ID_LIST.get(getItemID());
        if (result != null) {
            return result;
        }
        return 1;
    }
}
