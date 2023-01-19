package com.solverlabs.droid.rugl.gl.facets.mutable;

import com.solverlabs.droid.rugl.gl.enums.ComparisonFunction;
import com.solverlabs.droid.rugl.gl.facets.DepthTest;


public class MutDepthTest extends MutableFacet<DepthTest> {
    public boolean enabled;
    public ComparisonFunction func;

    public MutDepthTest(DepthTest dt) {
        this.enabled = dt.enabled;
        this.func = dt.func;
    }

    @Override
    public DepthTest compile() {
        return this.enabled ? new DepthTest(this.func) : DepthTest.disabled;
    }
}
