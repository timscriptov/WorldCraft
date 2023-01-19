package com.solverlabs.droid.rugl.util.geom;

import com.solverlabs.droid.rugl.util.math.Range;


public class BoundingCuboid extends BoundingRectangle {
    public final Range z;

    public BoundingCuboid(float x, float y, float z, float width, float height, float depth) {
        super(x, y, width, height);
        this.z = new Range(0.0f, 0.0f);
        this.z.set(z, z + depth);
    }

    public BoundingCuboid(BoundingCuboid c) {
        super(c);
        this.z = new Range(0.0f, 0.0f);
        this.z.set(c.z);
    }

    public void set(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        this.x.set(minX, maxX);
        this.y.set(minY, maxY);
        this.z.set(minZ, maxZ);
    }

    public void encompass(float px, float py, float pz) {
        super.encompass(px, py);
        this.z.encompass(pz);
    }

    public void encompass(BoundingCuboid c) {
        super.encompass((BoundingRectangle) c);
        this.z.encompass(c.z);
    }

    public boolean contains(float px, float py, float pz) {
        return super.contains(px, py) && this.z.contains(pz);
    }

    public boolean intersects(BoundingCuboid b) {
        return super.intersects((BoundingRectangle) b) && this.z.intersects(b.z);
    }

    public boolean intersection(BoundingCuboid b, BoundingCuboid dest) {
        if (intersects(b)) {
            this.x.intersection(b.x, dest.x);
            this.y.intersection(b.y, dest.y);
            this.z.intersection(b.z, dest.z);
            return true;
        }
        return false;
    }

    public void translate(float dx, float dy, float dz) {
        super.translate(dx, dy);
        this.z.translate(dz);
    }

    public void set(float fx, float fy, float fz) {
        super.set(fx, this.x.getSpan() + fx, fy, this.y.getSpan() + fy);
        this.z.set(fz);
    }

    public void scale(float sx, float sy, float sz) {
        super.scale(sx, sy);
        this.z.scale(sz);
    }

    @Override
    public String toString() {
        return "( " + this.x.getMin() + ", " + this.y.getMin() + ", " + this.z.getMin() + " ) [ " + this.x.getSpan() + " x " + this.y.getSpan() + " x " + this.z.getSpan() + " ]";
    }
}
