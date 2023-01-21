package com.solverlabs.droid.rugl.texture;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/* loaded from: classes.dex */
public abstract class Image {
    static final /* synthetic */ boolean $assertionsDisabled;

    static {
        $assertionsDisabled = !Image.class.desiredAssertionStatus();
    }

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
        if (!$assertionsDisabled && (formatOrdinal < 0 || formatOrdinal >= Format.values().length)) {
            throw new AssertionError("Format with ordinal " + formatOrdinal + " not found");
        }
        this.format = Format.values()[formatOrdinal];
    }

    public static Image loadImage(String fileName) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(fileName, "r");
        FileChannel ch = raf.getChannel();
        MappedByteBuffer buffer = ch.map(FileChannel.MapMode.READ_ONLY, 0L, raf.length());
        Image i = new BufferImage(buffer);
        ch.close();
        return i;
    }

    public abstract void writeToTexture(int i, int i2);

    public String toString() {
        return "Image " + this.format + " " + this.width + "x" + this.height;
    }

    /* loaded from: classes.dex */
    public enum Format {
        RGBA(4, 6408) { // from class: com.solverlabs.droid.rugl.texture.Image.Format.1

            @Override // com.solverlabs.droid.rugl.texture.Image.Format
            public void write(int pixel, ByteBuffer buffer) {
                buffer.put((byte) ((pixel >> 16) & 255));
                buffer.put((byte) ((pixel >> 8) & 255));
                buffer.put((byte) ((pixel >> 0) & 255));
                buffer.put((byte) ((pixel >> 24) & 255));
            }
        },
        LUMINANCE_ALPHA(2, 6410) { // from class: com.solverlabs.droid.rugl.texture.Image.Format.2

            @Override // com.solverlabs.droid.rugl.texture.Image.Format
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
