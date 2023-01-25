package com.mcal.droid.rugl.input;

import androidx.annotation.NonNull;

import com.mcal.droid.rugl.geom.ColouredShape;
import com.mcal.droid.rugl.geom.ShapeUtil;
import com.mcal.droid.rugl.gl.StackedRenderer;
import com.mcal.droid.rugl.gl.State;
import com.mcal.droid.rugl.util.Colour;
import com.mcal.droid.rugl.util.FloatMath;
import com.mcal.droid.rugl.util.Trig;
import com.mcal.droid.rugl.util.math.Range;

import org.apache.commons.compress.archivers.cpio.CpioConstants;


public class TouchStick extends AbstractTouchStick {
    public float radius;
    public float ramp = 1.0f;
    private ColouredShape limit;
    private float startPointX;
    private float startPointY;
    private ColouredShape stick;
    private float xPos;
    private float yPos;
    /**
     * Indicates that the touch has been lifted
     */
    private boolean touchLeft = false;
    /**
     * The time at which a touch was added
     */
    private long touchTime = -1;
    /**
     * The time of the last click event
     */
    private long clickTime = -1;
    private boolean clickHoldPrimed = false;
    private boolean clickHoldActive = false;

    /**
     * @param x           position, in screen coordinates
     * @param y           position, in screen coordinates
     * @param limitRadius radius, in screen coordinates
     */
    public TouchStick(float x, float y, float limitRadius) {
        setPosition(x, y);
        radius = limitRadius;
    }

    private void buildShape() {
        limit = new ColouredShape(ShapeUtil.innerCircle(0.0f, 0.0f, radius, 10.0f, 30.0f, 0.0f), Colour.white, (State) null);
        stick = new ColouredShape(limit.clone(), Colour.white, (State) null);
        stick.scale(0.5f, 0.5f, 1.0f);
        Colour.withAlphai(stick.colours, (int) CpioConstants.C_IWUSR);
        for (int i = 0; i < limit.colours.length; i += 2) {
            limit.colours[i] = Colour.withAlphai(limit.colours[i], 0);
        }
        limit.translate(xPos, yPos, 0.0f);
        stick.translate(xPos, yPos, 0.0f);
    }

    /**
     * @param x
     * @param y
     */
    public void setPosition(float x, float y) {
        if (limit != null) {
            limit.translate(x - xPos, y - yPos, 0.0f);
            stick.translate(x - xPos, y - yPos, 0.0f);
        }
        xPos = x;
        yPos = y;
    }

    @Override
    public void advance() {
        long now = System.currentTimeMillis();
        if (touchLeft) {
            long tapDuration = now - touchTime;
            if (tapDuration < tapTime && listener != null) {
                listener.onClick();
                clickTime = now;
            }
            if (clickHoldActive) {
                clickHoldActive = false;
                listener.onClickHold(clickHoldActive);
            }
            touch = null;
            touchLeft = false;
        }
        if (touch != null) {
            long chd = now - clickTime;
            clickHoldPrimed = chd < clickHoldDelay;
            if (clickHoldPrimed) {
                // keep it active till we get the long-hold
                clickTime = now;
            }
            if (now - touchTime > tapTime && Math.abs(getStartPointX() - touch.x) < 5.0f && Math.abs(getStartPointY() - touch.y) < 5.0f && !clickHoldActive) {
                clickHoldPrimed = false;
                clickHoldActive = true;
                listener.onClickHold(clickHoldActive);
            } else {
                listener.onMove();
            }
            float dx = touch.x - xPos;
            float dy = touch.y - yPos;
            float a = Trig.atan2(dy, dx);
            float r = (float) Math.pow(Range.limit(FloatMath.sqrt((dx * dx) + (dy * dy)) / radius, 0.0f, 1.0f), ramp);
            x = Trig.cos(a) * r;
            y = Trig.sin(a) * r;
            return;
        }
        listener.onUp();
        x = 0.0f;
        y = 0.0f;
    }

    @Override
    public void pointerRemoved(Touch.Pointer p) {
        if (p == touch) {
            touchLeft = true;
        }
    }

    @Override
    public boolean pointerAdded(@NonNull Touch.Pointer p) {
        if (Math.hypot(p.x - xPos, p.y - yPos) < radius) {
            touch = p;
            touchTime = System.currentTimeMillis();
            setStartPointX(p.x);
            setStartPointY(p.y);
            return true;
        }
        return false;
    }

    @Override
    public void reset() {
        touch = null;
    }

    /**
     * @param r
     */
    @Override
    public void draw(StackedRenderer r) {
        if (limit == null) {
            buildShape();
        }
        limit.render(r);
        r.pushMatrix();
        r.translate(x * radius, y * radius, 0.0f);
        stick.render(r);
        r.popMatrix();
    }

    public float getStartPointX() {
        return startPointX;
    }

    public void setStartPointX(float startPointX) {
        this.startPointX = startPointX;
    }

    public float getStartPointY() {
        return startPointY;
    }

    public void setStartPointY(float startPointY) {
        this.startPointY = startPointY;
    }
}
