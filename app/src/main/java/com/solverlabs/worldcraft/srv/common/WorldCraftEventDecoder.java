package com.solverlabs.worldcraft.srv.common;

import com.solverlabs.worldcraft.factories.DescriptionFactory;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

import java.nio.ByteBuffer;


public class WorldCraftEventDecoder extends OneToOneDecoder {
    public static final int HEADER_SIZE = 12;
    private int clientVersionId;
    private boolean gotHeader;
    private int payloadSize;

    private boolean isEventReady(ChannelBuffer channelBuffer, Channel channel) throws IllegalArgumentException {
        return parseHeader(channelBuffer, channel) && isPayloadReady(channelBuffer);
    }

    private boolean isPayloadReady(ChannelBuffer channelBuffer) {
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
        if (channelBuffer.readableBytes() < 12) {
            return false;
        }
        this.clientVersionId = channelBuffer.readInt();
        this.payloadSize = channelBuffer.readInt();
        if (this.payloadSize <= 5000) {
            this.gotHeader = true;
            return true;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Header specifies payload size (").append(this.payloadSize).append(") greater than MAX_EVENT_SIZE(").append(Globals.MAX_EVENT_SIZE).append(").").append(" clientVersion: ").append(this.clientVersionId).append(" payloadSize: ").append(this.payloadSize).append(" buffer: ").append(toHex(channelBuffer));
        throw new IllegalArgumentException(sb.toString());
    }

    private String toHex(ChannelBuffer channelBuffer) {
        String hexString = DescriptionFactory.emptyText;
        while (channelBuffer.readableBytes() > 0) {
            hexString = hexString + " " + (Integer.toHexString(channelBuffer.readByte()).length() == 1 ? "0" + hexString : hexString.substring(hexString.length() - 2));
        }
        return hexString;
    }

    private boolean validateEvent(BaseGameEvent baseGameEvent) {
        return baseGameEvent.getClientVersionId() >= 0 && baseGameEvent.getClientVersionId() <= 1 && baseGameEvent.getPlayerId() >= 0;
    }

    @Override
    protected Object decode(ChannelHandlerContext channelHandlerContext, Channel channel, Object obj) throws Exception {
        if (!(obj instanceof ChannelBuffer)) {
            return obj;
        }
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
}
