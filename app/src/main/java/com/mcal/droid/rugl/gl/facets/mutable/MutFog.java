package com.mcal.droid.rugl.gl.facets.mutable;

import androidx.annotation.NonNull;

import com.mcal.droid.rugl.gl.enums.FogMode;
import com.mcal.droid.rugl.gl.facets.Fog;


public class MutFog extends MutableFacet<Fog> {
    public int colour;
    public float density;
    public boolean enabled;
    public float end;
    public FogMode mode;
    public float start;

    public MutFog(@NonNull Fog f) {
        this.enabled = false;
        this.mode = FogMode.EXP;
        this.density = 1.0f;
        this.start = 0.0f;
        this.end = 1.0f;
        this.colour = 0;
        this.enabled = f.enabled;
        this.mode = f.mode;
        this.density = f.density;
        this.start = f.start;
        this.end = f.end;
        this.colour = f.colour;
    }

    @Override
    public Fog compile() {
        return this.enabled ? new Fog(this.mode, this.density, this.start, this.end, this.colour) : Fog.disabled;
    }
}
