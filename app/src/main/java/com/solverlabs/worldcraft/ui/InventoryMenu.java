package com.solverlabs.worldcraft.ui;

import android.opengl.GLES10;

import com.solverlabs.droid.rugl.Game;
import com.solverlabs.droid.rugl.geom.ColouredShape;
import com.solverlabs.droid.rugl.geom.DoorBlock;
import com.solverlabs.droid.rugl.geom.Shape;
import com.solverlabs.droid.rugl.geom.ShapeUtil;
import com.solverlabs.droid.rugl.gl.StackedRenderer;
import com.solverlabs.droid.rugl.gl.State;
import com.solverlabs.droid.rugl.input.Touch;
import com.solverlabs.droid.rugl.util.Colour;
import com.solverlabs.droid.rugl.util.geom.BoundingRectangle;
import com.solverlabs.worldcraft.GameMode;
import com.solverlabs.worldcraft.Player;
import com.solverlabs.worldcraft.factories.BlockFactory;
import com.solverlabs.worldcraft.inventory.InventoryItem;
import com.solverlabs.worldcraft.inventory.InventoryTapItem;
import com.solverlabs.worldcraft.math.MathUtils;

import java.util.ArrayList;


public class InventoryMenu implements Touch.TouchListener {
    private final Player player;
    private final ArrayList<InventoryTapItem> renderItemsList = new ArrayList<>();
    public BoundingRectangle bounds = new BoundingRectangle(55.0f, 100.0f, 675.0f, 320.0f);
    public int boundsColour = Colour.packFloat(1.0f, 1.0f, 1.0f, 1.0f);
    public int arrowColour = Colour.packFloat(1.0f, 1.0f, 1.0f, 1.0f);
    public int innerColour = Colour.packInt(148, 134, 123, 255);
    public int sliderColour = Colour.grey;
    private boolean autoScrollDown;
    private ColouredShape boundsShape;
    private ColouredShape downScrollArrowShape;
    private ColouredShape innerShape;
    private float prevYpoint;
    private ColouredShape scrollSliderShape;
    private float sliderSize;
    private float sliderY;
    private Touch.Pointer touch;
    private float touchDelta;
    private ColouredShape upScrollArrowShape;
    private boolean show = false;
    private boolean autoScrollUp = false;
    private boolean itemInited = false;

    public InventoryMenu(Player player) {
        this.player = player;
    }

    private void initItems() {
        float yOffset = 5.0f;
        int k = 0;
        if (GameMode.isCreativeMode() && !this.itemInited) {
            int id = 0;
            BlockFactory.Block[] arr$ = BlockFactory.Block.values();
            for (BlockFactory.Block block : arr$) {
                if (!DoorBlock.isOpenedDoor(block.id) && block != BlockFactory.Block.Ladder) {
                    float x = this.bounds.x.getMin() + (84.0f / 2.0f) + (k * 84.0f);
                    if (x > this.bounds.x.getMax()) {
                        k = 0;
                        yOffset += 84.0f;
                        x = this.bounds.x.getMin() + (84.0f / 2.0f) + (0 * 84.0f);
                    }
                    float y = this.bounds.y.getMin() + 40.0f + yOffset;
                    this.renderItemsList.add(new InventoryTapItem(this.player, new InventoryItem(block, id), x, y));
                    k++;
                    id++;
                }
            }
            this.itemInited = true;
        }
        if (GameMode.isSurvivalMode()) {
            this.renderItemsList.clear();
            ArrayList<InventoryItem> allItems = this.player.inventory.getAllInventoryItems();
            for (int i = 0; i < allItems.size(); i++) {
                InventoryItem item = allItems.get(i);
                if (!item.isEmpty()) {
                    float x2 = this.bounds.x.getMin() + (84.0f / 2.0f) + (k * 84.0f);
                    if (x2 > this.bounds.x.getMax()) {
                        k = 0;
                        yOffset += 80.0f;
                        x2 = this.bounds.x.getMin() + (84.0f / 2.0f) + (0 * 84.0f);
                    }
                    float y2 = this.bounds.y.getMin() + 40.0f + yOffset;
                    InventoryTapItem barItem = new InventoryTapItem(this.player, item);
                    barItem.setPosition(x2, y2);
                    this.renderItemsList.add(barItem);
                    k++;
                } else {
                    this.player.inventory.remove(item);
                }
            }
        }
    }

    public void advance() {
        if (this.touch != null && !this.autoScrollUp && !this.autoScrollDown && GameMode.isCreativeMode()) {
            this.touchDelta = this.touch.y - this.prevYpoint;
        }
        for (int i = 0; i < this.renderItemsList.size(); i++) {
            if (this.renderItemsList.get(i).getInventoryItem().isEmpty()) {
                initItems();
            }
        }
    }

    public void draw(StackedRenderer sr) {
        if (this.show) {
            if (this.renderItemsList.size() != 0) {
                normalizeScroll();
                if (GameMode.isCreativeMode()) {
                    drawScrollSlider(sr);
                    drawScrollArrows(sr);
                }
            }
            drawInnerShape(sr);
            sr.render();
            GLES10.glEnable(3089);
            float deltaX = Game.mScreenWidth / Game.mGameWidth;
            float deltaY = Game.mScreenHeight / Game.mGameHeight;
            GLES10.glScissor((int) (40.0f * deltaX), (int) (100.0f * deltaY), (int) (710.0f * deltaX), (int) (320.0f * deltaY));
            for (int i = 0; i < this.renderItemsList.size(); i++) {
                this.renderItemsList.get(i).draw(sr, this.touchDelta);
            }
            sr.render();
            GLES10.glDisable(3089);
            drawBoundShape(sr);
        }
    }

    private void normalizeScroll() {
        InventoryTapItem lastItem = this.renderItemsList.get(this.renderItemsList.size() - 1);
        float topPoint = lastItem.bounds.y.getMax();
        float bottomPoint = this.renderItemsList.get(0).bounds.y.getMin();
        if (topPoint < this.bounds.y.getMax() + 10.0f) {
            this.autoScrollUp = true;
        }
        if (bottomPoint > this.bounds.y.getMin() + 40.0f) {
            this.autoScrollDown = true;
        }
        if (this.autoScrollUp) {
            if (this.touch != null) {
                for (InventoryTapItem item : getBlockBarItems()) {
                    item.translateYOffset(this.touchDelta);
                }
                this.touchDelta = 0.0f;
            } else if (topPoint < this.bounds.y.getMax() + 20.0f) {
                this.touchDelta += 10.0f;
            } else {
                this.autoScrollUp = false;
                this.touchDelta -= Math.abs(topPoint - (this.bounds.y.getMax() + 20.0f));
                for (InventoryTapItem item2 : getBlockBarItems()) {
                    item2.translateYOffset(this.touchDelta);
                }
                this.touchDelta = 0.0f;
            }
        }
        if (this.autoScrollDown) {
            if (this.touch != null) {
                for (InventoryTapItem item3 : getBlockBarItems()) {
                    item3.translateYOffset(this.touchDelta);
                }
                this.touchDelta = 0.0f;
            } else if (bottomPoint > this.bounds.y.getMin() + 40.0f) {
                this.touchDelta -= 10.0f;
            } else {
                this.autoScrollDown = false;
                this.touchDelta += Math.abs(bottomPoint - (this.bounds.y.getMin() + 40.0f));
                for (InventoryTapItem item4 : getBlockBarItems()) {
                    item4.translateYOffset(this.touchDelta);
                }
                this.touchDelta = 0.0f;
            }
        }
    }

    private void drawBoundShape(StackedRenderer sr) {
        if (this.boundsShape == null) {
            Shape bs = ShapeUtil.innerQuad(this.bounds.x.getMin() - 5.0f, this.bounds.y.getMin() - 5.0f, this.bounds.x.getMax() + 5.0f, this.bounds.y.getMax() + 5.0f, 5.0f, 0.0f);
            this.boundsShape = new ColouredShape(bs, this.boundsColour, (State) null);
        }
        this.boundsShape.render(sr);
    }

    private void drawInnerShape(StackedRenderer sr) {
        if (this.innerShape == null) {
            Shape is = ShapeUtil.innerQuad(this.bounds.x.getMin(), this.bounds.y.getMin(), this.bounds.x.getMax(), this.bounds.y.getMax(), this.bounds.y.getSpan(), 0.0f);
            this.innerShape = new ColouredShape(is, this.innerColour, (State) null);
        }
        this.innerShape.render(sr);
    }

    private void drawScrollArrows(StackedRenderer sr) {
        if (this.downScrollArrowShape == null) {
            Shape us = ShapeUtil.triangle(750.0f, 100.0f, 750.0f, 140.0f, 770.0f, 140.0f);
            this.downScrollArrowShape = new ColouredShape(us, this.arrowColour, (State) null);
        }
        if (this.upScrollArrowShape == null) {
            Shape us2 = ShapeUtil.triangle(750.0f, 420.0f, 750.0f, 380.0f, 770.0f, 380.0f);
            this.upScrollArrowShape = new ColouredShape(us2, this.arrowColour, (State) null);
        }
        this.upScrollArrowShape.render(sr);
        this.downScrollArrowShape.render(sr);
    }

    private void drawScrollSlider(StackedRenderer sr) {
        float size = 0.0f;
        if (this.scrollSliderShape == null) {
            int rowCount = MathUtils.ceil(this.player.inventory.getSize() / 8);
            float size2 = (1.0f * rowCount) / 4.0f;
            size = 240.0f / size2;
            this.sliderSize = size;
            Shape ss = ShapeUtil.filledQuad(750.0f, 140.0f, 765.0f, 140.0f + size, 0.0f);
            this.scrollSliderShape = new ColouredShape(ss, this.sliderColour, (State) null);
        }
        float delta = (this.bounds.y.getMin() - getBlockBarItems().get(0).bounds.y.getMin()) + 40.0f;
        if (this.touchDelta != 0.0f && !this.autoScrollDown && !this.autoScrollUp) {
            this.sliderY = ((240.0f - size) * delta) / 540.0f;
            float y = 140.0f + this.sliderY;
            if (y < 140.0f) {
                y = 140.0f;
            }
            if (y > 380.0f - this.sliderSize) {
                y = 380.0f - this.sliderSize;
            }
            this.scrollSliderShape.set(650.0f, y, 0.0f);
        }
        this.scrollSliderShape.render(sr);
    }

    @Override
    public boolean pointerAdded(Touch.Pointer p) {
        if (this.touch != null || !this.bounds.contains(p.x, p.y) || !this.show) {
            return false;
        }
        this.touch = p;
        for (InventoryTapItem item : getBlockBarItems()) {
            item.pointerAdded(this.touch);
        }
        this.prevYpoint = this.touch.y;
        return true;
    }

    @Override
    public void pointerRemoved(Touch.Pointer p) {
        if (this.touch == p && this.touch != null) {
            for (InventoryTapItem item : getBlockBarItems()) {
                item.pointerRemoved(this.touch);
                item.translateYOffset(this.touchDelta);
            }
            this.touch = null;
            this.touchDelta = 0.0f;
        }
    }

    @Override
    public void reset() {
    }

    public void show() {
        setShow(!this.show);
    }

    public boolean isShow() {
        return this.show;
    }

    public void setShow(boolean show) {
        this.show = show;
        if (show) {
            initItems();
        }
        if (!show && this.scrollSliderShape != null) {
            this.scrollSliderShape.set(650.0f, 140.0f, 0.0f);
        }
        for (InventoryTapItem item : this.renderItemsList) {
            item.setShown(show);
        }
    }

    public ArrayList<InventoryTapItem> getBlockBarItems() {
        return this.renderItemsList;
    }
}
