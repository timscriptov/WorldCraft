package com.solverlabs.worldcraft.chunk.tile_entity;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.util.geom.Vector3i;
import com.solverlabs.worldcraft.factories.DescriptionFactory;
import com.solverlabs.worldcraft.inventory.InventoryItem;
import com.solverlabs.worldcraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

public class Chest extends TileEntity {
    private static final int CHEST_MAX_SIZE = 27;
    private final List<InventoryItem> chestItems;

    public Chest(Vector3i pos) {
        super(TileEntity.CHEST_ID, pos);
        this.chestItems = new ArrayList<>();
    }

    public Chest(int x, int y, int z) {
        super(TileEntity.CHEST_ID, x, y, z);
        this.chestItems = new ArrayList<>();
    }

    public Chest(@NonNull Tag tag) {
        super(TileEntity.CHEST_ID, 0, 0, 0);
        this.chestItems = new ArrayList<>();
        this.x = (Integer) tag.findTagByName("x").getValue();
        this.y = (Integer) tag.findTagByName("y").getValue();
        this.z = (Integer) tag.findTagByName("z").getValue();
        Tag itemList = tag.findTagByName("Items");
        if (itemList != null) {
            Tag[] items = (Tag[]) itemList.getValue();
            for (Tag tag2 : items) {
                this.chestItems.add(parseItemTag(tag2));
            }
        }
    }

    public boolean addItem(InventoryItem invItem) {
        for (int i = 0; i < this.chestItems.size(); i++) {
            InventoryItem chestItem = this.chestItems.get(i);
            if (invItem.getItemID() == chestItem.getItemID() && !chestItem.isFull()) {
                chestItem.incCount();
                return true;
            }
        }
        if (this.chestItems.size() < CHEST_MAX_SIZE) {
            InventoryItem chestItem2 = invItem.clone();
            chestItem2.incCount();
            this.chestItems.add(chestItem2);
            return true;
        }
        return false;
    }

    public void decItem(@NonNull InventoryItem invItem) {
        invItem.decCount();
        if (invItem.isEmpty()) {
            this.chestItems.remove(invItem);
        }
    }

    @Override
    public Tag getTag() {
        Tag[] tags = {new Tag(Tag.Type.TAG_String, "id", this.id), new Tag(Tag.Type.TAG_Int, "x", this.x), new Tag(Tag.Type.TAG_Int, "y", this.y), new Tag(Tag.Type.TAG_Int, "z", this.z), getItemListTag(), new Tag(Tag.Type.TAG_End, null, null)};
        return new Tag(Tag.Type.TAG_Compound, DescriptionFactory.emptyText, tags);
    }

    @NonNull
    private Tag getItemListTag() {
        Tag list = new Tag("Items", Tag.Type.TAG_Compound);
        if (this.chestItems != null) {
            for (int i = 0; i < this.chestItems.size(); i++) {
                InventoryItem item = this.chestItems.get(i);
                list.addTag(getItemTag(item.getSlot(), item.getItemID(), item.getDamage(), item.getCount()));
            }
        }
        return list;
    }

    public List<InventoryItem> getChestItems() {
        return this.chestItems;
    }
}
