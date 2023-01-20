package com.solverlabs.worldcraft.client.common;

import androidx.annotation.NonNull;

import com.solverlabs.worldcraft.srv.common.GameEvent;
import com.solverlabs.worldcraft.srv.domain.Player;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Set;

public class ClientGameEvent implements GameEvent {
    protected SocketChannel channel;
    protected byte[] data;
    protected byte error;
    protected byte eventType;
    protected String message;
    protected Player player;
    protected int playerId;
    protected Set<Integer> recipientSet;

    public ClientGameEvent() {
    }

    public ClientGameEvent(byte b) {
        this.eventType = b;
        this.error = (byte) 0;
    }

    public ClientGameEvent(byte b, byte b2) {
        this(b);
        this.error = b2;
    }

    public ClientGameEvent(byte b, byte b2, String str) {
        this(b, b2);
        this.message = str;
    }

    public ClientGameEvent(byte b, String str) {
        this(b);
        this.message = str;
    }

    public SocketChannel getChannel() {
        return this.channel;
    }

    public byte[] getData() {
        return this.data;
    }

    @Override
    public byte getError() {
        return this.error;
    }

    public String getMessage() {
        return this.message;
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    @Override
    public int getPlayerId() {
        return this.playerId;
    }

    public Set<Integer> getRecipientSet() {
        return this.recipientSet;
    }

    @Override
    public byte getType() {
        return this.eventType;
    }

    public void read(@NonNull ByteBuffer byteBuffer) {
        this.eventType = byteBuffer.get();
        this.error = byteBuffer.get();
        this.playerId = byteBuffer.getInt();
        this.message = NIOUtils.getStr(byteBuffer);
        this.data = NIOUtils.getByteArray(byteBuffer);
    }

    public void setChannel(SocketChannel socketChannel) {
        this.channel = socketChannel;
    }

    public void setData(byte[] bArr) {
        this.data = bArr;
    }

    @Override
    public void setError(byte b) {
        this.error = b;
    }

    public void setMessage(String str) {
        this.message = str;
    }

    @Override
    public void setPlayer(Player player) {
        this.player = player;
        if (player != null) {
            setPlayerId(player.getId());
        }
    }

    @Override
    public void setPlayerId(int i) {
        this.playerId = i;
    }

    public void setRecipientSet(Set<Integer> set) {
        this.recipientSet = set;
    }

    @Override
    public void setType(byte b) {
        this.eventType = b;
    }

    @NonNull
    public String toString() {
        return "Event[type:" + (int) this.eventType + ", playerId:" + this.playerId + ", message:" + this.message + "]";
    }

    public int write(@NonNull ByteBuffer byteBuffer) {
        int position = byteBuffer.position();
        byteBuffer.put(this.eventType);
        byteBuffer.put(this.error);
        byteBuffer.putInt(this.playerId);
        NIOUtils.putStr(byteBuffer, this.message);
        NIOUtils.putByteArray(byteBuffer, this.data);
        return byteBuffer.position() - position;
    }
}
