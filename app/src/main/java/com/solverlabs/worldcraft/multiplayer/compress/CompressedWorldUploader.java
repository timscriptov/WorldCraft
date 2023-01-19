package com.solverlabs.worldcraft.multiplayer.compress;

import static com.solverlabs.worldcraft.util.HttpPostBodyUtil2.DEFAULT_TEXT_CONTENT_TYPE;

import android.util.Log;

import com.solverlabs.droid.rugl.util.WorldUtils;
import com.solverlabs.worldcraft.srv.compress.DirectoryTarCompressor;
import com.solverlabs.worldcraft.util.Properties;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.utils.CharsetNames;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;


public class CompressedWorldUploader {
    private static final String ENCODING_GZIP = "gzip";
    private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    private static final String HEADER_CONTENT_ENCODING = "Content-Encoding";
    private static final String OK_RESP = "OK";
    private static final String PARAM_FILE = "file";
    private static final String PARAM_UPLOAD_TOKEN = "uploadToken";
    private static final String TAG = "CompressedWorldUploader";
    private String uploadToken;
    private String worldId;

    public CompressedWorldUploader(String worldId, String uploadToken) {
        this.worldId = worldId;
        this.uploadToken = uploadToken;
    }

    public boolean upload() {
        String resp = null;
        Log.d(TAG, "uploading...");
        DefaultHttpClient client = new DefaultHttpClient();
        client.addRequestInterceptor(new HttpRequestInterceptor() {
            public void process(HttpRequest request, HttpContext context) {
                if (!request.containsHeader("Accept-Encoding")) {
                    request.addHeader("Accept-Encoding", "gzip");
                }
            }
        });
        client.addResponseInterceptor(new HttpResponseInterceptor() {
            public void process(HttpResponse response, HttpContext context) {
                HttpEntity entity = response.getEntity();
                Header encoding = entity.getContentEncoding();
                if (encoding != null) {
                    HeaderElement[] arr$ = encoding.getElements();
                    for (HeaderElement element : arr$) {
                        if (element.getName().equalsIgnoreCase("gzip")) {
                            response.setEntity(new InflatingEntity(response.getEntity()));
                            return;
                        }
                    }
                }
            }
        });
        HttpPost httppost = new HttpPost(Properties.WORLD_UPLOAD_URL);
        httppost.addHeader("Content-Encoding", "gzip");
        try {
            MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            File tempWorldTar = new File(WorldUtils.WORLD_DIR, Properties.COMPRESSED_WORLD_NAME);
            DirectoryTarCompressor c = new DirectoryTarCompressor(new File(WorldUtils.WORLD_DIR, this.worldId), tempWorldTar);
            try {
                c.createArchive();
            } catch (ArchiveException e) {
                e.printStackTrace();
            }
            entity.addPart(PARAM_UPLOAD_TOKEN, new StringBody(this.uploadToken, DEFAULT_TEXT_CONTENT_TYPE, Charset.forName(CharsetNames.UTF_8)));
            entity.addPart("file", new GzipEntity(tempWorldTar));
            httppost.setEntity(entity);
            HttpResponse response = client.execute(httppost);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            resp = rd.readLine();
            Log.d(TAG, "upload response: " + resp);
        } catch (IOException e2) {
            e2.printStackTrace();
        } catch (Throwable e4) {
            e4.printStackTrace();
        }
        if (OK_RESP.equals(resp)) {
            return true;
        }
        return false;
    }


    private static class InflatingEntity extends HttpEntityWrapper {
        public InflatingEntity(HttpEntity wrapped) {
            super(wrapped);
        }

        public InputStream getContent() throws IOException {
            return new GZIPInputStream(this.wrappedEntity.getContent());
        }

        public long getContentLength() {
            return -1L;
        }
    }
}
