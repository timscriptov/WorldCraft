package com.solverlabs.droid.rugl.util.io;

import com.solverlabs.droid.rugl.util.geom.Vector2f;
import com.solverlabs.droid.rugl.util.geom.Vector3f;


public class SerialUtils {
    public static void write(int[] array, DataSink buffer) {
        buffer.putInt(array.length);
        for (int i : array) {
            buffer.putInt(i);
        }
    }

    public static int[] readIntArray(DataSource buffer) {
        int[] array = new int[buffer.getInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = buffer.getInt();
        }
        return array;
    }

    public static void write(short[] array, DataSink buffer) {
        buffer.putInt(array.length);
        for (short s : array) {
            buffer.putShort(s);
        }
    }

    public static short[] readShortArray(DataSource buffer) {
        short[] array = new short[buffer.getInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = buffer.getShort();
        }
        return array;
    }

    public static void write(String s, DataSink buffer) {
        buffer.putInt(s.length());
        for (int i = 0; i < s.length(); i++) {
            buffer.putChar(s.charAt(i));
        }
    }

    public static String readString(DataSource buffer) {
        char[] c = readCharArray(buffer);
        return new String(c).intern();
    }

    public static void write(boolean[] array, DataSink buffer) {
        buffer.putInt(array.length);
        for (boolean z : array) {
            buffer.putBoolean(z);
        }
    }

    public static boolean[] readBooleanArray(DataSource buffer) {
        boolean[] array = new boolean[buffer.getInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = buffer.getBoolean();
        }
        return array;
    }

    public static void write(float[] array, DataSink buffer) {
        buffer.putInt(array.length);
        for (float f : array) {
            buffer.putFloat(f);
        }
    }

    public static float[] readFloatArray(DataSource buffer) {
        float[] array = new float[buffer.getInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = buffer.getFloat();
        }
        return array;
    }

    public static char[] readCharArray(DataSource buffer) {
        char[] array = new char[buffer.getInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = buffer.getChar();
        }
        return array;
    }

    public static void encode(Vector3f tuple, DataSink buffer) {
        buffer.putFloat(tuple.getX());
        buffer.putFloat(tuple.getY());
        buffer.putFloat(tuple.getZ());
    }

    public static Vector3f decodeVector3f(DataSource buffer, Vector3f tuple) {
        if (tuple == null) {
            tuple = new Vector3f();
        }
        tuple.x = buffer.getFloat();
        tuple.y = buffer.getFloat();
        tuple.z = buffer.getFloat();
        return tuple;
    }

    public static void encode(Vector2f tuple, DataSink buffer) {
        buffer.putFloat(tuple.x);
        buffer.putFloat(tuple.y);
    }

    public static Vector2f decodeTuple2f(DataSource buffer, Vector2f tuple) {
        if (tuple == null) {
            tuple = new Vector2f();
        }
        tuple.x = buffer.getFloat();
        tuple.y = buffer.getFloat();
        return tuple;
    }
}
