package com.solverlabs.worldcraft.chunk.tile_entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.solverlabs.worldcraft.nbt.Tag;

import org.jetbrains.annotations.Contract;

public class TileEntityFactory {
    @Nullable
    public static TileEntity parse(@NonNull Tag tag) {
        String id = (String) tag.findTagByName("id").getValue();
        if (isFurnace(id)) {
            return new Furnace(tag);
        }
        if (isChest(id)) {
            return new Chest(tag);
        }
        return null;
    }

    @Contract(pure = true)
    private static boolean isFurnace(@NonNull String id) {
        return id.equals(TileEntity.FURNACE_ID);
    }

    @Contract(pure = true)
    private static boolean isChest(@NonNull String id) {
        return id.equals(TileEntity.CHEST_ID);
    }
}
