package com.mcal.worldcraft.multiplayer.compress;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;

import com.mcal.droid.rugl.util.WorldUtils;
import com.mcal.worldcraft.srv.compress.DirectoryTarDecompressor;
import com.mcal.worldcraft.srv.compress.GzipDecompressor;
import com.mcal.worldcraft.util.Properties;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jetbrains.annotations.Contract;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class CompressedWorldDownloader {
    private static final String ENCODING_GZIP = "gzip";
    private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    private static final String TAG = "CompressedWorldDownloader";
    private final String url;

    public CompressedWorldDownloader(String url) {
        this.url = url;
    }

    @SuppressLint("LongLogTag")
    public boolean download() {
        DefaultHttpClient client = new DefaultHttpClient();
        client.addRequestInterceptor((request, context) -> {
            if (!request.containsHeader(HEADER_ACCEPT_ENCODING)) {
                request.addHeader(HEADER_ACCEPT_ENCODING, "gzip");
            }
        });
        client.addResponseInterceptor((response, context) -> {
            HttpEntity entity = response.getEntity();
            Header encoding = entity.getContentEncoding();
            if (encoding != null) {
                HeaderElement[] arr$ = encoding.getElements();
                for (HeaderElement element : arr$) {
                    if (element.getName().equalsIgnoreCase(ENCODING_GZIP)) {
                        response.setEntity(new InflatingEntity(response.getEntity()));
                        return;
                    }
                }
            }
        });
        Log.d(TAG, "GET: " + this.url);
        HttpGet httpGet = new HttpGet(this.url);
        try {
            HttpResponse response = client.execute(httpGet);
            if (response.getStatusLine().getStatusCode() != 200) {
                Log.d(TAG, "Not OK http response: " + response.getStatusLine().getStatusCode());
                return false;
            }
            InputStream is = response.getEntity().getContent();
            File mpWorldDir = new File(WorldUtils.WORLD_DIR, Properties.MULTIPLAYER_WORLD_NAME);
            if (!mpWorldDir.exists()) {
                mpWorldDir.mkdir();
            }
            File worldTarGzfile = new File(mpWorldDir, Properties.COMPRESSED_WORLD_NAME);
            FileOutputStream fos = new FileOutputStream(worldTarGzfile);
            IOUtils.copy(is, fos);
            fos.close();
            GzipDecompressor gzipDecompressor = new GzipDecompressor(worldTarGzfile);
            String tarFilename = gzipDecompressor.decompressArchive();
            gzipDecompressor.removeSrc();
            DirectoryTarDecompressor decompressor = new DirectoryTarDecompressor(tarFilename);
            decompressor.unpackTar();
            decompressor.removeSrc();
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    private static class InflatingEntity extends HttpEntityWrapper {
        public InflatingEntity(HttpEntity wrapped) {
            super(wrapped);
        }

        @NonNull
        @Contract(" -> new")
        public InputStream getContent() throws IOException {
            return new GZIPInputStream(this.wrappedEntity.getContent());
        }

        public long getContentLength() {
            return -1L;
        }
    }
}
