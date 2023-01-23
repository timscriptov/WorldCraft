package com.solverlabs.worldcraft.ui;

import com.solverlabs.droid.rugl.gl.StackedRenderer;
import com.solverlabs.droid.rugl.input.TapPad;
import com.solverlabs.droid.rugl.text.Font;
import com.solverlabs.worldcraft.multiplayer.Multiplayer;

import java.util.ArrayList;
import java.util.LinkedList;

public class ChatBox extends TapPad {
    public static final int MAX_CHAT_MESSAGE__SIZE = 50;
    private static final int MAX_LIST_MESSAGE = 5;
    private final LinkedList<ChatMessage> messageList;
    private Font font;
    private float height;
    private float width;
    private float x;
    private float y;

    public ChatBox(float x, float y, float width, float height, Font font) {
        super(x, y, width, height);
        messageList = new LinkedList<>();
        if (font != null) {
            this.font = font;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    public static int getMaxChatMessageLength() {
        return (MAX_CHAT_MESSAGE__SIZE - Multiplayer.instance.playerName.length()) - 2;
    }

    @Override
    public void draw(StackedRenderer sr) {
        if (sr != null && isVisible) {
            try {
                ArrayList<ChatMessage> list = new ArrayList<>(messageList);
                for (ChatMessage chatMessage : list) {
                    if (chatMessage != null) {
                        chatMessage.draw(sr);
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public synchronized void advance(float delta) {
        super.advance();
        ArrayList<ChatMessage> list = new ArrayList<>(messageList);
        for (ChatMessage chatMessage : list) {
            if (chatMessage != null) {
                chatMessage.advance(delta);
            }
        }
    }

    public void addMessage(String msg) {
        synchronized (messageList) {
            if (messageList.size() >= MAX_LIST_MESSAGE) {
                messageList.removeLast();
            }
            messageList.addFirst(new ChatMessage(msg, x, y, width, height, font));
        }
        int position = 0;
        ArrayList<ChatMessage> list = new ArrayList<>(messageList);
        for (ChatMessage chatMessage : list) {
            chatMessage.update(position);
            position++;
        }
    }

    public void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }
}
