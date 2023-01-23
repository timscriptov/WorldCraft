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
    public boolean needRecalcLight = true;
    private long burnTime;
    private long cookTime;
    private InventoryItem craftedItem;
    private byte craftedItemId;
    private InventoryItem currentFuel;
    private InventoryItem currentMaterial;
    private int fuelProcessPercent = 100;
    private boolean inProgress;
    private int maxBurnTime;
    private long processBlockTime = 0L;
    private int processPercent = 0;
    private long saveTime;
    private long startTime = 0L;

    public Furnace(int x, int y, int z) {
        super(TileEntity.FURNACE_ID, x, y, z);
    }

    public Furnace(Vector3i pos) {
        super(TileEntity.FURNACE_ID, pos);
    }

    public Furnace(@NonNull Tag tag) {
        super(TileEntity.FURNACE_ID, 0, 0, 0);
        x = (Integer) tag.findTagByName("x").getValue();
        y = (Integer) tag.findTagByName("y").getValue();
        z = (Integer) tag.findTagByName("z").getValue();
        Tag itemList = tag.findTagByName("Items");
        if (itemList != null) {
            Tag[] items = (Tag[]) itemList.getValue();
            for (Tag tag2 : items) {
                InventoryItem item = parseItemTag(tag2);
                switch (item.getSlot()) {
                    case 0:
                        currentMaterial = item;
                        break;
                    case 1:
                        currentFuel = item;
                        break;
                    case 2:
                        craftedItem = item;
                        break;
                }
            }
            if (currentMaterial != null) {
                craftedItemId = FurnaceItemFactory.FurnaceItem.getCraftedItemId(currentMaterial.getItemID());
            }
        }
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getZ() {
        return z;
    }

    public void initFurnace() {
        long time = GameTime.getTime();
        long timeDelta = time - saveTime;
        int fuelCount = 0;
        int materialCount = 0;
        if (currentFuel != null) {
            fuelCount = currentFuel.getCount();
        }
        if (currentMaterial != null) {
            materialCount = currentMaterial.getCount();
        }
        int sumBurningTime = (int) ((maxBurnTime * fuelCount) + (maxBurnTime - burnTime));
        int sumProcessMaterialTime = (int) ((materialCount * PROCESS_ONE_BLOCK_TIME) + cookTime);
        float usedMaterial = (((float) (cookTime + timeDelta))) / 10000.0f;
        if (usedMaterial > materialCount) {
        }
        float usedFuel = (((float) (timeDelta - burnTime))) / maxBurnTime;
        if (usedFuel > fuelCount) {
        }
        if (timeDelta >= sumBurningTime) {
            currentFuel.decCount(fuelCount);
            if (sumProcessMaterialTime >= timeDelta) {
                currentMaterial.incCount();
            }
        }
    }

    public void advance() {
        long time = GameTime.getTime();
        if (currentFuel != null && !currentFuel.isEmpty() && currentMaterial != null && !currentMaterial.isEmpty() && !inProgress) {
            startTime = time;
            processBlockTime = startTime;
            inProgress = true;
            decFuel();
            needRecalcLight = true;
        }
        burnTime = time - startTime;
        if (burnTime <= maxBurnTime) {
            fuelProcessPercent = (int) ((burnTime * 100) / maxBurnTime);
            if (craftedItem == null || craftedItemId == craftedItem.getItemID()) {
                processFurance(time);
            } else {
                processBlockTime = time;
            }
        } else if (currentFuel != null && !currentFuel.isEmpty() && currentMaterial != null && !currentMaterial.isEmpty()) {
            decFuel();
            startTime = time;
        } else if (inProgress) {
            startTime = 0L;
            processBlockTime = 0L;
            processPercent = 0;
            inProgress = false;
            needRecalcLight = true;
        }
    }

    private void processFurance(long time) {
        cookTime = time - processBlockTime;
        if (getMaterial() != null && !getMaterial().isEmpty()) {
            if (cookTime >= PROCESS_ONE_BLOCK_TIME) {
                incCraftedItem();
                decMaterial();
                processBlockTime = time;
                return;
            }
            processPercent = (int) ((cookTime * 100) / PROCESS_ONE_BLOCK_TIME);
            return;
        }
        processPercent = 0;
        processBlockTime = time;
    }

    private void incCraftedItem() {
        if (craftedItem == null) {
            craftedItemId = FurnaceItemFactory.FurnaceItem.getCraftedItemId(currentMaterial.getItemID());
            craftedItem = new InventoryItem(ItemFactory.Item.getItemByID(craftedItemId), 0);
        }
        if (craftedItemId == craftedItem.getItemID()) {
            craftedItem.incCount();
        }
    }

    public boolean addFuel(InventoryItem fuel) {
        if (currentFuel == null) {
            currentFuel = fuel;
            currentFuel.incCount();
            return true;
        } else if (fuel.getItemID() == currentFuel.getItemID() && !currentFuel.isFull()) {
            currentFuel.incCount();
            return true;
        } else {
            return false;
        }
    }

    public boolean addMaterial(InventoryItem material) {
        if (currentMaterial == null) {
            currentMaterial = material;
            currentMaterial.incCount();
            craftedItemId = FurnaceItemFactory.FurnaceItem.getCraftedItemId(currentMaterial.getItemID());
            return true;
        } else if (material.getItemID() == currentMaterial.getItemID() && !currentMaterial.isFull()) {
            currentMaterial.incCount();
            return true;
        } else {
            return false;
        }
    }

    public void decFuel() {
        if (currentFuel != null) {
            currentFuel.decCount();
            maxBurnTime = FurnaceItemFactory.FurnaceItem.getBurningTime(currentFuel.getItemID());
            if (currentFuel.isEmpty()) {
                currentFuel = null;
            }
        }
    }

    public void decMaterial() {
        if (currentMaterial != null) {
            currentMaterial.decCount();
            if (currentMaterial.isEmpty()) {
                currentMaterial = null;
            }
        }
    }

    public void removeAllFuel() {
        currentFuel = null;
    }

    public void removeAllMaterial() {
        currentMaterial = null;
    }

    public void removeAllCraftedItem() {
        craftedItem = null;
    }

    public byte getCraftedItemId() {
        return craftedItemId;
    }

    public int getCraftedItemCount() {
        return craftedItem.getCount();
    }

    public int getMaterialCount() {
        return currentMaterial.getCount();
    }

    public int getFuelCount() {
        return currentFuel.getCount();
    }

    public byte getMaterialId() {
        if (currentMaterial != null) {
            return currentMaterial.getItemID();
        }
        return (byte) 0;
    }

    public byte getFuelId() {
        if (currentFuel != null) {
            return currentFuel.getItemID();
        }
        return (byte) 0;
    }

    public InventoryItem getMaterial() {
        return currentMaterial;
    }

    public InventoryItem getFuel() {
        return currentFuel;
    }

    public InventoryItem getCraftedItem() {
        return craftedItem;
    }

    public int getProcessPercent() {
        return processPercent;
    }

    public int getFuelProcessPercent() {
        return fuelProcessPercent;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    @Override
    public Tag getTag() {
        Tag[] tags = {new Tag(Tag.Type.TAG_String, "id", id), new Tag(Tag.Type.TAG_Int, "x", x), new Tag(Tag.Type.TAG_Int, "y", y), new Tag(Tag.Type.TAG_Int, "z", z), new Tag(Tag.Type.TAG_Long, "BurnTime", burnTime), new Tag(Tag.Type.TAG_Long, "CookTime", cookTime), getItemListTag(), new Tag(Tag.Type.TAG_End, null, null)};
        return new Tag(Tag.Type.TAG_Compound, DescriptionFactory.emptyText, tags);
    }

    @NonNull
    private Tag getItemListTag() {
        Tag list = new Tag("Items", Tag.Type.TAG_Compound);
        if (currentMaterial != null) {
            list.addTag(getItemTag(0, currentMaterial.getItemID(), currentMaterial.getDamage(), currentMaterial.getCount()));
        }
        if (currentFuel != null) {
            list.addTag(getItemTag(1, currentFuel.getItemID(), currentFuel.getDamage(), currentFuel.getCount()));
        }
        if (craftedItem != null) {
            list.addTag(getItemTag(2, craftedItem.getItemID(), craftedItem.getDamage(), craftedItem.getCount()));
        }
        return list;
    }
}
