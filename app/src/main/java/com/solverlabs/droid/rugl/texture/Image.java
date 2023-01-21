package com.solverlabs.droid.rugl.texture;

import androidx.annotation.NonNull;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;


public abstract class Image {
    static final boolean assertionsDisabled = !Image.class.desiredAssertionStatus();

    public final Format format;
    public final int height;
    public final int width;

    public Image(int width, int height, Format format) {
        this.width = width;
        this.height = height;
        this.format = format;
    }

    public Image(InputStream is) throws IOException {
        DataInputStream dis = new DataInputStream(is);
        this.width = dis.readInt();
        this.height = dis.readInt();
        int formatOrdinal = dis.readInt();
        if (assertionsDisabled || (formatOrdinal >= 0 && formatOrdinal < Format.values().length)) {
            this.format = Format.values()[formatOrdinal];
            return;
        }
        throw new AssertionError("Format with ordinal " + formatOrdinal + " not found");
    }

    @NonNull
    public static Image loadImage(String fileName) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(fileName, "r");
        FileChannel ch = raf.getChannel();
        MappedByteBuffer buffer = ch.map(FileChannel.MapMode.READ_ONLY, 0L, raf.length());
        Image i = new BufferImage(buffer);
        ch.close();
        return i;
    }

    public abstract void writeToTexture(int i, int i2);

    @NonNull
    public String toString() {
        return "Image " + this.format + " " + this.width + "x" + this.height;
    }


    public enum Format {
        RGBA(4, 6408) {
            @Override
            public void write(int pixel, ByteBuffer buffer) {
                buffer.put((byte) ((pixel >> 16) & 255));
                buffer.put((byte) ((pixel >> 8) & 255));
                buffer.put((byte) ((pixel >> 0) & 255));
                buffer.put((byte) ((pixel >> 24) & 255));
            }
        },
        LUMINANCE_ALPHA(2, 6410) {
            @Override
            public void write(int pixel, ByteBuffer buffer) {
                buffer.put((byte) ((pixel >> 8) & 255));
                buffer.put((byte) ((pixel >> 24) & 255));
            }
        };

        public final int bytes;
        public final int glFormat;

        Format(int bytes, int glFormat) {
            this.bytes = bytes;
            this.glFormat = glFormat;
        }

        public abstract void write(int i, ByteBuffer byteBuffer);
    }
}
