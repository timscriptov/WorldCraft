package com.mcal.worldcraft.client.common;

import com.mcal.worldcraft.srv.common.Globals;
import com.mcal.worldcraft.srv.log.WcLog;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class Attachment {
    public static final int HEADER_SIZE = 12;
    private static final WcLog log = WcLog.getLogger("Attachment");
    public int gameNameHash;
    public byte[] payload = new byte[Globals.MAX_EVENT_SIZE];
    public ByteBuffer readBuff = ByteBuffer.allocateDirect(512);
    int clientId;
    int payloadSize;
    private boolean gotHeader;

    private boolean checkHeader() throws IllegalArgumentException {
        if (gotHeader) {
            return true;
        }
        if (readBuff.remaining() >= HEADER_SIZE) {
            clientId = readBuff.getInt();
            payloadSize = readBuff.getInt();
            if (payloadSize > 5000) {
                throw new IllegalArgumentException("Header specifies payload size (" + payloadSize + ") greater than MAX_EVENT_SIZE(" + Globals.MAX_EVENT_SIZE + ")");
            }
            gotHeader = true;
            return true;
        }
        return false;
    }

    private boolean checkPayload() {
        if (readBuff.remaining() >= payloadSize) {
            try {
                readBuff.get(payload, 0, payloadSize);
            } catch (BufferUnderflowException e) {
                log.error("buffer underflow", e);
            }
            return true;
        }
        return false;
    }

    public boolean eventReady() throws IllegalArgumentException {
        return checkHeader() && checkPayload();
    }

    public void reset() {
        gotHeader = false;
    }
}
