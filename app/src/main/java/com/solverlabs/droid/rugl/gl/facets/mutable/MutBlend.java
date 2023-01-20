package com.solverlabs.droid.rugl.gl.facets.mutable;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.gl.enums.DestinationFactor;
import com.solverlabs.droid.rugl.gl.enums.SourceFactor;
import com.solverlabs.droid.rugl.gl.facets.Blend;


public class MutBlend extends MutableFacet<Blend> {
    public DestinationFactor destFactor;
    public boolean enabled;
    public SourceFactor srcFactor;

    public MutBlend(@NonNull Blend b) {
        this.enabled = b.enabled;
        this.srcFactor = b.srcFactor;
        this.destFactor = b.destFactor;
    }

    @Override
    public Blend compile() {
        return this.enabled ? new Blend(this.srcFactor, this.destFactor) : Blend.disabled;
    }
}
