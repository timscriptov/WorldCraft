package com.solverlabs.worldcraft.srv.common;

import androidx.annotation.NonNull;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

public class FrameSplitter extends FrameDecoder {
    @Override
    protected Object decode(ChannelHandlerContext channelHandlerContext, Channel channel, @NonNull ChannelBuffer channelBuffer) throws Exception {
        if (channelBuffer.readableBytes() < 8) {
            return null;
        }
        channelBuffer.markReaderIndex();
        channelBuffer.readInt();
        int readInt = channelBuffer.readInt();
        if (channelBuffer.readableBytes() < readInt) {
            channelBuffer.resetReaderIndex();
            return null;
        }
        return channelBuffer.readBytes(readInt);
    }
}
