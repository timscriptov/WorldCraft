package com.solverlabs.droid.rugl.geom;

import com.solverlabs.droid.rugl.gl.State;


public class ColouredShapeWelder extends ShapeWelder<ColouredShape> {
    private State state = null;

    public static ColouredShape fuse(ColouredShape... cs) {
        ColouredShapeWelder csb = new ColouredShapeWelder();
        for (ColouredShape colouredShape : cs) {
            csb.addShape(colouredShape);
        }
        return csb.mo82fuse();
    }

    @Override
    public boolean addShape(ColouredShape s) {
        if (this.state == null) {
            this.state = s.state;
        }
        if (this.state.equals(s.state)) {
            return super.addShape(s);
        }
        return false;
    }

    @Override
    /* renamed from: fuse */
    public ColouredShape mo82fuse() {
        float[] verts = new float[this.vertexCount * 3];
        short[] tris = new short[this.triangleCount];
        int[] colours = new int[this.vertexCount];
        int vi = 0;
        int ti = 0;
        int ci = 0;
        while (!this.shapes.isEmpty()) {
            ColouredShape s = (ColouredShape) this.shapes.removeFirst();
            System.arraycopy(s.vertices, 0, verts, vi, s.vertices.length);
            System.arraycopy(s.colours, 0, colours, ci, s.colours.length);
            System.arraycopy(s.indices, 0, tris, ti, s.indices.length);
            for (int i = 0; i < s.indices.length; i++) {
                int i2 = ti + i;
                tris[i2] = (short) (tris[i2] + (vi / 3));
            }
            vi += s.vertices.length;
            ti += s.indices.length;
            ci += s.colours.length;
        }
        clear();
        return new ColouredShape(new Shape(verts, tris), colours, this.state);
    }

    @Override
    public void clear() {
        super.clear();
        this.state = null;
    }
}
