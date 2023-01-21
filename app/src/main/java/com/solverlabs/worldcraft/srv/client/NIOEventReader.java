package com.solverlabs.worldcraft.srv.client;

import androidx.annotation.NonNull;

import com.solverlabs.worldcraft.client.common.Attachment;
import com.solverlabs.worldcraft.client.common.ClientGameEvent;
import com.solverlabs.worldcraft.client.common.EventQueue;
import com.solverlabs.worldcraft.srv.client.base.GameClient;
import com.solverlabs.worldcraft.srv.log.WcLog;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NIOEventReader extends Thread {
    private static final WcLog log = WcLog.getLogger(NIOEventReader.class);
    private final SocketChannel channel;
    private final EventQueue queue;
    GameClient gameClient;
    private OnEventReaderStartedListener onEventReaderStartedListener;
    private boolean running;
    private Selector selector;

    public NIOEventReader(GameClient gameClient, SocketChannel socketChannel, EventQueue eventQueue) {
        super("NIOEventReader");
        this.gameClient = gameClient;
        this.queue = eventQueue;
        this.channel = socketChannel;
    }

    public NIOEventReader(GameClient gameClient, SocketChannel socketChannel, EventQueue eventQueue, OnEventReaderStartedListener onEventReaderStartedListener) {
        this(gameClient, socketChannel, eventQueue);
        setOnEventReaderStartedListener(onEventReaderStartedListener);
    }

    private void errorOccurs(String str, Exception exc) {
        log.error(str);
        if (this.onEventReaderStartedListener != null) {
            this.onEventReaderStartedListener.onEventReaderErrorOccurs(str, exc);
        }
    }

    @NonNull
    private ClientGameEvent getEvent(@NonNull Attachment attachment) {
        ByteBuffer wrap = ByteBuffer.wrap(attachment.payload);
        ClientGameEvent clientGameEvent = new ClientGameEvent();
        clientGameEvent.read(wrap);
        return clientGameEvent;
    }

    private void notifyEventReaderStarted() {
        if (this.onEventReaderStartedListener != null) {
            this.onEventReaderStartedListener.onEventReaderListenerStarted();
        }
    }

    @Override
    public void run() {
        try {
            this.selector = Selector.open();
            this.channel.register(this.selector, 1, new Attachment());
            this.running = true;
            notifyEventReaderStarted();
            while (this.running) {
                try {
                    this.selector.select();
                    Iterator<SelectionKey> it = this.selector.selectedKeys().iterator();
                    while (it.hasNext()) {
                        SelectionKey next = it.next();
                        it.remove();
                        SocketChannel socketChannel = (SocketChannel) next.channel();
                        Attachment attachment = (Attachment) next.attachment();
                        try {
                            if (socketChannel.read(attachment.readBuff) == -1) {
                                socketChannel.close();
                                if (this.gameClient != null) {
                                    this.gameClient.connectionLost();
                                }
                            }
                            try {
                                if (attachment.readBuff.position() >= 12) {
                                    attachment.readBuff.flip();
                                    while (attachment.eventReady()) {
                                        getEvent(attachment).setChannel(socketChannel);
                                        attachment.reset();
                                    }
                                    attachment.readBuff.compact();
                                }
                            } catch (IllegalArgumentException e) {
                                log.error("illegalargument while parsing incoming event", e);
                            }
                        } catch (IOException e2) {
                            log.warn("IOException during read(), closing channel:" + socketChannel.socket().getInetAddress());
                            if (this.gameClient != null) {
                                this.gameClient.connectionLost();
                            }
                            socketChannel.close();
                        }
                    }
                } catch (Exception e3) {
                    log.error("exception during select()", e3);
                }
                try {
                    Thread.sleep(30L);
                } catch (InterruptedException e5) {
                    e5.printStackTrace();
                }
            }
        } catch (ClosedChannelException e6) {
            errorOccurs("closedchannelexception while registering channel with selector", e6);
        } catch (IOException e7) {
            errorOccurs("ioexception while registering channel with selector", e7);
        }
    }

    public void setOnEventReaderStartedListener(OnEventReaderStartedListener onEventReaderStartedListener) {
        this.onEventReaderStartedListener = onEventReaderStartedListener;
    }

    public void shutdown() {
        this.running = false;
        this.selector.wakeup();
    }

    public interface OnEventReaderStartedListener {
        void onEventReaderErrorOccurs(String str, Exception exc);

        void onEventReaderListenerStarted();
    }
}
