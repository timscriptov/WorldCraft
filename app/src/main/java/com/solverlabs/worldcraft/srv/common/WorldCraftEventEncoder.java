package com.solverlabs.worldcraft.srv.common;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

public class WorldCraftEventEncoder extends OneToOneEncoder {
    @Override
    protected Object encode(ChannelHandlerContext channelHandlerContext, Channel channel, Object obj) throws Exception {
        return !(obj instanceof BaseGameEvent) ? obj : ChannelBuffers.wrappedBuffer(((BaseGameEvent) obj).toByteBuffer());
    }
}
