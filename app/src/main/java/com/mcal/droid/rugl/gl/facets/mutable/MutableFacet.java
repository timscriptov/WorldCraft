package com.mcal.droid.rugl.gl.facets.mutable;

import com.mcal.droid.rugl.gl.Facet;


public abstract class MutableFacet<T extends Facet> {
    public abstract T compile();
}
