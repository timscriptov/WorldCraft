package com.solverlabs.worldcraft.multiplayer.compress;

import org.apache.commons.compress.archivers.cpio.CpioConstants;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;


public class GzipEntity implements ContentBody {
    private static final String GZIP_MIME_TYPE = "application/x-gzip";
    private ByteArrayBody body;

    public GzipEntity(byte[] data, String filename) throws IOException {
        this.body = new ByteArrayBody(getGzipedBytes(data), GZIP_MIME_TYPE, filename);
    }

    public GzipEntity(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        GZIPOutputStream zos = new GZIPOutputStream(new BufferedOutputStream(os));
        byte[] temp = new byte[CpioConstants.C_ISCHR];
        while (true) {
            int length = fis.read(temp);
            if (length != -1) {
                zos.write(temp, 0, length);
            } else {
                zos.close();
                fis.close();
                this.body = new ByteArrayBody(os.toByteArray(), GZIP_MIME_TYPE, file.getName());
                return;
            }
        }
    }

    private byte[] getGzipedBytes(byte[] data) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        GZIPOutputStream zos = new GZIPOutputStream(new BufferedOutputStream(os));
        zos.write(data);
        zos.close();
        return os.toByteArray();
    }

    @Override
    public String getCharset() {
        return this.body.getCharset();
    }

    @Override
    public long getContentLength() {
        return this.body.getContentLength();
    }

    @Override
    public String getMediaType() {
        return this.body.getMediaType();
    }

    @Override
    public String getMimeType() {
        return this.body.getMimeType();
    }

    @Override
    public String getSubType() {
        return this.body.getSubType();
    }

    @Override
    public String getTransferEncoding() {
        return this.body.getTransferEncoding();
    }

    @Override
    public String getFilename() {
        return this.body.getFilename();
    }

    @Override
    public void writeTo(OutputStream arg0) throws IOException {
        this.body.writeTo(arg0);
    }
}
