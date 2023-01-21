package com.solverlabs.droid.rugl.util.io;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.util.geom.Vector2f;
import com.solverlabs.droid.rugl.util.geom.Vector3f;

public class SerialUtils {
    public static void write(@NonNull int[] array, @NonNull DataSink buffer) {
        buffer.putInt(array.length);
        for (int i : array) {
            buffer.putInt(i);
        }
    }

    @NonNull
    public static int[] readIntArray(@NonNull DataSource buffer) {
        int[] array = new int[buffer.getInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = buffer.getInt();
        }
        return array;
    }

    public static void write(@NonNull short[] array, @NonNull DataSink buffer) {
        buffer.putInt(array.length);
        for (short s : array) {
            buffer.putShort(s);
        }
    }

    @NonNull
    public static short[] readShortArray(@NonNull DataSource buffer) {
        short[] array = new short[buffer.getInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = buffer.getShort();
        }
        return array;
    }

    public static void write(@NonNull String s, @NonNull DataSink buffer) {
        buffer.putInt(s.length());
        for (int i = 0; i < s.length(); i++) {
            buffer.putChar(s.charAt(i));
        }
    }

    @NonNull
    public static String readString(DataSource buffer) {
        char[] c = readCharArray(buffer);
        return new String(c).intern();
    }

    public static void write(@NonNull boolean[] array, @NonNull DataSink buffer) {
        buffer.putInt(array.length);
        for (boolean z : array) {
            buffer.putBoolean(z);
        }
    }

    @NonNull
    public static boolean[] readBooleanArray(@NonNull DataSource buffer) {
        boolean[] array = new boolean[buffer.getInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = buffer.getBoolean();
        }
        return array;
    }

    public static void write(@NonNull float[] array, @NonNull DataSink buffer) {
        buffer.putInt(array.length);
        for (float f : array) {
            buffer.putFloat(f);
        }
    }

    @NonNull
    public static float[] readFloatArray(@NonNull DataSource buffer) {
        float[] array = new float[buffer.getInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = buffer.getFloat();
        }
        return array;
    }

    @NonNull
    public static char[] readCharArray(@NonNull DataSource buffer) {
        char[] array = new char[buffer.getInt()];
        for (int i = 0; i < array.length; i++) {
            array[i] = buffer.getChar();
        }
        return array;
    }

    public static void encode(@NonNull Vector3f tuple, @NonNull DataSink buffer) {
        buffer.putFloat(tuple.getX());
        buffer.putFloat(tuple.getY());
        buffer.putFloat(tuple.getZ());
    }

    @NonNull
    public static Vector3f decodeVector3f(DataSource buffer, Vector3f tuple) {
        if (tuple == null) {
            tuple = new Vector3f();
        }
        tuple.x = buffer.getFloat();
        tuple.y = buffer.getFloat();
        tuple.z = buffer.getFloat();
        return tuple;
    }

    public static void encode(@NonNull Vector2f tuple, @NonNull DataSink buffer) {
        buffer.putFloat(tuple.x);
        buffer.putFloat(tuple.y);
    }

    @NonNull
    public static Vector2f decodeTuple2f(DataSource buffer, Vector2f tuple) {
        if (tuple == null) {
            tuple = new Vector2f();
        }
        tuple.x = buffer.getFloat();
        tuple.y = buffer.getFloat();
        return tuple;
    }
}
