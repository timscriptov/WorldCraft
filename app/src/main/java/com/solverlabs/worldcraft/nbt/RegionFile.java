package com.solverlabs.worldcraft.nbt;

import androidx.annotation.NonNull;

import com.solverlabs.worldcraft.util.HttpPostBodyUtil2;

import org.apache.commons.compress.archivers.cpio.CpioConstants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public class RegionFile {
    static final int CHUNK_HEADER_SIZE = 5;
    private static final int SECTOR_BYTES = 4096;
    private static final int SECTOR_INTS = 1024;
    private static final int VERSION_DEFLATE = 2;
    private static final int VERSION_GZIP = 1;
    private static final byte[] emptySector = new byte[4096];
    private final File fileName;
    private final ByteBuffer intBuffer = ByteBuffer.allocate(4);
    private final ByteBuffer byteBuffer = ByteBuffer.allocate(1);
    private final ByteBuffer readBuffer = ByteBuffer.allocate(4096);
    private final int[] offsets = new int[1024];
    private final int[] chunkTimestamps = new int[1024];
    private RandomAccessFile file;
    private FileChannel fileCh;
    private long lastModified;
    private ArrayList<Boolean> sectorFree;
    private int sizeDelta;

    public RegionFile(@NonNull File path) {
        this.lastModified = 0L;
        this.fileName = path;
        debugln("REGION LOAD " + this.fileName);
        this.sizeDelta = 0;
        this.intBuffer.clear();
        this.intBuffer.asIntBuffer().put(0);
        this.intBuffer.position(0);
        this.byteBuffer.clear();
        this.byteBuffer.put((byte) 0);
        this.byteBuffer.position(0);
        try {
            if (path.exists()) {
                this.lastModified = path.lastModified();
            }
            this.file = new RandomAccessFile(path, "rw");
            this.fileCh = this.file.getChannel();
            if (this.file.length() < 4096) {
                for (int i = 0; i < 1024; i++) {
                    this.file.writeInt(0);
                }
                for (int i2 = 0; i2 < 1024; i2++) {
                    this.file.writeInt(0);
                }
                this.sizeDelta += CpioConstants.C_ISCHR;
            }
            if ((this.file.length() & 4095) != 0) {
                for (int i3 = 0; i3 < (this.file.length() & 4095); i3++) {
                    this.fileCh.write(this.byteBuffer);
                }
            }
            int nSectors = ((int) this.file.length()) / 4096;
            this.sectorFree = new ArrayList<>(nSectors);
            for (int i4 = 0; i4 < nSectors; i4++) {
                this.sectorFree.add(Boolean.TRUE);
            }
            this.sectorFree.set(0, Boolean.FALSE);
            this.sectorFree.set(1, Boolean.FALSE);
            this.file.seek(0L);
            this.fileCh.position(0L);
            this.readBuffer.clear();
            this.fileCh.read(this.readBuffer);
            this.readBuffer.rewind();
            for (int i5 = 0; i5 < 1024; i5++) {
                int offset = this.readBuffer.getInt();
                this.offsets[i5] = offset;
                if (offset != 0 && (offset >> 8) + (offset & 255) <= this.sectorFree.size()) {
                    for (int sectorNum = 0; sectorNum < (offset & 255); sectorNum++) {
                        this.sectorFree.set((offset >> 8) + sectorNum, Boolean.FALSE);
                    }
                }
            }
            for (int i6 = 0; i6 < 1024; i6++) {
                int lastModValue = this.file.readInt();
                this.chunkTimestamps[i6] = lastModValue;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean outOfBounds(int x, int z) {
        return x < 0 || x >= 32 || z < 0 || z >= 32;
    }

    public long lastModified() {
        return this.lastModified;
    }

    public synchronized int getSizeDelta() {
        int ret;
        ret = this.sizeDelta;
        this.sizeDelta = 0;
        return ret;
    }

    private void debugln(String in) {
    }

    private void debug(String mode, int x, int z, int count, String in) {
    }

    private void debugln(String mode, int x, int z, String in) {
    }

    public synchronized DataInputStream getChunkDataInputStream(int x, int z) {
        DataInputStream dataInputStream;
        if (outOfBounds(x, z)) {
            debugln("READ", x, z, "out of bounds");
            dataInputStream = null;
        } else {
            try {
                int offset = getOffset(x, z);
                if (offset == 0) {
                    dataInputStream = null;
                } else {
                    int sectorNumber = offset >> 8;
                    int numSectors = offset & 255;
                    if (sectorNumber + numSectors > this.sectorFree.size()) {
                        debugln("READ", x, z, "invalid sector");
                        dataInputStream = null;
                    } else {
                        this.fileCh.position(sectorNumber * 4096L);
                        this.intBuffer.clear();
                        this.fileCh.read(this.intBuffer);
                        this.intBuffer.rewind();
                        int length = this.intBuffer.getInt();
                        if (length > numSectors * 4096) {
                            debugln("READ", x, z, "invalid length: " + length + " > 4096 * " + numSectors);
                            dataInputStream = null;
                        } else {
                            this.byteBuffer.clear();
                            this.fileCh.read(this.byteBuffer);
                            this.byteBuffer.rewind();
                            byte version = this.byteBuffer.get();
                            if (version == 1) {
                                ByteBuffer byteBuffer = ByteBuffer.allocate(length - 1);
                                this.fileCh.read(byteBuffer);
                                dataInputStream = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(byteBuffer.array())));
                            } else if (version == 2) {
                                ByteBuffer byteBuffer2 = ByteBuffer.allocate(length - 1);
                                this.fileCh.read(byteBuffer2);
                                dataInputStream = new DataInputStream(new InflaterInputStream(new ByteArrayInputStream(byteBuffer2.array())));
                            } else {
                                debugln("READ", x, z, "unknown version " + ((int) version));
                                dataInputStream = null;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                debugln("READ", x, z, "exception");
                dataInputStream = null;
            }
        }
        return dataInputStream;
    }

    public DataOutputStream getChunkDataOutputStream(int x, int z) {
        if (outOfBounds(x, z)) {
            return null;
        }
        return new DataOutputStream(new DeflaterOutputStream(new ChunkBuffer(x, z)));
    }

    public synchronized void write(int x, int z, byte[] data, int length) {
        try {
            int offset = getOffset(x, z);
            int sectorNumber = offset >> 8;
            int sectorsAllocated = offset & 255;
            int sectorsNeeded = ((length + 5) / 4096) + 1;
            if (sectorsNeeded < 256) {
                if (sectorNumber != 0 && sectorsAllocated == sectorsNeeded) {
                    debug("SAVE", x, z, length, "rewrite");
                    write(sectorNumber, data, length);
                } else {
                    for (int i = 0; i < sectorsAllocated; i++) {
                        this.sectorFree.set(sectorNumber + i, Boolean.TRUE);
                    }
                    int runStart = this.sectorFree.indexOf(Boolean.TRUE);
                    int runLength = 0;
                    if (runStart != -1) {
                        for (int i2 = runStart; i2 < this.sectorFree.size(); i2++) {
                            if (runLength != 0) {
                                if (this.sectorFree.get(i2)) {
                                    runLength++;
                                } else {
                                    runLength = 0;
                                }
                            } else if (this.sectorFree.get(i2)) {
                                runStart = i2;
                                runLength = 1;
                            }
                            if (runLength >= sectorsNeeded) {
                                break;
                            }
                        }
                    }
                    if (runLength >= sectorsNeeded) {
                        debug("SAVE", x, z, length, "reuse");
                        setOffset(x, z, (runStart << 8) | sectorsNeeded);
                        for (int i3 = 0; i3 < sectorsNeeded; i3++) {
                            this.sectorFree.set(runStart + i3, Boolean.FALSE);
                        }
                        write(runStart, data, length);
                    } else {
                        debug("SAVE", x, z, length, "grow");
                        this.file.seek(this.file.length());
                        int sectorNumber3 = this.sectorFree.size();
                        for (int i4 = 0; i4 < sectorsNeeded; i4++) {
                            ByteBuffer.wrap(emptySector);
                            this.file.write(emptySector);
                            this.sectorFree.add(Boolean.FALSE);
                        }
                        this.sizeDelta += sectorsNeeded * 4096;
                        write(sectorNumber3, data, length);
                        setOffset(x, z, (sectorNumber3 << 8) | sectorsNeeded);
                    }
                }
                setTimestamp(x, z, (int) (System.currentTimeMillis() / 1000));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void write(int sectorNumber, byte[] data, int length) throws IOException {
        debugln(" " + sectorNumber);
        this.file.seek(sectorNumber * 4096L);
        this.file.writeInt(length + 1);
        this.file.writeByte(2);
        this.file.write(data, 0, length);
    }

    private int getOffset(int x, int z) {
        return this.offsets[(z * 32) + x];
    }

    public boolean hasChunk(int x, int z) {
        return getOffset(x, z) != 0;
    }

    private void setOffset(int x, int z, int offset) throws IOException {
        this.offsets[(z * 32) + x] = offset;
        this.file.seek(((z * 32L) + x) * 4);
        this.file.writeInt(offset);
    }

    private void setTimestamp(int x, int z, int value) throws IOException {
        this.chunkTimestamps[(z * 32) + x] = value;
        this.file.seek((((z * 32L) + x) * 4) + 4096);
        this.file.writeInt(value);
    }

    public void close() throws IOException {
        this.fileCh.close();
        this.file.close();
    }

    @NonNull
    public String toString() {
        return this.fileName.toString();
    }

    public class ChunkBuffer extends ByteArrayOutputStream {
        private final int x;
        private final int z;

        public ChunkBuffer(int x, int z) {
            super(HttpPostBodyUtil2.chunkSize);
            this.x = x;
            this.z = z;
        }

        @Override
        public void close() {
            try {
                RegionFile.this.write(this.x, this.z, this.buf, this.count);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
