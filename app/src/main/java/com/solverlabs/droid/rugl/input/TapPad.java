package com.solverlabs.droid.rugl.input;

import com.solverlabs.droid.rugl.geom.ColouredShape;
import com.solverlabs.droid.rugl.geom.ShapeUtil;
import com.solverlabs.droid.rugl.gl.GLUtil;
import com.solverlabs.droid.rugl.gl.StackedRenderer;
import com.solverlabs.droid.rugl.util.Colour;
import com.solverlabs.droid.rugl.util.geom.BoundingRectangle;


public class TapPad implements Touch.TouchListener {
    private static final long DOUBLE_TAP_TIME = 250;
    public final BoundingRectangle pad = new BoundingRectangle();
    public boolean isSelected;
    public Touch.Pointer touch;
    public boolean needDraw = true;
    public float tapTime = 0.15f;
    public float longPressTime = 0.5f;
    public boolean isVisible = true;
    public int boundsWhiteColour = Colour.packFloat(1.0f, 1.0f, 1.0f, 0.6f);
    public int boundsBlackColour = Colour.packFloat(0.0f, 0.0f, 0.0f, 0.6f);
    public Listener listener = null;
    float offset;
    private ColouredShape circle;
    private ColouredShape circle2;
    private ColouredShape circle3;
    private long firstTapTime;
    private long longPressPeriod;
    private ColouredShape outlineBlack;
    private ColouredShape outlineWhite;
    private boolean wasPointerAdded;
    private long downTime = -1;
    private boolean tapped = false;
    private boolean longPressed = false;
    private int inlineColour = Colour.packFloat(1.0f, 1.0f, 1.0f, 1.0f);

    public TapPad(float x, float y, float width, float height) {
        this.offset = 0.0f;
        this.pad.set(x, x + width, y, y + height);
        this.offset = this.pad.x.getSpan() / 6.0f;
    }

    public void advance() {
        if (this.listener != null) {
            if (this.touch != null && this.isVisible) {
                if (!this.pad.contains(this.touch.x, this.touch.y)) {
                    int horizontal = 0;
                    if (this.touch.x < this.pad.x.getMin()) {
                        horizontal = -1;
                    } else if (this.touch.x > this.pad.x.getMax()) {
                        horizontal = 1;
                    }
                    int vertical = 0;
                    if (this.touch.y < this.pad.y.getMin()) {
                        vertical = -1;
                    } else if (this.touch.y > this.pad.y.getMax()) {
                        vertical = 1;
                    }
                    this.listener.onFlick(this, horizontal, vertical);
                }
                long delta = System.currentTimeMillis() - this.downTime;
                if (((float) delta) > this.longPressTime * 1000.0f && !this.longPressed) {
                    this.longPressPeriod = System.currentTimeMillis();
                    this.longPressed = true;
                }
                if (this.longPressed && System.currentTimeMillis() - this.longPressPeriod >= 50) {
                    this.listener.onLongPress(this);
                    this.longPressPeriod = System.currentTimeMillis();
                }
            }
            if (this.tapped) {
                if (System.currentTimeMillis() - this.firstTapTime <= DOUBLE_TAP_TIME) {
                    this.listener.onDoubleTap(this);
                    this.tapped = false;
                    return;
                }
                this.firstTapTime = System.currentTimeMillis();
                this.listener.onTap(this);
                this.tapped = false;
            }
        }
    }

    @Override
    public boolean pointerAdded(Touch.Pointer p) {
        if (!this.isVisible || !this.pad.contains(p.x, p.y)) {
            return false;
        }
        p.isUse = true;
        this.touch = p;
        this.wasPointerAdded = true;
        this.downTime = System.currentTimeMillis();
        return true;
    }

    @Override
    public void pointerRemoved(Touch.Pointer p) {
        if (this.pad.contains(p.x, p.y) && this.wasPointerAdded) {
            p.isUse = false;
            this.tapped = true;
            this.longPressed = false;
        }
        this.touch = null;
        this.wasPointerAdded = false;
    }

    @Override
    public void reset() {
        this.touch = null;
    }

    public BoundingRectangle getPad() {
        return this.pad;
    }

    public void setPad(BoundingRectangle pad) {
        this.pad.set(pad);
        this.outlineWhite = null;
    }

    public void draw(StackedRenderer sr) {
        if (this.needDraw && this.touch == null && this.isVisible) {
            if (this.outlineWhite == null) {
                this.outlineWhite = new ColouredShape(ShapeUtil.innerQuad(this.pad.x.getMin(), this.pad.y.getMin(), this.pad.x.getMax(), this.pad.y.getMax(), 5.0f, 0.0f), this.boundsWhiteColour, GLUtil.typicalState);
            }
            if (this.outlineBlack == null) {
                this.outlineBlack = new ColouredShape(ShapeUtil.innerQuad(this.pad.x.getMin() + 2.5f, this.pad.y.getMin() + 2.5f, this.pad.x.getMax() - 2.5f, this.pad.y.getMax() - 2.5f, 5.0f, 0.0f), this.boundsBlackColour, GLUtil.typicalState);
            }
            if (this.circle == null) {
                this.circle = new ColouredShape(ShapeUtil.innerCircle(this.pad.x.getMin() + this.offset, this.pad.y.getSpan() / 2.0f, 5.0f, 5.0f, 10.0f, 0.0f), this.inlineColour, GLUtil.typicalState);
                this.circle2 = new ColouredShape(ShapeUtil.innerCircle(this.pad.x.getMin() + (3.0f * this.offset), this.pad.y.getSpan() / 2.0f, 5.0f, 5.0f, 10.0f, 0.0f), this.inlineColour, GLUtil.typicalState);
                this.circle3 = new ColouredShape(ShapeUtil.innerCircle(this.pad.x.getMin() + (this.offset * 5.0f), this.pad.y.getSpan() / 2.0f, 5.0f, 5.0f, 10.0f, 0.0f), this.inlineColour, GLUtil.typicalState);
            }
            this.circle.render(sr);
            this.circle2.render(sr);
            this.circle3.render(sr);
            this.outlineWhite.render(sr);
            this.outlineBlack.render(sr);
        }
    }

    public void outLineDirty() {
        this.outlineWhite = null;
    }


    public static abstract class Listener {
        public abstract void onDoubleTap(TapPad tapPad);

        public abstract void onFlick(TapPad tapPad, int i, int i2);

        public abstract void onLongPress(TapPad tapPad);

        public abstract void onTap(TapPad tapPad);
    }
}
