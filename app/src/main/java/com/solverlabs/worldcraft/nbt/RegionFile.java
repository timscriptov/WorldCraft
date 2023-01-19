package com.solverlabs.worldcraft.nbt;

import androidx.annotation.NonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;


/**
 * Region File Format Concept: The minimum unit of storage on hard drives is
 * 4KB. 90% of Minecraft chunks are smaller than 4KB. 99% are smaller than 8KB.
 * Write a simple container to store chunks in single files in runs of 4KB
 * sectors. Each region file represents a 32x32 group of chunks. The conversion
 * from chunk number to region number is floor(coord / 32): a chunk at (30, -3)
 * would be in region (0, -1), and one at (70, -30) would be at (3, -1). Region
 * files are named "r.x.z.data", where x and z are the region coordinates. A
 * region file begins with a 4KB header that describes where chunks are stored
 * in the file. A 4-byte big-endian integer represents sector offsets and sector
 * counts. The chunk offset for a chunk (x, z) begins at byte 4*(x+z*32) in the
 * file. The bottom byte of the chunk offset indicates the number of sectors the
 * chunk takes up, and the top 3 bytes represent the sector number of the chunk.
 * Given a chunk offset o, the chunk data begins at byte 4096*(o/256) and takes
 * up at most 4096*(o%256) bytes. A chunk cannot exceed 1MB in size. If a chunk
 * offset is 0, the corresponding chunk is not stored in the region file. Chunk
 * data begins with a 4-byte big-endian integer representing the chunk data
 * length in bytes, not counting the length field. The length must be smaller
 * than 4096 times the number of sectors. The next byte is a version field, to
 * allow backwards-compatible updates to how chunks are encoded. A version of 1
 * represents a gzipped NBT file. The gzipped data is the chunk length - 1. A
 * version of 2 represents a deflated (zlib compressed) NBT file. The deflated
 * data is the chunk length - 1.
 */
public class RegionFile {
    static final int CHUNK_HEADER_SIZE = 5;
    private static final int VERSION_GZIP = 1;
    private static final int VERSION_DEFLATE = 2;
    private static final int SECTOR_BYTES = 4096;
    private static final int SECTOR_INTS = SECTOR_BYTES / 4;
    private static final byte[] emptySector = new byte[4096];

    private final File fileName;
    private final int[] offsets;
    private final int[] chunkTimestamps;
    private RandomAccessFile file;
    private ArrayList<Boolean> sectorFree;

    private int sizeDelta;

    private long lastModified = 0;

    /**
     * @param path
     */
    public RegionFile(@NonNull final File path) {
        offsets = new int[SECTOR_INTS];
        chunkTimestamps = new int[SECTOR_INTS];

        fileName = path;
        debugln("REGION LOAD " + fileName);

        sizeDelta = 0;

        try {
            if (path.exists())
                lastModified = path.lastModified();

            file = new RandomAccessFile(path, "rw");

            if (file.length() < SECTOR_BYTES) {
                /* we need to write the chunk offset table */
                for (int i = 0; i < SECTOR_INTS; ++i)
                    file.writeInt(0);
                // write another sector for the timestamp info
                for (int i = 0; i < SECTOR_INTS; ++i)
                    file.writeInt(0);

                sizeDelta += SECTOR_BYTES * 2;
            }

            if ((file.length() & 0xfff) != 0)
                /* the file size is not a multiple of 4KB, grow it */
                for (int i = 0; i < (file.length() & 0xfff); ++i)
                    file.write((byte) 0);

            /* set up the available sector map */
            final int nSectors = (int) file.length() / SECTOR_BYTES;
            sectorFree = new ArrayList<>(nSectors);

            for (int i = 0; i < nSectors; ++i)
                sectorFree.add(Boolean.TRUE);

            sectorFree.set(0, Boolean.FALSE); // chunk offset table
            sectorFree.set(1, Boolean.FALSE); // for the last modified
            // info

            file.seek(0);
            for (int i = 0; i < SECTOR_INTS; ++i) {
                final int offset = file.readInt();
                offsets[i] = offset;
                if (offset != 0
                        && (offset >> 8) + (offset & 0xFF) <= sectorFree.size())
                    for (int sectorNum = 0; sectorNum < (offset & 0xFF); ++sectorNum)
                        sectorFree.set((offset >> 8) + sectorNum, Boolean.FALSE);
            }
            for (int i = 0; i < SECTOR_INTS; ++i) {
                final int lastModValue = file.readInt();
                chunkTimestamps[i] = lastModValue;
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * is this an invalid chunk coordinate?
     */
    private static boolean outOfBounds(final int x, final int z) {
        return x < 0 || x >= 32 || z < 0 || z >= 32;
    }

    /**
     * @return the modification date of the region file when it was first opened
     */
    public long lastModified() {
        return lastModified;
    }

    // various small debug printing helpers
    // private void debug( String in )
    // {
    // // Log.i( Game.RUGL_TAG, in );
    // }

    /**
     * @return how much the region file has grown since it was last checked
     */
    public synchronized int getSizeDelta() {
        final int ret = sizeDelta;
        sizeDelta = 0;
        return ret;
    }

    // private void debug( String mode, int x, int z, String in )
    // {
    // // debug( "REGION " + mode + " " + fileName.getName() + "[" + x
    // // + "," + z + "] = "
    // // + in );
    // }

    @SuppressWarnings("unused")
    private void debugln(final String in) {
        // debug( in + "\n" );
    }

    @SuppressWarnings("unused")
    private void debug(final String mode, final int x, final int z,
                       final int count, final String in) {
        // debug( "REGION " + mode + " " + fileName.getName() + "[" + x
        // + "," + z + "] "
        // + count + "B = " + in );
    }

    @SuppressWarnings("unused")
    private void debugln(final String mode, final int x, final int z,
                         final String in) {
        // debug( mode, x, z, in + "\n" );
    }

    /**
     * @param x chunk coordinates <i>in this region</i>
     * @param z chunk coordinates <i>in this region</i>
     * @return an (uncompressed) stream representing the chunk data or
     * <code>null</code> if the chunk is not found or an error occurs
     */
    public synchronized DataInputStream getChunkDataInputStream(final int x,
                                                                final int z) {
        if (outOfBounds(x, z)) {
            debugln("READ", x, z, "out of bounds");
            return null;
        }

        try {
            final int offset = getOffset(x, z);
            if (offset == 0)
                // debugln("READ", x, z, "miss");
                return null;

            final int sectorNumber = offset >> 8;
            final int numSectors = offset & 0xFF;

            if (sectorNumber + numSectors > sectorFree.size()) {
                debugln("READ", x, z, "invalid sector");
                return null;
            }

            file.seek((long) sectorNumber * SECTOR_BYTES);
            final int length = file.readInt();

            if (length > SECTOR_BYTES * numSectors) {
                debugln("READ", x, z, "invalid length: " + length + " > 4096 * "
                        + numSectors);
                return null;
            }

            final byte version = file.readByte();
            if (version == VERSION_GZIP) {
                final byte[] data = new byte[length - 1];
                file.read(data);
                // debug("READ", x, z, " = found");
                return new DataInputStream(new GZIPInputStream(
                        new ByteArrayInputStream(data)));
            } else if (version == VERSION_DEFLATE) {
                final byte[] data = new byte[length - 1];
                file.read(data);
                // debug("READ", x, z, " = found");
                return new DataInputStream(new InflaterInputStream(
                        new ByteArrayInputStream(data)));
            }

            debugln("READ", x, z, "unknown version " + version);
            return null;
        } catch (final IOException e) {
            debugln("READ", x, z, "exception");
            return null;
        }
    }

    /**
     * @param x chunk coordinates
     * @param z chunk coordinates
     * @return A stream to write chunk data to
     */
    public DataOutputStream getChunkDataOutputStream(final int x, final int z) {
        if (outOfBounds(x, z))
            return null;

        return new DataOutputStream(new DeflaterOutputStream(new ChunkBuffer(
                x, z)));
    }

    /* write a chunk at (x,z) with length bytes of data to disk */
    private synchronized void write(final int x, final int z,
                                    final byte[] data, final int length) {
        try {
            final int offset = getOffset(x, z);
            int sectorNumber = offset >> 8;
            final int sectorsAllocated = offset & 0xFF;
            final int sectorsNeeded =
                    (length + CHUNK_HEADER_SIZE) / SECTOR_BYTES + 1;

            // maximum chunk size is 1MB
            if (sectorsNeeded >= 256)
                return;

            if (sectorNumber != 0 && sectorsAllocated == sectorsNeeded) {
                /* we can simply overwrite the old sectors */
                debug("SAVE", x, z, length, "rewrite");
                write(sectorNumber, data, length);
            } else {
                /* we need to allocate new sectors */

                /* mark the sectors previously used for this chunk as free */
                for (int i = 0; i < sectorsAllocated; ++i)
                    sectorFree.set(sectorNumber + i, Boolean.TRUE);

                /* scan for a free space large enough to store this chunk */
                int runStart = sectorFree.indexOf(Boolean.TRUE);
                int runLength = 0;
                if (runStart != -1)
                    for (int i = runStart; i < sectorFree.size(); ++i) {
                        if (runLength != 0) {
                            if (sectorFree.get(i))
                                runLength++;
                            else
                                runLength = 0;
                        } else if (sectorFree.get(i)) {
                            runStart = i;
                            runLength = 1;
                        }
                        if (runLength >= sectorsNeeded)
                            break;
                    }

                if (runLength >= sectorsNeeded) {
                    /* we found a free space large enough */
                    debug("SAVE", x, z, length, "reuse");
                    sectorNumber = runStart;
                    setOffset(x, z, sectorNumber << 8 | sectorsNeeded);
                    for (int i = 0; i < sectorsNeeded; ++i)
                        sectorFree.set(sectorNumber + i, Boolean.FALSE);
                    write(sectorNumber, data, length);
                } else {
                    /*
                     * no free space large enough found -- we need to grow the file
                     */
                    debug("SAVE", x, z, length, "grow");
                    file.seek(file.length());
                    sectorNumber = sectorFree.size();
                    for (int i = 0; i < sectorsNeeded; ++i) {
                        file.write(emptySector);
                        sectorFree.add(Boolean.FALSE);
                    }
                    sizeDelta += SECTOR_BYTES * sectorsNeeded;

                    write(sectorNumber, data, length);
                    setOffset(x, z, sectorNumber << 8 | sectorsNeeded);
                }
            }
            setTimestamp(x, z, (int) (System.currentTimeMillis() / 1000L));
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * write a chunk data to the region file at specified sector number
     */
    private void write(final int sectorNumber, final byte[] data,
                       final int length) throws IOException {
        debugln(" " + sectorNumber);
        file.seek((long) sectorNumber * SECTOR_BYTES);
        file.writeInt(length + 1); // chunk length
        file.writeByte(VERSION_DEFLATE); // chunk version number
        file.write(data, 0, length); // chunk data
    }

    private int getOffset(final int x, final int z) {
        return offsets[x + z * 32];
    }

    /**
     * @param x
     * @param z
     * @return <code>true</code> if this {@link RegionFile} contains the
     * specified chunk
     */
    public boolean hasChunk(final int x, final int z) {
        return getOffset(x, z) != 0;
    }

    private void setOffset(final int x, final int z, final int offset)
            throws IOException {
        offsets[x + z * 32] = offset;
        file.seek((x + z * 32) * 4);
        file.writeInt(offset);
    }

    private void setTimestamp(final int x, final int z, final int value)
            throws IOException {
        chunkTimestamps[x + z * 32] = value;
        file.seek(SECTOR_BYTES + (x + z * 32) * 4);
        file.writeInt(value);
    }

    /**
     * @throws IOException
     */
    public void close() throws IOException {
        file.close();
    }

    @Override
    public String toString() {
        return fileName.toString();
    }

    /*
     * lets chunk writing be multithreaded by not locking the whole file as a
     * chunk is serializing -- only writes when serialization is over
     */
    private class ChunkBuffer extends ByteArrayOutputStream {
        private final int x, z;

        public ChunkBuffer(final int x, final int z) {
            super(8096); // initialize to 8KB
            this.x = x;
            this.z = z;
        }

        @Override
        public void close() {
            RegionFile.this.write(x, z, buf, count);
        }
    }
}