package com.solverlabs.droid.rugl.util.geom;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.util.math.Range;

public class BoundingRectangle {
    public final Range x = new Range(0.0f, 0.0f);
    public final Range y = new Range(0.0f, 0.0f);

    public BoundingRectangle() {
    }

    public BoundingRectangle(float x, float y, float width, float height) {
        this.x.set(x, x + width);
        this.y.set(y, y + height);
    }

    public BoundingRectangle(@NonNull BoundingRectangle c) {
        this.x.set(c.x);
        this.y.set(c.y);
    }

    public void set(float minx, float maxx, float miny, float maxy) {
        this.x.set(minx, maxx);
        this.y.set(miny, maxy);
    }

    public void set(@NonNull BoundingRectangle r) {
        this.x.set(r.x);
        this.y.set(r.y);
    }

    public void encompass(float px, float py) {
        this.x.encompass(px);
        this.y.encompass(py);
    }

    public void encompass(@NonNull BoundingRectangle c) {
        this.x.encompass(c.x);
        this.y.encompass(c.y);
    }

    public boolean contains(float px, float py) {
        return this.x.contains(px) && this.y.contains(py);
    }

    public boolean intersects(@NonNull BoundingRectangle b) {
        return this.x.intersects(b.x) && this.y.intersects(b.y);
    }

    public boolean intersection(BoundingRectangle b, BoundingRectangle dest) {
        if (intersects(b)) {
            this.x.intersection(b.x, dest.x);
            this.y.intersection(b.y, dest.y);
            return true;
        }
        return false;
    }

    @NonNull
    public String toString() {
        return "( " + this.x.getMin() + ", " + this.y.getMin() + " ) [ " + this.x.getSpan() + " x " + this.y.getSpan() + " ]";
    }

    public void translate(float dx, float dy) {
        this.x.translate(dx);
        this.y.translate(dy);
    }

    private void set(float fx, float fy) {
        this.x.set(fx);
        this.y.set(fy);
    }

    public void scale(float sx, float sy) {
        this.x.scale(sx);
        this.x.scale(sy);
    }
}
