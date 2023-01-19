package com.solverlabs.droid.rugl.util.io;

import com.solverlabs.droid.rugl.util.geom.Vector2f;
import com.solverlabs.droid.rugl.util.geom.Vector3f;

/**
 * Utilities for serialising data to streams and reading the results
 * back
 *
 * @author ryanm
 */
public class SerialUtils {
    /**
     * Writes an array to the buffer, such that it can be read by
     * {@link #readIntArray(DataSource)}
     *
     * @param array
     * @param buffer
     */
    public static void write(int[] array, DataSink buffer) {
        buffer.putInt(array.length);
        for (int i = 0; i < array.length; i++) {
            buffer.putInt(array[i]);
        }
    }

    /**
     * Reads an array, previously written to the buffer by
     * {@link #write(boolean[], DataSink)}, from a buffer
     *
     * @param buffer
     * @return the int array
     */
    public static int[] readIntArray(DataSource buffer) {
        int[] array = new int[buffer.getInt()];

        for (int i = 0; i < array.length; i++) {
            array[i] = buffer.getInt();
        }

        return array;
    }

    /**
     * Writes an array to the buffer, such that it can be read by
     * {@link #readShortArray(DataSource)}
     *
     * @param array
     * @param buffer
     */
    public static void write(short[] array, DataSink buffer) {
        buffer.putInt(array.length);
        for (int i = 0; i < array.length; i++) {
            buffer.putShort(array[i]);
        }
    }

    /**
     * Reads an array, previously written to the buffer by
     * {@link #write(short[], DataSink)}, from a buffer
     *
     * @param buffer
     * @return the int array
     */
    public static short[] readShortArray(DataSource buffer) {
        short[] array = new short[buffer.getInt()];

        for (int i = 0; i < array.length; i++) {
            array[i] = buffer.getShort();
        }

        return array;
    }

    /**
     * Writes a {@link String} to a buffer
     *
     * @param s
     * @param buffer
     */
    public static void write(String s, DataSink buffer) {
        buffer.putInt(s.length());
        for (int i = 0; i < s.length(); i++) {
            buffer.putChar(s.charAt(i));
        }
    }

    /**
     * Reads a String from a buffer
     *
     * @param buffer
     * @return The string
     */
    public static String readString(DataSource buffer) {
        char[] c = readCharArray(buffer);
        return new String(c).intern();
    }

    /**
     * Writes an array to a stream, such that it can be read by
     * {@link SerialUtils#readBooleanArray(DataSource)}
     *
     * @param array
     * @param buffer
     */
    public static void write(boolean[] array, DataSink buffer) {
        buffer.putInt(array.length);
        for (int i = 0; i < array.length; i++) {
            buffer.putBoolean(array[i]);
        }
    }

    /**
     * Reads an array from the stream, written by
     * {@link SerialUtils#write(boolean[], DataSink)}
     *
     * @param buffer
     * @return The boolean data
     */
    public static boolean[] readBooleanArray(DataSource buffer) {
        boolean[] array = new boolean[buffer.getInt()];

        for (int i = 0; i < array.length; i++) {
            array[i] = buffer.getBoolean();
        }

        return array;
    }

    /**
     * Writes an array to the buffer, such that it can be read by
     * {@link #readFloatArray(DataSource)}
     *
     * @param array
     * @param buffer
     */
    public static void write(float[] array, DataSink buffer) {
        buffer.putInt(array.length);
        for (int i = 0; i < array.length; i++) {
            buffer.putFloat(array[i]);
        }
    }

    /**
     * Reads an array, previously written to the buffer by
     * {@link #write(boolean[], DataSink)}, from a buffer
     *
     * @param buffer
     * @return the float array
     */
    public static float[] readFloatArray(DataSource buffer) {
        float[] array = new float[buffer.getInt()];

        for (int i = 0; i < array.length; i++) {
            array[i] = buffer.getFloat();
        }

        return array;
    }

    /**
     * Reads an array, previously written to the buffer by
     * {@link #write(boolean[], DataSink)}, from a buffer
     *
     * @param buffer
     * @return the float array
     */
    public static char[] readCharArray(DataSource buffer) {
        char[] array = new char[buffer.getInt()];

        for (int i = 0; i < array.length; i++) {
            array[i] = buffer.getChar();
        }

        return array;
    }

    /**
     * Writes the values of the {@link Vector3f} to a buffer
     *
     * @param tuple  The vector to write
     * @param buffer The buffer to write to
     */
    public static void encode(Vector3f tuple, DataSink buffer) {
        buffer.putFloat(tuple.getX());
        buffer.putFloat(tuple.getY());
        buffer.putFloat(tuple.getZ());
    }

    /**
     * Reads the values of a {@link Vector3f} from the buffer
     *
     * @param buffer The buffer to read from
     * @param tuple  The tuple to store the read values into, or null to
     *               obtain a new {@link Vector3f}
     * @return A {@link Vector3f} containing the decoded values
     */
    public static Vector3f decodeVector3f(DataSource buffer, Vector3f tuple) {
        if (tuple == null) {
            tuple = new Vector3f();
        }

        tuple.x = buffer.getFloat();
        tuple.y = buffer.getFloat();
        tuple.z = buffer.getFloat();

        return tuple;
    }

    /**
     * Writes the values of the {@link Vector2f} to a buffer
     *
     * @param tuple  The vector to write
     * @param buffer The buffer to write to
     */
    public static void encode(Vector2f tuple, DataSink buffer) {
        buffer.putFloat(tuple.x);
        buffer.putFloat(tuple.y);
    }

    /**
     * Reads the values of a {@link Vector2f} from the buffer
     *
     * @param buffer The buffer to read from
     * @param tuple  The {@link Vector2f} to store the read values into, or
     *               null to obtain a new {@link Vector2f}
     * @return A {@link Vector2f} containing the decoded values
     */
    public static Vector2f decodeTuple2f(DataSource buffer, Vector2f tuple) {
        if (tuple == null) {
            tuple = new Vector2f();
        }

        tuple.x = buffer.getFloat();
        tuple.y = buffer.getFloat();

        return tuple;
    }
}
