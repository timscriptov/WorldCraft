package com.solverlabs.worldcraft.client.common;

import com.solverlabs.worldcraft.srv.common.WorldCraftGameEvent;
import com.solverlabs.worldcraft.srv.log.WcLog;


public abstract class Wrap implements Runnable, EventHandler {
    protected static final long WORKER_SLEEP_MILLIS = 10;
    protected EventQueue eventQueue;
    protected WcLog log;
    protected boolean running = false;
    private String shortname;
    private int spareCount;
    private Thread[] workers;
    private Object countLock = new Object();

    public void handleEvent(WorldCraftGameEvent worldCraftGameEvent) {
        this.eventQueue.enQueue(worldCraftGameEvent);
    }

    public final void initWrap(int i) {
        this.shortname = getClass().getName().substring(getClass().getName().lastIndexOf(".") + 1);
        this.log = WcLog.getLogger(this.shortname);
        this.log.info("initWrap - " + this.shortname);
        this.eventQueue = new EventQueue(this.shortname + "-in");
        this.workers = new Thread[i];
        for (int i2 = 0; i2 < i; i2++) {
            this.workers[i2] = new Thread(this, this.shortname + "-" + (i2 + 1));
            this.workers[i2].setDaemon(true);
            this.workers[i2].start();
        }
    }

    protected abstract void processEvent(WorldCraftGameEvent worldCraftGameEvent);

    @Override
    public void run() {
        this.running = true;
        while (this.running) {
            try {
                WorldCraftGameEvent deQueue = this.eventQueue.deQueue();
                if (deQueue != null) {
                    processEvent(deQueue);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e2) {
                e2.printStackTrace();
            }
        }
    }

    public void shutdown() {
        this.running = false;
        if (this.workers != null) {
            for (int i = 0; i < this.workers.length; i++) {
                this.workers[i].interrupt();
            }
        }
    }
}
