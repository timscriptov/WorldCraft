package com.solverlabs.droid.rugl.input;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.geom.ColouredShape;
import com.solverlabs.droid.rugl.geom.ShapeUtil;
import com.solverlabs.droid.rugl.gl.StackedRenderer;
import com.solverlabs.droid.rugl.gl.State;
import com.solverlabs.droid.rugl.util.Colour;
import com.solverlabs.droid.rugl.util.geom.BoundingRectangle;


public class TouchStickArea extends AbstractTouchStick {
    public final TouchStick stick;
    public BoundingRectangle pad = new BoundingRectangle();
    public boolean draw = true;
    public int boundsColour = Colour.packFloat(1.0f, 1.0f, 1.0f, 0.6f);
    public int innerBoundsColour = Colour.packFloat(0.0f, 0.0f, 0.0f, 0.6f);
    public int inlineColour = Colour.packFloat(1.0f, 1.0f, 1.0f, 1.0f);
    private float centerX;
    private float centerY;
    private ColouredShape cirlce;
    private ColouredShape inline;
    private ColouredShape outline;

    public TouchStickArea(float x, float y, float width, float height, float stickRadius) {
        this.pad.set(x, x + width, y, y + height);
        this.stick = new TouchStick(x, y, stickRadius);
        this.stick.listener = new AbstractTouchStick.ClickListener() {
            @Override
            public void onClick() {
                if (listener != null) {
                    listener.onClick();
                }
            }

            @Override
            public void onClickHold(boolean active) {
                if (listener != null) {
                    listener.onClickHold(active);
                }
            }

            @Override
            public void onMove() {
                if (listener != null) {
                    listener.onMove();
                }
            }

            @Override
            public void onUp() {
                if (listener != null) {
                    listener.onUp();
                }
            }
        };
    }

    @Override
    public boolean pointerAdded(@NonNull Touch.Pointer p) {
        if (this.pad.contains(p.x, p.y)) {
            p.isUse = true;
            this.touch = p;
            this.stick.setPosition(p.x, p.y);
            this.stick.pointerAdded(p);
            return true;
        }
        return false;
    }

    @Override
    public void pointerRemoved(Touch.Pointer p) {
        if (p == this.touch) {
            p.isUse = false;
            this.stick.pointerRemoved(p);
            this.touch = null;
        }
    }

    public Touch.Pointer getPointer() {
        return this.stick.touch;
    }

    @Override
    public void reset() {
        this.touch = null;
        this.stick.reset();
    }

    @Override
    public void advance() {
        this.stick.advance();
        this.x = this.stick.x;
        this.y = this.stick.y;
    }

    @Override
    public void draw(StackedRenderer sr) {
        if (this.draw) {
            if (this.outline == null) {
                this.centerX = (this.pad.x.getMax() - this.pad.x.getMin()) / 2.0f;
                this.centerY = (this.pad.y.getMax() - this.pad.y.getMin()) / 2.0f;
                this.outline = new ColouredShape(ShapeUtil.innerCircle(this.centerX, this.centerY, 70.0f, 2.0f, 10.0f, 0.0f), this.boundsColour, (State) null);
            }
            if (this.inline == null) {
                this.inline = new ColouredShape(ShapeUtil.innerCircle(this.centerX, this.centerY, 68.0f, 2.0f, 10.0f, 0.0f), this.innerBoundsColour, (State) null);
            }
            this.inline.render(sr);
            this.outline.render(sr);
        }
        if (this.draw && this.touch != null) {
            if (this.cirlce == null) {
                this.cirlce = new ColouredShape(ShapeUtil.innerCircle(this.touch.x, this.touch.y, 20.0f, 20.0f, 10.0f, 0.0f), this.inlineColour, (State) null);
            }
            if (((this.centerX - this.touch.x) * (this.centerX - this.touch.x)) + ((this.centerY - this.touch.y) * (this.centerY - this.touch.y)) <= 4900.0f) {
                this.cirlce.set(this.touch.x - 20.0f, this.touch.y - 20.0f, 0.0f);
            }
            this.cirlce.render(sr);
            return;
        }
        this.cirlce = null;
    }

    public void outLineDirty() {
        this.outline = null;
    }
}
