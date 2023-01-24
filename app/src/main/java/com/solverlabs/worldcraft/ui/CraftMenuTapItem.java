package com.solverlabs.worldcraft.ui;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.geom.ColouredShape;
import com.solverlabs.droid.rugl.geom.Shape;
import com.solverlabs.droid.rugl.geom.ShapeUtil;
import com.solverlabs.droid.rugl.geom.TexturedShape;
import com.solverlabs.droid.rugl.gl.StackedRenderer;
import com.solverlabs.droid.rugl.gl.State;
import com.solverlabs.droid.rugl.input.Touch;
import com.solverlabs.droid.rugl.text.Readout;
import com.solverlabs.droid.rugl.text.TextShape;
import com.solverlabs.droid.rugl.util.Colour;
import com.solverlabs.droid.rugl.util.geom.BoundingRectangle;
import com.solverlabs.worldcraft.chunk.tile_entity.Inventory;
import com.solverlabs.worldcraft.factories.CraftFactory;
import com.solverlabs.worldcraft.factories.ItemFactory;

import org.apache.commons.compress.archivers.cpio.CpioConstants;

public class CraftMenuTapItem {
    public static final float HEIGHT = 70.0f;
    public static final float WIDTH = 300.0f;
    private static final float HALF_HEIGHT = 35.0f;
    private static final float HALF_WIDTH = 150.0f;
    private static final float TAP_TIME = 0.3f;
    public static boolean isResetFocus = false;
    private final CraftFactory.CraftItem craftItem;
    private final Inventory inventory;
    private final ItemFactory.Item item;
    private final String name;
    private final TexturedShape texShape;
    private final float x;
    private final float y;
    public BoundingRectangle bounds;
    public boolean canBeCrafted;
    public int innerColour = Colour.packInt(241, 241, 249, CpioConstants.C_IWUSR);
    public boolean isSelected;
    private ColouredShape bottomBound;
    private int count;
    private Readout countTextShape;
    private long downTime = -1;
    private ColouredShape innerShape;
    private TextShape nameShape;
    private Touch.Pointer touch;
    private ColouredShape upBound;
    private float yOffset;

    public CraftMenuTapItem(@NonNull CraftFactory.CraftItem craftItem, Inventory inventory, float x, float y) {
        this.craftItem = craftItem;
        item = ItemFactory.Item.getItemByID(craftItem.getID());
        texShape = item.itemShape.clone();
        this.inventory = inventory;
        this.name = craftItem.name();
        this.x = x;
        this.y = y;
        bounds = new BoundingRectangle(x, y, 300.0f, 70.0f);
    }

    public CraftFactory.CraftItem getCraftItem() {
        return craftItem;
    }

    public void draw(StackedRenderer sr, float deltaY) {
        bounds.y.set(y + deltaY + yOffset);
        if (isSelected) {
            drawInnerShape(sr, deltaY);
        }
        drawBounds(sr, deltaY);
        sr.render();
        sr.pushMatrix();
        sr.translate(x + 10.0f, ((y + HALF_HEIGHT) - 10.0f) + deltaY + yOffset, 0.0f);
        drawName(sr);
        sr.translate(220.0f, -15.0f, 0.0f);
        drawCount(sr);
        sr.translate(0.0f, 25.0f, 0.0f);
        sr.scale(50.0f, 50.0f, 1.0f);
        texShape.render(sr);
        sr.popMatrix();
        sr.render();
    }

    private void drawCount(StackedRenderer sr) {
        if (countTextShape == null) {
            countTextShape = new Readout(GUI.getFont(), Colour.white, " ", false, 2, 0);
        }
        countTextShape.updateValue(count);
        if (canBeCrafted) {
            countTextShape.colours = ShapeUtil.expand(Colour.white, countTextShape.vertexCount());
        } else {
            countTextShape.colours = ShapeUtil.expand(Colour.darkgrey, countTextShape.vertexCount());
        }
        countTextShape.render(sr);
    }

    private void drawName(StackedRenderer sr) {
        if (nameShape == null) {
            nameShape = GUI.getFont().buildTextShape(name, Colour.white);
        }
        if (canBeCrafted) {
            nameShape.colours = ShapeUtil.expand(Colour.white, nameShape.vertexCount());
        } else {
            nameShape.colours = ShapeUtil.expand(Colour.darkgrey, nameShape.vertexCount());
        }
        nameShape.render(sr);
    }

    private void drawInnerShape(StackedRenderer sr, float deltaY) {
        if (innerShape == null) {
            Shape is = ShapeUtil.innerQuad(bounds.x.getMin() + 2.0f, bounds.y.getMin(), bounds.x.getMax() - 2.0f, bounds.y.getMax(), bounds.y.getSpan(), 0.0f);
            innerShape = new ColouredShape(is, innerColour, (State) null);
        }
        innerShape.set(x, y + deltaY + yOffset, 0.0f);
        innerShape.render(sr);
    }

    private void drawBounds(StackedRenderer sr, float deltaY) {
        if (upBound == null) {
            Shape s = ShapeUtil.line(3.0f, 0.0f, 0.0f, 300.0f, 0.0f);
            upBound = new ColouredShape(s, Colour.white, (State) null);
            bottomBound = new ColouredShape(s, Colour.darkgrey, (State) null);
        }
        sr.pushMatrix();
        sr.translate(x, y + yOffset + deltaY, 0.0f);
        bottomBound.render(sr);
        sr.translate(0.0f, 67.0f, 0.0f);
        upBound.render(sr);
        sr.popMatrix();
    }

    private void canBeCrafted() {
        canBeCrafted = false;
        int temp = 0;
        for (int i = 0; i < craftItem.getMaterial().length; i++) {
            if (inventory.getItemTotalCount(craftItem.getMaterial()[i][0]) >= craftItem.getMaterial()[i][1]) {
                temp++;
            }
        }
        canBeCrafted = temp == craftItem.getMaterial().length;
    }

    public void checkCount() {
        count = inventory.getItemTotalCount(craftItem.getID());
    }

    public boolean pointerAdded(Touch.Pointer p) {
        if (touch == null && bounds.contains(p.x, p.y)) {
            isResetFocus = true;
            touch = p;
            downTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public void pointerRemoved(Touch.Pointer p) {
        if (touch == p && touch != null) {
            long delta = System.currentTimeMillis() - downTime;
            if (((float) delta) < 300.0f) {
                onTap();
            }
            touch = null;
        }
    }

    public void reset() {
    }

    private void onTap() {
        isSelected = true;
    }

    public void setShown(boolean isShown) {
        if (isShown) {
            setYOffset(0.0f);
        }
    }

    public float getYOffset() {
        return yOffset;
    }

    public void setYOffset(float f) {
        yOffset = f;
    }

    public float getY() {
        return yOffset + y;
    }

    public void translateYOffset(float yOffset) {
        this.yOffset += yOffset;
    }

    public void checkItem() {
        canBeCrafted();
        checkCount();
    }

    public ItemFactory.Item getItem() {
        return item;
    }
}
