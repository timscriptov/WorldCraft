package com.solverlabs.worldcraft.srv.client.base;

import androidx.annotation.NonNull;

import com.solverlabs.worldcraft.client.common.EventQueue;
import com.solverlabs.worldcraft.srv.client.EventReceiver;
import com.solverlabs.worldcraft.srv.client.EventReceiverListener;
import com.solverlabs.worldcraft.srv.client.NIOEventReader;
import com.solverlabs.worldcraft.srv.client.NetworkChecker;
import com.solverlabs.worldcraft.srv.common.FrameSplitter;
import com.solverlabs.worldcraft.srv.common.Globals;
import com.solverlabs.worldcraft.srv.common.WorldCraftEventDecoder;
import com.solverlabs.worldcraft.srv.common.WorldCraftEventEncoder;
import com.solverlabs.worldcraft.srv.common.WorldCraftGameEvent;
import com.solverlabs.worldcraft.srv.domain.PlayerDefault;
import com.solverlabs.worldcraft.srv.log.WcLog;
import com.solverlabs.worldcraft.srv.util.UsefullGameEvents;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.util.HashedWheelTimer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;

public abstract class GameClient implements Runnable, NIOEventReader.OnEventReaderStartedListener {
    protected static final ByteBuffer WRITE_BUFFER = ByteBuffer.allocate(Globals.MAX_EVENT_SIZE);
    protected static final WcLog log = WcLog.getLogger("GameClient");
    protected NIOEventReader.OnEventReaderStartedListener eventReaderStartedListener;
    protected ConnectionListener gameListener;
    protected EventQueue inQueue;
    protected NetworkChecker networkChecker;
    protected EventQueue outQueue;
    protected EventReceiver receiver;
    protected boolean running = true;
    protected String serverName;
    private ClientBootstrap bootstrap;
    private ClientHandler clientHandler;
    private long lastUsefullEventSentAt;
    private Channel nettyChannel;
    private HashedWheelTimer timer;

    public void connect() {
        this.timer = new HashedWheelTimer();
        this.bootstrap = getBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
        this.bootstrap.setOption("tcpNoDelay", true);
        ChannelFuture connect = this.bootstrap.connect(new InetSocketAddress(this.serverName, Globals.PORT));
        this.nettyChannel = connect.awaitUninterruptibly().getChannel();
        if (connect.isSuccess()) {
            this.eventReaderStartedListener.onEventReaderListenerStarted();
            return;
        }
        connect.getCause().printStackTrace();
        this.bootstrap.releaseExternalResources();
        this.eventReaderStartedListener.onEventReaderErrorOccurs(null, null);
    }

    @NonNull
    private ClientBootstrap getBootstrap(ChannelFactory channelFactory) {
        ClientBootstrap clientBootstrap = new ClientBootstrap(channelFactory);
        clientBootstrap.setPipelineFactory(getChannelPipelineFactory());
        clientBootstrap.setOption("tcpNoDelay", true);
        clientBootstrap.setOption("keepAlive", true);
        return clientBootstrap;
    }

    @NonNull
    private ChannelPipelineFactory getChannelPipelineFactory() {
        this.clientHandler = new ClientHandler(this.receiver, this);
        return () -> Channels.pipeline(new FrameSplitter(), new WorldCraftEventDecoder(), new WorldCraftEventEncoder(), clientHandler);
    }

    private void releaseBootstrapResources() {
        if (this.nettyChannel != null) {
            this.nettyChannel.close().awaitUninterruptibly();
            this.nettyChannel = null;
            if (this.bootstrap != null) {
                this.bootstrap.releaseExternalResources();
            }
            this.bootstrap = null;
        }
    }

    private void threadSleep(long j) {
        try {
            Thread.sleep(j);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void writeOutgoingEvents() {
        while (this.outQueue.size() > 0) {
            try {
                WorldCraftGameEvent deQueue = this.outQueue.deQueue();
                if (deQueue != null) {
                    writeEvent(deQueue);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (this.outQueue.size() == 0) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    protected boolean connectLater() {
        new Thread() {
            @Override
            public void run() {
                try {
                    connect();
                } catch (Throwable th) {
                    errorOccurs("Exception while connecting", th);
                }
            }
        }.start();
        return true;
    }

    public void connectionLost() {
        if (this.nettyChannel != null) {
            this.nettyChannel = null;
            errorOccurs("Connection lost", null);
        }
    }

    protected void errorOccurs(String str, Throwable th) {
        if (this.running) {
            log.warn(str, th);
            onConnectionLost(str, th);
        }
    }

    public boolean hasUsefullGameEventsDueIdleTime() {
        return System.currentTimeMillis() - this.lastUsefullEventSentAt < PlayerDefault.MAX_IDLE_TIME;
    }

    public void init(NIOEventReader.OnEventReaderStartedListener onEventReaderStartedListener, EventReceiverListener eventReceiverListener) {
        init(this.serverName, onEventReaderStartedListener, eventReceiverListener);
    }

    public void init(String str, NIOEventReader.OnEventReaderStartedListener onEventReaderStartedListener, EventReceiverListener eventReceiverListener) {
        this.eventReaderStartedListener = onEventReaderStartedListener;
        this.running = true;
        this.inQueue = new EventQueue("GameClient-in");
        this.outQueue = new EventQueue("GameClient-out");
        this.serverName = str;
        this.networkChecker = new NetworkChecker();
        this.networkChecker.setListener(new NetworkChecker.NetworkCheckListener() {
            @Override
            public void connectionLost() {
                errorOccurs("Connection lost", null);
            }

            @Override
            public void sendPingRequest() {
                ping();
            }
        });
        this.receiver = new EventReceiver(this.inQueue, eventReceiverListener, this.networkChecker);
        connectLater();
    }

    protected abstract void onConnectionLost(String str, Throwable th);

    @Override
    public void onEventReaderErrorOccurs(String str, Exception exc) {
        errorOccurs(str, exc);
    }

    @Override
    public void onEventReaderListenerStarted() {
        if (this.gameListener != null) {
            this.gameListener.onConnectionEstablished();
        }
    }

    protected abstract void ping();

    @Override
    public void run() {
        while (this.running) {
            writeOutgoingEvents();
            this.networkChecker.check();
            threadSleep(50L);
        }
    }

    public void setGameListener(ConnectionListener connectionListener) {
        this.gameListener = connectionListener;
    }

    public void setOnEventReaderStartedListener(NIOEventReader.OnEventReaderStartedListener onEventReaderStartedListener, EventReceiverListener eventReceiverListener) {
        this.eventReaderStartedListener = onEventReaderStartedListener;
        this.receiver.setEventReceiverListener(eventReceiverListener);
    }

    public void shutdown() {
        try {
            this.running = false;
            if (this.timer != null) {
                this.timer.stop();
            }
            if (this.clientHandler != null) {
                this.clientHandler.setReceiver(null);
            }
            releaseBootstrapResources();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    protected void writeEvent(@NonNull WorldCraftGameEvent worldCraftGameEvent) {
        if (UsefullGameEvents.contains(worldCraftGameEvent.getType())) {
            this.lastUsefullEventSentAt = System.currentTimeMillis();
        }
        worldCraftGameEvent.prepareToSend();
        if (this.nettyChannel != null) {
            this.clientHandler.sendMessage(this.nettyChannel, worldCraftGameEvent);
        } else {
            connectionLost();
        }
    }

    public interface ConnectionListener {
        void onConnectionEstablished();

        void onConnectionFailed(String str, Throwable th);
    }
}
