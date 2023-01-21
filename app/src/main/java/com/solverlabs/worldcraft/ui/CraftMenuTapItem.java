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
        this.item = ItemFactory.Item.getItemByID(craftItem.getID());
        this.texShape = this.item.itemShape.clone();
        this.inventory = inventory;
        this.name = craftItem.name();
        this.x = x;
        this.y = y;
        this.bounds = new BoundingRectangle(x, y, 300.0f, 70.0f);
    }

    public CraftFactory.CraftItem getCraftItem() {
        return this.craftItem;
    }

    public void draw(StackedRenderer sr, float deltaY) {
        this.bounds.y.set(this.y + deltaY + this.yOffset);
        if (this.isSelected) {
            drawInnerShape(sr, deltaY);
        }
        drawBounds(sr, deltaY);
        sr.render();
        sr.pushMatrix();
        sr.translate(this.x + 10.0f, ((this.y + HALF_HEIGHT) - 10.0f) + deltaY + this.yOffset, 0.0f);
        drawName(sr);
        sr.translate(220.0f, -15.0f, 0.0f);
        drawCount(sr);
        sr.translate(0.0f, 25.0f, 0.0f);
        sr.scale(50.0f, 50.0f, 1.0f);
        this.texShape.render(sr);
        sr.popMatrix();
        sr.render();
    }

    private void drawCount(StackedRenderer sr) {
        if (this.countTextShape == null) {
            this.countTextShape = new Readout(GUI.getFont(), Colour.white, " ", false, 2, 0);
        }
        this.countTextShape.updateValue(this.count);
        if (this.canBeCrafted) {
            this.countTextShape.colours = ShapeUtil.expand(Colour.white, this.countTextShape.vertexCount());
        } else {
            this.countTextShape.colours = ShapeUtil.expand(Colour.darkgrey, this.countTextShape.vertexCount());
        }
        this.countTextShape.render(sr);
    }

    private void drawName(StackedRenderer sr) {
        if (this.nameShape == null) {
            this.nameShape = GUI.getFont().buildTextShape(this.name, Colour.white);
        }
        if (this.canBeCrafted) {
            this.nameShape.colours = ShapeUtil.expand(Colour.white, this.nameShape.vertexCount());
        } else {
            this.nameShape.colours = ShapeUtil.expand(Colour.darkgrey, this.nameShape.vertexCount());
        }
        this.nameShape.render(sr);
    }

    private void drawInnerShape(StackedRenderer sr, float deltaY) {
        if (this.innerShape == null) {
            Shape is = ShapeUtil.innerQuad(this.bounds.x.getMin() + 2.0f, this.bounds.y.getMin(), this.bounds.x.getMax() - 2.0f, this.bounds.y.getMax(), this.bounds.y.getSpan(), 0.0f);
            this.innerShape = new ColouredShape(is, this.innerColour, (State) null);
        }
        this.innerShape.set(this.x, this.y + deltaY + this.yOffset, 0.0f);
        this.innerShape.render(sr);
    }

    private void drawBounds(StackedRenderer sr, float deltaY) {
        if (this.upBound == null) {
            Shape s = ShapeUtil.line(3.0f, 0.0f, 0.0f, 300.0f, 0.0f);
            this.upBound = new ColouredShape(s, Colour.white, (State) null);
            this.bottomBound = new ColouredShape(s, Colour.darkgrey, (State) null);
        }
        sr.pushMatrix();
        sr.translate(this.x, this.y + this.yOffset + deltaY, 0.0f);
        this.bottomBound.render(sr);
        sr.translate(0.0f, 67.0f, 0.0f);
        this.upBound.render(sr);
        sr.popMatrix();
    }

    private void canBeCrafted() {
        this.canBeCrafted = false;
        int temp = 0;
        for (int i = 0; i < this.craftItem.getMaterial().length; i++) {
            if (this.inventory.getItemTotalCount(this.craftItem.getMaterial()[i][0]) >= this.craftItem.getMaterial()[i][1]) {
                temp++;
            }
        }
        this.canBeCrafted = temp == this.craftItem.getMaterial().length;
    }

    public void checkCount() {
        this.count = this.inventory.getItemTotalCount(this.craftItem.getID());
    }

    public boolean pointerAdded(Touch.Pointer p) {
        if (this.touch == null && this.bounds.contains(p.x, p.y)) {
            isResetFocus = true;
            this.touch = p;
            this.downTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public void pointerRemoved(Touch.Pointer p) {
        if (this.touch == p && this.touch != null) {
            long delta = System.currentTimeMillis() - this.downTime;
            if (((float) delta) < 300.0f) {
                onTap();
            }
            this.touch = null;
        }
    }

    public void reset() {
    }

    private void onTap() {
        this.isSelected = true;
    }

    public void setShown(boolean isShown) {
        if (isShown) {
            setYOffset(0.0f);
        }
    }

    public float getYOffset() {
        return this.yOffset;
    }

    public void setYOffset(float f) {
        this.yOffset = f;
    }

    public float getY() {
        return this.yOffset + this.y;
    }

    public void translateYOffset(float yOffset) {
        this.yOffset += yOffset;
    }

    public void checkItem() {
        canBeCrafted();
        checkCount();
    }

    public ItemFactory.Item getItem() {
        return this.item;
    }
}
