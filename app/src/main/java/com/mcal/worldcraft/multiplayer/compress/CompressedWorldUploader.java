package com.mcal.worldcraft.multiplayer.compress;

import android.util.Log;

import androidx.annotation.NonNull;

import com.mcal.droid.rugl.util.WorldUtils;
import com.mcal.worldcraft.srv.compress.DirectoryTarCompressor;
import com.mcal.worldcraft.utils.HttpPostBodyUtil2;
import com.mcal.worldcraft.utils.Properties;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jetbrains.annotations.Contract;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

public class CompressedWorldUploader {
    private static final String ENCODING_GZIP = "gzip";
    private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    private static final String HEADER_CONTENT_ENCODING = "Content-Encoding";
    private static final String OK_RESP = "OK";
    private static final String PARAM_FILE = "file";
    private static final String PARAM_UPLOAD_TOKEN = "uploadToken";
    private static final String TAG = "CompressedWorldUploader";
    private final String uploadToken;
    private final String worldId;

    public CompressedWorldUploader(String worldId, String uploadToken) {
        this.worldId = worldId;
        this.uploadToken = uploadToken;
    }

    public boolean upload() {
        String resp = null;
        Log.d(TAG, "uploading...");
        DefaultHttpClient client = new DefaultHttpClient();
        client.addRequestInterceptor((request, context) -> {
            if (!request.containsHeader(HEADER_ACCEPT_ENCODING)) {
                request.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
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
        HttpPost httppost = new HttpPost(Properties.WORLD_UPLOAD_URL);
        httppost.addHeader(HEADER_CONTENT_ENCODING, ENCODING_GZIP);
        try {
            MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            File tempWorldTar = new File(WorldUtils.WORLD_DIR, Properties.COMPRESSED_WORLD_NAME);
            DirectoryTarCompressor c = new DirectoryTarCompressor(new File(WorldUtils.WORLD_DIR, this.worldId), tempWorldTar);
            try {
                c.createArchive();
            } catch (ArchiveException e) {
                e.printStackTrace();
            }
            entity.addPart(PARAM_UPLOAD_TOKEN, new StringBody(this.uploadToken, HttpPostBodyUtil2.DEFAULT_TEXT_CONTENT_TYPE, StandardCharsets.UTF_8));
            entity.addPart(PARAM_FILE, new GzipEntity(tempWorldTar));
            httppost.setEntity(entity);
            HttpResponse response = client.execute(httppost);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            resp = rd.readLine();
            Log.d(TAG, "upload response: " + resp);
        } catch (Throwable e2) {
            e2.printStackTrace();
        }
        return OK_RESP.equals(resp);
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
