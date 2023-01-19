package com.solverlabs.worldcraft.framework.gl;

import android.opengl.GLES10;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;


public class Vertices3 {
    final boolean hasColor;
    final boolean hasNormals;
    final boolean hasTexCoords;
    final ShortBuffer indices;
    final int[] tmpBuffer;
    final int vertexSize;
    final IntBuffer vertices;

    public Vertices3(int maxVertices, int maxIndices, boolean hasColor, boolean hasTexCoords, boolean hasNormals) {
        int i = 0;
        this.hasColor = hasColor;
        this.hasTexCoords = hasTexCoords;
        this.hasNormals = hasNormals;
        this.vertexSize = ((hasNormals ? 3 : i) + (hasTexCoords ? 2 : 0) + (hasColor ? 4 : 0) + 3) * 4;
        this.tmpBuffer = new int[(this.vertexSize * maxVertices) / 4];
        ByteBuffer buffer = ByteBuffer.allocateDirect(this.vertexSize * maxVertices);
        buffer.order(ByteOrder.nativeOrder());
        this.vertices = buffer.asIntBuffer();
        if (maxIndices > 0) {
            ByteBuffer buffer2 = ByteBuffer.allocateDirect((maxIndices * 16) / 8);
            buffer2.order(ByteOrder.nativeOrder());
            this.indices = buffer2.asShortBuffer();
            return;
        }
        this.indices = null;
    }

    public void setVertices(float[] vertices, int offset, int length, float zoom) {
        this.vertices.clear();
        int len = offset + length;
        int i = offset;
        int j = 0;
        while (i < len) {
            this.tmpBuffer[j] = Float.floatToRawIntBits(vertices[i] * zoom);
            i++;
            j++;
        }
        this.vertices.put(this.tmpBuffer, 0, length);
        this.vertices.flip();
    }

    public void setVertices(float[] vertices, int offset, int length) {
        this.vertices.clear();
        int len = offset + length;
        int i = offset;
        int j = 0;
        while (i < len) {
            this.tmpBuffer[j] = Float.floatToRawIntBits(vertices[i]);
            i++;
            j++;
        }
        this.vertices.put(this.tmpBuffer, 0, length);
        this.vertices.flip();
    }

    public void setIndices(short[] indices, int offset, int length) {
        this.indices.clear();
        this.indices.put(indices, offset, length);
        this.indices.flip();
    }

    public void bind() {
        int i = 3;
        this.vertices.position(0);
        GLES10.glVertexPointer(3, 5126, this.vertexSize, this.vertices);
        if (this.hasColor) {
            this.vertices.position(3);
            GLES10.glColorPointer(4, 5126, this.vertexSize, this.vertices);
        }
        if (this.hasTexCoords) {
            if (this.hasColor) {
                i = 7;
            }
            this.vertices.position(i);
            GLES10.glTexCoordPointer(2, 5126, this.vertexSize, this.vertices);
        }
        if (this.hasNormals) {
            GLES10.glEnableClientState(32885);
            int offset = 3;
            if (this.hasColor) {
                offset = 3 + 4;
            }
            if (this.hasTexCoords) {
                offset += 2;
            }
            this.vertices.position(offset);
            GLES10.glNormalPointer(5126, this.vertexSize, this.vertices);
        }
    }

    public void draw(int primitiveType, int offset, int numVertices) {
        if (this.indices != null) {
            this.indices.position(offset);
            GLES10.glDrawElements(primitiveType, numVertices, 5123, this.indices);
            return;
        }
        GLES10.glDrawArrays(primitiveType, offset, numVertices);
    }

    public void unbind() {
        if (this.hasTexCoords) {
        }
        if (this.hasColor) {
        }
        if (this.hasNormals) {
        }
    }

    public int getNumIndices() {
        return this.indices.limit();
    }

    public int getNumVertices() {
        return this.vertices.limit() / (this.vertexSize / 4);
    }
}
