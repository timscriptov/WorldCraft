package com.solverlabs.worldcraft.srv.client;

public class NetworkChecker {
    protected static final long CONNECT_TIMEOUT = 40000;
    protected static final long PING_TIMEOUT = 30000;
    protected long lastServerPacketTime;
    protected NetworkCheckListener listener;

    public NetworkChecker() {
        updatePacketTime();
    }

    public void check() {
        if (System.currentTimeMillis() - this.lastServerPacketTime > CONNECT_TIMEOUT && this.listener != null) {
            this.listener.connectionLost();
        } else if (System.currentTimeMillis() - this.lastServerPacketTime <= PING_TIMEOUT || this.listener == null) {
        } else {
            this.listener.sendPingRequest();
        }
    }

    public void setListener(NetworkCheckListener networkCheckListener) {
        this.listener = networkCheckListener;
    }

    public void updatePacketTime() {
        this.lastServerPacketTime = System.currentTimeMillis();
    }

    public interface NetworkCheckListener {
        void connectionLost();

        void sendPingRequest();
    }
}
