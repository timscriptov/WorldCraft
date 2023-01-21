package com.solverlabs.droid.rugl.geom;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.gl.State;

import java.util.Arrays;


public class ShapeBuilder {
    public int triangleOffset;
    public int vertexCount = 0;
    public int vertexOffset = 0;
    public float[] vertices = new float[30];
    public int texCoordOffset = 0;
    public float[] texCoords = new float[20];
    public int colourOffset = 0;
    public int[] colours = new int[10];
    public short[] triangles = new short[30];

    @NonNull
    private static int[] grow(@NonNull int[] in) {
        int[] na = new int[in.length * 2];
        System.arraycopy(in, 0, na, 0, in.length);
        return na;
    }

    @NonNull
    private static float[] grow(@NonNull float[] in) {
        float[] na = new float[in.length * 2];
        System.arraycopy(in, 0, na, 0, in.length);
        return na;
    }

    @NonNull
    private static short[] grow(@NonNull short[] in) {
        short[] na = new short[in.length * 2];
        System.arraycopy(in, 0, na, 0, in.length);
        return na;
    }

    public void vertex(float x, float y, float z, int c, float u, float v) {
        this.vertexCount++;
        ensureCapacity(1, 0);
        float[] fArr = this.vertices;
        int i = this.vertexOffset;
        this.vertexOffset = i + 1;
        fArr[i] = x;
        float[] fArr2 = this.vertices;
        int i2 = this.vertexOffset;
        this.vertexOffset = i2 + 1;
        fArr2[i2] = y;
        float[] fArr3 = this.vertices;
        int i3 = this.vertexOffset;
        this.vertexOffset = i3 + 1;
        fArr3[i3] = z;
        int[] iArr = this.colours;
        int i4 = this.colourOffset;
        this.colourOffset = i4 + 1;
        iArr[i4] = c;
        float[] fArr4 = this.texCoords;
        int i5 = this.texCoordOffset;
        this.texCoordOffset = i5 + 1;
        fArr4[i5] = u;
        float[] fArr5 = this.texCoords;
        int i6 = this.texCoordOffset;
        this.texCoordOffset = i6 + 1;
        fArr5[i6] = v;
    }

    public void triangle(short a, short b, short c) {
        ensureCapacity(0, 1);
        short[] sArr = this.triangles;
        int i = this.triangleOffset;
        this.triangleOffset = i + 1;
        sArr[i] = a;
        short[] sArr2 = this.triangles;
        int i2 = this.triangleOffset;
        this.triangleOffset = i2 + 1;
        sArr2[i2] = b;
        short[] sArr3 = this.triangles;
        int i3 = this.triangleOffset;
        this.triangleOffset = i3 + 1;
        sArr3[i3] = c;
    }

    public void relTriangle(int a, int b, int c) {
        ensureCapacity(0, 1);
        short[] sArr = this.triangles;
        int i = this.triangleOffset;
        this.triangleOffset = i + 1;
        sArr[i] = (short) (this.vertexCount + a);
        short[] sArr2 = this.triangles;
        int i2 = this.triangleOffset;
        this.triangleOffset = i2 + 1;
        sArr2[i2] = (short) (this.vertexCount + b);
        short[] sArr3 = this.triangles;
        int i3 = this.triangleOffset;
        this.triangleOffset = i3 + 1;
        sArr3[i3] = (short) (this.vertexCount + c);
    }

    public void ensureCapacity(int verts, int tris) {
        int vleft = this.colours.length - this.colourOffset;
        while (vleft < verts) {
            this.vertices = grow(this.vertices);
            this.texCoords = grow(this.texCoords);
            this.colours = grow(this.colours);
            vleft = (this.vertices.length - this.vertexOffset) / 3;
        }
        int tleft = this.triangles.length - this.triangleOffset;
        while (tleft < tris * 3) {
            this.triangles = grow(this.triangles);
            tleft = (this.triangles.length - this.triangleOffset) / 3;
        }
    }

    public void clear() {
        this.vertexCount = 0;
        this.vertexOffset = 0;
        this.colourOffset = 0;
        this.texCoordOffset = 0;
        this.triangleOffset = 0;
        Arrays.fill(this.vertices, 0.0f);
        Arrays.fill(this.texCoords, 0.0f);
        Arrays.fill(this.triangles, (short) 0);
        Arrays.fill(this.colours, 0);
    }

    public ShapeBuilder clone() {
        ShapeBuilder clone = new ShapeBuilder();
        clone.vertexCount = this.vertexCount;
        clone.vertexOffset = this.vertexOffset;
        clone.colourOffset = this.colourOffset;
        clone.texCoordOffset = this.texCoordOffset;
        clone.triangleOffset = this.triangleOffset;
        clone.vertices = new float[this.vertices.length];
        System.arraycopy(this.vertices, 0, clone.vertices, 0, this.vertices.length);
        clone.texCoords = new float[this.texCoords.length];
        System.arraycopy(this.texCoords, 0, clone.texCoords, 0, this.texCoords.length);
        clone.triangles = new short[this.triangles.length];
        System.arraycopy(this.triangles, 0, clone.triangles, 0, this.triangles.length);
        clone.colours = new int[this.colours.length];
        System.arraycopy(this.colours, 0, clone.colours, 0, this.colours.length);
        return clone;
    }

    public TexturedShape compile() {
        if (this.vertexCount > 3) {
            float[] verts = new float[this.vertexOffset];
            System.arraycopy(this.vertices, 0, verts, 0, verts.length);
            float[] txc = new float[this.texCoordOffset];
            System.arraycopy(this.texCoords, 0, txc, 0, txc.length);
            int[] col = new int[this.colourOffset];
            System.arraycopy(this.colours, 0, col, 0, col.length);
            short[] tris = new short[this.triangleOffset];
            System.arraycopy(this.triangles, 0, tris, 0, tris.length);
            clear();
            Shape s = new Shape(verts, tris);
            ColouredShape cs = new ColouredShape(s, col, (State) null);
            return new TexturedShape(cs, txc, null);
        }
        clear();
        return null;
    }
}
