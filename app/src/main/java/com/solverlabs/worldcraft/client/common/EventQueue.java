package com.solverlabs.worldcraft.client.common;

import com.solverlabs.worldcraft.srv.common.WorldCraftGameEvent;

import java.util.concurrent.ConcurrentLinkedQueue;


public class EventQueue {
    private final ConcurrentLinkedQueue<WorldCraftGameEvent> events = new ConcurrentLinkedQueue<>();

    public EventQueue(String str) {
    }

    public WorldCraftGameEvent deQueue() throws InterruptedException {
        if (this.events.size() > 0) {
            return this.events.poll();
        }
        return null;
    }

    public void enQueue(WorldCraftGameEvent worldCraftGameEvent) {
        this.events.add(worldCraftGameEvent);
    }

    public int size() {
        return this.events.size();
    }
}
