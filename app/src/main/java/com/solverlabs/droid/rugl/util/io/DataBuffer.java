package com.solverlabs.droid.rugl.util.io;

import java.nio.ByteBuffer;


public class DataBuffer implements DataSource, DataSink {
    private static final byte FALSE = 0;
    private static final byte TRUE = 1;
    private final ByteBuffer delegate;

    public DataBuffer(ByteBuffer buffy) {
        this.delegate = buffy;
    }

    @Override
    public boolean getBoolean() {
        return this.delegate.get() == 1;
    }

    @Override
    public byte getByte() {
        return this.delegate.get();
    }

    @Override
    public char getChar() {
        return this.delegate.getChar();
    }

    @Override
    public double getDouble() {
        return this.delegate.getDouble();
    }

    @Override
    public float getFloat() {
        return this.delegate.getFloat();
    }

    @Override
    public int getInt() {
        return this.delegate.getInt();
    }

    @Override
    public long getLong() {
        return this.delegate.getLong();
    }

    @Override
    public short getShort() {
        return this.delegate.getShort();
    }

    @Override
    public void putBoolean(boolean b) {
        this.delegate.put(b ? (byte) 1 : (byte) 0);
    }

    @Override
    public void putByte(byte b) {
        this.delegate.put(b);
    }

    @Override
    public void putChar(char value) {
        this.delegate.putChar(value);
    }

    @Override
    public void putDouble(double value) {
        this.delegate.putDouble(value);
    }

    @Override
    public void putFloat(float value) {
        this.delegate.putFloat(value);
    }

    @Override
    public void putInt(int value) {
        this.delegate.putInt(value);
    }

    @Override
    public void putLong(long value) {
        this.delegate.putLong(value);
    }

    @Override
    public void putShort(short value) {
        this.delegate.putShort(value);
    }
}
