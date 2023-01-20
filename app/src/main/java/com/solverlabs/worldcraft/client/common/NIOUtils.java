package com.solverlabs.worldcraft.client.common;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;

public class NIOUtils {
    public static boolean channelWrite(SocketChannel socketChannel, @NonNull ByteBuffer byteBuffer) {
        boolean z = false;
        long j = 0;
        long remaining = byteBuffer.remaining();
        if (remaining > 512) {
            System.err.println("PACKET IS TOO BIG!!!");
        }
        try {
            long currentTimeMillis = System.currentTimeMillis();
            while (true) {
                if (j == remaining) {
                    z = true;
                    break;
                } else if (System.currentTimeMillis() - currentTimeMillis > 2000) {
                    break;
                } else {
                    j += socketChannel.write(byteBuffer);
                    try {
                        Thread.sleep(10L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        byteBuffer.rewind();
        return z;
    }

    @Nullable
    public static byte[] getByteArray(@NonNull ByteBuffer byteBuffer) {
        int i = byteBuffer.getInt();
        if (i == 0) {
            return null;
        }
        byte[] bArr = new byte[i];
        byteBuffer.get(bArr);
        return bArr;
    }

    @Nullable
    public static String getStr(@NonNull ByteBuffer byteBuffer) {
        int i = byteBuffer.getShort();
        if (i <= 0) {
            return null;
        }
        byte[] bArr = new byte[i];
        byteBuffer.get(bArr);
        return new String(bArr);
    }

    public static void prepBuffer(@NonNull ClientGameEvent clientGameEvent, @NonNull ByteBuffer byteBuffer) {
        byteBuffer.clear();
        byteBuffer.putInt(0);
        int position = byteBuffer.position();
        byteBuffer.putInt(0);
        byteBuffer.putInt(position, clientGameEvent.write(byteBuffer));
        byteBuffer.flip();
    }

    public static void putByteArray(ByteBuffer byteBuffer, byte[] bArr) {
        if (bArr == null) {
            byteBuffer.putInt(0);
            return;
        }
        byteBuffer.putInt(bArr.length);
        byteBuffer.put(bArr);
    }

    public static void putStr(ByteBuffer byteBuffer, String str) {
        if (str == null) {
            byteBuffer.putShort((short) 0);
            return;
        }
        byteBuffer.putShort((short) str.length());
        byteBuffer.put(str.getBytes());
    }
}
