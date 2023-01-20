package com.solverlabs.worldcraft.srv.common;

import androidx.annotation.NonNull;

import com.solverlabs.worldcraft.factories.DescriptionFactory;
import java.nio.ByteBuffer;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

public class WorldCraftEventDecoder extends OneToOneDecoder {
    public static final int HEADER_SIZE = 12;
    private int clientVersionId;
    private boolean gotHeader;
    private int payloadSize;

    private boolean isEventReady(ChannelBuffer channelBuffer, Channel channel) throws IllegalArgumentException {
        return parseHeader(channelBuffer, channel) && isPayloadReady(channelBuffer);
    }

    private boolean isPayloadReady(@NonNull ChannelBuffer channelBuffer) {
        return channelBuffer.readableBytes() >= this.payloadSize;
    }

    private void parseEvent(BaseGameEvent baseGameEvent, ChannelBuffer channelBuffer) {
        try {
            baseGameEvent.setType(channelBuffer.readByte());
            baseGameEvent.setError(channelBuffer.readByte());
            baseGameEvent.setPlayerId(channelBuffer.readInt());
            baseGameEvent.setClientVersionId(this.clientVersionId);
            channelBuffer.readerIndex(channelBuffer.readShort() + channelBuffer.readerIndex());
            int readInt = channelBuffer.readInt();
            ByteBuffer inputBuffer = baseGameEvent.getInputBuffer();
            for (int i = 0; i < readInt; i++) {
                inputBuffer.put(channelBuffer.readByte());
            }
            inputBuffer.flip();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private boolean parseHeader(ChannelBuffer channelBuffer, Channel channel) throws IllegalArgumentException {
        if (this.gotHeader) {
            return true;
        }
        if (channelBuffer.readableBytes() >= 12) {
            this.clientVersionId = channelBuffer.readInt();
            this.payloadSize = channelBuffer.readInt();
            if (this.payloadSize <= 5000) {
                this.gotHeader = true;
                return true;
            }
            throw new IllegalArgumentException("Header specifies payload size (" + this.payloadSize + ") greater than MAX_EVENT_SIZE(" + Globals.MAX_EVENT_SIZE + ")." + " clientVersion: " + this.clientVersionId + " payloadSize: " + this.payloadSize + " buffer: " + toHex(channelBuffer));
        }
        return false;
    }

    @NonNull
    private String toHex(@NonNull ChannelBuffer channelBuffer) {
        StringBuilder hexString = new StringBuilder(DescriptionFactory.emptyText);
        while (channelBuffer.readableBytes() > 0) {
            hexString.append(" ").append(Integer.toHexString(channelBuffer.readByte()).length() == 1 ? "0" + hexString : hexString.substring(hexString.length() - 2));
        }
        return hexString.toString();
    }

    private boolean validateEvent(@NonNull BaseGameEvent baseGameEvent) {
        return baseGameEvent.getClientVersionId() >= 0 && baseGameEvent.getClientVersionId() <= 1 && baseGameEvent.getPlayerId() >= 0;
    }

    @Override
    protected Object decode(ChannelHandlerContext channelHandlerContext, Channel channel, Object obj) throws Exception {
        if (obj instanceof ChannelBuffer) {
            BaseGameEvent create = WorldCraftGameEvent.create();
            parseEvent(create, (ChannelBuffer) obj);
            create.setChannel(channel);
            this.gotHeader = false;
            this.payloadSize = -1;
            if (validateEvent(create)) {
                return create;
            }
            return null;
        }
        return obj;
    }
}
