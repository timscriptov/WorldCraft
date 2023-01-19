package com.solverlabs.droid.rugl.util;

import androidx.annotation.NonNull;

import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;


/**
 * Convenient work-around for poor {@link FloatBuffer#put(float[])}
 * performance. This should become unnecessary in gingerbread, @see <a
 * href
 * ="http://code.google.com/p/android/issues/detail?id=11078">Issue
 * 11078</a>
 *
 * @author ryanm
 */
public class FastFloatBuffer {
    /**
     * Use a {@link SoftReference} so that the array can be collected
     * if necessary
     */
    private static SoftReference<int[]> intArray = new SoftReference<>(new int[0]);
    private final FloatBuffer floats;

    private final IntBuffer ints;
    /**
     * Underlying data - give this to OpenGL
     */
    public ByteBuffer bytes;

    /**
     * Constructs a new direct native-ordered buffer
     *
     * @param capacity the number of floats
     */
    public FastFloatBuffer(int capacity) {
        bytes =
                ByteBuffer.allocateDirect((capacity * 4)).order(ByteOrder.nativeOrder());
        floats = bytes.asFloatBuffer();
        ints = bytes.asIntBuffer();
    }

    /**
     * Converts float data to a format that can be quickly added to the
     * buffer with {@link #put(int[])}
     *
     * @param data
     * @return the int-formatted data
     */
    @NonNull
    public static int[] convert(@NonNull float... data) {
        int[] id = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            id[i] = Float.floatToRawIntBits(data[i]);
        }

        return id;
    }

    /**
     * See {@link FloatBuffer#flip()}
     */
    public void flip() {
        bytes.flip();
        floats.flip();
        ints.flip();
    }

    /**
     * See {@link FloatBuffer#put(float)}
     *
     * @param f
     */
    public void put(float f) {
        bytes.position(bytes.position() + 4);
        floats.put(f);
        ints.position(ints.position() + 1);
    }

    /**
     * It's like {@link FloatBuffer#put(float[])}, but about 10 times
     * faster
     *
     * @param data
     */
    public void put(float[] data) {
        int[] ia = intArray.get();
        if (ia == null || ia.length < data.length) {
            ia = new int[data.length];
            intArray = new SoftReference<int[]>(ia);
        }

        for (int i = 0; i < data.length; i++) {
            ia[i] = Float.floatToRawIntBits(data[i]);
        }

        bytes.position(bytes.position() + 4 * data.length);
        floats.position(floats.position() + data.length);
        ints.put(ia, 0, data.length);
    }

    /**
     * For use with pre-converted data. This is 50x faster than
     * {@link #put(float[])}, and 500x faster than
     * {@link FloatBuffer#put(float[])}, so if you've got float[] data
     * that won't change, {@link #convert(float...)} it to an int[]
     * once and use this method to put it in the buffer
     *
     * @param data floats that have been converted with
     *             {@link Float#floatToIntBits(float)}
     */
    public void put(@NonNull int[] data) {
        bytes.position(bytes.position() + 4 * data.length);
        floats.position(floats.position() + data.length);
        ints.put(data, 0, data.length);
    }

    /**
     * See {@link FloatBuffer#put(FloatBuffer)}
     *
     * @param b
     */
    public void put(@NonNull FastFloatBuffer b) {
        bytes.put(b.bytes);
        floats.position(bytes.position() >> 2);
        ints.position(bytes.position() >> 2);
    }

    /**
     * @return See {@link FloatBuffer#capacity()}
     */
    public int capacity() {
        return floats.capacity();
    }

    /**
     * @return See {@link FloatBuffer#position()}
     */
    public int position() {
        return floats.position();
    }

    /**
     * See {@link FloatBuffer#position(int)}
     *
     * @param p
     */
    public void position(int p) {
        bytes.position(4 * p);
        floats.position(p);
        ints.position(p);
    }

    /**
     * @return See {@link FloatBuffer#slice()}
     */
    public FloatBuffer slice() {
        return floats.slice();
    }

    /**
     * @return See {@link FloatBuffer#remaining()}
     */
    public int remaining() {
        return floats.remaining();
    }

    /**
     * @return See {@link FloatBuffer#limit()}
     */
    public int limit() {
        return floats.limit();
    }

    /**
     * See {@link FloatBuffer#clear()}
     */
    public void clear() {
        bytes.clear();
        floats.clear();
        ints.clear();
    }
}
