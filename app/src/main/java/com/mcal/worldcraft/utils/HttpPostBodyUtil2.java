package com.mcal.worldcraft.utils;

import androidx.annotation.NonNull;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.util.CharsetUtil;

import java.nio.charset.Charset;

/**
 * Shared Static object between HttpMessageDecoder, HttpPostRequestDecoder and HttpPostRequestEncoder
 */
public final class HttpPostBodyUtil2 {

    public static final int chunkSize = 8096;
    /**
     * HTTP content disposition header name.
     */
    public static final String CONTENT_DISPOSITION = "Content-Disposition";

    public static final String NAME = "name";

    public static final String FILENAME = "filename";

    /**
     * Content-disposition value for form data.
     */
    public static final String FORM_DATA = "form-data";

    /**
     * Content-disposition value for file attachment.
     */
    public static final String ATTACHMENT = "attachment";

    /**
     * Content-disposition value for file attachment.
     */
    public static final String FILE = "file";

    /**
     * HTTP content type body attribute for multiple uploads.
     */
    public static final String MULTIPART_MIXED = "multipart/mixed";

    /**
     * Charset for 8BIT
     */
    public static final Charset ISO_8859_1 = CharsetUtil.ISO_8859_1;

    /**
     * Charset for 7BIT
     */
    public static final Charset US_ASCII = CharsetUtil.US_ASCII;

    /**
     * Default Content-Type in binary form
     */
    public static final String DEFAULT_BINARY_CONTENT_TYPE = "application/octet-stream";

    /**
     * Default Content-Type in Text form
     */
    public static final String DEFAULT_TEXT_CONTENT_TYPE = "text/plain";

    public HttpPostBodyUtil2() {
    }

    /**
     * Find the first non whitespace
     *
     * @return the rank of the first non whitespace
     */
    static int findNonWhitespace(@NonNull String sb, int offset) {
        int result;
        for (result = offset; result < sb.length(); result++) {
            if (!Character.isWhitespace(sb.charAt(result))) {
                break;
            }
        }
        return result;
    }

    /**
     * Find the first whitespace
     *
     * @return the rank of the first whitespace
     */
    static int findWhitespace(@NonNull String sb, int offset) {
        int result;
        for (result = offset; result < sb.length(); result++) {
            if (Character.isWhitespace(sb.charAt(result))) {
                break;
            }
        }
        return result;
    }

    /**
     * Find the end of String
     *
     * @return the rank of the end of string
     */
    static int findEndOfString(@NonNull String sb) {
        int result;
        for (result = sb.length(); result > 0; result--) {
            if (!Character.isWhitespace(sb.charAt(result - 1))) {
                break;
            }
        }
        return result;
    }

    /**
     * Allowed mechanism for multipart
     * mechanism := "7bit"
     * / "8bit"
     * / "binary"
     * Not allowed: "quoted-printable"
     * / "base64"
     */
    public enum TransferEncodingMechanism {
        /**
         * Default encoding
         */
        BIT7("7bit"),
        /**
         * Short lines but not in ASCII - no encoding
         */
        BIT8("8bit"),
        /**
         * Could be long text not in ASCII - no encoding
         */
        BINARY("binary");

        private final String value;

        TransferEncodingMechanism(String value) {
            this.value = value;
        }

        TransferEncodingMechanism() {
            value = name();
        }

        public String value() {
            return value;
        }

        @NonNull
        @Override
        public String toString() {
            return value;
        }
    }

    /**
     * Exception when NO Backend Array is found
     */
    public static class SeekAheadNoBackArrayException extends Exception {
        private static final long serialVersionUID = -630418804938699495L;
    }

    /**
     * This class intends to decrease the CPU in seeking ahead some bytes in
     * HttpPostRequestDecoder
     */
    public static class SeekAheadOptimize {
        byte[] bytes;
        int readerIndex;
        int pos;
        int origPos;
        int limit;
        ChannelBuffer buffer;

        SeekAheadOptimize(@NonNull ChannelBuffer buffer) throws HttpPostBodyUtil2.SeekAheadNoBackArrayException {
            if (!buffer.hasArray()) {
                throw new HttpPostBodyUtil2.SeekAheadNoBackArrayException();
            }
            this.buffer = buffer;
            bytes = buffer.array();
            readerIndex = buffer.readerIndex();
            origPos = pos = buffer.arrayOffset() + readerIndex;
            limit = buffer.arrayOffset() + buffer.writerIndex();
        }

        /**
         * @param minus this value will be used as (currentPos - minus) to set
         *              the current readerIndex in the buffer.
         */
        void setReadPosition(int minus) {
            pos -= minus;
            readerIndex = getReadPosition(pos);
            buffer.readerIndex(readerIndex);
        }

        /**
         * @param index raw index of the array (pos in general)
         * @return the value equivalent of raw index to be used in readerIndex(value)
         */
        int getReadPosition(int index) {
            return index - origPos + readerIndex;
        }

        void clear() {
            buffer = null;
            bytes = null;
            limit = 0;
            pos = 0;
            readerIndex = 0;
        }
    }
}
