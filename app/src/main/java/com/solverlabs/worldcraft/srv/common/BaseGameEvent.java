package com.solverlabs.worldcraft.srv.common;

import org.jboss.netty.channel.Channel;

import java.nio.ByteBuffer;
import java.util.Collection;

public interface BaseGameEvent extends GameEvent {
    void addRecipient(int i);

    void addRecipients(Collection<Integer> collection);

    void addRecipients(Collection<Integer> collection, int i);

    Channel getChannel();

    void setChannel(Channel channel);

    int getClientVersionId();

    void setClientVersionId(int i);

    ByteBuffer getInputBuffer();

    ByteBuffer getOutputBuffer();

    Collection<Integer> getRecipients();

    boolean isHeavy();

    void prepareToSend();

    void putData(int i);

    void putData(String str);

    void putData(ByteBuffer byteBuffer);

    void putData(byte[] bArr);

    void putMessage(String str);

    ByteBuffer toByteBuffer();
}
