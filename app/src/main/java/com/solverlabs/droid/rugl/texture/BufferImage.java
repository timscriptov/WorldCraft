package com.solverlabs.droid.rugl.texture;

import android.opengl.GLES10;

import com.solverlabs.droid.rugl.gl.BufferUtils;
import com.solverlabs.droid.rugl.gl.GLUtil;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;


public class BufferImage extends Image {
    static final /* synthetic */ boolean $assertionsDisabled;

    static {
        $assertionsDisabled = !BufferImage.class.desiredAssertionStatus();
    }

    public final ByteBuffer data;

    public BufferImage(int width, int height, Image.Format format, ByteBuffer data) {
        super(width, height, format);
        this.data = data;
    }

    public BufferImage(ByteBuffer input) {
        super(input.getInt(), input.getInt(), Image.Format.values()[input.getInt()]);
        this.data = input.slice();
        this.data.limit(this.width * this.height * this.format.bytes);
    }

    public BufferImage(InputStream is) throws IOException {
        super(is);
        DataInputStream dis = new DataInputStream(is);
        byte[] bd = new byte[this.width * this.height * this.format.bytes];
        dis.readFully(bd);
        this.data = BufferUtils.createByteBuffer(bd.length);
        this.data.put(bd);
        this.data.flip();
        if ($assertionsDisabled || this.width * this.height * this.format.bytes == this.data.capacity()) {
            return;
        }
        throw new AssertionError();
    }

    public void write(ByteBuffer output) {
        output.putInt(this.width);
        output.putInt(this.height);
        output.putInt(this.format.ordinal());
        output.put(this.data);
    }

    public void write(String fileName) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
        FileChannel ch = raf.getChannel();
        int fileLength = dataSize();
        raf.setLength(fileLength);
        MappedByteBuffer buffer = ch.map(FileChannel.MapMode.READ_WRITE, 0L, fileLength);
        write(buffer);
        buffer.force();
        ch.close();
    }

    public int dataSize() {
        return this.data.capacity() + 12;
    }

    public boolean equals(Object obj) {
        if (obj instanceof BufferImage) {
            BufferImage i = (BufferImage) obj;
            if (this.format != i.format || this.height != i.height || this.width != i.width || this.data.capacity() != i.data.capacity()) {
                return false;
            }
            for (int j = 0; j < this.data.capacity(); j++) {
                if (this.data.get(j) != i.data.get(j)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void writeToTexture(int x, int y) {
        GLES10.glPixelStorei(3317, this.format.bytes);
        GLES10.glTexSubImage2D(3553, 0, x, y, this.width, this.height, this.format.glFormat, 5121, this.data);
        GLUtil.checkGLError();
    }
}
