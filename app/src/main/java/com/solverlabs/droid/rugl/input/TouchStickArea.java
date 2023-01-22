package com.solverlabs.droid.rugl.input;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.geom.ColouredShape;
import com.solverlabs.droid.rugl.geom.ShapeUtil;
import com.solverlabs.droid.rugl.gl.StackedRenderer;
import com.solverlabs.droid.rugl.gl.State;
import com.solverlabs.droid.rugl.util.Colour;
import com.solverlabs.droid.rugl.util.geom.BoundingRectangle;

/**
 * An area that causes a {@link TouchStick} to appear when and where a touch is
 * placed. This solves the unwanted initial input when you don't manage to place
 * your touch exactly in the center of a static {@link TouchStick}
 */
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

    /**
     * @param x           left edge of area
     * @param y           lower edge of area
     * @param width
     * @param height
     * @param stickRadius radius of stick that appears
     */
    public TouchStickArea(float x, float y, float width, float height, float stickRadius) {
        pad.set(x, x + width, y, y + height);
        stick = new TouchStick(x, y, stickRadius);
        // redirect clicks to our listener
        stick.listener = new AbstractTouchStick.ClickListener() {
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
        if (pad.contains(p.x, p.y)) {
            p.isUse = true;
            touch = p;
            stick.setPosition(p.x, p.y);
            stick.pointerAdded(p);
            return true;
        }
        return false;
    }

    @Override
    public void pointerRemoved(Touch.Pointer p) {
        if (p == touch) {
            p.isUse = false;
            stick.pointerRemoved(p);
            touch = null;
        }
    }

    public Touch.Pointer getPointer() {
        return stick.touch;
    }

    @Override
    public void reset() {
        touch = null;
        stick.reset();
    }

    @Override
    public void advance() {
        stick.advance();
        x = stick.x;
        y = stick.y;
    }

    @Override
    public void draw(StackedRenderer sr) {
        if (draw) {
            if (outline == null) {
                centerX = (pad.x.getMax() - pad.x.getMin()) / 2.0f;
                centerY = (pad.y.getMax() - pad.y.getMin()) / 2.0f;
                outline = new ColouredShape(ShapeUtil.innerCircle(centerX, centerY, 70.0f, 2.0f, 10.0f, 0.0f), boundsColour, (State) null);
            }
            if (inline == null) {
                inline = new ColouredShape(ShapeUtil.innerCircle(centerX, centerY, 68.0f, 2.0f, 10.0f, 0.0f), innerBoundsColour, (State) null);
            }
            inline.render(sr);
            outline.render(sr);
        }
        if (draw && touch != null) {
            if (cirlce == null) {
                cirlce = new ColouredShape(ShapeUtil.innerCircle(touch.x, touch.y, 20.0f, 20.0f, 10.0f, 0.0f), inlineColour, (State) null);
            }
            if (((centerX - touch.x) * (centerX - touch.x)) + ((centerY - touch.y) * (centerY - touch.y)) <= 4900.0f) {
                cirlce.set(touch.x - 20.0f, touch.y - 20.0f, 0.0f);
            }
            cirlce.render(sr);
            return;
        }
        cirlce = null;
    }

    public void outLineDirty() {
        outline = null;
    }
}
