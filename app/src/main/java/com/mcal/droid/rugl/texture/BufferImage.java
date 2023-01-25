package com.mcal.droid.rugl.texture;

import android.opengl.GLES10;

import com.mcal.droid.rugl.gl.BufferUtils;
import com.mcal.droid.rugl.gl.GLUtil;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * An image based on a bytebuffer
 */
public class BufferImage extends Image {
    /**
     * The image data
     */
    public final ByteBuffer data;

    /**
     * @param width
     * @param height
     * @param format
     * @param data
     */
    public BufferImage(int width, int height, Format format, ByteBuffer data) {
        super(width, height, format);
        this.data = data;
    }

    /**
     * Constructs an image from the data in the buffer, which is of the
     * format "int:width int:height int:formatOrdinal data"
     *
     * @param input contains the image data
     */
    public BufferImage(ByteBuffer input) {
        super(input.getInt(), input.getInt(), Format.values()[input.getInt()]);

        data = input.slice();
        data.limit(width * height * format.bytes);
    }

    /**
     * Reads an image from a stream
     *
     * @param is
     * @throws IOException
     */
    public BufferImage(InputStream is) throws IOException {
        super(is);

        DataInputStream dis = new DataInputStream(is);

        byte[] bd = new byte[width * height * format.bytes];

        dis.readFully(bd);

        data = BufferUtils.createByteBuffer(bd.length);
        data.put(bd);
        data.flip();

        assert width * height * format.bytes == data.capacity();
    }

    /**
     * Write the image to a buffer, in a format suitable for reading
     * with {@link #BufferImage(ByteBuffer)}
     *
     * @param output The buffer to write to
     */
    public void write(ByteBuffer output) {
        output.putInt(width);
        output.putInt(height);
        output.putInt(format.ordinal());

        output.put(data);
    }

    /**
     * Saves the image to a file
     *
     * @param fileName
     * @throws IOException
     */
    public void write(String fileName) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
        FileChannel ch = raf.getChannel();
        int fileLength = dataSize();
        raf.setLength(fileLength);
        MappedByteBuffer buffer = ch.map(FileChannel.MapMode.READ_WRITE, 0, fileLength);

        write(buffer);

        buffer.force();
        ch.close();
    }

    /**
     * Calculates the size of buffer needed to store this image
     *
     * @return The necessary number of bytes
     */
    public int dataSize() {
        return data.capacity() + 3 * 4;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BufferImage) {
            BufferImage i = (BufferImage) obj;
            if (format == i.format && height == i.height && width == i.width
                    && data.capacity() == i.data.capacity()) {
                // check image data
                for (int j = 0; j < data.capacity(); j++) {
                    if (data.get(j) != i.data.get(j)) {
                        return false;
                    }
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public void writeToTexture(int x, int y) {
        GLES10.glPixelStorei(GLES10.GL_UNPACK_ALIGNMENT, format.bytes);

        GLES10.glTexSubImage2D(GLES10.GL_TEXTURE_2D, 0, x, y, width, height,
                format.glFormat, GLES10.GL_UNSIGNED_BYTE, data);

        GLUtil.checkGLError();
    }
}