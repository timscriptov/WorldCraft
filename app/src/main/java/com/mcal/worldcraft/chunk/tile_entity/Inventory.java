package com.mcal.worldcraft.chunk.tile_entity;

import androidx.annotation.NonNull;

import com.mcal.worldcraft.DroppableItem;
import com.mcal.worldcraft.Player;
import com.mcal.worldcraft.factories.ItemFactory;
import com.mcal.worldcraft.inventory.InventoryItem;
import com.mcal.worldcraft.inventory.InventoryTapItem;

import java.util.ArrayList;

public class Inventory {
    public static final int INVENTORY_MAX_SIZE = 32;
    private final Player player;
    private final ArrayList<InventoryItem> inventory = new ArrayList<>();

    public Inventory(Player player) {
        this.player = player;
    }

    public boolean add(@NonNull DroppableItem item) {
        int count = item.getCount();
        byte blockId = item.getBlockID();
        boolean canAdd = true;
        for (int i = 0; i < count && canAdd; i++) {
            canAdd = add(blockId);
            if (canAdd) {
                item.decCount();
            }
        }
        return canAdd;
    }

    public boolean add(byte blockID) {
        for (int i = 0; i < inventory.size(); i++) {
            InventoryItem invItem = inventory.get(i);
            if (invItem.getItemID() == blockID && !invItem.isFull()) {
                invItem.incCount();
                return true;
            }
        }
        if (inventory.size() < INVENTORY_MAX_SIZE) {
            InventoryItem invItem = new InventoryItem(ItemFactory.Item.getItemByID(blockID), getFreeSlot());
            inventory.add(invItem);
            invItem.incCount();
            if (player.hotbar.size() >= 9 || player.isHotBarContainsItem(invItem)) {
                return true;
            }
            player.addItemToHotBar(new InventoryTapItem(player, invItem), true);
            return true;
        }
        return false;
    }

    public boolean add(InventoryItem item) {
        for (int i = 0; i < inventory.size(); i++) {
            InventoryItem invItem = inventory.get(i);
            if (item.getItemID() == invItem.getItemID() && !invItem.isFull()) {
                invItem.incCount();
                return true;
            }
        }
        if (inventory.size() < 32) {
            InventoryItem invItem = item.clone();
            invItem.incCount();
            inventory.add(invItem);
            return true;
        }
        return false;
    }

    private int getFreeSlot() {
        int slot = 0;
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.get(i).getSlot() > slot) {
                slot = inventory.get(i).getSlot();
            }
        }
        return slot + 1;
    }

    public void insertItem(InventoryItem item) {
        inventory.add(item);
    }

    public void decItem(@NonNull InventoryItem invItem) {
        invItem.decCount();
        if (invItem.isEmpty()) {
            inventory.remove(invItem);
        }
    }

    public void decItem(byte itemID) {
        for (int i = 0; i < inventory.size(); i++) {
            InventoryItem invItem = inventory.get(i);
            if (invItem.getItemID() == itemID) {
                invItem.decCount();
                if (invItem.isEmpty()) {
                    inventory.remove(invItem);
                    return;
                }
                return;
            }
        }
    }

    public void remove(@NonNull InventoryItem item) {
        item.decCount(item.getCount());
        inventory.remove(item);
    }

    public int getSize() {
        return inventory.size();
    }

    public ArrayList<InventoryItem> getAllInventoryItems() {
        return inventory;
    }

    public int getItemTotalCount(byte itemID) {
        int totalCount = 0;
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.get(i).getItemID() == itemID) {
                totalCount += inventory.get(i).getCount();
            }
        }
        return totalCount;
    }

    public InventoryItem getElement(int index) {
        return inventory.get(index);
    }

    public void clear() {
        if (inventory != null) {
            inventory.clear();
        }
    }
}
