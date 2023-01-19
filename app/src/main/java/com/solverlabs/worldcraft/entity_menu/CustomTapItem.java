package com.solverlabs.worldcraft.entity_menu;

import com.solverlabs.droid.rugl.gl.StackedRenderer;
import com.solverlabs.worldcraft.inventory.InventoryItem;
import com.solverlabs.worldcraft.inventory.InventoryTapItem;


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
