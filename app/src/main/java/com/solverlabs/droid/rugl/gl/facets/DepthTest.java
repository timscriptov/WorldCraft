package com.solverlabs.droid.rugl.gl.facets;

import android.opengl.GLES10;

import com.solverlabs.droid.rugl.gl.Facet;
import com.solverlabs.droid.rugl.gl.enums.ComparisonFunction;


public class DepthTest extends Facet<DepthTest> {
    public static final DepthTest disabled = new DepthTest();
    public final boolean enabled;
    public final ComparisonFunction func;

    private DepthTest() {
        this.enabled = false;
        this.func = ComparisonFunction.LESS;
    }

    public DepthTest(ComparisonFunction func) {
        this.enabled = true;
        this.func = func;
    }

    @Override
    public void transitionFrom(DepthTest d) {
        if (this.enabled && !d.enabled) {
            GLES10.glEnable(2929);
        } else if (!this.enabled && d.enabled) {
            GLES10.glDisable(2929);
        }
        if (this.enabled) {
            GLES10.glDepthFunc(this.func.value);
        }
    }

    @Override
    public int compareTo(DepthTest d) {
        int i = 1;
        if (this.enabled || d.enabled) {
            int i2 = this.enabled ? 1 : 0;
            if (!d.enabled) {
                i = 0;
            }
            int i3 = i2 - i;
            if (i3 == 0) {
                i3 = this.func.ordinal() - d.func.ordinal();
            }
            return i3;
        }
        return 0;
    }

    public String toString() {
        return "Depth test " + this.enabled + " func = " + this.func.value;
    }
}
