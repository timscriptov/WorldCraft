package com.mcal.droid.rugl.gl.facets.mutable;

import androidx.annotation.NonNull;

import com.mcal.droid.rugl.gl.enums.ComparisonFunction;
import com.mcal.droid.rugl.gl.facets.DepthTest;


public class MutDepthTest extends MutableFacet<DepthTest> {
    public boolean enabled;
    public ComparisonFunction func;

    public MutDepthTest(@NonNull DepthTest dt) {
        this.enabled = dt.enabled;
        this.func = dt.func;
    }

    @Override
    public DepthTest compile() {
        return this.enabled ? new DepthTest(this.func) : DepthTest.disabled;
    }
}
