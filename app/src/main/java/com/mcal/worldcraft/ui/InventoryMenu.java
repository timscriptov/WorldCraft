package com.mcal.worldcraft.ui;

import android.opengl.GLES10;

import com.mcal.droid.rugl.Game;
import com.mcal.droid.rugl.geom.ColouredShape;
import com.mcal.droid.rugl.geom.DoorBlock;
import com.mcal.droid.rugl.geom.Shape;
import com.mcal.droid.rugl.geom.ShapeUtil;
import com.mcal.droid.rugl.gl.StackedRenderer;
import com.mcal.droid.rugl.input.Touch;
import com.mcal.droid.rugl.util.Colour;
import com.mcal.droid.rugl.util.geom.BoundingRectangle;
import com.mcal.worldcraft.GameMode;
import com.mcal.worldcraft.Player;
import com.mcal.worldcraft.factories.BlockFactory;
import com.mcal.worldcraft.inventory.InventoryItem;
import com.mcal.worldcraft.inventory.InventoryTapItem;
import com.mcal.worldcraft.math.MathUtils;

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
        if (GameMode.isCreativeMode() && !itemInited) {
            int id = 0;
            for (BlockFactory.Block block : BlockFactory.Block.values()) {
                if (!DoorBlock.isOpenedDoor(block.id) && block != BlockFactory.Block.Ladder) {
                    float x = bounds.x.getMin() + (84.0f / 2.0f) + (k * 84.0f);
                    if (x > bounds.x.getMax()) {
                        k = 0;
                        yOffset += 84.0f;
                        x = bounds.x.getMin() + (84.0f / 2.0f) + (0 * 84.0f);
                    }
                    float y = bounds.y.getMin() + 40.0f + yOffset;
                    renderItemsList.add(new InventoryTapItem(player, new InventoryItem(block, id), x, y));
                    k++;
                    id++;
                }
            }
            itemInited = true;
        }
        if (GameMode.isSurvivalMode()) {
            renderItemsList.clear();
            ArrayList<InventoryItem> allItems = player.inventory.getAllInventoryItems();
            for (int i = 0; i < allItems.size(); i++) {
                InventoryItem item = allItems.get(i);
                if (!item.isEmpty()) {
                    float x2 = bounds.x.getMin() + (84.0f / 2.0f) + (k * 84.0f);
                    if (x2 > bounds.x.getMax()) {
                        k = 0;
                        yOffset += 80.0f;
                        x2 = bounds.x.getMin() + (84.0f / 2.0f) + (0 * 84.0f);
                    }
                    float y2 = bounds.y.getMin() + 40.0f + yOffset;
                    InventoryTapItem barItem = new InventoryTapItem(player, item);
                    barItem.setPosition(x2, y2);
                    renderItemsList.add(barItem);
                    k++;
                } else {
                    player.inventory.remove(item);
                }
            }
        }
    }

    public void advance() {
        if (touch != null && !autoScrollUp && !autoScrollDown && GameMode.isCreativeMode()) {
            touchDelta = touch.y - prevYpoint;
        }
        for (int i = 0; i < renderItemsList.size(); i++) {
            if (renderItemsList.get(i).getInventoryItem().isEmpty()) {
                initItems();
            }
        }
    }

    public void draw(StackedRenderer sr) {
        if (show) {
            if (renderItemsList.size() != 0) {
                normalizeScroll();
                if (GameMode.isCreativeMode()) {
                    drawScrollSlider(sr);
                    drawScrollArrows(sr);
                }
            }
            drawInnerShape(sr);
            sr.render();
            GLES10.glEnable(3089);
            float deltaX = Game.screenWidth / Game.gameWidth;
            float deltaY = Game.screenHeight / Game.gameHeight;
            GLES10.glScissor((int) (40.0f * deltaX), (int) (100.0f * deltaY), (int) (710.0f * deltaX), (int) (320.0f * deltaY));
            for (int i = 0; i < renderItemsList.size(); i++) {
                renderItemsList.get(i).draw(sr, touchDelta);
            }
            sr.render();
            GLES10.glDisable(3089);
            drawBoundShape(sr);
        }
    }

    private void normalizeScroll() {
        InventoryTapItem lastItem = renderItemsList.get(renderItemsList.size() - 1);
        float topPoint = lastItem.bounds.y.getMax();
        float bottomPoint = renderItemsList.get(0).bounds.y.getMin();
        if (topPoint < bounds.y.getMax() + 10.0f) {
            autoScrollUp = true;
        }
        if (bottomPoint > bounds.y.getMin() + 40.0f) {
            autoScrollDown = true;
        }
        if (autoScrollUp) {
            if (touch != null) {
                for (InventoryTapItem item : getBlockBarItems()) {
                    item.translateYOffset(touchDelta);
                }
                touchDelta = 0.0f;
            } else if (topPoint < bounds.y.getMax() + 20.0f) {
                touchDelta += 10.0f;
            } else {
                autoScrollUp = false;
                touchDelta -= Math.abs(topPoint - (bounds.y.getMax() + 20.0f));
                for (InventoryTapItem item2 : getBlockBarItems()) {
                    item2.translateYOffset(touchDelta);
                }
                touchDelta = 0.0f;
            }
        }
        if (autoScrollDown) {
            if (touch != null) {
                for (InventoryTapItem item3 : getBlockBarItems()) {
                    item3.translateYOffset(touchDelta);
                }
                touchDelta = 0.0f;
            } else if (bottomPoint > bounds.y.getMin() + 40.0f) {
                touchDelta -= 10.0f;
            } else {
                autoScrollDown = false;
                touchDelta += Math.abs(bottomPoint - (bounds.y.getMin() + 40.0f));
                for (InventoryTapItem item4 : getBlockBarItems()) {
                    item4.translateYOffset(touchDelta);
                }
                touchDelta = 0.0f;
            }
        }
    }

    private void drawBoundShape(StackedRenderer sr) {
        if (boundsShape == null) {
            Shape bs = ShapeUtil.innerQuad(bounds.x.getMin() - 5.0f, bounds.y.getMin() - 5.0f, bounds.x.getMax() + 5.0f, bounds.y.getMax() + 5.0f, 5.0f, 0.0f);
            boundsShape = new ColouredShape(bs, boundsColour, null);
        }
        boundsShape.render(sr);
    }

    private void drawInnerShape(StackedRenderer sr) {
        if (innerShape == null) {
            Shape is = ShapeUtil.innerQuad(bounds.x.getMin(), bounds.y.getMin(), bounds.x.getMax(), bounds.y.getMax(), bounds.y.getSpan(), 0.0f);
            innerShape = new ColouredShape(is, innerColour, null);
        }
        innerShape.render(sr);
    }

    private void drawScrollArrows(StackedRenderer sr) {
        if (downScrollArrowShape == null) {
            Shape us = ShapeUtil.triangle(750.0f, 100.0f, 750.0f, 140.0f, 770.0f, 140.0f);
            downScrollArrowShape = new ColouredShape(us, arrowColour, null);
        }
        if (upScrollArrowShape == null) {
            Shape us2 = ShapeUtil.triangle(750.0f, 420.0f, 750.0f, 380.0f, 770.0f, 380.0f);
            upScrollArrowShape = new ColouredShape(us2, arrowColour, null);
        }
        upScrollArrowShape.render(sr);
        downScrollArrowShape.render(sr);
    }

    private void drawScrollSlider(StackedRenderer sr) {
        float size = 0.0f;
        if (scrollSliderShape == null) {
            int rowCount = MathUtils.ceil(player.inventory.getSize() / 8);
            float size2 = (1.0f * rowCount) / 4.0f;
            size = 240.0f / size2;
            sliderSize = size;
            Shape ss = ShapeUtil.filledQuad(750.0f, 140.0f, 765.0f, 140.0f + size, 0.0f);
            scrollSliderShape = new ColouredShape(ss, sliderColour, null);
        }
        float delta = (bounds.y.getMin() - getBlockBarItems().get(0).bounds.y.getMin()) + 40.0f;
        if (touchDelta != 0.0f && !autoScrollDown && !autoScrollUp) {
            float sliderY = ((240.0f - size) * delta) / 540.0f;
            float y = 140.0f + sliderY;
            if (y < 140.0f) {
                y = 140.0f;
            }
            if (y > 380.0f - sliderSize) {
                y = 380.0f - sliderSize;
            }
            scrollSliderShape.set(650.0f, y, 0.0f);
        }
        scrollSliderShape.render(sr);
    }

    @Override
    public boolean pointerAdded(Touch.Pointer p) {
        if (touch == null && bounds.contains(p.x, p.y) && show) {
            touch = p;
            for (InventoryTapItem item : getBlockBarItems()) {
                item.pointerAdded(touch);
            }
            prevYpoint = touch.y;
            return true;
        }
        return false;
    }

    @Override
    public void pointerRemoved(Touch.Pointer p) {
        if (touch == p && touch != null) {
            for (InventoryTapItem item : getBlockBarItems()) {
                item.pointerRemoved(touch);
                item.translateYOffset(touchDelta);
            }
            touch = null;
            touchDelta = 0.0f;
        }
    }

    @Override
    public void reset() {
    }

    public void show() {
        setShow(!show);
    }

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
        if (show) {
            initItems();
        }
        if (!show && scrollSliderShape != null) {
            scrollSliderShape.set(650.0f, 140.0f, 0.0f);
        }
        for (InventoryTapItem item : renderItemsList) {
            item.setShown(show);
        }
    }

    public ArrayList<InventoryTapItem> getBlockBarItems() {
        return renderItemsList;
    }
}
