package com.solverlabs.worldcraft.client.common;

import com.solverlabs.worldcraft.srv.common.Globals;
import com.solverlabs.worldcraft.srv.log.WcLog;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class Attachment {
    public static final int HEADER_SIZE = 12;
    private static final WcLog log = WcLog.getLogger("Attachment");
    int clientId;
    public int gameNameHash;
    private boolean gotHeader;
    int payloadSize;
    public byte[] payload = new byte[Globals.MAX_EVENT_SIZE];
    public ByteBuffer readBuff = ByteBuffer.allocateDirect(512);

    private boolean checkHeader() throws IllegalArgumentException {
        if (this.gotHeader) {
            return true;
        }
        if (this.readBuff.remaining() >= 12) {
            this.clientId = this.readBuff.getInt();
            this.payloadSize = this.readBuff.getInt();
            if (this.payloadSize > 5000) {
                throw new IllegalArgumentException("Header specifies payload size (" + this.payloadSize + ") greater than MAX_EVENT_SIZE(" + Globals.MAX_EVENT_SIZE + ")");
            }
            this.gotHeader = true;
            return true;
        }
        return false;
    }

    private boolean checkPayload() {
        if (this.readBuff.remaining() >= this.payloadSize) {
            try {
                this.readBuff.get(this.payload, 0, this.payloadSize);
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
        this.gotHeader = false;
    }
}
