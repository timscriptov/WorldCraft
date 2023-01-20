package com.solverlabs.worldcraft.srv.compress;

import androidx.annotation.NonNull;

import com.solverlabs.worldcraft.factories.DescriptionFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.utils.IOUtils;

public class DirectoryTarCompressor {
    private final File dest;
    private final String pathPrefix;
    private final File src;

    public DirectoryTarCompressor(File file, File file2) {
        this.src = file;
        this.dest = file2;
        this.pathPrefix = this.src.getAbsolutePath().replace('\\', '/');
    }

    private void addFile(ArchiveOutputStream archiveOutputStream, @NonNull File file) throws IOException {
        if (file.isDirectory()) {
            for (String str : file.list()) {
                addFile(archiveOutputStream, new File(file, str));
            }
            return;
        }
        String replaceAll = file.getAbsolutePath().replace('\\', '/').replaceAll(this.pathPrefix, DescriptionFactory.emptyText);
        if (replaceAll.endsWith(".tar") || replaceAll.endsWith(".tar.gz")) {
            return;
        }
        TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(replaceAll);
        tarArchiveEntry.setSize(file.length());
        archiveOutputStream.putArchiveEntry(tarArchiveEntry);
        IOUtils.copy(new FileInputStream(file), archiveOutputStream);
        archiveOutputStream.closeArchiveEntry();
    }

    public void createArchive() throws IOException, ArchiveException {
        if (!this.dest.exists()) {
            this.dest.createNewFile();
        }
        ArchiveOutputStream createArchiveOutputStream = new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.TAR, new FileOutputStream(this.dest));
        addFile(createArchiveOutputStream, this.src);
        createArchiveOutputStream.close();
    }
}
