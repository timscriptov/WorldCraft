package com.solverlabs.worldcraft.mob.util;

import android.opengl.GLES10;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Piramide {
    private static final FloatBuffer colorBuffer;
    private static final ByteBuffer indexBuffer;
    private static final FloatBuffer vertexBuffer;
    private static final float[] vertices = {-1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f};
    private static final float[] colors = {0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f};
    private static final byte[] indices = {2, 4, 3, 1, 4, 2, 0, 4, 1, 4, 0, 3};

    static {
        StringBuilder vertsString = new StringBuilder();
        for (float vert : vertices) {
            vertsString.append(vert).append(",");
        }
        System.out.println("vertices: " + ((Object) vertsString));
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        vertexBuffer = vbb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);
        ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * 4);
        cbb.order(ByteOrder.nativeOrder());
        colorBuffer = cbb.asFloatBuffer();
        colorBuffer.put(colors);
        colorBuffer.position(0);
        indexBuffer = ByteBuffer.allocateDirect(indices.length);
        indexBuffer.put(indices);
        indexBuffer.position(0);
    }

    public static void render(float x, float y, float z) {
        GLES10.glFrontFace(2305);
        GLES10.glDisableClientState(32888);
        GLES10.glEnableClientState(32884);
        GLES10.glVertexPointer(3, 5126, 0, vertexBuffer);
        GLES10.glEnableClientState(32886);
        GLES10.glColorPointer(4, 5126, 0, colorBuffer);
        GLES10.glPushMatrix();
        GLES10.glTranslatef(x, y, z);
        GLES10.glDrawElements(4, indices.length, 5121, indexBuffer);
        GLES10.glPopMatrix();
        GLES10.glDisableClientState(32884);
        GLES10.glDisableClientState(32886);
    }
}
