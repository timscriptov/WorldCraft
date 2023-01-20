package com.solverlabs.worldcraft.srv.compress;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.utils.IOUtils;

public class GzipCompressor {
    private final File src;

    public GzipCompressor(File file) {
        this.src = file;
    }

    public String compress() throws IOException, ArchiveException {
        File file = new File(this.src.getAbsolutePath() + ".gz");
        FileInputStream fileInputStream = new FileInputStream(this.src);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        GZIPOutputStream gZIPOutputStream = new GZIPOutputStream(new BufferedOutputStream(fileOutputStream));
        IOUtils.copy(fileInputStream, gZIPOutputStream);
        gZIPOutputStream.close();
        fileOutputStream.close();
        fileInputStream.close();
        return file.getName();
    }

    public void removeSrc() {
        this.src.delete();
    }
}
