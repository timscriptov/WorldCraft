package com.solverlabs.droid.rugl.gl;

import com.solverlabs.droid.rugl.gl.enums.DrawMode;
import com.solverlabs.droid.rugl.gl.facets.mutable.MutAlphaTest;
import com.solverlabs.droid.rugl.gl.facets.mutable.MutBlend;
import com.solverlabs.droid.rugl.gl.facets.mutable.MutDepthTest;
import com.solverlabs.droid.rugl.gl.facets.mutable.MutFog;
import com.solverlabs.droid.rugl.gl.facets.mutable.MutPolygonOffset;
import com.solverlabs.droid.rugl.gl.facets.mutable.MutTextureState;


public class MutableState {
    public final MutAlphaTest alphaTest;
    public final MutBlend blend;
    public final MutDepthTest depthTest;
    public final MutFog fog;
    public final MutPolygonOffset polyOffset;
    public final MutTextureState texture;
    public boolean dirty = false;
    public DrawMode drawMode;

    public MutableState(State state) {
        this.drawMode = state.drawMode;
        this.texture = new MutTextureState(state.texture);
        this.alphaTest = new MutAlphaTest(state.alphaTest);
        this.blend = new MutBlend(state.blend);
        this.depthTest = new MutDepthTest(state.depthTest);
        this.polyOffset = new MutPolygonOffset(state.polyOffset);
        this.fog = new MutFog(state.fog);
    }

    public State compile() {
        return new State(this.drawMode, this.texture.compile(), this.alphaTest.compile(), this.blend.compile(), this.depthTest.compile(), this.polyOffset.compile(), this.fog.compile());
    }
}
