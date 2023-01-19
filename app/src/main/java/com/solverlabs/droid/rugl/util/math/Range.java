package com.solverlabs.droid.rugl.util.math;


public class Range {
    static final /* synthetic */ boolean $assertionsDisabled;

    static {
        $assertionsDisabled = !Range.class.desiredAssertionStatus();
    }

    private float min = 0.0f;
    private float max = 0.0f;

    public Range(float min, float max) {
        set(min, max);
    }

    public static float limit(float v, float min, float max) {
        if (v < min) {
            return min;
        }
        if (v > max) {
            return max;
        }
        return v;
    }

    public static float wrap(float v, float min, float max) {
        float v2 = v - min;
        float max2 = max - min;
        if (v2 < 0.0f) {
            v2 += (((int) ((-v2) / max2)) + 1) * max2;
        }
        return (v2 % max2) + min;
    }

    public static float toRatio(float v, float min, float max) {
        float d = v - min;
        float e = max - min;
        return d / e;
    }

    public static float toValue(float ratio, float min, float max) {
        return ((max - min) * ratio) + min;
    }

    public static boolean inRange(float value, float min, float max) {
        return value >= min && value <= max;
    }

    public static boolean overlaps(float minA, float maxA, float minB, float maxB) {
        if ($assertionsDisabled || minA <= maxA) {
            if (!$assertionsDisabled && minB > maxB) {
                throw new AssertionError();
            }
            return minA <= maxB && maxA >= minB;
        }
        throw new AssertionError();
    }

    public static Range intersectionTime(Range a, Range b, float bv) {
        if (bv == 0.0f) {
            if (a.intersects(b)) {
                return new Range(-3.4028235E38f, Float.MAX_VALUE);
            }
            return null;
        }
        float t1 = (a.getMin() - b.getMax()) / bv;
        float t2 = (a.getMax() - b.getMin()) / bv;
        return new Range(t1, t2);
    }

    public static boolean contains(float minA, float maxA, float minB, float maxB) {
        return minA <= minB && maxA >= maxB;
    }

    public static float intersection(float minA, float maxA, float minB, float maxB) {
        float highMin = Math.max(minA, minB);
        float lowMax = Math.min(maxA, maxB);
        if (lowMax > highMin) {
            return lowMax - highMin;
        }
        return 0.0f;
    }

    public static final float smooth(float ratio, float min, float max) {
        return toValue(ratio * ratio * (3.0f - (ratio + ratio)), min, max);
    }

    public static final float smooth2(float ratio, float min, float max) {
        float t3 = ratio * ratio * ratio;
        return toValue((10.0f + (((-15.0f) + (6.0f * ratio)) * ratio)) * t3, min, max);
    }

    public boolean intersection(Range r, Range dest) {
        if (intersects(r)) {
            dest.set(Math.max(this.min, r.min), Math.min(this.max, r.max));
            return true;
        }
        return false;
    }

    public boolean contains(float value) {
        return this.min <= value && this.max >= value;
    }

    public void set(float min, float max) {
        this.min = min;
        this.max = max;
        sort();
    }

    public void set(Range r) {
        set(r.getMin(), r.getMax());
    }

    public float getSpan() {
        return this.max - this.min;
    }

    public float getMin() {
        return this.min;
    }

    public void setMin(float min) {
        this.min = min;
        sort();
    }

    public float getMax() {
        return this.max;
    }

    public void setMax(float max) {
        this.max = max;
        sort();
    }

    private void sort() {
        if (this.min > this.max) {
            float t = this.min;
            this.min = this.max;
            this.max = t;
        }
    }

    public float limit(float v) {
        return limit(v, this.min, this.max);
    }

    public float wrap(float v) {
        return wrap(v, this.min, this.max);
    }

    public float toRatio(float value) {
        return toRatio(value, this.min, this.max);
    }

    public float toValue(float ratio) {
        return toValue(ratio, this.min, this.max);
    }

    public boolean intersects(Range r) {
        return overlaps(this.min, this.max, r.min, r.max);
    }

    public void encompass(float f) {
        if (f < this.min) {
            this.min = f;
        } else if (f > this.max) {
            this.max = f;
        }
    }

    public void encompass(Range r) {
        encompass(r.min);
        encompass(r.max);
    }

    public String toString() {
        return "[ " + this.min + " : " + this.max + " ]";
    }

    public void translate(float d) {
        this.min += d;
        this.max += d;
    }

    public void set(float f) {
        float delta = getSpan();
        this.min = f;
        this.max = f + delta;
    }

    public void scale(float s) {
        this.min *= s;
        this.max *= s;
    }
}
