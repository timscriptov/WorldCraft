package com.mcal.droid.rugl.geom;

import androidx.annotation.NonNull;

import com.mcal.droid.rugl.gl.Renderer;
import com.mcal.droid.rugl.gl.State;
import com.mcal.droid.rugl.util.FastFloatBuffer;


public class CompiledShape {
    private final int[] colours;
    private final int[] texCoords;
    private final short[] triangles;
    private final int[] vertices;
    public State state;

    public CompiledShape(@NonNull TexturedShape ts) {
        this.vertices = FastFloatBuffer.convert(ts.vertices);
        this.triangles = ts.indices.clone();
        this.colours = ts.colours.clone();
        this.texCoords = FastFloatBuffer.convert(ts.texCoords);
        this.state = ts.state;
    }

    public CompiledShape(@NonNull ColouredShape cs) {
        this.vertices = FastFloatBuffer.convert(cs.vertices);
        this.triangles = cs.indices.clone();
        this.colours = cs.colours.clone();
        this.texCoords = null;
        this.state = cs.state;
    }

    public void render(@NonNull Renderer r) {
        this.state = r.intern(this.state);
        r.addGeometry(this.vertices, this.texCoords, this.colours, this.triangles, this.state);
    }

    public int bytes() {
        return (this.vertices.length + this.colours.length + this.texCoords.length) * 4 * 2 * this.triangles.length;
    }
}
