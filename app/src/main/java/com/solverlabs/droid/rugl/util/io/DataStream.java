package com.solverlabs.droid.rugl.util.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class DataStream implements DataSource, DataSink {
    public boolean printErrors;
    private DataInputStream dis;
    private DataOutputStream dos;
    private IOException[] errors;

    public DataStream(OutputStream os) {
        this.errors = new IOException[2];
        this.printErrors = true;
        this.dos = new DataOutputStream(os);
    }

    public DataStream(InputStream is) {
        this.errors = new IOException[2];
        this.printErrors = true;
        this.dis = new DataInputStream(is);
    }

    public DataStream(InputStream is, OutputStream os) {
        this.errors = new IOException[2];
        this.printErrors = true;
        this.dis = new DataInputStream(is);
        this.dos = new DataOutputStream(os);
    }

    public boolean checkError() {
        return this.errors[0] != null;
    }

    public IOException[] clearError() {
        IOException[] e = {this.errors[0], this.errors[1]};
        this.errors[0] = null;
        this.errors[1] = null;
        return e;
    }

    private void logError(IOException ioe) {
        if (this.printErrors) {
            ioe.printStackTrace();
        }
        if (this.errors[0] == null) {
            this.errors[0] = ioe;
        }
        this.errors[1] = ioe;
    }

    @Override
    public boolean getBoolean() {
        try {
            return this.dis.readBoolean();
        } catch (IOException e) {
            logError(e);
            return false;
        }
    }

    @Override
    public byte getByte() {
        try {
            return this.dis.readByte();
        } catch (IOException e) {
            logError(e);
            return (byte) 0;
        }
    }

    @Override
    public char getChar() {
        try {
            return this.dis.readChar();
        } catch (IOException e) {
            logError(e);
            return (char) 0;
        }
    }

    @Override
    public double getDouble() {
        try {
            return this.dis.readDouble();
        } catch (IOException e) {
            logError(e);
            return 0.0d;
        }
    }

    @Override
    public float getFloat() {
        try {
            return this.dis.readFloat();
        } catch (IOException e) {
            logError(e);
            return 0.0f;
        }
    }

    @Override
    public int getInt() {
        try {
            return this.dis.readInt();
        } catch (IOException e) {
            logError(e);
            return 0;
        }
    }

    @Override
    public long getLong() {
        try {
            return this.dis.readLong();
        } catch (IOException e) {
            logError(e);
            return 0L;
        }
    }

    @Override
    public short getShort() {
        try {
            return this.dis.readShort();
        } catch (IOException e) {
            logError(e);
            return (short) 0;
        }
    }

    @Override
    public void putBoolean(boolean b) {
        try {
            this.dos.writeBoolean(b);
        } catch (IOException e) {
            logError(e);
        }
    }

    @Override
    public void putByte(byte b) {
        try {
            this.dos.writeByte(b);
        } catch (IOException e) {
            logError(e);
        }
    }

    @Override
    public void putChar(char c) {
        try {
            this.dos.writeChar(c);
        } catch (IOException e) {
            logError(e);
        }
    }

    @Override
    public void putDouble(double d) {
        try {
            this.dos.writeDouble(d);
        } catch (IOException e) {
            logError(e);
        }
    }

    @Override
    public void putFloat(float f) {
        try {
            this.dos.writeFloat(f);
        } catch (IOException e) {
            logError(e);
        }
    }

    @Override
    public void putInt(int i) {
        try {
            this.dos.writeInt(i);
        } catch (IOException e) {
            logError(e);
        }
    }

    @Override
    public void putLong(long l) {
        try {
            this.dos.writeLong(l);
        } catch (IOException e) {
            logError(e);
        }
    }

    @Override
    public void putShort(short s) {
        try {
            this.dos.writeShort(s);
        } catch (IOException e) {
            logError(e);
        }
    }
}
