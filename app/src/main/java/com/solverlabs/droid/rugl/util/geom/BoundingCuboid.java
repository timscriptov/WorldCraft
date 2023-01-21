package com.solverlabs.droid.rugl.util.geom;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.util.math.Range;

/**
 * Axis aligned cuboid
 */
public class BoundingCuboid extends BoundingRectangle {
    /**
     * Extent on the z-axis
     */
    public final Range z = new Range(0, 0);

    /**
     * @param x
     * @param y
     * @param z
     * @param width
     * @param height
     * @param depth
     */
    public BoundingCuboid(float x, float y, float z, float width, float height,
                          float depth) {
        super(x, y, width, height);
        this.z.set(z, z + depth);
    }

    /**
     * Copy constructor
     *
     * @param c
     */
    public BoundingCuboid(BoundingCuboid c) {
        super(c);
        z.set(c.z);
    }

    /**
     * @param minX
     * @param minY
     * @param minZ
     * @param maxX
     * @param maxY
     * @param maxZ
     */
    public void set(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        x.set(minX, maxX);
        y.set(minY, maxY);
        z.set(minZ, maxZ);
    }

    public void set(float fx, float fy, float fz) {
        super.set(fx, this.x.getSpan() + fx, fy, this.y.getSpan() + fy);
        this.z.set(fz);
    }

    /**
     * Alters this {@link BoundingRectangle} as necessary to contain
     * the specified point
     *
     * @param px
     * @param py
     * @param pz
     */
    public void encompass(float px, float py, float pz) {
        super.encompass(px, py);
        z.encompass(pz);
    }

    /**
     * Alters this {@link BoundingRectangle} to entirely encompass
     * another
     *
     * @param c
     */
    public void encompass(BoundingCuboid c) {
        super.encompass(c);
        z.encompass(c.z);
    }

    /**
     * Determines if this {@link BoundingRectangle} contains the
     * supplied point
     *
     * @param px
     * @param py
     * @param pz
     * @return <code>true</code> if the point lies within this
     * {@link BoundingRectangle}'s volume, <code>false</code>
     * otherwise
     */
    public boolean contains(float px, float py, float pz) {
        return super.contains(px, py) && z.contains(pz);
    }

    /**
     * @param b
     * @return <code>true</code> if this and b intersect
     */
    public boolean intersects(BoundingCuboid b) {
        return super.intersects(b) && z.intersects(b.z);
    }

    /**
     * @param b
     * @param dest
     * @return true if the intersection exists
     */
    public boolean intersection(BoundingCuboid b, BoundingCuboid dest) {
        if (intersects(b)) {
            x.intersection(b.x, dest.x);
            y.intersection(b.y, dest.y);
            z.intersection(b.z, dest.z);
            return true;
        }

        return false;
    }

    /**
     * Shifts this cuboid
     *
     * @param dx
     * @param dy
     * @param dz
     */
    public void translate(float dx, float dy, float dz) {
        super.translate(dx, dy);
        z.translate(dz);
    }

    /**
     * Scales this cuboid around the origin
     *
     * @param sx
     * @param sy
     * @param sz
     */
    public void scale(float sx, float sy, float sz) {
        super.scale(sx, sy);
        z.scale(sz);
    }

    @NonNull
    @Override
    public String toString() {
        return "( " + x.getMin() +
                ", " +
                y.getMin() +
                ", " +
                z.getMin() +
                " ) [ " +
                x.getSpan() +
                " x " +
                y.getSpan() +
                " x " +
                z.getSpan() +
                " ]";
    }
}
