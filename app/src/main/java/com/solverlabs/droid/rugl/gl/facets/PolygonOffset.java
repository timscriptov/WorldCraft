package com.solverlabs.droid.rugl.gl.facets;

import android.opengl.GLES10;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.gl.Facet;


public class PolygonOffset extends Facet<PolygonOffset> {
    public static final PolygonOffset disabled = new PolygonOffset();
    public final boolean enabled;
    public final float factor;
    public final float units;

    private PolygonOffset() {
        this.enabled = false;
        this.factor = 0.0f;
        this.units = 0.0f;
    }

    public PolygonOffset(float factor, float units) {
        this.enabled = true;
        this.factor = factor;
        this.units = units;
    }

    @Override
    public void transitionFrom(@NonNull PolygonOffset po) {
        if (this.enabled != po.enabled) {
            if (this.enabled) {
                GLES10.glEnable(32823);
            } else {
                GLES10.glDisable(32823);
            }
        }
        if (this.factor != po.factor || this.units != po.units) {
            GLES10.glPolygonOffset(this.factor, this.units);
        }
    }

    @Override
    public int compareTo(@NonNull PolygonOffset po) {
        int f = this.enabled ? 1 : 0;
        int pof = po.enabled ? 1 : 0;
        float d = f - pof;
        if (d == 0.0f) {
            d = this.factor - po.factor;
            if (d == 0.0f) {
                d = this.units - po.units;
            }
        }
        return (int) Math.signum(d);
    }

    @NonNull
    public String toString() {
        return "Polygon offset: " + (this.enabled ? "factor = " + this.factor + " units = " + this.units : "disabled ");
    }
}
