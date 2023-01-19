package com.solverlabs.droid.rugl.gl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;


public class BufferUtils {
    public static ByteBuffer createByteBuffer(int length) {
        ByteBuffer temp = ByteBuffer.allocateDirect(length).order(ByteOrder.nativeOrder());
        return temp;
    }

    public static IntBuffer createIntBuffer(int i) {
        return createByteBuffer(i * 4).asIntBuffer();
    }

    public static FloatBuffer createFloatBuffer(int i) {
        return createByteBuffer(i * 4).asFloatBuffer();
    }

    public static ShortBuffer createShortBuffer(int i) {
        return createByteBuffer(i * 2).asShortBuffer();
    }
}
