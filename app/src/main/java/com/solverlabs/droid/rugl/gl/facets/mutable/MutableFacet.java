package com.solverlabs.droid.rugl.gl.facets.mutable;

import com.solverlabs.droid.rugl.gl.Facet;


public abstract class MutableFacet<T extends Facet> {
    public abstract T compile();
}
