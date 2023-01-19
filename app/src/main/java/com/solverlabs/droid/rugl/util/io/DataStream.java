package com.solverlabs.droid.rugl.util.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Wraps streams as a {@link DataSource} and {@link DataSink}. Any
 * {@link IOException}s that occur may be printed to stderr, and saved
 * for later retrieval, see {@link #printErrors},
 * {@link #checkError()} and {@link #clearError()}
 *
 * @author ryanm
 */
public class DataStream implements DataSource, DataSink {
    /**
     * Determines if {@link IOException}s are printed to stderr when
     * they occur. Defaults to <code>true</code>.
     */
    public boolean printErrors = true;
    private DataOutputStream dos;
    private DataInputStream dis;
    private IOException[] errors = new IOException[2];

    /**
     * Wraps an {@link OutputStream}. Any attempt to read data from
     * this {@link DataStream} will result in a
     * {@link NullPointerException}
     *
     * @param os
     */
    public DataStream(OutputStream os) {
        dos = new DataOutputStream(os);
    }

    /**
     * Wraps an {@link InputStream}. Any attempt to write data to this
     * {@link DataStream} will result in a {@link NullPointerException}
     *
     * @param is
     */
    public DataStream(InputStream is) {
        dis = new DataInputStream(is);
    }

    /**
     * Wraps an {@link InputStream} and an {@link OutputStream}
     *
     * @param is
     * @param os
     */
    public DataStream(InputStream is, OutputStream os) {
        dis = new DataInputStream(is);
        dos = new DataOutputStream(os);
    }

    /**
     * Checks if an error has occurred since the last time
     * {@link #clearError()} was called
     *
     * @return <code>true</code> if a new error has occurred,
     * <code>false</code> otherwise
     */
    public boolean checkError() {
        return errors[0] != null;
    }

    /**
     * Clears and retrieves the error status
     *
     * @return An array containing the earliest and latest exceptions
     * to occur.
     */
    public IOException[] clearError() {
        IOException[] e = new IOException[]{errors[0], errors[1]};

        errors[0] = null;
        errors[1] = null;

        return e;
    }

    private void logError(IOException ioe) {
        if (printErrors) {
            ioe.printStackTrace();
        }

        if (errors[0] == null) {
            errors[0] = ioe;
        }

        errors[1] = ioe;
    }

    @Override
    public boolean getBoolean() {
        try {
            return dis.readBoolean();
        } catch (IOException e) {
            logError(e);
        }

        return false;
    }

    @Override
    public byte getByte() {
        try {
            return dis.readByte();
        } catch (IOException e) {
            logError(e);
        }

        return 0;
    }

    @Override
    public char getChar() {
        try {
            return dis.readChar();
        } catch (IOException e) {
            logError(e);
        }

        return 0;
    }

    @Override
    public double getDouble() {
        try {
            return dis.readDouble();
        } catch (IOException e) {
            logError(e);
        }

        return 0;
    }

    @Override
    public float getFloat() {
        try {
            return dis.readFloat();
        } catch (IOException e) {
            logError(e);
        }

        return 0;
    }

    @Override
    public int getInt() {
        try {
            return dis.readInt();
        } catch (IOException e) {
            logError(e);
        }

        return 0;
    }

    @Override
    public long getLong() {
        try {
            return dis.readLong();
        } catch (IOException e) {
            logError(e);
        }

        return 0;
    }

    @Override
    public short getShort() {
        try {
            return dis.readShort();
        } catch (IOException e) {
            logError(e);
        }

        return 0;
    }

    @Override
    public void putBoolean(boolean b) {
        try {
            dos.writeBoolean(b);
        } catch (IOException e) {
            logError(e);
        }
    }

    @Override
    public void putByte(byte b) {
        try {
            dos.writeByte(b);
        } catch (IOException e) {
            logError(e);
        }
    }

    @Override
    public void putChar(char c) {
        try {
            dos.writeChar(c);
        } catch (IOException e) {
            logError(e);
        }
    }

    @Override
    public void putDouble(double d) {
        try {
            dos.writeDouble(d);
        } catch (IOException e) {
            logError(e);
        }
    }

    @Override
    public void putFloat(float f) {
        try {
            dos.writeFloat(f);
        } catch (IOException e) {
            logError(e);
        }
    }

    @Override
    public void putInt(int i) {
        try {
            dos.writeInt(i);
        } catch (IOException e) {
            logError(e);
        }
    }

    @Override
    public void putLong(long l) {
        try {
            dos.writeLong(l);
        } catch (IOException e) {
            logError(e);
        }
    }

    @Override
    public void putShort(short s) {
        try {
            dos.writeShort(s);
        } catch (IOException e) {
            logError(e);
        }
    }
}
