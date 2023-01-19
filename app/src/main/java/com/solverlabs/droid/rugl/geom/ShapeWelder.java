package com.solverlabs.droid.rugl.geom;

import java.util.LinkedList;


public class ShapeWelder<T extends Shape> {
    protected LinkedList<T> shapes = new LinkedList<>();
    protected int vertexCount = 0;
    protected int triangleCount = 0;

    public static <T extends Shape> Shape fuse(T... shapes) {
        ShapeWelder<T> sb = new ShapeWelder<>();
        for (T t : shapes) {
            sb.addShape(t);
        }
        return sb.mo82fuse();
    }

    public boolean addShape(T s) {
        this.shapes.add(s);
        this.vertexCount += s.vertexCount();
        this.triangleCount += s.indices.length;
        return true;
    }

    public void clear() {
        this.shapes.clear();
        this.vertexCount = 0;
        this.triangleCount = 0;
    }

    /* renamed from: fuse */
    public Shape mo82fuse() {
        float[] verts = new float[this.vertexCount * 3];
        short[] tris = new short[this.triangleCount];
        int vi = 0;
        int ti = 0;
        while (!this.shapes.isEmpty()) {
            Shape s = this.shapes.removeFirst();
            System.arraycopy(s.vertices, 0, verts, vi, s.vertices.length);
            System.arraycopy(s.indices, 0, tris, ti, s.indices.length);
            for (int i = 0; i < s.indices.length; i++) {
                int i2 = ti + i;
                tris[i2] = (short) (tris[i2] + (vi / 3));
            }
            vi += s.vertices.length;
            ti += s.indices.length;
        }
        clear();
        return new Shape(verts, tris);
    }
}
