package com.solverlabs.worldcraft.srv.common;

import java.nio.ByteBuffer;
import java.util.Collection;
import org.jboss.netty.channel.Channel;

public interface BaseGameEvent extends GameEvent {
    void addRecipient(int i);

    void addRecipients(Collection<Integer> collection);

    void addRecipients(Collection<Integer> collection, int i);

    Channel getChannel();

    int getClientVersionId();

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

    void setChannel(Channel channel);

    void setClientVersionId(int i);

    ByteBuffer toByteBuffer();
}
