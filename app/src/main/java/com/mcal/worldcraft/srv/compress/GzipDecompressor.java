package com.mcal.worldcraft.srv.compress;

import com.mcal.worldcraft.factories.DescriptionFactory;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class GzipDecompressor {
    private final File src;

    public GzipDecompressor(File file) {
        this.src = file;
    }

    public String decompressArchive() throws IOException, ArchiveException {
        GzipCompressorInputStream gzipCompressorInputStream;
        BufferedInputStream bufferedInputStream;
        BufferedInputStream bufferedInputStream2 = null;
        FileOutputStream fileOutputStream = null;
        FileOutputStream fileOutputStream2 = null;
        String replaceAll = this.src.getAbsolutePath().replaceAll("\\.gz", DescriptionFactory.emptyText);
        try {
            bufferedInputStream2 = new BufferedInputStream(new FileInputStream(this.src));
            try {
                fileOutputStream = new FileOutputStream(replaceAll);
                try {
                    gzipCompressorInputStream = new GzipCompressorInputStream(bufferedInputStream2);
                } catch (Throwable th) {
                    gzipCompressorInputStream = null;
                    fileOutputStream2 = fileOutputStream;
                    bufferedInputStream = bufferedInputStream2;
                }
            } catch (Throwable th2) {
                gzipCompressorInputStream = null;
                bufferedInputStream = bufferedInputStream2;
            }
        } catch (Throwable th3) {
            gzipCompressorInputStream = null;
            bufferedInputStream = null;
        }
        try {
            IOUtils.copy(gzipCompressorInputStream, fileOutputStream);
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            if (gzipCompressorInputStream != null) {
                gzipCompressorInputStream.close();
            }
            if (bufferedInputStream2 != null) {
                bufferedInputStream2.close();
            }
            return replaceAll;
        } catch (Throwable th4) {
            fileOutputStream2 = fileOutputStream;
            bufferedInputStream = bufferedInputStream2;
            if (fileOutputStream2 != null) {
                fileOutputStream2.close();
            }
            if (gzipCompressorInputStream != null) {
                gzipCompressorInputStream.close();
            }
            if (bufferedInputStream != null) {
                bufferedInputStream.close();
            }
        }
        return null;
    }

    public void removeSrc() {
        this.src.delete();
    }
}
