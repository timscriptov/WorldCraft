package com.solverlabs.droid.rugl.gl.facets.mutable;

import com.solverlabs.droid.rugl.gl.enums.MagFilter;
import com.solverlabs.droid.rugl.gl.enums.MinFilter;
import com.solverlabs.droid.rugl.gl.enums.TextureWrap;
import com.solverlabs.droid.rugl.gl.facets.TextureState;


public class MutTextureState extends MutableFacet<TextureState> {
    public final MutFilters filter;
    public final MutWrapParameters wrap;
    public int id;

    public MutTextureState(TextureState ts) {
        this.id = ts.id;
        this.filter = new MutFilters(ts.filter);
        this.wrap = new MutWrapParameters(ts.wrap);
    }

    @Override
    public TextureState compile() {
        return new TextureState(this.id, this.filter.compile(), this.wrap.compile());
    }


    public static class MutFilters extends MutableFacet<TextureState.Filters> {
        public MagFilter mag;
        public MinFilter min;

        public MutFilters(TextureState.Filters f) {
            this.min = f.min;
            this.mag = f.mag;
        }

        @Override
        public TextureState.Filters compile() {
            return new TextureState.Filters(this.min, this.mag);
        }
    }


    public static class MutWrapParameters extends MutableFacet<TextureState.WrapParameters> {
        public TextureWrap s;
        public TextureWrap t;

        public MutWrapParameters(TextureState.WrapParameters wp) {
            this.s = wp.s;
            this.t = wp.t;
        }

        @Override
        public TextureState.WrapParameters compile() {
            return new TextureState.WrapParameters(this.s, this.t);
        }
    }
}
