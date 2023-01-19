package com.solverlabs.worldcraft.srv.common;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;


public class FrameSplitter extends FrameDecoder {
    @Override
    protected Object decode(ChannelHandlerContext channelHandlerContext, Channel channel, ChannelBuffer channelBuffer) throws Exception {
        if (channelBuffer.readableBytes() < 8) {
            return null;
        }
        channelBuffer.markReaderIndex();
        channelBuffer.readInt();
        int readInt = channelBuffer.readInt();
        if (channelBuffer.readableBytes() >= readInt) {
            return channelBuffer.readBytes(readInt);
        }
        channelBuffer.resetReaderIndex();
        return null;
    }
}
