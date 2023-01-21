package com.solverlabs.droid.rugl.input;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.geom.ColouredShape;
import com.solverlabs.droid.rugl.geom.ShapeUtil;
import com.solverlabs.droid.rugl.gl.StackedRenderer;
import com.solverlabs.droid.rugl.gl.State;
import com.solverlabs.droid.rugl.util.Colour;
import com.solverlabs.droid.rugl.util.FloatMath;
import com.solverlabs.droid.rugl.util.Trig;
import com.solverlabs.droid.rugl.util.math.Range;

import org.apache.commons.compress.archivers.cpio.CpioConstants;


public class TouchStick extends AbstractTouchStick {
    public float radius;
    public Touch.Pointer touchTemp;
    public float ramp = 1.0f;
    private ColouredShape limit;
    private float startPointX;
    private float startPointY;
    private ColouredShape stick;
    private float xPos;
    private float yPos;
    private boolean touchLeft = false;
    private long touchTime = -1;
    private long clickTime = -1;
    private boolean clickHoldPrimed = false;
    private boolean clickHoldActive = false;

    public TouchStick(float x, float y, float limitRadius) {
        setPosition(x, y);
        this.radius = limitRadius;
    }

    private void buildShape() {
        this.limit = new ColouredShape(ShapeUtil.innerCircle(0.0f, 0.0f, this.radius, 10.0f, 30.0f, 0.0f), Colour.white, (State) null);
        this.stick = new ColouredShape(this.limit.clone(), Colour.white, (State) null);
        this.stick.scale(0.5f, 0.5f, 1.0f);
        Colour.withAlphai(this.stick.colours, (int) CpioConstants.C_IWUSR);
        for (int i = 0; i < this.limit.colours.length; i += 2) {
            this.limit.colours[i] = Colour.withAlphai(this.limit.colours[i], 0);
        }
        this.limit.translate(this.xPos, this.yPos, 0.0f);
        this.stick.translate(this.xPos, this.yPos, 0.0f);
    }

    public void setPosition(float x, float y) {
        if (this.limit != null) {
            this.limit.translate(x - this.xPos, y - this.yPos, 0.0f);
            this.stick.translate(x - this.xPos, y - this.yPos, 0.0f);
        }
        this.xPos = x;
        this.yPos = y;
    }

    @Override
    public void advance() {
        long now = System.currentTimeMillis();
        if (this.touchLeft) {
            long tapDuration = now - this.touchTime;
            if (tapDuration < this.tapTime && this.listener != null) {
                this.listener.onClick();
                this.clickTime = now;
            }
            if (this.clickHoldActive) {
                this.clickHoldActive = false;
                this.listener.onClickHold(this.clickHoldActive);
            }
            this.touch = null;
            this.touchLeft = false;
        }
        if (this.touch != null) {
            long chd = now - this.clickTime;
            this.clickHoldPrimed = chd < this.clickHoldDelay;
            if (this.clickHoldPrimed) {
                this.clickTime = now;
            }
            if (now - this.touchTime > this.tapTime && Math.abs(getStartPointX() - this.touch.x) < 5.0f && Math.abs(getStartPointY() - this.touch.y) < 5.0f && !this.clickHoldActive) {
                this.clickHoldPrimed = false;
                this.clickHoldActive = true;
                this.listener.onClickHold(this.clickHoldActive);
            } else {
                this.listener.onMove();
            }
            float dx = this.touch.x - this.xPos;
            float dy = this.touch.y - this.yPos;
            float a = Trig.atan2(dy, dx);
            float r = (float) Math.pow(Range.limit(FloatMath.sqrt((dx * dx) + (dy * dy)) / this.radius, 0.0f, 1.0f), this.ramp);
            this.x = Trig.cos(a) * r;
            this.y = Trig.sin(a) * r;
            return;
        }
        this.listener.onUp();
        this.x = 0.0f;
        this.y = 0.0f;
    }

    @Override
    public void pointerRemoved(Touch.Pointer p) {
        if (p == this.touch) {
            this.touchLeft = true;
        }
    }

    @Override
    public boolean pointerAdded(@NonNull Touch.Pointer p) {
        if (Math.hypot(p.x - this.xPos, p.y - this.yPos) < this.radius) {
            this.touch = p;
            this.touchTime = System.currentTimeMillis();
            setStartPointX(p.x);
            setStartPointY(p.y);
            return true;
        }
        return false;
    }

    @Override
    public void reset() {
        this.touch = null;
    }

    @Override
    public void draw(StackedRenderer r) {
        if (this.limit == null) {
            buildShape();
        }
        this.limit.render(r);
        r.pushMatrix();
        r.translate(this.x * this.radius, this.y * this.radius, 0.0f);
        this.stick.render(r);
        r.popMatrix();
    }

    public float getStartPointX() {
        return this.startPointX;
    }

    public void setStartPointX(float startPointX) {
        this.startPointX = startPointX;
    }

    public float getStartPointY() {
        return this.startPointY;
    }

    public void setStartPointY(float startPointY) {
        this.startPointY = startPointY;
    }
}
