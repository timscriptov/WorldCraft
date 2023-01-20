package com.solverlabs.droid.rugl.gl.facets;

import android.opengl.GLES10;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.gl.Facet;
import com.solverlabs.droid.rugl.gl.enums.ComparisonFunction;


public class AlphaTest extends Facet<AlphaTest> {
    public static final AlphaTest disabled = new AlphaTest();
    public final boolean enabled;
    public final ComparisonFunction func;
    public final float ref;

    private AlphaTest() {
        this.enabled = false;
        this.func = ComparisonFunction.ALWAYS;
        this.ref = 0.0f;
    }

    public AlphaTest(ComparisonFunction func, float ref) {
        this.enabled = true;
        this.func = func;
        this.ref = ref;
    }

    @Override
    public void transitionFrom(AlphaTest a) {
        if (this.enabled && !a.enabled) {
            GLES10.glEnable(3008);
        } else if (!this.enabled && a.enabled) {
            GLES10.glDisable(3008);
        }
        if (this.enabled) {
            if (this.func != a.func || this.ref != a.ref) {
                GLES10.glAlphaFunc(this.func.value, this.ref);
            }
        }
    }

    @Override
    public int compareTo(@NonNull AlphaTest a) {
        int i = 1;
        int i2 = this.enabled ? 1 : 0;
        if (!a.enabled) {
            i = 0;
        }
        float d = i2 - i;
        if (d == 0.0f) {
            d = this.func.ordinal() - a.func.ordinal();
            if (d == 0.0f) {
                d = this.ref - a.ref;
            }
        }
        return (int) Math.signum(d);
    }

    @NonNull
    public String toString() {
        return "Alpha test " + this.enabled + " " + this.func + " " + this.ref;
    }
}
