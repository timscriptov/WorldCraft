package com.solverlabs.worldcraft.inventory;

import com.solverlabs.droid.rugl.geom.ColouredShape;
import com.solverlabs.droid.rugl.geom.Shape;
import com.solverlabs.droid.rugl.geom.ShapeUtil;
import com.solverlabs.droid.rugl.gl.StackedRenderer;
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
    private final int durabilityBrokeColor = Colour.red;
    private final int durabilityFullColor = Colour.green;
    private final int durabilityHalfColor = Colour.yellow;
    private final float longPressTime = 0.5f;
    private final Player player;
    public BoundingRectangle bounds;
    public boolean isDrawBounds = true;
    protected InventoryItem item;
    private ColouredShape buttonBottomBound;
    private ColouredShape buttonLeftBound;
    private ColouredShape buttonRightBound;
    private ColouredShape buttonUpBound;
    private Readout countTextShape;
    private long downTime = -1L;
    private float dropProgresRatio;
    private ColouredShape dropProgresShape;
    private ColouredShape durabilityShape;
    private ColouredShape innerShape;
    private long longPressPeriod;
    private long longPressStartTime;
    private boolean longPressed = false;
    private Touch.Pointer touch;
    private float x;
    private float y;
    private float yOffset;

    public InventoryTapItem(InventoryItem item) {
        bounds = new BoundingRectangle(0.0f, 0.0f, WIDTH, HEIGHT);
        this.item = item;
        player = null;
    }

    public InventoryTapItem(Player player, InventoryItem item) {
        bounds = new BoundingRectangle(0.0f, 0.0f, WIDTH, HEIGHT);
        this.item = item;
        this.player = player;
    }

    public InventoryTapItem(Player player, InventoryItem item, float x, float y) {
        bounds = new BoundingRectangle(x, y, WIDTH, HEIGHT);
        this.item = item;
        this.x = x;
        this.y = y;
        this.player = player;
    }

    public InventoryItem getInventoryItem() {
        return item;
    }

    public void draw(StackedRenderer sr, float deltaY) {
        if (touch != null) {
            drawInnerBound(sr, deltaY);
        }
        if (longPressed) {
            drawDropProgress(sr);
        }
        if (isDrawBounds) {
            drawBounds(sr, deltaY);
        }
        bounds.y.set(y + deltaY + yOffset);
        sr.pushMatrix();
        sr.translate(x, y + deltaY + yOffset, 0.0f);
        sr.scale(60.0f, 60.0f, 1.0f);
        item.getItemShape().render(sr);
        sr.popMatrix();
        sr.render();
        if (GameMode.isSurvivalMode()) {
            if (item.getItem().isTool()) {
                drawItemDurability(sr, deltaY);
            } else {
                drawBlockCount(sr, deltaY);
            }
        }
    }

    public void advance() {
        if (touch != null) {
            long delta = System.currentTimeMillis() - downTime;
            if (((float) delta) > longPressTime * 1000.0f && !longPressed) {
                longPressPeriod = System.currentTimeMillis();
                longPressStartTime = System.currentTimeMillis();
                longPressed = true;
            }
            if (longPressed && System.currentTimeMillis() - longPressPeriod >= 50) {
                onLongPress();
                longPressPeriod = System.currentTimeMillis();
            }
        }
    }

    private void drawInnerBound(StackedRenderer sr, float deltaY) {
        if (innerShape == null) {
            Shape is = ShapeUtil.innerQuad(x - HALF_WIDTH, y - HALF_WIDTH, x + HALF_WIDTH, y + HALF_WIDTH, 80.0f, 0.0f);
            innerShape = new ColouredShape(is, Colour.withAlphai(Colour.white, CpioConstants.C_IWUSR), null);
        }
        sr.pushMatrix();
        sr.translate(0.0f, yOffset + deltaY, 0.0f);
        innerShape.render(sr);
        sr.popMatrix();
    }

    private void drawBounds(StackedRenderer sr, float deltaY) {
        if (buttonBottomBound == null) {
            Shape s = ShapeUtil.line(4.0f, 0.0f, 0.0f, 80.0f, 0.0f);
            buttonUpBound = new ColouredShape(s, Colour.white, null);
            buttonBottomBound = new ColouredShape(s, Colour.darkgrey, null);
            Shape s2 = ShapeUtil.line(4.0f, 0.0f, 0.0f, 0.0f, 80.0f);
            buttonLeftBound = new ColouredShape(s2, Colour.withAlphai(Colour.white, CpioConstants.C_IWUSR), null);
            buttonRightBound = new ColouredShape(s2, Colour.withAlphai(Colour.darkgrey, CpioConstants.C_IWUSR), null);
        }
        sr.pushMatrix();
        sr.translate(x - HALF_WIDTH, ((y + deltaY) + yOffset) - HALF_WIDTH, 0.0f);
        buttonBottomBound.render(sr);
        sr.translate(0.0f, 80.0f, 0.0f);
        buttonUpBound.render(sr);
        sr.popMatrix();
        sr.pushMatrix();
        sr.translate(x - HALF_WIDTH, ((y + deltaY) + yOffset) - HALF_WIDTH, 0.0f);
        buttonLeftBound.render(sr);
        sr.translate(80.0f, 0.0f, 0.0f);
        buttonRightBound.render(sr);
        sr.popMatrix();
        sr.render();
    }

    private void drawBlockCount(StackedRenderer sr, float deltaY) {
        int count = item.getCount();
        if (countTextShape == null) {
            countTextShape = new Readout(GUI.getFont(), Colour.white, " ", false, 2, 0);
        }
        countTextShape.updateValue(count);
        sr.pushMatrix();
        sr.translate(x, (((y + deltaY) + yOffset) - HALF_WIDTH) + 5.0f, 0.0f);
        sr.scale(0.65f, 0.65f, 0.65f);
        countTextShape.render(sr);
        sr.popMatrix();
    }

    private void drawItemDurability(StackedRenderer sr, float deltaY) {
        if (durabilityShape == null) {
            Shape s = ShapeUtil.line(4.0f, 0.0f, 0.0f, 72.0f, 0.0f);
            durabilityShape = new ColouredShape(s, durabilityFullColor, null);
        }
        sr.pushMatrix();
        sr.translate((x - HALF_WIDTH) + 5.0f, (((y + deltaY) + yOffset) - HALF_WIDTH) + 5.0f, 0.0f);
        float durabilityRatio = item.getDurabilityRatio();
        if (durabilityRatio < 0.7f) {
            durabilityShape.colours = ShapeUtil.expand(durabilityHalfColor, durabilityShape.vertexCount());
        }
        if (durabilityRatio < 0.3f) {
            durabilityShape.colours = ShapeUtil.expand(durabilityBrokeColor, durabilityShape.vertexCount());
        }
        sr.scale(durabilityRatio, 1.0f, 1.0f);
        durabilityShape.render(sr);
        sr.popMatrix();
    }

    private void drawDropProgress(StackedRenderer sr) {
        if (dropProgresShape == null) {
            Shape s = ShapeUtil.filledQuad(x - HALF_WIDTH, y - HALF_WIDTH, x + HALF_WIDTH, y + HALF_WIDTH, 0.0f);
            dropProgresShape = new ColouredShape(s, Colour.green, null);
        }
        sr.pushMatrix();
        sr.scale(1.0f, dropProgresRatio, 1.0f);
        dropProgresShape.render(sr);
        sr.popMatrix();
    }

    @Override
    public boolean pointerAdded(Touch.Pointer p) {
        if (touch == null && bounds.contains(p.x + HALF_WIDTH, p.y + HALF_WIDTH)) {
            touch = p;
            downTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    @Override
    public void pointerRemoved(Touch.Pointer p) {
        if (touch != null && touch == p) {
            long delta = System.currentTimeMillis() - downTime;
            if (((float) delta) < 600.0f && !longPressed) {
                onTap();
            }
            touch = null;
            longPressed = false;
            longPressStartTime = 0L;
        }
    }

    @Override
    public void reset() {
    }

    protected void onTap() {
        if (player != null) {
            player.addItemToHotBar(new InventoryTapItem(player, item));
        }
    }

    protected void onLongPress() {
        if (!GameMode.isCreativeMode()) {
            long timeDelta = System.currentTimeMillis() - longPressStartTime;
            if (timeDelta >= DROP_TIME) {
                if (player != null) {
                    player.dropItemFronHotbar(getInventoryItem());
                    player.inventory.remove(getInventoryItem());
                }
            } else if (timeDelta == 0) {
                dropProgresRatio = 0.0f;
            } else {
                dropProgresRatio = (((float) timeDelta) * 1.0f) / 2000.0f;
            }
        }
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

    public void translateYOffset(float yOffset) {
        this.yOffset += yOffset;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
        bounds.y.set(y);
    }

    public void setX(float x) {
        this.x = x;
        bounds.x.set(x);
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        bounds.x.set(x);
        bounds.y.set(y);
    }
}
