package com.solverlabs.worldcraft.inventory;

import com.solverlabs.droid.rugl.geom.ColouredShape;
import com.solverlabs.droid.rugl.geom.Shape;
import com.solverlabs.droid.rugl.geom.ShapeUtil;
import com.solverlabs.droid.rugl.gl.StackedRenderer;
import com.solverlabs.droid.rugl.gl.State;
import com.solverlabs.droid.rugl.input.Touch;
import com.solverlabs.droid.rugl.text.Readout;
import com.solverlabs.droid.rugl.util.Colour;
import com.solverlabs.droid.rugl.util.geom.BoundingRectangle;
import com.solverlabs.worldcraft.GameMode;
import com.solverlabs.worldcraft.Player;
import com.solverlabs.worldcraft.ui.GUI;

import org.apache.commons.compress.archivers.cpio.CpioConstants;

public class InventoryTapItem implements Touch.TouchListener {
    public static final float HALF_WIDTH = 40.0f;
    public static final float HEIGHT = 80.0f;
    public static final float WIDTH = 80.0f;
    private static final long DROP_TIME = 2000;
    private static final float TAP_TIME = 0.6f;
    private final int durabilityBrokeColor;
    private final int durabilityFullColor;
    private final int durabilityHalfColor;
    private final float longPressTime;
    private final Player player;
    public BoundingRectangle bounds;
    public boolean isDrawBounds;
    protected InventoryItem item;
    private ColouredShape buttonBottomBound;
    private ColouredShape buttonLeftBound;
    private ColouredShape buttonRightBound;
    private ColouredShape buttonUpBound;
    private Readout countTextShape;
    private long downTime;
    private float dropProgresRatio;
    private ColouredShape dropProgresShape;
    private ColouredShape durabilityShape;
    private ColouredShape innerShape;
    private long longPressPeriod;
    private long longPressStartTime;
    private boolean longPressed;
    private Touch.Pointer touch;
    private float x;
    private float y;
    private float yOffset;

    public InventoryTapItem(InventoryItem item) {
        this.downTime = -1L;
        this.durabilityFullColor = Colour.green;
        this.durabilityHalfColor = Colour.yellow;
        this.durabilityBrokeColor = Colour.red;
        this.isDrawBounds = true;
        this.longPressed = false;
        this.longPressTime = 0.5f;
        this.bounds = new BoundingRectangle(0.0f, 0.0f, 80.0f, 80.0f);
        this.item = item;
        this.player = null;
    }

    public InventoryTapItem(Player player, InventoryItem item) {
        this.downTime = -1L;
        this.durabilityFullColor = Colour.green;
        this.durabilityHalfColor = Colour.yellow;
        this.durabilityBrokeColor = Colour.red;
        this.isDrawBounds = true;
        this.longPressed = false;
        this.longPressTime = 0.5f;
        this.bounds = new BoundingRectangle(0.0f, 0.0f, 80.0f, 80.0f);
        this.item = item;
        this.player = player;
    }

    public InventoryTapItem(Player player, InventoryItem item, float x, float y) {
        this.downTime = -1L;
        this.durabilityFullColor = Colour.green;
        this.durabilityHalfColor = Colour.yellow;
        this.durabilityBrokeColor = Colour.red;
        this.isDrawBounds = true;
        this.longPressed = false;
        this.longPressTime = 0.5f;
        this.bounds = new BoundingRectangle(x, y, 80.0f, 80.0f);
        this.item = item;
        this.x = x;
        this.y = y;
        this.player = player;
    }

    public InventoryItem getInventoryItem() {
        return this.item;
    }

    public void draw(StackedRenderer sr, float deltaY) {
        if (this.touch != null) {
            drawInnerBound(sr, deltaY);
        }
        if (this.longPressed) {
            drawDropProgress(sr);
        }
        if (this.isDrawBounds) {
            drawBounds(sr, deltaY);
        }
        this.bounds.y.set(this.y + deltaY + this.yOffset);
        sr.pushMatrix();
        sr.translate(this.x, this.y + deltaY + this.yOffset, 0.0f);
        sr.scale(60.0f, 60.0f, 1.0f);
        this.item.getItemShape().render(sr);
        sr.popMatrix();
        sr.render();
        if (GameMode.isSurvivalMode()) {
            if (this.item.getItem().isTool()) {
                drawItemDurability(sr, deltaY);
            } else {
                drawBlockCount(sr, deltaY);
            }
        }
    }

    public void advance() {
        if (this.touch != null) {
            long delta = System.currentTimeMillis() - this.downTime;
            if (((float) delta) > this.longPressTime * 1000.0f && !this.longPressed) {
                this.longPressPeriod = System.currentTimeMillis();
                this.longPressStartTime = System.currentTimeMillis();
                this.longPressed = true;
            }
            if (this.longPressed && System.currentTimeMillis() - this.longPressPeriod >= 50) {
                onLongPress();
                this.longPressPeriod = System.currentTimeMillis();
            }
        }
    }

    private void drawInnerBound(StackedRenderer sr, float deltaY) {
        if (this.innerShape == null) {
            Shape is = ShapeUtil.innerQuad(this.x - 40.0f, this.y - 40.0f, this.x + 40.0f, this.y + 40.0f, 80.0f, 0.0f);
            this.innerShape = new ColouredShape(is, Colour.withAlphai(Colour.white, (int) CpioConstants.C_IWUSR), (State) null);
        }
        sr.pushMatrix();
        sr.translate(0.0f, this.yOffset + deltaY, 0.0f);
        this.innerShape.render(sr);
        sr.popMatrix();
    }

    private void drawBounds(StackedRenderer sr, float deltaY) {
        if (this.buttonBottomBound == null) {
            Shape s = ShapeUtil.line(4.0f, 0.0f, 0.0f, 80.0f, 0.0f);
            this.buttonUpBound = new ColouredShape(s, Colour.white, (State) null);
            this.buttonBottomBound = new ColouredShape(s, Colour.darkgrey, (State) null);
            Shape s2 = ShapeUtil.line(4.0f, 0.0f, 0.0f, 0.0f, 80.0f);
            this.buttonLeftBound = new ColouredShape(s2, Colour.withAlphai(Colour.white, (int) CpioConstants.C_IWUSR), (State) null);
            this.buttonRightBound = new ColouredShape(s2, Colour.withAlphai(Colour.darkgrey, (int) CpioConstants.C_IWUSR), (State) null);
        }
        sr.pushMatrix();
        sr.translate(this.x - 40.0f, ((this.y + deltaY) + this.yOffset) - 40.0f, 0.0f);
        this.buttonBottomBound.render(sr);
        sr.translate(0.0f, 80.0f, 0.0f);
        this.buttonUpBound.render(sr);
        sr.popMatrix();
        sr.pushMatrix();
        sr.translate(this.x - 40.0f, ((this.y + deltaY) + this.yOffset) - 40.0f, 0.0f);
        this.buttonLeftBound.render(sr);
        sr.translate(80.0f, 0.0f, 0.0f);
        this.buttonRightBound.render(sr);
        sr.popMatrix();
        sr.render();
    }

    private void drawBlockCount(StackedRenderer sr, float deltaY) {
        int count = this.item.getCount();
        if (this.countTextShape == null) {
            this.countTextShape = new Readout(GUI.getFont(), Colour.white, " ", false, 2, 0);
        }
        this.countTextShape.updateValue(count);
        sr.pushMatrix();
        sr.translate(this.x, (((this.y + deltaY) + this.yOffset) - 40.0f) + 5.0f, 0.0f);
        sr.scale(0.65f, 0.65f, 0.65f);
        this.countTextShape.render(sr);
        sr.popMatrix();
    }

    private void drawItemDurability(StackedRenderer sr, float deltaY) {
        if (this.durabilityShape == null) {
            Shape s = ShapeUtil.line(4.0f, 0.0f, 0.0f, 72.0f, 0.0f);
            this.durabilityShape = new ColouredShape(s, this.durabilityFullColor, (State) null);
        }
        sr.pushMatrix();
        sr.translate((this.x - 40.0f) + 5.0f, (((this.y + deltaY) + this.yOffset) - 40.0f) + 5.0f, 0.0f);
        float durabilityRatio = this.item.getDurabilityRatio();
        if (durabilityRatio < 0.7f) {
            this.durabilityShape.colours = ShapeUtil.expand(this.durabilityHalfColor, this.durabilityShape.vertexCount());
        }
        if (durabilityRatio < 0.3f) {
            this.durabilityShape.colours = ShapeUtil.expand(this.durabilityBrokeColor, this.durabilityShape.vertexCount());
        }
        sr.scale(durabilityRatio, 1.0f, 1.0f);
        this.durabilityShape.render(sr);
        sr.popMatrix();
    }

    private void drawDropProgress(StackedRenderer sr) {
        if (this.dropProgresShape == null) {
            Shape s = ShapeUtil.filledQuad(this.x - 40.0f, this.y - 40.0f, this.x + 40.0f, this.y + 40.0f, 0.0f);
            this.dropProgresShape = new ColouredShape(s, Colour.green, (State) null);
        }
        sr.pushMatrix();
        sr.scale(1.0f, this.dropProgresRatio, 1.0f);
        this.dropProgresShape.render(sr);
        sr.popMatrix();
    }

    @Override
    public boolean pointerAdded(Touch.Pointer p) {
        if (this.touch == null && this.bounds.contains(p.x + 40.0f, p.y + 40.0f)) {
            this.touch = p;
            this.downTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    @Override
    public void pointerRemoved(Touch.Pointer p) {
        if (this.touch != null && this.touch == p) {
            long delta = System.currentTimeMillis() - this.downTime;
            if (((float) delta) < 600.0f && !this.longPressed) {
                onTap();
            }
            this.touch = null;
            this.longPressed = false;
            this.longPressStartTime = 0L;
        }
    }

    @Override
    public void reset() {
    }

    protected void onTap() {
        if (this.player != null) {
            this.player.addItemToHotBar(new InventoryTapItem(this.player, this.item));
        }
    }

    protected void onLongPress() {
        if (!GameMode.isCreativeMode()) {
            long timeDelta = System.currentTimeMillis() - this.longPressStartTime;
            if (timeDelta >= DROP_TIME) {
                if (this.player != null) {
                    this.player.dropItemFronHotbar(getInventoryItem());
                    this.player.inventory.remove(getInventoryItem());
                }
            } else if (timeDelta == 0) {
                this.dropProgresRatio = 0.0f;
            } else {
                this.dropProgresRatio = (((float) timeDelta) * 1.0f) / 2000.0f;
            }
        }
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

    public void translateYOffset(float yOffset) {
        this.yOffset += yOffset;
    }

    public float getY() {
        return this.y;
    }

    public void setY(float y) {
        this.y = y;
        this.bounds.y.set(y);
    }

    public void setX(float x) {
        this.x = x;
        this.bounds.x.set(x);
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        this.bounds.x.set(x);
        this.bounds.y.set(y);
    }
}
