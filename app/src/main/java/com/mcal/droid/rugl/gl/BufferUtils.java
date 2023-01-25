package com.mcal.droid.rugl.gl;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;


public class BufferUtils {
    @NonNull
    public static ByteBuffer createByteBuffer(int length) {
        return ByteBuffer.allocateDirect(length).order(ByteOrder.nativeOrder());
    }

    @NonNull
    public static IntBuffer createIntBuffer(int i) {
        return createByteBuffer(i * 4).asIntBuffer();
    }

    @NonNull
    public static FloatBuffer createFloatBuffer(int i) {
        return createByteBuffer(i * 4).asFloatBuffer();
    }

    @NonNull
    public static ShortBuffer createShortBuffer(int i) {
        return createByteBuffer(i * 2).asShortBuffer();
    }
}
