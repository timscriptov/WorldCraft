package com.solverlabs.worldcraft.chunk.tile_entity;

import androidx.annotation.NonNull;

import com.solverlabs.worldcraft.DroppableItem;
import com.solverlabs.worldcraft.Player;
import com.solverlabs.worldcraft.factories.ItemFactory;
import com.solverlabs.worldcraft.inventory.InventoryItem;
import com.solverlabs.worldcraft.inventory.InventoryTapItem;

import java.util.ArrayList;

public class Inventory {
    public static final int INVENTORY_MAX_SIZE = 32;
    private final Player player;
    ArrayList<InventoryItem> inventory = new ArrayList<>();

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
        for (int i = 0; i < this.inventory.size(); i++) {
            InventoryItem invItem = this.inventory.get(i);
            if (invItem.getItemID() == blockID && !invItem.isFull()) {
                invItem.incCount();
                return true;
            }
        }
        if (this.inventory.size() < INVENTORY_MAX_SIZE) {
            InventoryItem invItem2 = new InventoryItem(ItemFactory.Item.getItemByID(blockID), getFreeSlot());
            this.inventory.add(invItem2);
            invItem2.incCount();
            if (this.player.hotbar.size() >= 5 || this.player.isHotBarContainsItem(invItem2)) {
                return true;
            }
            this.player.addItemToHotBar(new InventoryTapItem(this.player, invItem2), true);
            return true;
        }
        return false;
    }

    public boolean add(InventoryItem item) {
        for (int i = 0; i < this.inventory.size(); i++) {
            InventoryItem invItem = this.inventory.get(i);
            if (item.getItemID() == invItem.getItemID() && !invItem.isFull()) {
                invItem.incCount();
                return true;
            }
        }
        if (this.inventory.size() < 32) {
            InventoryItem invItem2 = item.clone();
            invItem2.incCount();
            this.inventory.add(invItem2);
            return true;
        }
        return false;
    }

    private int getFreeSlot() {
        int slot = 0;
        for (int i = 0; i < this.inventory.size(); i++) {
            if (this.inventory.get(i).getSlot() > slot) {
                slot = this.inventory.get(i).getSlot();
            }
        }
        return slot + 1;
    }

    public void insertItem(InventoryItem item) {
        this.inventory.add(item);
    }

    public void decItem(@NonNull InventoryItem invItem) {
        invItem.decCount();
        if (invItem.isEmpty()) {
            this.inventory.remove(invItem);
        }
    }

    public void decItem(byte itemID) {
        for (int i = 0; i < this.inventory.size(); i++) {
            InventoryItem invItem = this.inventory.get(i);
            if (invItem.getItemID() == itemID) {
                invItem.decCount();
                if (invItem.isEmpty()) {
                    this.inventory.remove(invItem);
                    return;
                }
                return;
            }
        }
    }

    public void remove(@NonNull InventoryItem item) {
        item.decCount(item.getCount());
        this.inventory.remove(item);
    }

    public int getSize() {
        return this.inventory.size();
    }

    public ArrayList<InventoryItem> getAllInventoryItems() {
        return this.inventory;
    }

    public int getItemTotalCount(byte itemID) {
        int totalCount = 0;
        for (int i = 0; i < this.inventory.size(); i++) {
            if (this.inventory.get(i).getItemID() == itemID) {
                totalCount += this.inventory.get(i).getCount();
            }
        }
        return totalCount;
    }

    public InventoryItem getElement(int index) {
        return this.inventory.get(index);
    }

    public void clear() {
        if (this.inventory != null) {
            this.inventory.clear();
        }
    }
}
