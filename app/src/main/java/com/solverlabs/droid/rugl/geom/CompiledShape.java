package com.solverlabs.droid.rugl.geom;

import com.solverlabs.droid.rugl.gl.Renderer;
import com.solverlabs.droid.rugl.gl.State;
import com.solverlabs.droid.rugl.util.FastFloatBuffer;


public class CompiledShape {
    private final int[] colours;
    private final int[] texCoords;
    private final short[] triangles;
    private final int[] vertices;
    public State state;

    public CompiledShape(TexturedShape ts) {
        this.vertices = FastFloatBuffer.convert(ts.vertices);
        this.triangles = (short[]) ts.indices.clone();
        this.colours = (int[]) ts.colours.clone();
        this.texCoords = FastFloatBuffer.convert(ts.mTexCoords);
        this.state = ts.state;
    }

    public CompiledShape(ColouredShape cs) {
        this.vertices = FastFloatBuffer.convert(cs.vertices);
        this.triangles = (short[]) cs.indices.clone();
        this.colours = (int[]) cs.colours.clone();
        this.texCoords = null;
        this.state = cs.state;
    }

    public void render(Renderer r) {
        this.state = r.intern(this.state);
        r.addGeometry(this.vertices, this.texCoords, this.colours, this.triangles, this.state);
    }

    public int bytes() {
        return (this.vertices.length + this.colours.length + this.texCoords.length) * 4 * 2 * this.triangles.length;
    }
}
