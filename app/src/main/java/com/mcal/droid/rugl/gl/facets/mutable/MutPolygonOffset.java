package com.mcal.droid.rugl.gl.facets.mutable;

import androidx.annotation.NonNull;

import com.mcal.droid.rugl.gl.facets.PolygonOffset;


public class MutPolygonOffset extends MutableFacet<PolygonOffset> {
    public boolean enabled;
    public float factor;
    public float units;

    public MutPolygonOffset(@NonNull PolygonOffset po) {
        this.enabled = po.enabled;
        this.factor = po.factor;
        this.units = po.units;
    }

    @Override
    public PolygonOffset compile() {
        return this.enabled ? new PolygonOffset(this.factor, this.units) : PolygonOffset.disabled;
    }
}
