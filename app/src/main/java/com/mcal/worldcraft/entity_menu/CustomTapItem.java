package com.mcal.worldcraft.entity_menu;

import com.mcal.droid.rugl.gl.StackedRenderer;
import com.mcal.worldcraft.inventory.InventoryItem;
import com.mcal.worldcraft.inventory.InventoryTapItem;

public class CustomTapItem extends InventoryTapItem {
    public CustomTapItem(InventoryItem item) {
        super(item);
    }

    public CustomTapItem(InventoryItem item, float x, float y) {
        super(null, item, x, y);
    }

    @Override
    public void draw(StackedRenderer sr, float deltaY) {
        super.draw(sr, deltaY);
    }
}
