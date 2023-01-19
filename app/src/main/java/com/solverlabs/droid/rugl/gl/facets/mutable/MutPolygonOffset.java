package com.solverlabs.droid.rugl.gl.facets.mutable;

import com.solverlabs.droid.rugl.gl.facets.PolygonOffset;


public class MutPolygonOffset extends MutableFacet<PolygonOffset> {
    public boolean enabled;
    public float factor;
    public float units;

    public MutPolygonOffset(PolygonOffset po) {
        this.enabled = po.enabled;
        this.factor = po.factor;
        this.units = po.units;
    }

    @Override
    public PolygonOffset compile() {
        return this.enabled ? new PolygonOffset(this.factor, this.units) : PolygonOffset.disabled;
    }
}
