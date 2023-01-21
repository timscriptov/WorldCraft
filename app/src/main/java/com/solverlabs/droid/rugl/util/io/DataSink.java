package com.solverlabs.droid.rugl.util.io;

/**
 * A write-only interface for a data sink
 *
 * @author ryanm
 */
public interface DataSink {
    /**
     * Puts a boolean into the sink
     *
     * @param b
     */
    public void putBoolean(boolean b);

    /**
     * Puts a byte into the sink
     *
     * @param b
     */
    public void putByte(byte b);

    /**
     * Puts a char into the sink
     *
     * @param c
     */
    public void putChar(char c);

    /**
     * Puts a double into the sink
     *
     * @param d
     */
    public void putDouble(double d);

    /**
     * Puts a float into the sink
     *
     * @param f
     */
    public void putFloat(float f);

    /**
     * Puts a int into the sink
     *
     * @param i
     */
    public void putInt(int i);

    /**
     * Puts a long into the sink
     *
     * @param l
     */
    public void putLong(long l);

    /**
     * Puts a short into the sink
     *
     * @param s
     */
    public void putShort(short s);

}