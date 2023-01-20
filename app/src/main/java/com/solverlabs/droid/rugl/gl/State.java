package com.solverlabs.droid.rugl.gl;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.Game;
import com.solverlabs.droid.rugl.gl.enums.DrawMode;
import com.solverlabs.droid.rugl.gl.enums.MagFilter;
import com.solverlabs.droid.rugl.gl.enums.MinFilter;
import com.solverlabs.droid.rugl.gl.facets.AlphaTest;
import com.solverlabs.droid.rugl.gl.facets.Blend;
import com.solverlabs.droid.rugl.gl.facets.DepthTest;
import com.solverlabs.droid.rugl.gl.facets.Fog;
import com.solverlabs.droid.rugl.gl.facets.PolygonOffset;
import com.solverlabs.droid.rugl.gl.facets.TextureState;


public class State implements Comparable<State> {
    private static State currentState;

    static {
        Game.addSurfaceLIstener(new Game.SurfaceListener() {
            @Override
            public void onSurfaceCreated() {
                State.stateReset();
            }
        });
        currentState = new State();
    }

    public final AlphaTest alphaTest;
    public final Blend blend;
    public final DepthTest depthTest;
    public final DrawMode drawMode;
    public final Fog fog;
    public final PolygonOffset polyOffset;
    public final TextureState texture;
    private final Facet[] facets;
    int compilationBatch;
    int compiledIndex;

    public State() {
        this(DrawMode.Triangles, TextureState.disabled, AlphaTest.disabled, Blend.disabled, DepthTest.disabled, PolygonOffset.disabled, Fog.disabled);
    }

    public State(DrawMode drawMode, TextureState texture, AlphaTest alphaTest, Blend blend, DepthTest depthTest, PolygonOffset polyOffset, Fog fog) {
        this.compilationBatch = -1;
        this.compiledIndex = -1;
        this.texture = texture;
        this.drawMode = drawMode;
        this.alphaTest = alphaTest;
        this.blend = blend;
        this.depthTest = depthTest;
        this.polyOffset = polyOffset;
        this.fog = fog;
        this.facets = new Facet[]{texture, alphaTest, blend, depthTest, polyOffset, fog};
    }

    public static State getCurrentState() {
        return currentState;
    }

    public static void stateReset() {
        currentState = new State();
    }

    public State with(TextureState texture) {
        return new State(this.drawMode, texture, this.alphaTest, this.blend, this.depthTest, this.polyOffset, this.fog);
    }

    public State with(DrawMode drawMode) {
        return new State(drawMode, this.texture, this.alphaTest, this.blend, this.depthTest, this.polyOffset, this.fog);
    }

    public State with(AlphaTest alphaTest) {
        return new State(this.drawMode, this.texture, alphaTest, this.blend, this.depthTest, this.polyOffset, this.fog);
    }

    public State with(Blend blend) {
        return new State(this.drawMode, this.texture, this.alphaTest, blend, this.depthTest, this.polyOffset, this.fog);
    }

    public State with(DepthTest depthTest) {
        return new State(this.drawMode, this.texture, this.alphaTest, this.blend, depthTest, this.polyOffset, this.fog);
    }

    public State with(PolygonOffset polyOffset) {
        return new State(this.drawMode, this.texture, this.alphaTest, this.blend, this.depthTest, polyOffset, this.fog);
    }

    public State with(Fog fog) {
        return new State(this.drawMode, this.texture, this.alphaTest, this.blend, this.depthTest, this.polyOffset, fog);
    }

    public State with(MinFilter min, MagFilter mag) {
        return new State(this.drawMode, this.texture.with(new TextureState.Filters(min, mag)), this.alphaTest, this.blend, this.depthTest, this.polyOffset, this.fog);
    }

    public State withTexture(int id) {
        return id != this.texture.id ? new State(this.drawMode, this.texture.with(id), this.alphaTest, this.blend, this.depthTest, this.polyOffset, this.fog) : this;
    }

    public void apply() {
        if (currentState != this) {
            for (int i = 0; i < this.facets.length; i++) {
                if (this.facets[i] != null) {
                    this.facets[i].transitionFrom(currentState.facets[i]);
                }
            }
            currentState = this;
        }
    }

    public int getCompilationBatch() {
        return this.compilationBatch;
    }

    public int getCompiledIndex() {
        return this.compiledIndex;
    }

    @Override
    public int compareTo(State o) {
        return (this.compilationBatch < 0 || this.compilationBatch != o.compilationBatch) ? deepCompare(o) : this.compiledIndex - o.compiledIndex;
    }

    private int deepCompare(State o) {
        for (int i = 0; i < this.facets.length; i++) {
            int d = this.facets[i].compareTo(o.facets[i]);
            if (d != 0) {
                return d;
            }
        }
        return 0;
    }

    public boolean equals(Object o) {
        return (o instanceof State) && compareTo((State) o) == 0;
    }

    @NonNull
    public String toString() {
        StringBuilder buff = new StringBuilder("RenderState");
        for (int i = 0; i < this.facets.length; i++) {
            if (this.facets[i] != null) {
                buff.append("\n\t").append(this.facets[i]);
            }
        }
        return buff.toString();
    }
}
