package com.mcal.worldcraft.chunk.tile_entity;

import androidx.annotation.NonNull;

import com.mcal.droid.rugl.util.geom.Vector3i;
import com.mcal.worldcraft.factories.DescriptionFactory;
import com.mcal.worldcraft.inventory.InventoryItem;
import com.mcal.worldcraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

public class Chest extends TileEntity {
    private static final int CHEST_MAX_SIZE = 27;
    private final List<InventoryItem> chestItems = new ArrayList<>();

    public Chest(Vector3i pos) {
        super(TileEntity.CHEST_ID, pos);
    }

    public Chest(int x, int y, int z) {
        super(TileEntity.CHEST_ID, x, y, z);
    }

    public Chest(@NonNull Tag tag) {
        super(TileEntity.CHEST_ID, 0, 0, 0);
        x = (Integer) tag.findTagByName("x").getValue();
        y = (Integer) tag.findTagByName("y").getValue();
        z = (Integer) tag.findTagByName("z").getValue();
        Tag itemList = tag.findTagByName("Items");
        if (itemList != null) {
            Tag[] items = (Tag[]) itemList.getValue();
            for (Tag tag2 : items) {
                this.chestItems.add(parseItemTag(tag2));
            }
        }
    }

    public boolean addItem(InventoryItem invItem) {
        for (int i = 0; i < chestItems.size(); i++) {
            InventoryItem chestItem = chestItems.get(i);
            if (invItem.getItemID() == chestItem.getItemID() && !chestItem.isFull()) {
                chestItem.incCount();
                return true;
            }
        }
        if (chestItems.size() < CHEST_MAX_SIZE) {
            InventoryItem chestItem = invItem.clone();
            chestItem.incCount();
            chestItems.add(chestItem);
            return true;
        }
        return false;
    }

    public void decItem(@NonNull InventoryItem invItem) {
        invItem.decCount();
        if (invItem.isEmpty()) {
            chestItems.remove(invItem);
        }
    }

    @Override
    public Tag getTag() {
        Tag[] tags = {
                new Tag(Tag.Type.TAG_String, "id", id),
                new Tag(Tag.Type.TAG_Int, "x", x),
                new Tag(Tag.Type.TAG_Int, "y", y),
                new Tag(Tag.Type.TAG_Int, "z", z),
                getItemListTag(),
                new Tag(Tag.Type.TAG_End, null, null)
        };
        return new Tag(Tag.Type.TAG_Compound, DescriptionFactory.emptyText, tags);
    }

    @NonNull
    private Tag getItemListTag() {
        Tag list = new Tag("Items", Tag.Type.TAG_Compound);
        if (chestItems != null) {
            for (int i = 0; i < chestItems.size(); i++) {
                InventoryItem item = chestItems.get(i);
                list.addTag(getItemTag(item.getSlot(), item.getItemID(), item.getDamage(), item.getCount()));
            }
        }
        return list;
    }

    public List<InventoryItem> getChestItems() {
        return this.chestItems;
    }
}
