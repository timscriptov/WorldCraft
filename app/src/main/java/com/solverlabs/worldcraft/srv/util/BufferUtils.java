package com.solverlabs.worldcraft.srv.util;

import java.nio.ByteBuffer;


public class BufferUtils {
    public static String byteBufferToString(ByteBuffer byteBuffer) {
        int limit = byteBuffer.limit();
        if (limit <= 0) {
            return null;
        }
        byte[] bArr = new byte[limit];
        byteBuffer.get(bArr);
        return new String(bArr);
    }

    public static byte[] readByteArray(ByteBuffer byteBuffer) {
        int i = byteBuffer.getInt();
        if (i == 0) {
            return null;
        }
        byte[] bArr = new byte[i];
        byteBuffer.get(bArr);
        return bArr;
    }

    public static String readStr(ByteBuffer byteBuffer) {
        int i = byteBuffer.getShort();
        if (i <= 0) {
            return null;
        }
        byte[] bArr = new byte[i];
        byteBuffer.get(bArr);
        return new String(bArr);
    }

    public static void writeByteArray(ByteBuffer byteBuffer, byte[] bArr) {
        if (bArr == null) {
            byteBuffer.putInt(0);
            return;
        }
        byteBuffer.putInt(bArr.length);
        byteBuffer.put(bArr);
    }

    public static void writeStr(ByteBuffer byteBuffer, String str, boolean z) {
        if (str == null) {
            if (!z) {
                return;
            }
            byteBuffer.putShort((short) 0);
            return;
        }
        int length = str.length();
        if (z) {
            byteBuffer.putShort((short) length);
        }
        for (int i = 0; i < length; i++) {
            byteBuffer.put((byte) str.charAt(i));
        }
    }
}
