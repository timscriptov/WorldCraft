package com.solverlabs.worldcraft.entity_menu;

import android.opengl.GLES10;

import com.solverlabs.droid.rugl.Game;
import com.solverlabs.droid.rugl.geom.ColouredShape;
import com.solverlabs.droid.rugl.geom.Shape;
import com.solverlabs.droid.rugl.geom.ShapeUtil;
import com.solverlabs.droid.rugl.gl.StackedRenderer;
import com.solverlabs.droid.rugl.gl.State;
import com.solverlabs.droid.rugl.input.Touch;
import com.solverlabs.droid.rugl.text.Font;
import com.solverlabs.droid.rugl.text.TextShape;
import com.solverlabs.droid.rugl.util.Colour;
import com.solverlabs.droid.rugl.util.geom.BoundingRectangle;
import com.solverlabs.worldcraft.SoundManager;
import com.solverlabs.worldcraft.Sounds;
import com.solverlabs.worldcraft.chunk.tile_entity.Chest;
import com.solverlabs.worldcraft.chunk.tile_entity.Inventory;
import com.solverlabs.worldcraft.chunk.tile_entity.TileEntity;
import com.solverlabs.worldcraft.math.MathUtils;
import com.solverlabs.worldcraft.ui.GUI;

import java.util.ArrayList;

public class ChestMenu extends CustomMenu {
    private final ArrayList<CustomTapItem> chestTapItems;
    private final ArrayList<CustomTapItem> inventoryTapItems;
    public BoundingRectangle chestScissorBound;
    public BoundingRectangle inventoryScissorBound;
    private Chest chest;
    private ColouredShape chestScissorBoundShape;
    private TextShape chestTextShape;
    private ColouredShape fillTitleShape;
    private ColouredShape inventoryScissorBoundShape;
    private TextShape inventoryTextShape;
    private boolean needToScrollChest;
    private boolean needToScrollInv;
    private float prevChestScrollY;
    private float prevInvScrollY;
    private float touchChestDelta;
    private float touchInvDelta;

    public ChestMenu(Inventory inventory) {
        super(inventory);
        chestScissorBound = new BoundingRectangle(380.0f, 10.0f, 340.0f, 400.0f);
        inventoryScissorBound = new BoundingRectangle(20.0f, 10.0f, 340.0f, 400.0f);
        chestTapItems = new ArrayList<>();
        inventoryTapItems = new ArrayList<>();
    }

    public void initInventoryItems() {
        inventoryTapItems.clear();
        if (inventory != null) {
            for (int i = 0; i < inventory.getSize(); i++) {
                inventoryTapItems.add(new CustomTapItem(inventory.getElement(i)) {
                    @Override
                    protected void onTap() {
                        if (chest.addItem(item)) {
                            inventory.decItem(item);
                            initChestItems();
                            if (item.isEmpty()) {
                                initInventoryItems();
                            }
                        }
                    }

                    @Override
                    protected void onLongPress() {
                        if (chest.addItem(item)) {
                            inventory.decItem(item);
                            initChestItems();
                            if (item.isEmpty()) {
                                initInventoryItems();
                            }
                        }
                    }
                });
            }
        }
    }

    public void initChestItems() {
        chestTapItems.clear();
        if (chest != null) {
            for (int i = 0; i < chest.getChestItems().size(); i++) {
                chestTapItems.add(new CustomTapItem(chest.getChestItems().get(i)) {
                    @Override
                    protected void onTap() {
                        if (inventory.add(item)) {
                            chest.decItem(item);
                            initInventoryItems();
                            if (item.isEmpty()) {
                                initChestItems();
                            }
                        }
                    }

                    @Override
                    protected void onLongPress() {
                        if (inventory.add(item)) {
                            chest.decItem(item);
                            initInventoryItems();
                            if (item.isEmpty()) {
                                initChestItems();
                            }
                        }
                    }
                });
            }
        }
    }

    public void advance() {
        if (show) {
            exitTap.advance();
            for (int i = 0; i < chestTapItems.size(); i++) {
                chestTapItems.get(i).advance();
            }
            for (int i = 0; i < inventoryTapItems.size(); i++) {
                inventoryTapItems.get(i).advance();
            }
            if (needToScrollChest && chestTapItems.size() / 4.0f > 4.0f) {
                touchChestDelta = touch.y - prevChestScrollY;
            }
            if (needToScrollInv && inventoryTapItems.size() / 4.0f > 4.0f) {
                touchInvDelta = touch.y - prevInvScrollY;
            }
            if (!chestTapItems.isEmpty() && chestTapItems.size() / 4.0f > 4.0f) {
                normalizeChestScroll();
            }
            if (!inventoryTapItems.isEmpty() && inventoryTapItems.size() / 4.0f > 4.0f) {
                normalizeInvScroll();
            }
        }
    }

    private void normalizeChestScroll() {
        CustomTapItem lastItem = chestTapItems.get(chestTapItems.size() - 1);
        float bottomPoint = lastItem.getY() + lastItem.getYOffset();
        float topPoint = chestTapItems.get(0).bounds.y.getMax();
        if (touchChestDelta + bottomPoint > chestScissorBound.y.getMin()) {
            float yOffset = (80.0f * MathUtils.roundUp(chestTapItems.size() / 4.0f)) - 375.0f;
            touchChestDelta = 0.0f;
            for (CustomTapItem item : chestTapItems) {
                item.setYOffset(yOffset);
            }
        }
        if (touchChestDelta + topPoint < chestScissorBound.y.getMax()) {
            touchChestDelta = 0.0f;
            for (CustomTapItem item2 : chestTapItems) {
                item2.setYOffset(0.0f);
            }
        }
    }

    private void normalizeInvScroll() {
        CustomTapItem lastItem = inventoryTapItems.get(inventoryTapItems.size() - 1);
        float bottomPoint = lastItem.getY() + lastItem.getYOffset();
        float topPoint = inventoryTapItems.get(0).bounds.y.getMax();
        if (touchInvDelta + bottomPoint > inventoryScissorBound.y.getMin()) {
            float yOffset = (80.0f * MathUtils.roundUp(inventoryTapItems.size() / 4.0f)) - 375.0f;
            for (CustomTapItem item : inventoryTapItems) {
                item.setYOffset(yOffset);
            }
        }
        if (touchInvDelta + topPoint < inventoryScissorBound.y.getMax()) {
            touchInvDelta = 0.0f;
            for (CustomTapItem item2 : inventoryTapItems) {
                item2.setYOffset(0.0f);
            }
        }
    }

    @Override
    public void draw(StackedRenderer sr) {
        if (show) {
            super.draw(sr);
            drawInventoryItems(sr);
            drawChestItems(sr);
            drawScissorBound(sr);
            drawTitle(sr);
            exitTap.draw(sr);
        }
    }

    private void drawInventoryItems(StackedRenderer sr) {
        GLES10.glEnable(3089);
        GLES10.glScissor((int) (20.0f * RATIO_X), (int) (10.0f * RATIO_Y), (int) (340.0f * RATIO_X), (int) (400.0f * RATIO_Y));
        float x = 65.0f;
        float y = 365.0f;
        float yOffset = 0.0f;
        int k = 0;
        for (int i = 0; i < inventoryTapItems.size(); i++) {
            if (k == 4) {
                k = 0;
                yOffset += 1.0f;
                x = 65.0f;
                y = 365.0f - (84.0f * yOffset);
            }
            inventoryTapItems.get(i).setPosition(x, y);
            inventoryTapItems.get(i).draw(sr, touchInvDelta);
            x += 84.0f;
            k++;
        }
        sr.render();
        GLES10.glDisable(3089);
    }

    private void drawChestItems(StackedRenderer sr) {
        GLES10.glEnable(3089);
        GLES10.glScissor((int) (380.0f * RATIO_X), (int) (10.0f * RATIO_Y), (int) (340.0f * RATIO_X), (int) (400.0f * RATIO_Y));
        float x = 425.0f;
        float y = 365.0f;
        float yOffset = 0.0f;
        int k = 0;
        for (int i = 0; i < chestTapItems.size(); i++) {
            if (k == 4) {
                k = 0;
                yOffset += 1.0f;
                x = 425.0f;
                y = 365.0f - (84.0f * yOffset);
            }
            chestTapItems.get(i).setPosition(x, y);
            chestTapItems.get(i).draw(sr, touchChestDelta);
            x += 84.0f;
            k++;
        }
        sr.render();
        GLES10.glDisable(3089);
    }

    private void drawScissorBound(StackedRenderer sr) {
        if (inventoryScissorBoundShape == null) {
            Shape bs = ShapeUtil.innerQuad(20.0f, 10.0f, 360.0f, 410.0f, 4.0f, 0.0f);
            inventoryScissorBoundShape = new ColouredShape(bs, Colour.black, (State) null);
            Shape bs2 = ShapeUtil.innerQuad(380.0f, 10.0f, 720.0f, 410.0f, 4.0f, 0.0f);
            chestScissorBoundShape = new ColouredShape(bs2, Colour.black, (State) null);
        }
        inventoryScissorBoundShape.render(sr);
        chestScissorBoundShape.render(sr);
    }

    private void drawTitle(StackedRenderer sr) {
        if (inventoryTextShape == null) {
            Font font = GUI.getFont();
            inventoryTextShape = font.buildTextShape("Inventory", Colour.white);
            inventoryTextShape.translate(((340.0f - font.getStringLength(TileEntity.FURNACE_ID)) / 2.0f) + 20.0f, (Game.gameHeight - font.size) - 20.0f, 0.0f);
            chestTextShape = font.buildTextShape(TileEntity.CHEST_ID, Colour.white);
            chestTextShape.translate(((340.0f - font.getStringLength(TileEntity.CHEST_ID)) / 2.0f) + 380.0f, (Game.gameHeight - font.size) - 20.0f, 0.0f);
            Shape s = ShapeUtil.filledQuad(20.0f, Game.gameHeight - 10.0f, 360.0f, Game.gameHeight - 78.0f, 0.0f);
            fillTitleShape = new ColouredShape(s, Colour.packFloat(0.0f, 0.0f, 0.0f, 0.5f), (State) null);
        }
        sr.pushMatrix();
        fillTitleShape.render(sr);
        sr.translate(360.0f, 0.0f, 0.0f);
        fillTitleShape.render(sr);
        sr.popMatrix();
        sr.render();
        inventoryTextShape.render(sr);
        chestTextShape.render(sr);
    }

    public ArrayList<CustomTapItem> getTapItems() {
        return chestTapItems;
    }

    public void setChest(Chest chest) {
        this.chest = chest;
    }

    public void showOrHide() {
        setShow(!show);
        if (isVisible()) {
            initInventoryItems();
            initChestItems();
        }
    }

    @Override
    public boolean pointerAdded(Touch.Pointer p) {
        if (touch == null && bounds.contains(p.x, p.y) && show) {
            touch = p;
            exitTap.pointerAdded(touch);
            if (chestScissorBound.contains(p.x, p.y)) {
                needToScrollChest = true;
                prevChestScrollY = touch.y;
            }
            if (inventoryScissorBound.contains(p.x, p.y)) {
                needToScrollInv = true;
                prevInvScrollY = touch.y;
            }
            for (int i = 0; i < chestTapItems.size(); i++) {
                chestTapItems.get(i).pointerAdded(touch);
            }
            for (int i = 0; i < inventoryTapItems.size(); i++) {
                inventoryTapItems.get(i).pointerAdded(touch);
            }
            return true;
        }
        return false;
    }

    @Override
    public void pointerRemoved(Touch.Pointer p) {
        if (touch == p && touch != null) {
            exitTap.pointerRemoved(touch);
            for (int i = 0; i < chestTapItems.size(); i++) {
                chestTapItems.get(i).translateYOffset(touchChestDelta);
                chestTapItems.get(i).pointerRemoved(touch);
            }
            for (int i2 = 0; i2 < inventoryTapItems.size(); i2++) {
                inventoryTapItems.get(i2).translateYOffset(touchInvDelta);
                inventoryTapItems.get(i2).pointerRemoved(touch);
            }
            touchChestDelta = 0.0f;
            touchInvDelta = 0.0f;
            needToScrollChest = false;
            needToScrollInv = false;
            touch = null;
        }
    }

    @Override
    public void setShow(boolean isShow) {
        if (isShow != isVisible()) {
            SoundManager.playSound(isShow ? Sounds.CHEST_OPEN : Sounds.CHEST_CLOSE);
        }
        super.setShow(isShow);
    }

    @Override
    public void reset() {
    }
}
