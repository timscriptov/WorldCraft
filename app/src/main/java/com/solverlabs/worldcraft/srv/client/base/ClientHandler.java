package com.solverlabs.worldcraft.srv.client.base;

import com.solverlabs.worldcraft.srv.client.EventReceiver;
import com.solverlabs.worldcraft.srv.common.WorldCraftGameEvent;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;


public class ClientHandler extends SimpleChannelHandler {
    private GameClient gameClient;
    private long lastEventReceivedAt;
    private long lastEventSentAt;
    private EventReceiver receiver;

    public ClientHandler(EventReceiver eventReceiver, GameClient gameClient) {
        this.receiver = eventReceiver;
        this.gameClient = gameClient;
    }

    @Override
    public void channelClosed(ChannelHandlerContext channelHandlerContext, ChannelStateEvent channelStateEvent) throws Exception {
        System.out.println("closed");
        super.channelClosed(channelHandlerContext, channelStateEvent);
    }

    @Override
    public void channelConnected(ChannelHandlerContext channelHandlerContext, ChannelStateEvent channelStateEvent) throws Exception {
        super.channelConnected(channelHandlerContext, channelStateEvent);
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext channelHandlerContext, ChannelStateEvent channelStateEvent) throws Exception {
        System.out.println("disconnected");
        super.channelDisconnected(channelHandlerContext, channelStateEvent);
        this.gameClient.connectionLost();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, ExceptionEvent exceptionEvent) {
        exceptionEvent.getCause().printStackTrace();
        exceptionEvent.getChannel().close();
    }

    public long getLastEventReceivedAt() {
        return this.lastEventReceivedAt;
    }

    public long getLastEventSentAt() {
        return this.lastEventSentAt;
    }

    @Override
    public void messageReceived(ChannelHandlerContext channelHandlerContext, MessageEvent messageEvent) {
        this.lastEventReceivedAt = System.currentTimeMillis();
        if (this.receiver != null) {
            this.receiver.processIncomingEvents((WorldCraftGameEvent) messageEvent.getMessage());
        }
    }

    public void sendMessage(Channel channel, WorldCraftGameEvent worldCraftGameEvent) {
        this.lastEventSentAt = System.currentTimeMillis();
        channel.write(worldCraftGameEvent);
    }

    public void setReceiver(EventReceiver eventReceiver) {
        this.receiver = eventReceiver;
    }
}
