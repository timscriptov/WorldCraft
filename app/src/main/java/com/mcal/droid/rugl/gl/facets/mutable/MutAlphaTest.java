package com.mcal.droid.rugl.gl.facets.mutable;

import androidx.annotation.NonNull;

import com.mcal.droid.rugl.gl.enums.ComparisonFunction;
import com.mcal.droid.rugl.gl.facets.AlphaTest;


public class MutAlphaTest extends MutableFacet<AlphaTest> {
    public boolean enabled;
    public ComparisonFunction func;
    public float ref;

    public MutAlphaTest(@NonNull AlphaTest at) {
        this.enabled = at.enabled;
        this.func = at.func;
        this.ref = at.ref;
    }

    @Override
    public AlphaTest compile() {
        return this.enabled ? new AlphaTest(this.func, this.ref) : AlphaTest.disabled;
    }
}
