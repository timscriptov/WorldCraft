package com.solverlabs.worldcraft.chunk.tile_entity;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.util.geom.Vector3i;
import com.solverlabs.worldcraft.factories.DescriptionFactory;
import com.solverlabs.worldcraft.inventory.InventoryItem;
import com.solverlabs.worldcraft.nbt.Tag;

public abstract class TileEntity {
    public static final String CHEST_ID = "Chest";
    public static final String FURNACE_ID = "Furnace";
    protected String id;
    protected int x;
    protected int y;
    protected int z;

    public TileEntity(String id, int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.id = id;
    }

    public TileEntity(String id, @NonNull Vector3i pos) {
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
        this.id = id;
    }

    public abstract Tag getTag();

    public boolean isFurnace() {
        return this.id.equals(FURNACE_ID);
    }

    public boolean isChest() {
        return this.id.equals(CHEST_ID);
    }

    public boolean isSelectedEntity(int x, int y, int z) {
        return this.x == x && this.y == y && this.z == z;
    }

    public Tag getItemTag(Integer slot, byte id, int damage, int count) {
        Tag[] tags = {new Tag(Tag.Type.TAG_Int, "Slot", slot), new Tag(Tag.Type.TAG_Byte, "id", id), new Tag(Tag.Type.TAG_Int, "Damage", damage), new Tag(Tag.Type.TAG_Int, "Count", count), new Tag(Tag.Type.TAG_End, null, null)};
        return new Tag(Tag.Type.TAG_Compound, DescriptionFactory.emptyText, tags);
    }

    public InventoryItem parseItemTag(@NonNull Tag tag) {
        int slot = (Integer) tag.findTagByName("Slot").getValue();
        byte id = (Byte) tag.findTagByName("id").getValue();
        int damage = (Integer) tag.findTagByName("Damage").getValue();
        int count = (Integer) tag.findTagByName("Count").getValue();
        return new InventoryItem(id, slot, damage, count, false);
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }
}
