package com.solverlabs.worldcraft.client.common;

import com.solverlabs.worldcraft.srv.common.WorldCraftGameEvent;
import com.solverlabs.worldcraft.srv.log.WcLog;

public abstract class Wrap implements Runnable, EventHandler {
    protected static final long WORKER_SLEEP_MILLIS = 10;
    private final Object countLock = new Object();
    protected EventQueue eventQueue;
    protected WcLog log;
    protected boolean running = false;
    private String shortname;
    private int spareCount;
    private Thread[] workers;

    public void handleEvent(WorldCraftGameEvent worldCraftGameEvent) {
        eventQueue.enQueue(worldCraftGameEvent);
    }

    public final void initWrap(int i) {
        shortname = getClass().getName().substring(getClass().getName().lastIndexOf(".") + 1);
        log = WcLog.getLogger(shortname);
        log.info("initWrap - " + shortname);
        eventQueue = new EventQueue(shortname + "-in");
        workers = new Thread[i];
        for (int i2 = 0; i2 < i; i2++) {
            workers[i2] = new Thread(this, shortname + "-" + (i2 + 1));
            workers[i2].setDaemon(true);
            workers[i2].start();
        }
    }

    protected abstract void processEvent(WorldCraftGameEvent worldCraftGameEvent);

    @Override
    public void run() {
        running = true;
        while (running) {
            try {
                WorldCraftGameEvent deQueue = eventQueue.deQueue();
                if (deQueue != null) {
                    processEvent(deQueue);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void shutdown() {
        running = false;
        if (workers != null) {
            for (int i = 0; i < workers.length; i++) {
                workers[i].interrupt();
            }
        }
    }
}
