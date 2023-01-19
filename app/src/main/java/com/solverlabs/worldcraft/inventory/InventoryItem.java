package com.solverlabs.worldcraft.inventory;

import com.solverlabs.droid.rugl.geom.BedBlock;
import com.solverlabs.droid.rugl.geom.DoorBlock;
import com.solverlabs.droid.rugl.geom.TexturedShape;
import com.solverlabs.worldcraft.GameMode;
import com.solverlabs.worldcraft.etc.Food;
import com.solverlabs.worldcraft.factories.BlockFactory;
import com.solverlabs.worldcraft.factories.ItemFactory;
import com.solverlabs.worldcraft.material.Material;


public class InventoryItem {
    private final int slot;
    public boolean isInHotbar;
    private BlockFactory.Block block;
    private int count;
    private int currentDurability;
    private ItemFactory.Item item;
    private int maxCount;
    private int maxDurability;

    public InventoryItem(ItemFactory.Item item, int slot) {
        this.count = 0;
        this.isInHotbar = false;
        this.item = item;
        this.maxCount = item.maxCountInStack;
        this.slot = slot;
        this.maxDurability = item.durability;
        this.currentDurability = this.maxDurability;
    }

    public InventoryItem(ItemFactory.Item item, int slot, int durability) {
        this.count = 0;
        this.isInHotbar = false;
        this.item = item;
        this.maxCount = item.maxCountInStack;
        this.slot = slot;
        this.maxDurability = item.durability;
        this.currentDurability = durability;
    }

    public InventoryItem(BlockFactory.Block block, int slot) {
        this.count = 0;
        this.isInHotbar = false;
        this.block = block;
        this.maxCount = 99;
        this.slot = slot;
        this.maxDurability = 1;
        this.currentDurability = this.maxDurability;
    }

    public InventoryItem(byte itemId, int slot, int damage, int count, boolean isInHotbar) {
        this.count = 0;
        this.isInHotbar = false;
        this.item = ItemFactory.Item.getItemByID(itemId);
        this.slot = slot;
        this.currentDurability = damage;
        this.maxDurability = this.item.durability;
        this.count = count;
        this.maxCount = this.item.maxCountInStack;
        this.isInHotbar = isInHotbar;
    }

    /* renamed from: clone */
    public InventoryItem m83clone() {
        return this.item != null ? new InventoryItem(this.item, this.slot, this.currentDurability) : new InventoryItem(this.block, this.slot);
    }

    public int getCount() {
        return this.count;
    }

    public void incCount() {
        this.count++;
    }

    public void decCount() {
        this.count--;
    }

    public void decCount(int count) {
        if (this.count >= count) {
            this.count -= count;
        }
    }

    public void decDurability() {
        this.currentDurability--;
    }

    public boolean isTool() {
        return this.block == null && this.item.isTool();
    }

    public float getDurabilityRatio() {
        return (this.currentDurability * 1.0f) / this.maxDurability;
    }

    public ItemFactory.Item getItem() {
        return this.item;
    }

    public BlockFactory.Block getBlock() {
        if (this.block == null) {
            BlockFactory.Block b = this.item.block;
            return b;
        }
        BlockFactory.Block b2 = this.block;
        return b2;
    }

    public Material getMaterial() {
        return getBlock() != null ? getBlock().material : Material.UNKNOWN;
    }

    public TexturedShape getItemShape() {
        this.item = getItem();
        if (this.item != null) {
            return this.item.itemShape;
        }
        if (this.block != null) {
            if (DoorBlock.isDoor(this.block) || BedBlock.isBed(this.block)) {
                this.block.blockItemShape.state = ItemFactory.itemState;
            }
            return this.block.blockItemShape;
        }
        return null;
    }

    public boolean isFull() {
        return this.count >= this.maxCount;
    }

    public boolean isEmpty() {
        if (GameMode.isCreativeMode()) {
            return false;
        }
        return this.count <= 0 || this.currentDurability <= 0;
    }

    public boolean isUseAsFuel() {
        return this.item.isUseAsFuel();
    }

    public boolean isUseAsMaterial() {
        return this.item.isUseAsMaterials();
    }

    public int getSlot() {
        return this.slot;
    }

    public byte getItemID() {
        if (this.item != null) {
            return this.item.id;
        }
        if (this.block != null) {
            return this.block.id;
        }
        return (byte) 0;
    }

    public int getCurrentDurability() {
        return this.currentDurability;
    }

    public boolean isFood() {
        return ItemFactory.FOOD_ID_LIST.containsKey(Byte.valueOf(getItemID()));
    }

    public Food getFood() {
        return ItemFactory.FOOD_ID_LIST.get(Byte.valueOf(getItemID()));
    }

    public int getDamage() {
        Integer result = ItemFactory.WEAPON_ID_LIST.get(Byte.valueOf(getItemID()));
        if (result != null) {
            return result.intValue();
        }
        return 1;
    }
}
