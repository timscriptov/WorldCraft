package com.solverlabs.worldcraft.srv.compress;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;

public class DirectoryTarDecompressor {
    private final String pathPrefix;
    private final File src;

    public DirectoryTarDecompressor(File file) {
        this.src = file;
        this.pathPrefix = this.src.getParent();
    }

    public DirectoryTarDecompressor(String str) {
        this(new File(str));
    }

    public void removeSrc() {
        this.src.delete();
    }

    public void unpackTar() throws IOException, ArchiveException {
        FileInputStream fileInputStream = new FileInputStream(this.src);
        ArchiveInputStream createArchiveInputStream = new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.TAR, new BufferedInputStream(fileInputStream));
        if (createArchiveInputStream instanceof TarArchiveInputStream) {
            TarArchiveInputStream tarArchiveInputStream = (TarArchiveInputStream) createArchiveInputStream;
            for (TarArchiveEntry nextTarEntry = tarArchiveInputStream.getNextTarEntry(); nextTarEntry != null; nextTarEntry = tarArchiveInputStream.getNextTarEntry()) {
                File file = new File(this.pathPrefix + "/" + nextTarEntry.getName());
                File file2 = new File(file.getParent());
                if (!file2.exists()) {
                    file2.mkdir();
                }
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                IOUtils.copy(createArchiveInputStream, fileOutputStream);
                fileOutputStream.close();
            }
        }
        createArchiveInputStream.close();
        fileInputStream.close();
    }
}
