package com.solverlabs.worldcraft.chunk.tile_entity;

import com.solverlabs.worldcraft.nbt.Tag;


public class TileEntityFactory {
    public static TileEntity parse(Tag tag) {
        String id = (String) tag.findTagByName("id").getValue();
        if (isFurnace(id)) {
            return new Furnace(tag);
        }
        if (isChest(id)) {
            return new Chest(tag);
        }
        return null;
    }

    private static boolean isFurnace(String id) {
        return id.equals(TileEntity.FURNACE_ID);
    }

    private static boolean isChest(String id) {
        return id.equals(TileEntity.CHEST_ID);
    }
}
