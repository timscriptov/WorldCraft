package com.solverlabs.droid.rugl.res;

import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.LinkedList;


public class ResourceLoader {
    public static Resources mResources;
    public static LinkedList<Loader<?>> mComplete = new LinkedList<>();
    protected static LoaderService mPostLoaderService = new LoaderService();
    private static LoaderService mLoaderService;

    static {
        mLoaderService = new LoaderService();
        mLoaderService = new LoaderService();
        mLoaderService.start();
        mPostLoaderService.start();
    }

    public static void start(Resources resources2) {
        mResources = resources2;
    }

    public static void load(Loader<?> l) {
        mLoaderService.add(new LoaderRunnable(l));
    }

    public static void loadNow(@NonNull Loader<?> l) {
        l.load();
        l.postLoad();
        l.complete();
    }

    public static void checkCompletion() {
        while (!mComplete.isEmpty()) {
            Loader<?> l = mComplete.remove(0);
            l.complete();
        }
    }


    public static class LoaderService implements Runnable {
        private static final long SLEEP_TIME = 10;
        final LinkedList<Runnable> queue = new LinkedList<>();
        private boolean active;

        public void add(Runnable runnable) {
            synchronized (queue) {
                queue.offer(runnable);
            }
        }

        public Runnable poll() {
            Runnable runnable;
            synchronized (queue) {
                runnable = queue.poll();
            }
            return runnable;
        }

        @Override
        public void run() {
            while (active) {
                try {
                    Runnable runnable = poll();
                    if (runnable != null) {
                        runnable.run();
                    }
                    Thread.sleep(SLEEP_TIME);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }

        public void start() {
            active = true;
            Thread t = new Thread(this);
            t.setPriority(1);
            t.start();
        }

        public void stop() {
            active = false;
        }
    }


    public static abstract class Loader<T> {
        public boolean selfCompleting = true;
        protected Throwable exception;
        @Nullable
        protected T resource;

        public abstract void complete();

        public abstract void load();

        public void postLoad() {
        }
    }

    public static class LoaderRunnable implements Runnable {
        private final Loader<?> mLoader;
        private boolean mLoaded;

        private LoaderRunnable(Loader<?> loader) {
            mLoaded = false;
            mLoader = loader;
        }

        @Override
        public void run() {
            if (!mLoaded) {
                mLoader.load();
                mLoaded = true;
                ResourceLoader.mPostLoaderService.add(this);
                return;
            }
            mLoader.postLoad();
            if (mLoader.selfCompleting) {
                mLoader.complete();
            } else {
                ResourceLoader.mComplete.add(mLoader);
            }
        }
    }
}
