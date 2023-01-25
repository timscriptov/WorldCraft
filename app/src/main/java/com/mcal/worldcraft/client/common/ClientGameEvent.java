package com.mcal.worldcraft.client.common;

import androidx.annotation.NonNull;

import com.mcal.worldcraft.srv.common.GameEvent;
import com.mcal.worldcraft.srv.domain.Player;

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
        eventType = b;
        error = (byte) 0;
    }

    public ClientGameEvent(byte b, byte b2) {
        this(b);
        error = b2;
    }

    public ClientGameEvent(byte b, byte b2, String str) {
        this(b, b2);
        message = str;
    }

    public ClientGameEvent(byte b, String str) {
        this(b);
        message = str;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public void setChannel(SocketChannel socketChannel) {
        channel = socketChannel;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] bArr) {
        data = bArr;
    }

    @Override
    public byte getError() {
        return error;
    }

    @Override
    public void setError(byte b) {
        error = b;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String str) {
        message = str;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public void setPlayer(Player player) {
        this.player = player;
        if (player != null) {
            setPlayerId(player.getId());
        }
    }

    @Override
    public int getPlayerId() {
        return playerId;
    }

    @Override
    public void setPlayerId(int i) {
        playerId = i;
    }

    public Set<Integer> getRecipientSet() {
        return recipientSet;
    }

    public void setRecipientSet(Set<Integer> set) {
        recipientSet = set;
    }

    @Override
    public byte getType() {
        return eventType;
    }

    @Override
    public void setType(byte b) {
        eventType = b;
    }

    public void read(@NonNull ByteBuffer byteBuffer) {
        eventType = byteBuffer.get();
        error = byteBuffer.get();
        playerId = byteBuffer.getInt();
        message = NIOUtils.getStr(byteBuffer);
        data = NIOUtils.getByteArray(byteBuffer);
    }

    @NonNull
    public String toString() {
        return "Event[type:" + (int) eventType + ", playerId:" + playerId + ", message:" + message + "]";
    }

    public int write(@NonNull ByteBuffer byteBuffer) {
        int position = byteBuffer.position();
        byteBuffer.put(eventType);
        byteBuffer.put(error);
        byteBuffer.putInt(playerId);
        NIOUtils.putStr(byteBuffer, message);
        NIOUtils.putByteArray(byteBuffer, data);
        return byteBuffer.position() - position;
    }
}
