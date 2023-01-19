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
        this.chestScissorBound = new BoundingRectangle(380.0f, 10.0f, 340.0f, 400.0f);
        this.inventoryScissorBound = new BoundingRectangle(20.0f, 10.0f, 340.0f, 400.0f);
        this.chestTapItems = new ArrayList<>();
        this.inventoryTapItems = new ArrayList<>();
    }

    public void initInventoryItems() {
        this.inventoryTapItems.clear();
        if (this.inventory != null) {
            for (int i = 0; i < this.inventory.getSize(); i++) {
                this.inventoryTapItems.add(new CustomTapItem(this.inventory.getElement(i)) {
                    @Override
                    protected void onTap() {
                        if (chest.addItem(this.item)) {
                            inventory.decItem(this.item);
                            initChestItems();
                            if (this.item.isEmpty()) {
                                initInventoryItems();
                            }
                        }
                    }

                    @Override
                    protected void onLongPress() {
                        if (chest.addItem(this.item)) {
                            inventory.decItem(this.item);
                            initChestItems();
                            if (this.item.isEmpty()) {
                                initInventoryItems();
                            }
                        }
                    }
                });
            }
        }
    }

    public void initChestItems() {
        this.chestTapItems.clear();
        if (this.chest != null) {
            for (int i = 0; i < this.chest.getChestItems().size(); i++) {
                this.chestTapItems.add(new CustomTapItem(this.chest.getChestItems().get(i)) {
                    @Override
                    protected void onTap() {
                        if (inventory.add(this.item)) {
                            chest.decItem(this.item);
                            initInventoryItems();
                            if (this.item.isEmpty()) {
                                initChestItems();
                            }
                        }
                    }

                    @Override
                    protected void onLongPress() {
                        if (inventory.add(this.item)) {
                            chest.decItem(this.item);
                            initInventoryItems();
                            if (this.item.isEmpty()) {
                                initChestItems();
                            }
                        }
                    }
                });
            }
        }
    }

    public void advance() {
        if (this.show) {
            this.exitTap.advance();
            for (int i = 0; i < this.chestTapItems.size(); i++) {
                this.chestTapItems.get(i).advance();
            }
            for (int i2 = 0; i2 < this.inventoryTapItems.size(); i2++) {
                this.inventoryTapItems.get(i2).advance();
            }
            if (this.needToScrollChest && this.chestTapItems.size() / 4.0f > 4.0f) {
                this.touchChestDelta = this.touch.y - this.prevChestScrollY;
            }
            if (this.needToScrollInv && this.inventoryTapItems.size() / 4.0f > 4.0f) {
                this.touchInvDelta = this.touch.y - this.prevInvScrollY;
            }
            if (!this.chestTapItems.isEmpty() && this.chestTapItems.size() / 4.0f > 4.0f) {
                normalizeChestScroll();
            }
            if (!this.inventoryTapItems.isEmpty() && this.inventoryTapItems.size() / 4.0f > 4.0f) {
                normalizeInvScroll();
            }
        }
    }

    private void normalizeChestScroll() {
        CustomTapItem lastItem = this.chestTapItems.get(this.chestTapItems.size() - 1);
        float bottomPoint = lastItem.getY() + lastItem.getYOffset();
        float topPoint = this.chestTapItems.get(0).bounds.y.getMax();
        if (this.touchChestDelta + bottomPoint > this.chestScissorBound.y.getMin()) {
            float yOffset = (80.0f * MathUtils.roundUp(this.chestTapItems.size() / 4.0f)) - 375.0f;
            this.touchChestDelta = 0.0f;
            for (CustomTapItem item : this.chestTapItems) {
                item.setYOffset(yOffset);
            }
        }
        if (this.touchChestDelta + topPoint < this.chestScissorBound.y.getMax()) {
            this.touchChestDelta = 0.0f;
            for (CustomTapItem item2 : this.chestTapItems) {
                item2.setYOffset(0.0f);
            }
        }
    }

    private void normalizeInvScroll() {
        CustomTapItem lastItem = this.inventoryTapItems.get(this.inventoryTapItems.size() - 1);
        float bottomPoint = lastItem.getY() + lastItem.getYOffset();
        float topPoint = this.inventoryTapItems.get(0).bounds.y.getMax();
        if (this.touchInvDelta + bottomPoint > this.inventoryScissorBound.y.getMin()) {
            float yOffset = (80.0f * MathUtils.roundUp(this.inventoryTapItems.size() / 4.0f)) - 375.0f;
            for (CustomTapItem item : this.inventoryTapItems) {
                item.setYOffset(yOffset);
            }
        }
        if (this.touchInvDelta + topPoint < this.inventoryScissorBound.y.getMax()) {
            this.touchInvDelta = 0.0f;
            for (CustomTapItem item2 : this.inventoryTapItems) {
                item2.setYOffset(0.0f);
            }
        }
    }

    @Override
    public void draw(StackedRenderer sr) {
        if (this.show) {
            super.draw(sr);
            drawInventoryItems(sr);
            drawChestItems(sr);
            drawScissorBound(sr);
            drawTitle(sr);
            this.exitTap.draw(sr);
        }
    }

    private void drawInventoryItems(StackedRenderer sr) {
        GLES10.glEnable(3089);
        GLES10.glScissor((int) (20.0f * RATIO_X), (int) (10.0f * RATIO_Y), (int) (340.0f * RATIO_X), (int) (400.0f * RATIO_Y));
        float x = 65.0f;
        float y = 365.0f;
        float yOffset = 0.0f;
        int k = 0;
        for (int i = 0; i < this.inventoryTapItems.size(); i++) {
            if (k == 4) {
                k = 0;
                yOffset += 1.0f;
                x = 65.0f;
                y = 365.0f - (84.0f * yOffset);
            }
            this.inventoryTapItems.get(i).setPosition(x, y);
            this.inventoryTapItems.get(i).draw(sr, this.touchInvDelta);
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
        for (int i = 0; i < this.chestTapItems.size(); i++) {
            if (k == 4) {
                k = 0;
                yOffset += 1.0f;
                x = 425.0f;
                y = 365.0f - (84.0f * yOffset);
            }
            this.chestTapItems.get(i).setPosition(x, y);
            this.chestTapItems.get(i).draw(sr, this.touchChestDelta);
            x += 84.0f;
            k++;
        }
        sr.render();
        GLES10.glDisable(3089);
    }

    private void drawScissorBound(StackedRenderer sr) {
        if (this.inventoryScissorBoundShape == null) {
            Shape bs = ShapeUtil.innerQuad(20.0f, 10.0f, 360.0f, 410.0f, 4.0f, 0.0f);
            this.inventoryScissorBoundShape = new ColouredShape(bs, Colour.black, (State) null);
            Shape bs2 = ShapeUtil.innerQuad(380.0f, 10.0f, 720.0f, 410.0f, 4.0f, 0.0f);
            this.chestScissorBoundShape = new ColouredShape(bs2, Colour.black, (State) null);
        }
        this.inventoryScissorBoundShape.render(sr);
        this.chestScissorBoundShape.render(sr);
    }

    private void drawTitle(StackedRenderer sr) {
        if (this.inventoryTextShape == null) {
            Font font = GUI.getFont();
            this.inventoryTextShape = font.buildTextShape("Inventory", Colour.white);
            this.inventoryTextShape.translate(((340.0f - font.getStringLength(TileEntity.FURNACE_ID)) / 2.0f) + 20.0f, (Game.mGameHeight - font.size) - 20.0f, 0.0f);
            this.chestTextShape = font.buildTextShape(TileEntity.CHEST_ID, Colour.white);
            this.chestTextShape.translate(((340.0f - font.getStringLength(TileEntity.CHEST_ID)) / 2.0f) + 380.0f, (Game.mGameHeight - font.size) - 20.0f, 0.0f);
            Shape s = ShapeUtil.filledQuad(20.0f, Game.mGameHeight - 10.0f, 360.0f, Game.mGameHeight - 78.0f, 0.0f);
            this.fillTitleShape = new ColouredShape(s, Colour.packFloat(0.0f, 0.0f, 0.0f, 0.5f), (State) null);
        }
        sr.pushMatrix();
        this.fillTitleShape.render(sr);
        sr.translate(360.0f, 0.0f, 0.0f);
        this.fillTitleShape.render(sr);
        sr.popMatrix();
        sr.render();
        this.inventoryTextShape.render(sr);
        this.chestTextShape.render(sr);
    }

    public ArrayList<CustomTapItem> getTapItems() {
        return this.chestTapItems;
    }

    public void setChest(Chest chest) {
        this.chest = chest;
    }

    public void showOrHide() {
        setShow(!this.show);
        if (isVisible()) {
            initInventoryItems();
            initChestItems();
        }
    }

    @Override
    public boolean pointerAdded(Touch.Pointer p) {
        if (this.touch != null || !this.bounds.contains(p.x, p.y) || !this.show) {
            return false;
        }
        this.touch = p;
        this.exitTap.pointerAdded(this.touch);
        if (this.chestScissorBound.contains(p.x, p.y)) {
            this.needToScrollChest = true;
            this.prevChestScrollY = this.touch.y;
        }
        if (this.inventoryScissorBound.contains(p.x, p.y)) {
            this.needToScrollInv = true;
            this.prevInvScrollY = this.touch.y;
        }
        for (int i = 0; i < this.chestTapItems.size(); i++) {
            this.chestTapItems.get(i).pointerAdded(this.touch);
        }
        for (int i2 = 0; i2 < this.inventoryTapItems.size(); i2++) {
            this.inventoryTapItems.get(i2).pointerAdded(this.touch);
        }
        return true;
    }

    @Override
    public void pointerRemoved(Touch.Pointer p) {
        if (this.touch == p && this.touch != null) {
            this.exitTap.pointerRemoved(this.touch);
            for (int i = 0; i < this.chestTapItems.size(); i++) {
                this.chestTapItems.get(i).translateYOffset(this.touchChestDelta);
                this.chestTapItems.get(i).pointerRemoved(this.touch);
            }
            for (int i2 = 0; i2 < this.inventoryTapItems.size(); i2++) {
                this.inventoryTapItems.get(i2).translateYOffset(this.touchInvDelta);
                this.inventoryTapItems.get(i2).pointerRemoved(this.touch);
            }
            this.touchChestDelta = 0.0f;
            this.touchInvDelta = 0.0f;
            this.needToScrollChest = false;
            this.needToScrollInv = false;
            this.touch = null;
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
