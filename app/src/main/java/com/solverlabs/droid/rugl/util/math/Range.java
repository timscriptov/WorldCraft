package com.solverlabs.droid.rugl.util.math;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A numerical interval
 *
 * @author ryanm
 */
public class Range {
    /**
     * The lower value
     */
    private float min = 0;

    /**
     * The upper value
     */
    private float max = 0;

    /**
     * @param min
     * @param max
     */
    public Range(float min, float max) {
        set(min, max);
    }

    /**
     * Limits a value to lie within some range
     *
     * @param v
     * @param min
     * @param max
     * @return min if v < min, max if v > max, otherwise v
     */
    public static float limit(float v, float min, float max) {
        if (v < min) {
            v = min;
        } else if (v > max) {
            v = max;
        }

        return v;
    }

    /**
     * Wraps a value into a range, in a modular arithmetic style (but
     * it works for negatives too)
     *
     * @param v
     * @param min
     * @param max
     * @return the wrapped value
     */
    public static float wrap(float v, float min, float max) {
        v -= min;
        max -= min;

        if (v < 0) {
            v += max * ((int) (-v / max) + 1);
        }

        v %= max;

        return v + min;
    }

    /**
     * @param v
     * @param min
     * @param max
     * @return The proportion of the distance between min and max that
     * v lies from min
     */
    public static float toRatio(float v, float min, float max) {
        float d = v - min;
        float e = max - min;

        return d / e;
    }

    /**
     * @param ratio
     * @param min
     * @param max
     * @return min + ratio * ( max - min )
     */
    public static float toValue(float ratio, float min, float max) {
        return min + ratio * (max - min);
    }

    /**
     * @param value
     * @param min
     * @param max
     * @return <code>true</code> if value lies within min and max,
     * inclusively
     */
    public static boolean inRange(float value, float min, float max) {
        return value >= min && value <= max;
    }

    /**
     * @param minA
     * @param maxA
     * @param minB
     * @param maxB
     * @return <code>true</code> if the ranges overlap
     */
    public static boolean overlaps(float minA, float maxA, float minB, float maxB) {
        assert minA <= maxA;
        assert minB <= maxB;

        return !(minA > maxB || maxA < minB);
    }

    /**
     * @param a  a static range
     * @param b  a mobile range
     * @param bv The velocity of b
     * @return The time period over which a and b intersect, or
     * <code>null</code> if they never do
     */
    @Nullable
    public static Range intersectionTime(Range a, Range b, float bv) {
        if (bv == 0) { // nobody likes division by zero
            if (a.intersects(b)) { // continual intersection
                return new Range(-Float.MAX_VALUE, Float.MAX_VALUE);
            } else { // no intersection
                return null;
            }
        }

        // time when low edge of a meets high edge of b
        float t1 = (a.getMin() - b.getMax()) / bv;
        // time when high edge of a meets low edge of b
        float t2 = (a.getMax() - b.getMin()) / bv;

        // constructor sorts the times into proper order
        return new Range(t1, t2);
    }

    /**
     * @param minA
     * @param maxA
     * @param minB
     * @param maxB
     * @return <code>true</code> if rangeA contains rangeB
     */
    public static boolean contains(float minA, float maxA, float minB, float maxB) {
        return minA <= minB && maxA >= maxB;
    }

    /**
     * @param minA
     * @param maxA
     * @param minB
     * @param maxB
     * @return The size of the intersection of the two ranges
     */
    public static float intersection(float minA, float maxA, float minB, float maxB) {
        float highMin = Math.max(minA, minB);
        float lowMax = Math.min(maxA, maxB);

        if (lowMax > highMin) {
            return lowMax - highMin;
        }

        return 0;
    }

    /**
     * Smooth interpolation between min and max. Copy the following
     * into gnuplot to see the difference between
     * {@link #smooth(float, float, float)} and
     * {@link #smooth2(float, float, float)} <br>
     * set xrange [0:1] <br>
     * set yrange [0:1] <br>
     * plot (x**2*(3-2*x)) title "smooth", x**3*(10+x*(-15+6*x)) title
     * "smooth2"
     *
     * @param ratio in range 0-1
     * @param min   minimum output value
     * @param max   maximum output value
     * @return first-order continuous graduation between min and max
     * @see #smooth2(float, float, float)
     */
    public static final float smooth(float ratio, float min, float max) {
        return toValue(ratio * ratio * (3.0f - (ratio + ratio)), min, max);
    }

    /**
     * Smooth interpolation between min and max
     *
     * @param ratio in range 0-1
     * @param min   minimum output value
     * @param max   maximum output value
     * @return all-derivation continuous graduation between min and max
     * @see Range#smooth(float, float, float)
     */
    public static final float smooth2(float ratio, float min, float max) {
        float t3 = ratio * ratio * ratio;

        return toValue(t3 * (10.f + ratio * (-15.f + 6.f * ratio)), min, max);
    }

    /**
     * @param r
     * @param dest
     * @return <code>true</code> if the intersection exists
     */
    public boolean intersection(Range r, Range dest) {
        if (intersects(r)) {
            dest.set(Math.max(min, r.min), Math.min(max, r.max));
            return true;
        }

        return false;
    }

    /**
     * @param value
     * @return true if the value lies between the min and max values,
     * inclusively
     */
    public boolean contains(float value) {
        return min <= value && max >= value;
    }

    /**
     * @param min
     * @param max
     */
    public void set(float min, float max) {
        this.min = min;
        this.max = max;

        sort();
    }

    /**
     * @param r
     */
    public void set(@NonNull Range r) {
        set(r.getMin(), r.getMax());
    }

    /**
     * @return The difference between min and max
     */
    public float getSpan() {
        return max - min;
    }

    /**
     * @return The lower range boundary
     */
    public float getMin() {
        return min;
    }

    /**
     * @param min
     */
    public void setMin(float min) {
        this.min = min;

        sort();
    }

    /**
     * @return The upper range boundary
     */
    public float getMax() {
        return max;
    }

    /**
     * @param max
     */
    public void setMax(float max) {
        this.max = max;

        sort();
    }

    private void sort() {
        if (min > max) {
            float t = min;
            min = max;
            max = t;
        }
    }

    /**
     * Limits a value to lie within this range
     *
     * @param v The value to limit
     * @return min if v < min, max if v > max, otherwise v
     */
    public float limit(float v) {
        return limit(v, min, max);
    }

    /**
     * Wraps a value into this range, modular arithmetic style
     *
     * @param v
     * @return The wrapped value
     */
    public float wrap(float v) {
        return wrap(v, min, max);
    }

    /**
     * @param value
     * @return The proportion of the distance between min and max that
     * value lies from min
     */
    public float toRatio(float value) {
        return toRatio(value, min, max);
    }

    /**
     * @param ratio
     * @return min + ratio * ( max - min )
     */
    public float toValue(float ratio) {
        return toValue(ratio, min, max);
    }

    /**
     * @param r
     * @return <code>true</code> if the ranges have values in common
     */
    public boolean intersects(@NonNull Range r) {
        return overlaps(min, max, r.min, r.max);
    }

    /**
     * Alters this range such that {@link #contains(float)} returns
     * <code>true</code> for f
     *
     * @param f
     */
    public void encompass(float f) {
        if (f < min) {
            min = f;
        } else if (f > max) {
            max = f;
        }
    }

    /**
     * Alters this range such that {@link #contains(float)} returns
     * <code>true</code> for all values in r
     *
     * @param r
     */
    public void encompass(@NonNull Range r) {
        encompass(r.min);
        encompass(r.max);
    }

    @NonNull
    @Override
    public String toString() {
        return "[ " + min + " : " + max + " ]";
    }

    /**
     * Shifts the range
     *
     * @param d
     */
    public void translate(float d) {
        min += d;
        max += d;
    }

    /**
     * Scales the range around the origin
     *
     * @param s
     */
    public void scale(float s) {
        min *= s;
        max *= s;
    }

    public void set(float f) {
        float delta = getSpan();
        min = f;
        max = f + delta;
    }
}