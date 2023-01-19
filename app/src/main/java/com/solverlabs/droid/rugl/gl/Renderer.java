package com.solverlabs.droid.rugl.gl;

import android.opengl.GLES10;
import android.opengl.Matrix;

import com.solverlabs.droid.rugl.util.FastFloatBuffer;
import com.solverlabs.droid.rugl.util.geom.MatrixUtils;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Renderer {
    private static int internedListVersion = 0;
    protected final float[] transform;
    public boolean automaticallyClear;
    public IntBuffer colours;
    private IndexList[] indices;
    private List<State> interned;
    private float[] t;
    private FastFloatBuffer texCoords;
    private int triangleCount;
    private ShortBuffer tris;
    private int vertexCount;
    private FastFloatBuffer vertices;

    public Renderer() {
        this(1000);
    }

    public Renderer(int verts) {
        this.transform = new float[16];
        this.t = new float[]{0.0f, 0.0f, 0.0f, 1.0f};
        this.indices = new IndexList[0];
        this.interned = new ArrayList();
        this.vertexCount = 0;
        this.triangleCount = 0;
        this.automaticallyClear = true;
        this.vertices = new FastFloatBuffer(verts * 3);
        this.texCoords = new FastFloatBuffer(verts * 2);
        this.colours = BufferUtils.createIntBuffer(verts);
        Matrix.setIdentityM(this.transform, 0);
    }

    public State intern(State s) {
        if (this.indices.length <= 0 || this.indices[0].state.getCompilationBatch() != s.getCompilationBatch()) {
            int index = Collections.binarySearch(this.interned, s);
            if (index >= 0) {
                return this.interned.get(index);
            }
            this.interned.add(-(index + 1), s);
            internedListVersion++;
            for (int i = 0; i < this.interned.size(); i++) {
                State t = this.interned.get(i);
                t.compiledIndex = i;
                t.compilationBatch = internedListVersion;
            }
            IndexList[] newTL = new IndexList[this.interned.size()];
            newTL[s.getCompiledIndex()] = new IndexList(s);
            for (int i2 = 0; i2 < this.indices.length; i2++) {
                newTL[this.indices[i2].state.getCompiledIndex()] = this.indices[i2];
            }
            this.indices = newTL;
            return s;
        }
        return s;
    }

    public void addGeometry(float[] verts, float[] textureCoordinates, int[] vertexColours, short[] geomIndices, State state) {
        int vc = verts.length / 3;
        if (state == null) {
            state = GLUtil.typicalState;
        }
        State state2 = intern(state);
        short indexOffset = (short) (this.vertices.position() / 3);
        while (this.vertices.remaining() < verts.length) {
            growArrays();
        }
        boolean doTransform = !MatrixUtils.isIdentity(this.transform);
        if (!doTransform) {
            this.vertices.put(verts);
        } else {
            for (int i = 0; i < vc; i++) {
                System.arraycopy(verts, i * 3, this.t, 0, 3);
                this.t[3] = 1.0f;
                Matrix.multiplyMV(this.t, 0, this.transform, 0, this.t, 0);
                this.vertices.put(this.t[0]);
                this.vertices.put(this.t[1]);
                this.vertices.put(this.t[2]);
            }
        }
        this.colours.put(vertexColours);
        if (textureCoordinates != null) {
            this.texCoords.put(textureCoordinates);
        } else {
            this.texCoords.position(this.texCoords.position() + (vc * 2));
        }
        this.indices[state2.getCompiledIndex()].add(geomIndices, indexOffset);
    }

    public void addGeometry(int[] verts, int[] textureCoordinates, int[] vertexColours, short[] geomIndices, State state) {
        int vc = verts.length / 3;
        if (state == null) {
            state = GLUtil.typicalState;
        }
        State state2 = intern(state);
        short indexOffset = (short) (this.vertices.position() / 3);
        while (this.vertices.remaining() < verts.length) {
            growArrays();
        }
        boolean doTransform = !MatrixUtils.isIdentity(this.transform);
        if (!doTransform) {
            this.vertices.put(verts);
        } else {
            for (int i = 0; i < vc; i++) {
                this.t[0] = Float.intBitsToFloat(verts[i * 3]);
                this.t[1] = Float.intBitsToFloat(verts[(i * 3) + 1]);
                this.t[2] = Float.intBitsToFloat(verts[(i * 3) + 2]);
                this.t[3] = 1.0f;
                Matrix.multiplyMV(this.t, 0, this.transform, 0, this.t, 0);
                this.vertices.put(this.t[0]);
                this.vertices.put(this.t[1]);
                this.vertices.put(this.t[2]);
            }
        }
        this.colours.put(vertexColours);
        if (textureCoordinates != null) {
            this.texCoords.put(textureCoordinates);
        } else {
            this.texCoords.position(this.texCoords.position() + (vc * 2));
        }
        try {
            this.indices[state2.getCompiledIndex()].add(geomIndices, indexOffset);
        } catch (Throwable th) {
            try {
                this.indices[state2.getCompiledIndex() - 1].add(geomIndices, indexOffset);
            } catch (Throwable th2) {
                this.indices[state2.getCompiledIndex() - 2].add(geomIndices, indexOffset);
            }
        }
    }

    private void growArrays() {
        FastFloatBuffer nv = new FastFloatBuffer(this.vertices.capacity() * 2);
        this.vertices.flip();
        nv.put(this.vertices);
        this.vertices = nv;
        FastFloatBuffer ntc = new FastFloatBuffer(this.texCoords.capacity() * 2);
        this.texCoords.flip();
        ntc.put(this.texCoords);
        this.texCoords = ntc;
        IntBuffer bc = BufferUtils.createIntBuffer(this.colours.capacity() * 2);
        this.colours.flip();
        bc.put(this.colours);
        this.colours = bc;
    }

    public void render() {
        this.vertices.flip();
        this.texCoords.flip();
        this.colours.flip();
        this.vertexCount = this.vertices.limit() / 3;
        this.triangleCount = 0;
        GLES10.glVertexPointer(3, 5126, 0, this.vertices.bytes);
        GLES10.glColorPointer(4, 5121, 0, this.colours);
        GLES10.glTexCoordPointer(2, 5126, 0, this.texCoords.bytes);
        for (int i = 0; i < this.indices.length; i++) {
            IndexList tl = this.indices[i];
            if (tl.count > 0) {
                tl.state.apply();
                this.triangleCount += tl.count;
                if (this.tris == null || this.tris.capacity() < tl.indices.length) {
                    this.tris = BufferUtils.createShortBuffer(tl.indices.length);
                }
                this.tris.clear();
                this.tris.put(tl.indices);
                this.tris.position(tl.count);
                this.tris.flip();
                GLES10.glDrawElements(tl.state.drawMode.glValue, tl.count, 5123, this.tris);
            }
        }
        this.triangleCount /= 3;
        if (this.automaticallyClear) {
            clear();
        }
    }

    public void clear() {
        this.vertices.clear();
        this.texCoords.clear();
        this.colours.clear();
        for (int i = 0; i < this.indices.length; i++) {
            this.indices[i].count = 0;
        }
    }

    public int countVertices() {
        return this.vertexCount;
    }

    public int countTriangles() {
        return this.triangleCount;
    }

    public float[] getTransform() {
        return this.transform;
    }

    public void setTransform(float[] m) {
        if (m != null) {
            System.arraycopy(m, 0, this.transform, 0, m.length);
        } else {
            Matrix.setIdentityM(this.transform, 0);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */

    public static class IndexList {
        private final State state;
        private int count;
        private short[] indices;

        private IndexList(State state) {
            this.count = 0;
            this.indices = new short[100];
            this.state = state;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void add(short[] ti, int indexOffset) {
            if (this.count + ti.length > this.indices.length) {
                short[] nInd = new short[this.count + ti.length];
                System.arraycopy(this.indices, 0, nInd, 0, this.indices.length);
                this.indices = nInd;
            }
            for (int i = 0; i < ti.length; i++) {
                this.indices[this.count + i] = (short) (ti[i] + indexOffset);
            }
            this.count += ti.length;
        }
    }
}
