package com.mcal.worldcraft.nbt;

import androidx.annotation.NonNull;

import java.io.Closeable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MemoryRandomAccessFile implements Closeable, DataInput, DataOutput {
    private final RandomAccessFile randomAccessFile;
    MappedByteBuffer buf;

    public MemoryRandomAccessFile(@NonNull File file, String mode) throws IOException {
        int length = (int) file.length();
        randomAccessFile = new RandomAccessFile(file, mode);
        buf = this.randomAccessFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0L, length);
        buf.load();
    }

    @Override
    public void write(byte[] buffer) throws IOException {
    }

    @Override
    public void write(int oneByte) throws IOException {
        buf.put((byte) oneByte);
    }

    @Override
    public void write(byte[] buffer, int offset, int count) throws IOException {
        buf.put(buffer, offset, count);
    }

    @Override
    public void writeBoolean(boolean val) throws IOException {
    }

    @Override
    public void writeByte(int val) throws IOException {
        buf.put((byte) val);
    }

    @Override
    public void writeBytes(String str) throws IOException {
    }

    @Override
    public void writeChar(int val) throws IOException {
    }

    @Override
    public void writeChars(String str) throws IOException {
    }

    @Override
    public void writeDouble(double val) throws IOException {
    }

    @Override
    public void writeFloat(float val) throws IOException {
    }

    @Override
    public void writeInt(int val) throws IOException {
        buf.putInt(val);
    }

    @Override
    public void writeLong(long val) throws IOException {
    }

    @Override
    public void writeShort(int val) throws IOException {
    }

    @Override
    public void writeUTF(String str) throws IOException {
    }

    @Override
    public boolean readBoolean() throws IOException {
        return false;
    }

    @Override
    public byte readByte() throws IOException {
        return buf.get();
    }

    @Override
    public char readChar() throws IOException {
        return (char) 0;
    }

    @Override
    public double readDouble() throws IOException {
        return 0.0d;
    }

    @Override
    public float readFloat() throws IOException {
        return 0.0f;
    }

    @Override
    public void readFully(byte[] dst) throws IOException {
    }

    @Override
    public void readFully(byte[] dst, int offset, int byteCount) throws IOException {
    }

    @Override
    public int readInt() throws IOException {
        return buf.getInt();
    }

    @Override
    public String readLine() throws IOException {
        return null;
    }

    @Override
    public long readLong() throws IOException {
        return 0L;
    }

    @Override
    public short readShort() throws IOException {
        return (short) 0;
    }

    @Override
    public String readUTF() throws IOException {
        return null;
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return 0;
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return 0;
    }

    @Override
    public int skipBytes(int count) throws IOException {
        return 0;
    }

    @Override
    public void close() throws IOException {
        randomAccessFile.close();
    }

    public int length() {
        return buf.limit();
    }

    public void seek(int i) {
        buf.position(i);
    }

    public void read(byte[] data) {
        buf.get(data);
    }
}
