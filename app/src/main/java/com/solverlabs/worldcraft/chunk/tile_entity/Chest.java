package com.solverlabs.worldcraft.chunk.tile_entity;

import com.solverlabs.droid.rugl.util.geom.Vector3i;
import com.solverlabs.worldcraft.factories.DescriptionFactory;
import com.solverlabs.worldcraft.inventory.InventoryItem;
import com.solverlabs.worldcraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;


public class Chest extends TileEntity {
    private static final int CHEST_MAX_SIZE = 27;
    private List<InventoryItem> chestItems;

    public Chest(Vector3i pos) {
        super(TileEntity.CHEST_ID, pos);
        this.chestItems = new ArrayList();
    }

    public Chest(int x, int y, int z) {
        super(TileEntity.CHEST_ID, x, y, z);
        this.chestItems = new ArrayList();
    }

    public Chest(Tag tag) {
        super(TileEntity.CHEST_ID, 0, 0, 0);
        this.chestItems = new ArrayList();
        this.x = ((Integer) tag.findTagByName("x").getValue()).intValue();
        this.y = ((Integer) tag.findTagByName("y").getValue()).intValue();
        this.z = ((Integer) tag.findTagByName("z").getValue()).intValue();
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
            InventoryItem chestItem2 = invItem.m83clone();
            chestItem2.incCount();
            this.chestItems.add(chestItem2);
            return true;
        }
        return false;
    }

    public void decItem(InventoryItem invItem) {
        invItem.decCount();
        if (invItem.isEmpty()) {
            this.chestItems.remove(invItem);
        }
    }

    @Override
    public Tag getTag() {
        Tag[] tags = {new Tag(Tag.Type.TAG_String, "id", this.id), new Tag(Tag.Type.TAG_Int, "x", Integer.valueOf(this.x)), new Tag(Tag.Type.TAG_Int, "y", Integer.valueOf(this.y)), new Tag(Tag.Type.TAG_Int, "z", Integer.valueOf(this.z)), getItemListTag(), new Tag(Tag.Type.TAG_End, (String) null, (Tag[]) null)};
        Tag chestTag = new Tag(Tag.Type.TAG_Compound, DescriptionFactory.emptyText, tags);
        return chestTag;
    }

    private Tag getItemListTag() {
        Tag list = new Tag("Items", Tag.Type.TAG_Compound);
        if (this.chestItems != null) {
            for (int i = 0; i < this.chestItems.size(); i++) {
                InventoryItem item = this.chestItems.get(i);
                list.addTag(getItemTag(Integer.valueOf(item.getSlot()), item.getItemID(), item.getDamage(), item.getCount()));
            }
        }
        return list;
    }

    public List<InventoryItem> getChestItems() {
        return this.chestItems;
    }
}
