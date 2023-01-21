package com.solverlabs.droid.rugl.util;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class FastFloatBuffer {
    private static WeakReference<int[]> intArray = new WeakReference<>(new int[0]);
    private final FloatBuffer floats;
    private final IntBuffer ints;
    public ByteBuffer bytes;

    public FastFloatBuffer(int capacity) {
        this.bytes = ByteBuffer.allocateDirect(capacity * 4).order(ByteOrder.nativeOrder());
        this.floats = this.bytes.asFloatBuffer();
        this.ints = this.bytes.asIntBuffer();
    }

    @NonNull
    public static int[] convert(@NonNull float... data) {
        int[] id = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            id[i] = Float.floatToRawIntBits(data[i]);
        }
        return id;
    }

    public void flip() {
        this.bytes.flip();
        this.floats.flip();
        this.ints.flip();
    }

    public void put(float f) {
        this.bytes.position(this.bytes.position() + 4);
        this.floats.put(f);
        this.ints.position(this.ints.position() + 1);
    }

    public void put(float[] data) {
        int[] ia = intArray.get();
        if (ia == null || ia.length < data.length) {
            ia = new int[data.length];
            intArray = new WeakReference<>(ia);
        }
        for (int i = 0; i < data.length; i++) {
            ia[i] = Float.floatToRawIntBits(data[i]);
        }
        this.bytes.position(this.bytes.position() + (data.length * 4));
        this.floats.position(this.floats.position() + data.length);
        this.ints.put(ia, 0, data.length);
    }

    public void put(@NonNull int[] data) {
        this.bytes.position(this.bytes.position() + (data.length * 4));
        this.floats.position(this.floats.position() + data.length);
        this.ints.put(data, 0, data.length);
    }

    public void put(@NonNull FastFloatBuffer b) {
        this.bytes.put(b.bytes);
        this.floats.position(this.bytes.position() >> 2);
        this.ints.position(this.bytes.position() >> 2);
    }

    public int capacity() {
        return this.floats.capacity();
    }

    public int position() {
        return this.floats.position();
    }

    public void position(int p) {
        this.bytes.position(p * 4);
        this.floats.position(p);
        this.ints.position(p);
    }

    public FloatBuffer slice() {
        return this.floats.slice();
    }

    public int remaining() {
        return this.floats.remaining();
    }

    public int limit() {
        return this.floats.limit();
    }

    public void clear() {
        this.bytes.clear();
        this.floats.clear();
        this.ints.clear();
    }
}
