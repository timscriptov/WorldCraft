package com.solverlabs.droid.rugl.gl.facets;

import android.opengl.GLES10;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.gl.Facet;
import com.solverlabs.droid.rugl.gl.enums.DestinationFactor;
import com.solverlabs.droid.rugl.gl.enums.SourceFactor;


public class Blend extends Facet<Blend> {
    public static final Blend disabled = new Blend();
    public final DestinationFactor destFactor;
    public final boolean enabled;
    public final SourceFactor srcFactor;

    private Blend() {
        this.enabled = false;
        this.srcFactor = SourceFactor.ONE;
        this.destFactor = DestinationFactor.ZERO;
    }

    public Blend(SourceFactor srcFactor, DestinationFactor destFactor) {
        this.enabled = true;
        this.srcFactor = srcFactor;
        this.destFactor = destFactor;
    }

    @Override
    public void transitionFrom(Blend b) {
        if (this.enabled && !b.enabled) {
            GLES10.glEnable(3042);
        } else if (!this.enabled && b.enabled) {
            GLES10.glDisable(3042);
        }
        if (this.enabled) {
            GLES10.glBlendFunc(this.srcFactor.value, this.destFactor.value);
        }
    }

    @Override
    public int compareTo(Blend b) {
        int i = 1;
        if (this.enabled || b.enabled) {
            int i2 = this.enabled ? 1 : 0;
            if (!b.enabled) {
                i = 0;
            }
            int d = i2 - i;
            if (d == 0 && (d = this.srcFactor.ordinal() - b.srcFactor.ordinal()) == 0) {
                d = this.destFactor.ordinal() - b.destFactor.ordinal();
            }
            return d;
        }
        return 0;
    }

    @NonNull
    public String toString() {
        return "Blending " + this.enabled + " src:" + this.srcFactor + " dest:" + this.destFactor;
    }
}
