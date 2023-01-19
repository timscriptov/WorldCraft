package com.solverlabs.worldcraft.chunk.tile_entity;

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

    public TileEntity(String id, Vector3i pos) {
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

    /* JADX INFO: Access modifiers changed from: protected */
    public Tag getItemTag(Integer slot, byte id, int damage, int count) {
        Tag[] tags = {new Tag(Tag.Type.TAG_Int, "Slot", slot), new Tag(Tag.Type.TAG_Byte, "id", Byte.valueOf(id)), new Tag(Tag.Type.TAG_Int, "Damage", Integer.valueOf(damage)), new Tag(Tag.Type.TAG_Int, "Count", Integer.valueOf(count)), new Tag(Tag.Type.TAG_End, (String) null, (Tag[]) null)};
        Tag itemTag = new Tag(Tag.Type.TAG_Compound, DescriptionFactory.emptyText, tags);
        return itemTag;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public InventoryItem parseItemTag(Tag tag) {
        int slot = ((Integer) tag.findTagByName("Slot").getValue()).intValue();
        byte id = ((Byte) tag.findTagByName("id").getValue()).byteValue();
        int damage = ((Integer) tag.findTagByName("Damage").getValue()).intValue();
        int count = ((Integer) tag.findTagByName("Count").getValue()).intValue();
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
