package com.mcal.droid.rugl.texture;

import android.opengl.GLES10;

import androidx.annotation.NonNull;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import javax.microedition.khronos.opengles.GL10;


/**
 * Represents a chunk of image data.
 */
public abstract class Image {
    /**
     * The image format
     */
    public final Format format;
    /**
     * The image width
     */
    public final int width;
    /**
     * The image height
     */
    public final int height;

    /**
     * @param width
     * @param height
     * @param format
     */
    protected Image(final int width, final int height, final Format format) {
        this.width = width;
        this.height = height;
        this.format = format;
    }

    /**
     * Reads an image from a stream
     *
     * @param is
     * @throws IOException
     */
    protected Image(final InputStream is) throws IOException {
        final DataInputStream dis = new DataInputStream(is);

        width = dis.readInt();
        height = dis.readInt();

        final int formatOrdinal = dis.readInt();
        assert formatOrdinal >= 0 && formatOrdinal < Format.values().length : "Format with ordinal "
                + formatOrdinal + " not found";
        format = Format.values()[formatOrdinal];
    }

    /**
     * Reads an image from a file
     *
     * @param fileName
     * @return An {@link Image}
     * @throws IOException
     */
    @NonNull
    public static Image loadImage(final String fileName) throws IOException {
        final RandomAccessFile raf = new RandomAccessFile(fileName, "r");
        final FileChannel ch = raf.getChannel();
        final MappedByteBuffer buffer =
                ch.map(FileChannel.MapMode.READ_ONLY, 0, raf.length());

        final Image i = new BufferImage(buffer);

        ch.close();

        return i;
    }

    /**
     * Write the image to a texture
     *
     * @param x the x-coord of where to insert
     * @param y The y-coord of where to insert
     */
    public abstract void writeToTexture(int x, int y);

    @Override
    public String toString() {
        return "Image " + format + " " + width + "x" + height;
    }

    /**
     * Flags for different image formats
     */
    public static enum Format {
        /**
         * Uses 8 bits for each component
         */
        RGBA(4, GLES10.GL_RGBA) {
            @Override
            public void write(final int pixel, final ByteBuffer buffer) {
                buffer.put((byte) (pixel >> 16 & 0xFF));
                buffer.put((byte) (pixel >> 8 & 0xFF));
                buffer.put((byte) (pixel >> 0 & 0xFF));
                buffer.put((byte) (pixel >> 24 & 0xFF));
            }
        },

        /**
         * Uses 8 bits for each component.
         */
        LUMINANCE_ALPHA(2, GL10.GL_LUMINANCE_ALPHA) {
            @Override
            public void write(final int pixel, final ByteBuffer buffer) {
                buffer.put((byte) (pixel >> 8 & 0xFF));
                buffer.put((byte) (pixel >> 24 & 0xFF));
            }
        };

        /**
         * The number of bytes that the format uses per pixel
         */
        public final int bytes;

        /**
         * The OpenGL format, e.g.: GL_RGBA, GL_LUMINANCE_ALPHA
         */
        public final int glFormat;

        private Format(final int bytes, final int glFormat) {
            this.bytes = bytes;
            this.glFormat = glFormat;
        }

        /**
         * Writes the appropriate values to a buffer for a given pixel
         *
         * @param argb   A pixel, in packed argb form
         * @param buffer The buffer to write to
         */
        public abstract void write(int argb, ByteBuffer buffer);
    }
}