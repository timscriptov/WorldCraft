package com.solverlabs.worldcraft.skin.geometry;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.geom.ShapeBuilder;
import com.solverlabs.worldcraft.math.MathUtils;

import org.jetbrains.annotations.Contract;

import java.util.Arrays;

public class Parallelepiped {
    public final boolean opaque;
    private final float sxtn;
    public Face bottom;
    public Face east;
    public Face north;
    public Face south;
    public float[] texCoords;
    public Face top;
    public Face west;
    private float depth;
    private float height;
    private float width;

    public Parallelepiped(boolean opaque, Face north, Face south, Face east, Face west, Face top, Face bottom, float sxtn, Object tc) {
        this.north = north;
        this.south = south;
        this.east = east;
        this.west = west;
        this.top = top;
        this.bottom = bottom;
        this.sxtn = sxtn;
        float[] floatTc = getFloatTc(tc);
        this.opaque = opaque;
        if (floatTc.length != 6) {
            if (floatTc.length != 4) {
                if (floatTc.length != 2) {
                    this.texCoords = new float[floatTc.length];
                    for (int i = 0; i < floatTc.length; i++) {
                        this.texCoords[i] = floatTc[i];
                    }
                    return;
                }
                this.texCoords = new float[]{floatTc[0], floatTc[1], floatTc[0], floatTc[1], floatTc[0], floatTc[1], floatTc[0], floatTc[1], floatTc[0], floatTc[1], floatTc[0], floatTc[1]};
                return;
            }
            this.texCoords = new float[]{floatTc[0], floatTc[1], floatTc[0], floatTc[1], floatTc[0], floatTc[1], floatTc[0], floatTc[1], floatTc[2], floatTc[3], floatTc[2], floatTc[3]};
            return;
        }
        this.texCoords = new float[]{floatTc[0], floatTc[1], floatTc[0], floatTc[1], floatTc[0], floatTc[1], floatTc[0], floatTc[1], floatTc[2], floatTc[3], floatTc[4], floatTc[5]};
    }

    public Parallelepiped(boolean opaque, Face north, Face south, Face east, Face west, Face top, Face bottom, float sxtn) {
        this.north = north;
        this.south = south;
        this.east = east;
        this.west = west;
        this.top = top;
        this.bottom = bottom;
        this.sxtn = sxtn;
        this.opaque = opaque;
    }

    @NonNull
    public static Parallelepiped createParallelepiped(float width, float height, float depth, float stxtn, Object tc) {
        float[] nbl = {0.0f, 0.0f, 0.0f};
        float[] ntl = {0.0f, height, 0.0f};
        float[] nbr = {width, 0.0f, 0.0f};
        float[] ntr = {width, height, 0.0f};
        float[] fbl = {0.0f, 0.0f, depth};
        float[] ftl = {0.0f, height, depth};
        float[] fbr = {width, 0.0f, depth};
        float[] ftr = {width, height, depth};
        Face faceNorth = new Face(fbl, ftl, nbl, ntl);
        Face faceSouth = new Face(nbr, ntr, fbr, ftr);
        Face faceEast = new Face(nbl, ntl, nbr, ntr);
        Face faceWest = new Face(fbr, ftr, fbl, ftl);
        Face faceTop = new Face(ntl, ftl, ntr, ftr);
        Face faceBottom = new Face(nbl, fbl, nbr, fbr);
        Parallelepiped p = new Parallelepiped(true, faceNorth, faceSouth, faceEast, faceWest, faceTop, faceBottom, stxtn, tc);
        p.setWidth(width);
        p.setDepth(depth);
        p.setHeight(height);
        return p;
    }

    @NonNull
    public static Parallelepiped createParallelepiped(float width, float height, float depth, float stxtn) {
        float[] nbl = {0.0f, 0.0f, 0.0f};
        float[] ntl = {0.0f, height, 0.0f};
        float[] nbr = {width, 0.0f, 0.0f};
        float[] ntr = {width, height, 0.0f};
        float[] fbl = {0.0f, 0.0f, depth};
        float[] ftl = {0.0f, height, depth};
        float[] fbr = {width, 0.0f, depth};
        float[] ftr = {width, height, depth};
        Face faceNorth = new Face(fbl, ftl, nbl, ntl);
        Face faceSouth = new Face(nbr, ntr, fbr, ftr);
        Face faceEast = new Face(nbl, ntl, nbr, ntr);
        Face faceWest = new Face(fbr, ftr, fbl, ftl);
        Face faceTop = new Face(ntl, ftl, ntr, ftr);
        Face faceBottom = new Face(nbl, fbl, nbr, fbr);
        Parallelepiped p = new Parallelepiped(true, faceNorth, faceSouth, faceEast, faceWest, faceTop, faceBottom, stxtn);
        p.setWidth(width);
        p.setDepth(depth);
        p.setHeight(height);
        return p;
    }

    public void rotate(float angle, int axisx, int axisy, int axisz) {
        float[] nbl = {0.0f, 0.0f, 0.0f};
        rotateVertex(nbl, angle, axisx, axisy, axisz);
        float[] ntl = {0.0f, this.height, 0.0f};
        rotateVertex(ntl, angle, axisx, axisy, axisz);
        float[] nbr = {this.width, 0.0f, 0.0f};
        rotateVertex(nbr, angle, axisx, axisy, axisz);
        float[] ntr = {this.width, this.height, 0.0f};
        rotateVertex(ntr, angle, axisx, axisy, axisz);
        float[] fbl = {0.0f, 0.0f, this.depth};
        rotateVertex(fbl, angle, axisx, axisy, axisz);
        float[] ftl = {0.0f, this.height, this.depth};
        rotateVertex(ftl, angle, axisx, axisy, axisz);
        float[] fbr = {this.width, 0.0f, this.depth};
        rotateVertex(fbr, angle, axisx, axisy, axisz);
        float[] ftr = {this.width, this.height, this.depth};
        rotateVertex(ftr, angle, axisx, axisy, axisz);
        this.north = new Face(fbl, ftl, nbl, ntl);
        this.south = new Face(nbr, ntr, fbr, ftr);
        this.east = new Face(nbl, ntl, nbr, ntr);
        this.west = new Face(fbr, ftr, fbl, ftl);
        this.top = new Face(ntl, ftl, ntr, ftr);
        this.bottom = new Face(nbl, fbl, nbr, fbr);
    }

    private void rotateVertex(@NonNull float[] vertices, float angle, int axisx, int axisy, int axisz) {
        float c = MathUtils.cos(angle);
        float s = MathUtils.sin(angle);
        float oneminusc = 1.0f - c;
        float xy = axisx * axisy;
        float yz = axisy * axisz;
        float xz = axisx * axisz;
        float xs = axisx * s;
        float ys = axisy * s;
        float zs = axisz * s;
        for (int i = 0; i < vertices.length; i += 3) {
            vertices[i] = vertices[i] - (this.width / 2.0f);
            int i2 = i + 2;
            vertices[i2] = vertices[i2] - (this.depth / 2.0f);
            float tempX = vertices[i];
            float tempY = vertices[i + 1];
            float tempZ = vertices[i + 2];
            vertices[i] = (((axisx * axisx * oneminusc) + c) * tempX) + (((xy * oneminusc) - zs) * tempY) + (((xz * oneminusc) + ys) * tempZ);
            vertices[i + 1] = (((xy * oneminusc) + zs) * tempX) + (((axisy * axisy * oneminusc) + c) * tempY) + (((yz * oneminusc) - xs) * tempZ);
            vertices[i + 2] = (((xz * oneminusc) - ys) * tempX) + (((yz * oneminusc) + xs) * tempY) + (((axisz * axisz * oneminusc) + c) * tempZ);
            vertices[i] = vertices[i] + (this.width / 2.0f);
            int i3 = i + 2;
            vertices[i3] = vertices[i3] + (this.depth / 2.0f);
        }
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setDepth(float depth) {
        this.depth = depth;
    }

    public void setTexCoords(Object tc) {
        float[] floatTc = getFloatTc(tc);
        this.texCoords = new float[floatTc.length];
        for (int i = 0; i < floatTc.length; i++) {
            this.texCoords[i] = floatTc[i];
        }
    }

    @NonNull
    @Contract(pure = true)
    private float[] getFloatTc(Object tc) {
        if (tc instanceof float[]) {
            return (float[]) tc;
        }
        int[] temp = (int[]) tc;
        float[] floatTc = new float[temp.length];
        for (int i = 0; i < temp.length; i++) {
            floatTc[i] = temp[i];
        }
        return floatTc;
    }

    public void setTexCoords(float[] texCoords) {
        this.texCoords = texCoords;
    }

    private int getMult(Face face) {
        if (this.north.equals(face)) {
            return 0;
        }
        if (this.south.equals(face)) {
            return 1;
        }
        if (this.east.equals(face)) {
            return 2;
        }
        if (this.west.equals(face)) {
            return 3;
        }
        if (this.top.equals(face)) {
            return 4;
        }
        return 5;
    }

    public void face(Face f, float bx, float by, float bz, float width, float height, int colour, ShapeBuilder sb) {
        face(f, bx, by, bz, width, height, colour, sb, false);
    }

    public void face(@NonNull Face f, float bx, float by, float bz, float width, float height, int colour, @NonNull ShapeBuilder sb, boolean invertTexture) {
        sb.ensureCapacity(4, 2);
        System.arraycopy(f.verts, 0, sb.vertices, sb.vertexOffset, f.verts.length);
        for (int i = 0; i < 4; i++) {
            float[] fArr = sb.vertices;
            int i2 = sb.vertexOffset;
            sb.vertexOffset = i2 + 1;
            fArr[i2] = fArr[i2] + bx;
            float[] fArr2 = sb.vertices;
            int i3 = sb.vertexOffset;
            sb.vertexOffset = i3 + 1;
            fArr2[i3] = fArr2[i3] + by;
            float[] fArr3 = sb.vertices;
            int i4 = sb.vertexOffset;
            sb.vertexOffset = i4 + 1;
            fArr3[i4] = fArr3[i4] + bz;
            int[] iArr = sb.colours;
            int i5 = sb.colourOffset;
            sb.colourOffset = i5 + 1;
            iArr[i5] = colour;
        }
        int txco = getMult(f) * 2;
        float bu = this.sxtn * this.texCoords[txco];
        float bv = this.sxtn * (this.texCoords[txco + 1] + height);
        float tu = this.sxtn * (this.texCoords[txco] + width);
        float tv = this.sxtn * this.texCoords[txco + 1];
        if (invertTexture) {
            float[] fArr4 = sb.texCoords;
            int i6 = sb.texCoordOffset;
            sb.texCoordOffset = i6 + 1;
            fArr4[i6] = bu;
            float[] fArr5 = sb.texCoords;
            int i7 = sb.texCoordOffset;
            sb.texCoordOffset = i7 + 1;
            fArr5[i7] = bv;
            float[] fArr6 = sb.texCoords;
            int i8 = sb.texCoordOffset;
            sb.texCoordOffset = i8 + 1;
            fArr6[i8] = bu;
            float[] fArr7 = sb.texCoords;
            int i9 = sb.texCoordOffset;
            sb.texCoordOffset = i9 + 1;
            fArr7[i9] = tv;
            float[] fArr8 = sb.texCoords;
            int i10 = sb.texCoordOffset;
            sb.texCoordOffset = i10 + 1;
            fArr8[i10] = tu;
            float[] fArr9 = sb.texCoords;
            int i11 = sb.texCoordOffset;
            sb.texCoordOffset = i11 + 1;
            fArr9[i11] = bv;
            float[] fArr10 = sb.texCoords;
            int i12 = sb.texCoordOffset;
            sb.texCoordOffset = i12 + 1;
            fArr10[i12] = tu;
            float[] fArr11 = sb.texCoords;
            int i13 = sb.texCoordOffset;
            sb.texCoordOffset = i13 + 1;
            fArr11[i13] = tv;
        } else {
            float[] fArr12 = sb.texCoords;
            int i14 = sb.texCoordOffset;
            sb.texCoordOffset = i14 + 1;
            fArr12[i14] = tu;
            float[] fArr13 = sb.texCoords;
            int i15 = sb.texCoordOffset;
            sb.texCoordOffset = i15 + 1;
            fArr13[i15] = bv;
            float[] fArr14 = sb.texCoords;
            int i16 = sb.texCoordOffset;
            sb.texCoordOffset = i16 + 1;
            fArr14[i16] = tu;
            float[] fArr15 = sb.texCoords;
            int i17 = sb.texCoordOffset;
            sb.texCoordOffset = i17 + 1;
            fArr15[i17] = tv;
            float[] fArr16 = sb.texCoords;
            int i18 = sb.texCoordOffset;
            sb.texCoordOffset = i18 + 1;
            fArr16[i18] = bu;
            float[] fArr17 = sb.texCoords;
            int i19 = sb.texCoordOffset;
            sb.texCoordOffset = i19 + 1;
            fArr17[i19] = bv;
            float[] fArr18 = sb.texCoords;
            int i20 = sb.texCoordOffset;
            sb.texCoordOffset = i20 + 1;
            fArr18[i20] = bu;
            float[] fArr19 = sb.texCoords;
            int i21 = sb.texCoordOffset;
            sb.texCoordOffset = i21 + 1;
            fArr19[i21] = tv;
        }
        sb.relTriangle(0, 2, 1);
        sb.relTriangle(2, 3, 1);
        sb.vertexCount += 4;
    }

    @NonNull
    public Parallelepiped clone() {
        Parallelepiped p = new Parallelepiped(this.opaque, this.north, this.south, this.east, this.west, this.top, this.bottom, this.sxtn);
        p.setTexCoords(this.texCoords);
        p.setDepth(this.depth);
        p.setHeight(this.height);
        p.setWidth(this.width);
        return p;
    }

    public static class Face {
        public final float[] verts = new float[12];

        public Face(@NonNull float[]... verts) {
            for (int i = 0; i < verts.length; i++) {
                System.arraycopy(verts[i], 0, this.verts, i * 3, 3);
            }
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o == null || !(o instanceof Face)) {
                return false;
            }
            return Arrays.equals(this.verts, ((Face) o).verts);
        }
    }
}
