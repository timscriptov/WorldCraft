package com.solverlabs.droid.rugl.gl.facets.mutable;

import com.solverlabs.droid.rugl.gl.enums.ComparisonFunction;
import com.solverlabs.droid.rugl.gl.facets.AlphaTest;


public class MutAlphaTest extends MutableFacet<AlphaTest> {
    public boolean enabled;
    public ComparisonFunction func;
    public float ref;

    public MutAlphaTest(AlphaTest at) {
        this.enabled = at.enabled;
        this.func = at.func;
        this.ref = at.ref;
    }

    @Override
    public AlphaTest compile() {
        return this.enabled ? new AlphaTest(this.func, this.ref) : AlphaTest.disabled;
    }
}
