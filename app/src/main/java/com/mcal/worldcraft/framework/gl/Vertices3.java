package com.mcal.worldcraft.framework.gl;

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
        this.hasColor = hasColor;
        this.hasTexCoords = hasTexCoords;
        this.hasNormals = hasNormals;
        vertexSize = ((hasNormals ? 3 : 0) + (hasTexCoords ? 2 : 0) + (hasColor ? 4 : 0) + 3) * 4;
        tmpBuffer = new int[(vertexSize * maxVertices) / 4];
        ByteBuffer buffer = ByteBuffer.allocateDirect(vertexSize * maxVertices);
        buffer.order(ByteOrder.nativeOrder());
        vertices = buffer.asIntBuffer();
        if (maxIndices > 0) {
            ByteBuffer buffer2 = ByteBuffer.allocateDirect((maxIndices * 16) / 8);
            buffer2.order(ByteOrder.nativeOrder());
            indices = buffer2.asShortBuffer();
            return;
        }
        indices = null;
    }

    public void setVertices(float[] vertices, int offset, int length, float zoom) {
        this.vertices.clear();
        int len = offset + length;
        int i = offset;
        int j = 0;
        while (i < len) {
            tmpBuffer[j] = Float.floatToRawIntBits(vertices[i] * zoom);
            i++;
            j++;
        }
        this.vertices.put(tmpBuffer, 0, length);
        this.vertices.flip();
    }

    public void setVertices(float[] vertices, int offset, int length) {
        this.vertices.clear();
        int len = offset + length;
        int i = offset;
        int j = 0;
        while (i < len) {
            tmpBuffer[j] = Float.floatToRawIntBits(vertices[i]);
            i++;
            j++;
        }
        this.vertices.put(tmpBuffer, 0, length);
        this.vertices.flip();
    }

    public void setIndices(short[] indices, int offset, int length) {
        this.indices.clear();
        this.indices.put(indices, offset, length);
        this.indices.flip();
    }

    public void bind() {
        vertices.position(0);
        GLES10.glVertexPointer(3, 5126, vertexSize, vertices);
        if (hasColor) {
            vertices.position(3);
            GLES10.glColorPointer(4, 5126, vertexSize, vertices);
        }
        if (hasTexCoords) {
            vertices.position(hasColor ? 7 : 3);
            GLES10.glTexCoordPointer(2, 5126, vertexSize, vertices);
        }
        if (hasNormals) {
            GLES10.glEnableClientState(32885);
            int offset = 3;
            if (hasColor) {
                offset = 3 + 4;
            }
            if (hasTexCoords) {
                offset += 2;
            }
            vertices.position(offset);
            GLES10.glNormalPointer(5126, vertexSize, vertices);
        }
    }

    public void draw(int primitiveType, int offset, int numVertices) {
        if (indices != null) {
            indices.position(offset);
            GLES10.glDrawElements(primitiveType, numVertices, 5123, indices);
            return;
        }
        GLES10.glDrawArrays(primitiveType, offset, numVertices);
    }

    public void unbind() {
        if (hasTexCoords) {
        }
        if (hasColor) {
        }
        if (hasNormals) {
        }
    }

    public int getNumIndices() {
        return indices.limit();
    }

    public int getNumVertices() {
        return vertices.limit() / (vertexSize / 4);
    }
}
