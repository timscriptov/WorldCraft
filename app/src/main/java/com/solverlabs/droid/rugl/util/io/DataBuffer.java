package com.solverlabs.droid.rugl.util.io;

import java.nio.ByteBuffer;

/**
 * Wraps a {@link ByteBuffer} to restrict people monkeying with the
 * position, data that don't concern 'em, etc
 *
 * @author ryanm
 */
public class DataBuffer implements DataSource, DataSink {
    /**
     * byte constant for <code>false</code>
     */
    private static final byte FALSE = (byte) 0;
    /**
     * byte constant for <code>true</code>
     */
    private static final byte TRUE = (byte) 1;
    private final ByteBuffer delegate;

    /**
     * @param buffy
     */
    public DataBuffer(ByteBuffer buffy) {
        delegate = buffy;
    }

    @Override
    public boolean getBoolean() {
        return delegate.get() == TRUE;
    }

    @Override
    public byte getByte() {
        return delegate.get();
    }

    @Override
    public char getChar() {
        return delegate.getChar();
    }

    @Override
    public double getDouble() {
        return delegate.getDouble();
    }

    @Override
    public float getFloat() {
        return delegate.getFloat();
    }

    @Override
    public int getInt() {
        return delegate.getInt();
    }

    @Override
    public long getLong() {
        return delegate.getLong();
    }

    @Override
    public short getShort() {
        return delegate.getShort();
    }

    @Override
    public void putBoolean(boolean b) {
        delegate.put(b ? TRUE : FALSE);
    }

    @Override
    public void putByte(byte b) {
        delegate.put(b);
    }

    @Override
    public void putChar(char value) {
        delegate.putChar(value);
    }

    @Override
    public void putDouble(double value) {
        delegate.putDouble(value);
    }

    @Override
    public void putFloat(float value) {
        delegate.putFloat(value);
    }

    @Override
    public void putInt(int value) {
        delegate.putInt(value);
    }

    @Override
    public void putLong(long value) {
        delegate.putLong(value);
    }

    @Override
    public void putShort(short value) {
        delegate.putShort(value);
    }
}
