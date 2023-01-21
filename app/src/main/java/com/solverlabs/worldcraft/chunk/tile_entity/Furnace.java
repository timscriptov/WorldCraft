package com.solverlabs.worldcraft.chunk.tile_entity;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.util.geom.Vector3i;
import com.solverlabs.worldcraft.factories.DescriptionFactory;
import com.solverlabs.worldcraft.factories.FurnaceItemFactory;
import com.solverlabs.worldcraft.factories.ItemFactory;
import com.solverlabs.worldcraft.inventory.InventoryItem;
import com.solverlabs.worldcraft.nbt.Tag;
import com.solverlabs.worldcraft.util.GameTime;

public class Furnace extends TileEntity {
    private static final long PROCESS_ONE_BLOCK_TIME = 10000;
    public boolean needRecalcLight;
    private long burnTime;
    private long cookTime;
    private InventoryItem craftedItem;
    private byte craftedItemId;
    private InventoryItem currentFuel;
    private InventoryItem currentMaterial;
    private int fuelProcessPercent;
    private boolean inProgress;
    private int maxBurnTime;
    private long processBlockTime;
    private int processPercent;
    private long saveTime;
    private long startTime;

    public Furnace(int x, int y, int z) {
        super(TileEntity.FURNACE_ID, x, y, z);
        this.startTime = 0L;
        this.processBlockTime = 0L;
        this.processPercent = 0;
        this.fuelProcessPercent = 100;
        this.needRecalcLight = true;
    }

    public Furnace(Vector3i pos) {
        super(TileEntity.FURNACE_ID, pos);
        this.startTime = 0L;
        this.processBlockTime = 0L;
        this.processPercent = 0;
        this.fuelProcessPercent = 100;
        this.needRecalcLight = true;
    }

    public Furnace(@NonNull Tag tag) {
        super(TileEntity.FURNACE_ID, 0, 0, 0);
        this.startTime = 0L;
        this.processBlockTime = 0L;
        this.processPercent = 0;
        this.fuelProcessPercent = 100;
        this.needRecalcLight = true;
        this.x = (Integer) tag.findTagByName("x").getValue();
        this.y = (Integer) tag.findTagByName("y").getValue();
        this.z = (Integer) tag.findTagByName("z").getValue();
        Tag itemList = tag.findTagByName("Items");
        if (itemList != null) {
            Tag[] items = (Tag[]) itemList.getValue();
            for (Tag tag2 : items) {
                InventoryItem item = parseItemTag(tag2);
                switch (item.getSlot()) {
                    case 0:
                        this.currentMaterial = item;
                        break;
                    case 1:
                        this.currentFuel = item;
                        break;
                    case 2:
                        this.craftedItem = item;
                        break;
                }
            }
            if (this.currentMaterial != null) {
                this.craftedItemId = FurnaceItemFactory.FurnaceItem.getCraftedItemId(this.currentMaterial.getItemID());
            }
        }
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public int getZ() {
        return this.z;
    }

    public void initFurnace() {
        long time = GameTime.getTime();
        long timeDelta = time - this.saveTime;
        int fuelCount = 0;
        int materialCount = 0;
        if (this.currentFuel != null) {
            fuelCount = this.currentFuel.getCount();
        }
        if (this.currentMaterial != null) {
            materialCount = this.currentMaterial.getCount();
        }
        int sumBurningTime = (int) ((this.maxBurnTime * fuelCount) + (this.maxBurnTime - this.burnTime));
        int sumProcessMaterialTime = (int) ((materialCount * PROCESS_ONE_BLOCK_TIME) + this.cookTime);
        float usedMaterial = (((float) (this.cookTime + timeDelta))) / 10000.0f;
        if (usedMaterial > materialCount) {
        }
        float usedFuel = (((float) (timeDelta - this.burnTime))) / this.maxBurnTime;
        if (usedFuel > fuelCount) {
        }
        if (timeDelta >= sumBurningTime) {
            this.currentFuel.decCount(fuelCount);
            if (sumProcessMaterialTime >= timeDelta) {
                this.currentMaterial.incCount();
            }
        }
    }

    public void advance() {
        long time = GameTime.getTime();
        if (this.currentFuel != null && !this.currentFuel.isEmpty() && this.currentMaterial != null && !this.currentMaterial.isEmpty() && !this.inProgress) {
            this.startTime = time;
            this.processBlockTime = this.startTime;
            this.inProgress = true;
            decFuel();
            this.needRecalcLight = true;
        }
        this.burnTime = time - this.startTime;
        if (this.burnTime <= this.maxBurnTime) {
            this.fuelProcessPercent = (int) ((this.burnTime * 100) / this.maxBurnTime);
            if (this.craftedItem == null || this.craftedItemId == this.craftedItem.getItemID()) {
                processFurance(time);
            } else {
                this.processBlockTime = time;
            }
        } else if (this.currentFuel != null && !this.currentFuel.isEmpty() && this.currentMaterial != null && !this.currentMaterial.isEmpty()) {
            decFuel();
            this.startTime = time;
        } else if (this.inProgress) {
            this.startTime = 0L;
            this.processBlockTime = 0L;
            this.processPercent = 0;
            this.inProgress = false;
            this.needRecalcLight = true;
        }
    }

    private void processFurance(long time) {
        this.cookTime = time - this.processBlockTime;
        if (getMaterial() != null && !getMaterial().isEmpty()) {
            if (this.cookTime >= PROCESS_ONE_BLOCK_TIME) {
                incCraftedItem();
                decMaterial();
                this.processBlockTime = time;
                return;
            }
            this.processPercent = (int) ((this.cookTime * 100) / PROCESS_ONE_BLOCK_TIME);
            return;
        }
        this.processPercent = 0;
        this.processBlockTime = time;
    }

    private void incCraftedItem() {
        if (this.craftedItem == null) {
            this.craftedItemId = FurnaceItemFactory.FurnaceItem.getCraftedItemId(this.currentMaterial.getItemID());
            this.craftedItem = new InventoryItem(ItemFactory.Item.getItemByID(this.craftedItemId), 0);
        }
        if (this.craftedItemId == this.craftedItem.getItemID()) {
            this.craftedItem.incCount();
        }
    }

    public boolean addFuel(InventoryItem fuel) {
        if (this.currentFuel == null) {
            this.currentFuel = fuel;
            this.currentFuel.incCount();
            return true;
        } else if (fuel.getItemID() == this.currentFuel.getItemID() && !this.currentFuel.isFull()) {
            this.currentFuel.incCount();
            return true;
        } else {
            return false;
        }
    }

    public boolean addMaterial(InventoryItem material) {
        if (this.currentMaterial == null) {
            this.currentMaterial = material;
            this.currentMaterial.incCount();
            this.craftedItemId = FurnaceItemFactory.FurnaceItem.getCraftedItemId(this.currentMaterial.getItemID());
            return true;
        } else if (material.getItemID() == this.currentMaterial.getItemID() && !this.currentMaterial.isFull()) {
            this.currentMaterial.incCount();
            return true;
        } else {
            return false;
        }
    }

    public void decFuel() {
        if (this.currentFuel != null) {
            this.currentFuel.decCount();
            this.maxBurnTime = FurnaceItemFactory.FurnaceItem.getBurningTime(this.currentFuel.getItemID());
            if (this.currentFuel.isEmpty()) {
                this.currentFuel = null;
            }
        }
    }

    public void decMaterial() {
        if (this.currentMaterial != null) {
            this.currentMaterial.decCount();
            if (this.currentMaterial.isEmpty()) {
                this.currentMaterial = null;
            }
        }
    }

    public void removeAllFuel() {
        this.currentFuel = null;
    }

    public void removeAllMaterial() {
        this.currentMaterial = null;
    }

    public void removeAllCraftedItem() {
        this.craftedItem = null;
    }

    public byte getCraftedItemId() {
        return this.craftedItemId;
    }

    public int getCraftedItemCount() {
        return this.craftedItem.getCount();
    }

    public int getMaterialCount() {
        return this.currentMaterial.getCount();
    }

    public int getFuelCount() {
        return this.currentFuel.getCount();
    }

    public byte getMaterialId() {
        if (this.currentMaterial != null) {
            return this.currentMaterial.getItemID();
        }
        return (byte) 0;
    }

    public byte getFuelId() {
        if (this.currentFuel != null) {
            return this.currentFuel.getItemID();
        }
        return (byte) 0;
    }

    public InventoryItem getMaterial() {
        return this.currentMaterial;
    }

    public InventoryItem getFuel() {
        return this.currentFuel;
    }

    public InventoryItem getCraftedItem() {
        return this.craftedItem;
    }

    public int getProcessPercent() {
        return this.processPercent;
    }

    public int getFuelProcessPercent() {
        return this.fuelProcessPercent;
    }

    public boolean isInProgress() {
        return this.inProgress;
    }

    @Override
    public Tag getTag() {
        Tag[] tags = {new Tag(Tag.Type.TAG_String, "id", this.id), new Tag(Tag.Type.TAG_Int, "x", this.x), new Tag(Tag.Type.TAG_Int, "y", this.y), new Tag(Tag.Type.TAG_Int, "z", this.z), new Tag(Tag.Type.TAG_Long, "BurnTime", this.burnTime), new Tag(Tag.Type.TAG_Long, "CookTime", this.cookTime), getItemListTag(), new Tag(Tag.Type.TAG_End, null, null)};
        return new Tag(Tag.Type.TAG_Compound, DescriptionFactory.emptyText, tags);
    }

    @NonNull
    private Tag getItemListTag() {
        Tag list = new Tag("Items", Tag.Type.TAG_Compound);
        if (this.currentMaterial != null) {
            list.addTag(getItemTag(0, this.currentMaterial.getItemID(), this.currentMaterial.getDamage(), this.currentMaterial.getCount()));
        }
        if (this.currentFuel != null) {
            list.addTag(getItemTag(1, this.currentFuel.getItemID(), this.currentFuel.getDamage(), this.currentFuel.getCount()));
        }
        if (this.craftedItem != null) {
            list.addTag(getItemTag(2, this.craftedItem.getItemID(), this.craftedItem.getDamage(), this.craftedItem.getCount()));
        }
        return list;
    }
}
