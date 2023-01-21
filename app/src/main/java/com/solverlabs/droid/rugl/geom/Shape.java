package com.solverlabs.droid.rugl.geom;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.util.geom.BoundingCuboid;
import com.solverlabs.droid.rugl.util.geom.Matrix4f;
import com.solverlabs.droid.rugl.util.geom.TriangleUtils;
import com.solverlabs.droid.rugl.util.geom.Vector3f;
import com.solverlabs.droid.rugl.util.geom.Vector4f;
import com.solverlabs.droid.rugl.util.io.DataSink;
import com.solverlabs.droid.rugl.util.io.DataSource;
import com.solverlabs.droid.rugl.util.io.SerialUtils;
import com.solverlabs.worldcraft.math.MathUtils;


public class Shape implements Cloneable {
    private static final Vector4f transformVector = new Vector4f();
    private static final Matrix4f rotateMatrix = new Matrix4f();
    public final short[] indices;
    public final float[] vertices;
    private final BoundingCuboid bounds = new BoundingCuboid(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
    private float c;
    private float oneminusc;
    private float s;
    private float tempX;
    private float tempY;
    private float tempZ;
    private float xs;
    private float xy;
    private float xz;
    private float ys;
    private float yz;
    private float zs;
    private float[] backupVertices = null;
    private float[] checkpointVertices = null;
    private short[] backupIndices = null;
    private short[] checkpointIndices = null;
    private BoundingCuboid backupBounds = null;
    private BoundingCuboid checkpointBounds = null;
    private boolean boundsDirty = true;

    public Shape(@NonNull Shape s) {
        this.vertices = s.vertices;
        this.indices = s.indices;
    }

    public Shape(float[] vertices, short[] indices) throws IllegalArgumentException {
        this.vertices = vertices;
        this.indices = indices;
        sanity();
    }

    public Shape(DataSource data) throws IllegalArgumentException {
        this.vertices = SerialUtils.readFloatArray(data);
        this.indices = SerialUtils.readShortArray(data);
        sanity();
    }

    private void sanity() throws IllegalArgumentException {
        if (this.vertices.length % 3 != 0) {
            throw new IllegalArgumentException("vertex count error\n" + shortString());
        }
        for (int i = 0; i < this.indices.length; i++) {
            if (this.indices[i] < 0 || this.indices[i] >= vertexCount()) {
                throw new IllegalArgumentException("triangle index error : " + ((int) this.indices[i]) + "\n" + shortString());
            }
        }
    }

    public void reset() {
        if (this.backupVertices != null && this.backupIndices != null && this.backupBounds != null) {
            System.arraycopy(this.backupVertices, 0, this.vertices, 0, this.vertices.length);
            System.arraycopy(this.backupIndices, 0, this.indices, 0, this.indices.length);
            this.bounds.x.set(this.backupBounds.x.getMin(), this.backupBounds.x.getMax());
            this.bounds.y.set(this.backupBounds.y.getMin(), this.backupBounds.y.getMax());
            this.bounds.z.set(this.backupBounds.z.getMin(), this.backupBounds.z.getMax());
        }
    }

    public void backup() {
        if (this.backupVertices == null) {
            this.backupVertices = new float[this.vertices.length];
        }
        if (this.backupIndices == null) {
            this.backupIndices = new short[this.indices.length];
        }
        if (this.backupBounds == null) {
            this.backupBounds = new BoundingCuboid(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        }
        System.arraycopy(this.vertices, 0, this.backupVertices, 0, this.vertices.length);
        System.arraycopy(this.indices, 0, this.backupIndices, 0, this.indices.length);
        this.backupBounds.x.set(this.bounds.x.getMin(), this.bounds.x.getMax());
        this.backupBounds.y.set(this.bounds.y.getMin(), this.bounds.y.getMax());
        this.backupBounds.z.set(this.bounds.z.getMin(), this.bounds.z.getMax());
    }

    public void checkpoint() {
        if (this.checkpointVertices == null) {
            this.checkpointVertices = new float[this.vertices.length];
        }
        if (this.checkpointIndices == null) {
            this.checkpointIndices = new short[this.indices.length];
        }
        if (this.checkpointBounds == null) {
            this.checkpointBounds = new BoundingCuboid(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        }
        System.arraycopy(this.vertices, 0, this.checkpointVertices, 0, this.vertices.length);
        System.arraycopy(this.indices, 0, this.checkpointIndices, 0, this.indices.length);
        this.checkpointBounds.x.set(this.bounds.x.getMin(), this.bounds.x.getMax());
        this.checkpointBounds.y.set(this.bounds.y.getMin(), this.bounds.y.getMax());
        this.checkpointBounds.z.set(this.bounds.z.getMin(), this.bounds.z.getMax());
    }

    public void restoreCheckpoint() {
        if (this.checkpointVertices != null && this.checkpointIndices != null && this.checkpointBounds != null) {
            System.arraycopy(this.checkpointVertices, 0, this.vertices, 0, this.vertices.length);
            System.arraycopy(this.checkpointIndices, 0, this.indices, 0, this.indices.length);
            this.bounds.x.set(this.checkpointBounds.x.getMin(), this.checkpointBounds.x.getMax());
            this.bounds.y.set(this.checkpointBounds.y.getMin(), this.checkpointBounds.y.getMax());
            this.bounds.z.set(this.checkpointBounds.z.getMin(), this.checkpointBounds.z.getMax());
        }
    }

    public void write(DataSink sink) {
        SerialUtils.write(this.vertices, sink);
        SerialUtils.write(this.indices, sink);
    }

    public int vertexCount() {
        return this.vertices.length / 3;
    }

    public Vector3f getVertex(int index, Vector3f v) {
        if (v == null) {
            v = new Vector3f();
        }
        v.set(this.vertices[index * 3], this.vertices[(index * 3) + 1], this.vertices[(index * 3) + 2]);
        return v;
    }

    public Shape transform(Matrix4f m) {
        for (int i = 0; i < this.vertices.length; i += 3) {
            transformVector.set(this.vertices[i], this.vertices[i + 1], this.vertices[i + 2], 1.0f);
            Matrix4f.transform(m, transformVector, transformVector);
            this.vertices[i] = transformVector.x;
            this.vertices[i + 1] = transformVector.y;
            this.vertices[i + 2] = transformVector.z;
        }
        this.boundsDirty = true;
        return this;
    }

    public BoundingCuboid getBounds() {
        if (this.boundsDirty) {
            recomputeBounds();
        }
        return this.bounds;
    }

    public void recomputeBounds() {
        this.bounds.x.set(this.vertices[0], this.vertices[0]);
        this.bounds.y.set(this.vertices[1], this.vertices[1]);
        this.bounds.z.set(this.vertices[2], this.vertices[2]);
        for (int i = 0; i < this.vertices.length; i += 3) {
            this.bounds.encompass(this.vertices[i], this.vertices[i + 1], this.vertices[i + 2]);
        }
        this.boundsDirty = false;
    }

    public Shape translate(float x, float y, float z) {
        for (int i = 0; i < this.vertices.length; i += 3) {
            float[] fArr = this.vertices;
            int i2 = i + 0;
            fArr[i2] = fArr[i2] + x;
            float[] fArr2 = this.vertices;
            int i3 = i + 1;
            fArr2[i3] = fArr2[i3] + y;
            float[] fArr3 = this.vertices;
            int i4 = i + 2;
            fArr3[i4] = fArr3[i4] + z;
        }
        this.bounds.translate(x, y, z);
        return this;
    }

    public Shape set(float x, float y, float z) {
        for (int i = 0; i < this.vertices.length; i += 3) {
            this.vertices[i + 0] = x;
            this.vertices[i + 1] = y;
            this.vertices[i + 2] = z;
        }
        this.bounds.set(x, y, z);
        return this;
    }

    public Shape scale(float x, float y, float z) {
        for (int i = 0; i < this.vertices.length; i += 3) {
            float[] fArr = this.vertices;
            int i2 = i + 0;
            fArr[i2] = fArr[i2] * x;
            float[] fArr2 = this.vertices;
            int i3 = i + 1;
            fArr2[i3] = fArr2[i3] * y;
            float[] fArr3 = this.vertices;
            int i4 = i + 2;
            fArr3[i4] = fArr3[i4] * z;
        }
        this.bounds.scale(x, y, z);
        return this;
    }

    public Shape rotate(float angle, float axisx, float axisy, float axisz) {
        this.c = MathUtils.cos(angle);
        this.s = MathUtils.sin(angle);
        this.oneminusc = 1.0f - this.c;
        this.xy = axisx * axisy;
        this.yz = axisy * axisz;
        this.xz = axisx * axisz;
        this.xs = this.s * axisx;
        this.ys = this.s * axisy;
        this.zs = this.s * axisz;
        for (int i = 0; i < this.vertices.length; i += 3) {
            this.tempX = this.vertices[i];
            this.tempY = this.vertices[i + 1];
            this.tempZ = this.vertices[i + 2];
            this.vertices[i] = (((axisx * axisx * this.oneminusc) + this.c) * this.tempX) + (((this.xy * this.oneminusc) - this.zs) * this.tempY) + (((this.xz * this.oneminusc) + this.ys) * this.tempZ);
            this.vertices[i + 1] = (((this.xy * this.oneminusc) + this.zs) * this.tempX) + (((axisy * axisy * this.oneminusc) + this.c) * this.tempY) + (((this.yz * this.oneminusc) - this.xs) * this.tempZ);
            this.vertices[i + 2] = (((this.xz * this.oneminusc) - this.ys) * this.tempX) + (((this.yz * this.oneminusc) + this.xs) * this.tempY) + (((axisz * axisz * this.oneminusc) + this.c) * this.tempZ);
        }
        this.boundsDirty = true;
        return this;
    }

    public Shape rotateYByOne(float angle) {
        this.c = MathUtils.cos(angle);
        this.s = MathUtils.sin(angle);
        for (int i = 0; i < this.vertices.length; i += 3) {
            this.tempX = this.vertices[i];
            this.tempZ = this.vertices[i + 2];
            this.vertices[i] = (this.c * this.tempX) + (this.s * this.tempZ);
            this.vertices[i + 2] = ((-this.s) * this.tempX) + (this.c * this.tempZ);
        }
        this.boundsDirty = true;
        return this;
    }

    public Shape rotateXByOne(float angle) {
        this.c = MathUtils.cos(angle);
        this.s = MathUtils.sin(angle);
        for (int i = 0; i < this.vertices.length; i += 3) {
            this.tempY = this.vertices[i + 1];
            this.tempZ = this.vertices[i + 2];
            this.vertices[i + 1] = (this.c * this.tempY) + ((-this.s) * this.tempZ);
            this.vertices[i + 2] = (this.s * this.tempY) + (this.c * this.tempZ);
        }
        this.boundsDirty = true;
        return this;
    }

    public Shape oldRotate(float angle, float axisx, float axisy, float axisz) {
        rotateMatrix.setIdentity();
        rotateMatrix.rotate(angle, axisx, axisy, axisz);
        transform(rotateMatrix);
        return this;
    }

    public boolean contains(float x, float y) {
        for (int i = 0; i < this.indices.length; i += 3) {
            int a = this.indices[i] * 3;
            int b = this.indices[i + 1] * 3;
            int c = this.indices[i + 2] * 3;
            if (TriangleUtils.contains(x, y, this.vertices[a], this.vertices[a + 1], this.vertices[b], this.vertices[b + 1], this.vertices[c], this.vertices[c + 1])) {
                return true;
            }
        }
        return false;
    }

    public String shortString() {
        return "Shape " + vertexCount() + " verts " + (this.indices.length / 3.0d) + " tris";
    }

    @NonNull
    public String toString() {
        StringBuilder buff = new StringBuilder("Shape ");
        buff.append(vertexCount());
        buff.append(" verts ");
        buff.append(this.indices.length / 3.0d);
        buff.append(" tris");
        for (int i = 0; i < this.vertices.length; i += 3) {
            buff.append("\n\t");
            buff.append(this.vertices[i]);
            buff.append(", ");
            buff.append(this.vertices[i + 1]);
            buff.append(", ");
            buff.append(this.vertices[i + 2]);
        }
        for (int i2 = 0; i2 < this.indices.length; i2 += 3) {
            buff.append("\n\t");
            buff.append(this.indices[i2]);
            buff.append("-");
            buff.append(this.indices[i2 + 1]);
            buff.append("-");
            buff.append(this.indices[i2 + 2]);
        }
        return buff.toString();
    }

    public float getSurfaceArea() {
        float sum = 0.0f;
        for (int i = 0; i < this.indices.length; i += 3) {
            short s = this.indices[i];
            short s2 = this.indices[i + 1];
            short s3 = this.indices[i + 2];
            sum += TriangleUtils.area(this.vertices[s], this.vertices[s + 1], this.vertices[s + 2], this.vertices[s2], this.vertices[s2 + 1], this.vertices[s2 + 2], this.vertices[s3], this.vertices[s3 + 1], this.vertices[s3 + 2]);
        }
        return sum;
    }

    public int bytes() {
        return (this.vertices.length * 4) + (this.indices.length * 2);
    }

    public Shape clone() {
        return new Shape(this.vertices.clone(), this.indices.clone());
    }
}
