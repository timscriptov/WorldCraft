package com.solverlabs.worldcraft.client.common;

import com.solverlabs.worldcraft.srv.common.WorldCraftGameEvent;

import java.util.concurrent.ConcurrentLinkedQueue;

public class EventQueue {
    private final ConcurrentLinkedQueue<WorldCraftGameEvent> events = new ConcurrentLinkedQueue<>();

    public EventQueue(String str) {
    }

    public WorldCraftGameEvent deQueue() throws InterruptedException {
        if (events.size() > 0) {
            return events.poll();
        }
        return null;
    }

    public void enQueue(WorldCraftGameEvent worldCraftGameEvent) {
        events.add(worldCraftGameEvent);
    }

    public int size() {
        return events.size();
    }
}
