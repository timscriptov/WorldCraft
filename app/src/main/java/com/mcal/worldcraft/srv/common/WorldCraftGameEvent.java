package com.mcal.worldcraft.srv.common;

import androidx.annotation.NonNull;

import com.mcal.worldcraft.srv.domain.Player;
import com.mcal.worldcraft.srv.util.BufferUtils;

import org.jboss.netty.channel.Channel;
import org.jetbrains.annotations.Contract;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class WorldCraftGameEvent implements BaseGameEvent {
    private static final int DATA_POSITION = 20;
    private static final int DATA_SIZE_POSITION = 16;
    private static final int HEADER_SIZE = 8;
    private static final Set<Byte> HEAVY_EVENTS = new HashSet<>();
    private static final int MIN_EVENT_SIZE = 20;
    private static final int PAYLOAD_HEADER_SIZE = 12;
    private static final int PAYLOAD_SIZE_POSITION = 4;

    static {
        HEAVY_EVENTS.add((byte) 1);
        HEAVY_EVENTS.add((byte) 14);
        HEAVY_EVENTS.add((byte) 18);
        HEAVY_EVENTS.add((byte) 24);
        HEAVY_EVENTS.add((byte) 49);
        HEAVY_EVENTS.add((byte) 50);
        HEAVY_EVENTS.add((byte) 51);
    }

    private final ByteBuffer byteBuffer;
    protected Channel channel;
    protected int dataIndex;
    protected int dataSize;
    protected byte error;
    protected byte eventType;
    protected Player player;
    protected int playerId;
    protected LinkedList<Integer> recipients;
    private int clientVersionId;
    private String message;
    private int pendingRecipients;

    private WorldCraftGameEvent() {
        this((byte) 0);
    }

    public WorldCraftGameEvent(byte b) {
        this(b, (byte) 0);
    }

    private WorldCraftGameEvent(byte b, byte b2) {
        this.recipients = new LinkedList<>();
        this.pendingRecipients = 0;
        this.byteBuffer = ByteBuffer.allocate(512);
        this.eventType = b;
        this.error = b2;
    }

    @NonNull
    @Contract(" -> new")
    public static BaseGameEvent create() {
        return new WorldCraftGameEvent();
    }

    private int getDataPosition() {
        return getMessageSize() + 20;
    }

    private int getMessageSize() {
        if (this.message != null) {
            return this.message.length();
        }
        return 0;
    }

    private void skipBufferToData() {
        this.byteBuffer.position(getDataPosition());
    }

    @Override
    public void addRecipient(int i) {
        this.recipients.add(i);
    }

    @Override
    public void addRecipients(Collection<Integer> collection) {
        this.recipients.addAll(collection);
    }

    @Override
    public void addRecipients(@NonNull Collection<Integer> collection, int i) {
        for (Integer num : collection) {
            if (num != i) {
                this.recipients.add(num);
            }
        }
    }

    @Override
    public Channel getChannel() {
        return this.channel;
    }

    @Override
    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public int getClientVersionId() {
        return this.clientVersionId;
    }

    @Override
    public void setClientVersionId(int i) {
        this.clientVersionId = i;
    }

    @Override
    public byte getError() {
        return this.error;
    }

    @Override
    public void setError(byte b) {
        this.error = b;
    }

    @Override
    public ByteBuffer getInputBuffer() {
        return this.byteBuffer;
    }

    @Override
    public ByteBuffer getOutputBuffer() {
        skipBufferToData();
        return this.byteBuffer;
    }

    @Override
    public Player getPlayer() {
        return this.player;
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
        return this.playerId;
    }

    @Override
    public void setPlayerId(int i) {
        this.playerId = i;
    }

    @Override
    public Collection<Integer> getRecipients() {
        return this.recipients;
    }

    @Override
    public byte getType() {
        return this.eventType;
    }

    @Override
    public void setType(byte b) {
        this.eventType = b;
    }

    @Override
    public boolean isHeavy() {
        return HEAVY_EVENTS.contains(this.eventType);
    }

    @Override
    public void prepareToSend() {
        int max = Math.max(this.byteBuffer.position(), 20) + getMessageSize();
        this.byteBuffer.position(0);
        int i = max - 8;
        int messageSize = (i - 12) - getMessageSize();
        this.byteBuffer.putInt(1);
        this.byteBuffer.putInt(i);
        this.byteBuffer.put(this.eventType);
        this.byteBuffer.put(this.error);
        this.byteBuffer.putInt(this.playerId);
        BufferUtils.writeStr(this.byteBuffer, this.message, true);
        this.byteBuffer.putInt(messageSize);
        this.byteBuffer.position(max);
        this.byteBuffer.flip();
        if (this.recipients != null) {
            this.pendingRecipients = this.recipients.size();
        }
        this.pendingRecipients = Math.max(1, this.pendingRecipients);
    }

    @Override
    public void putData(int i) {
        getOutputBuffer().putInt(i);
    }

    @Override
    public void putData(String str) {
        BufferUtils.writeStr(getOutputBuffer(), str, false);
    }

    @Override
    public void putData(ByteBuffer byteBuffer) {
        getOutputBuffer().put(byteBuffer);
    }

    @Override
    public void putData(byte[] bArr) {
        getOutputBuffer().put(bArr);
    }

    @Override
    public void putMessage(String str) {
        this.message = str;
    }

    @Override
    public ByteBuffer toByteBuffer() {
        return this.byteBuffer;
    }

    @NonNull
    public String toString() {
        return "Event[type:" + (int) this.eventType + ", playerId:" + this.playerId + "]";
    }
}
