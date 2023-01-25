package com.mcal.droid.rugl.res;

import android.content.res.Resources;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * An asynchronous resource-loading service. Maintains two threads: one to do
 * the loading IO, one to do post-loading processing
 */
public class ResourceLoader {
    /***/
    public static final String LOG_TAG = "ResourceLoader";
    private static final List<Loader> complete = Collections.synchronizedList(new LinkedList<Loader>());
    private static final ExecutorService loaderService = Executors.newSingleThreadExecutor();
    private static final AtomicInteger queueSize = new AtomicInteger(0);
    private static final ExecutorService postLoaderService = Executors
            .newSingleThreadExecutor();
    /***/
    public static Resources resources;

    /**
     * Starts the loader thread
     *
     * @param resources
     */
    public static void start(final Resources resources) {
        ResourceLoader.resources = resources;
    }

    /**
     * Asynchronously load a resource
     *
     * @param l
     */
    public static void load(final Loader l) {
        queueSize.incrementAndGet();
        loaderService.submit(new LoaderRunnable(l));
    }

    /**
     * Synchronously load a resource
     *
     * @param l
     */
    public static void loadNow(@NonNull final Loader l) {
        l.load();
        l.postLoad();
        l.complete();
    }

    /**
     * Call this in the main thread, it'll cause completed loaders to call
     * {@link Loader#complete()}
     */
    public static void checkCompletion() {
        while (!complete.isEmpty()) {
            final Loader l = complete.remove(0);
            queueSize.decrementAndGet();
            Log.i(LOG_TAG, "Loaded resource " + l);

            l.complete();
        }
    }

    /**
     * Gets the size of the loader queue
     *
     * @return the number of loaders waiting to be executed
     */
    public static int queueSize() {
        return queueSize.get();
    }

    /**
     * Override this class to load resources
     *
     * @param <T>
     */
    public static abstract class Loader<T> {
        /**
         * Indicates if the loader should {@link #complete()} as soon as possible,
         * or if it should be deferred to whenever
         * {@link ResourceLoader#checkCompletion()} is called.
         */
        public boolean selfCompleting = false;
        /**
         * The loaded resource
         */
        protected T resource;
        /**
         * If an exception is encountered during loading, save it here and you can
         * deal with it later
         */
        protected Throwable exception;

        /**
         * Overload this to do the loading IO and set {@link #resource}. This is
         * called on a shared loading thread
         */
        public abstract void load();

        /**
         * This method is called on shared thread. Use it to do any processing
         */
        public void postLoad() {
        }

        /**
         * This is called on the main thread when loading is complete
         */
        public abstract void complete();
    }

    private static class LoaderRunnable implements Runnable {
        private final Loader loader;

        private boolean loaded = false;

        private LoaderRunnable(final Loader loader) {
            this.loader = loader;
        }

        @Override
        public void run() {
            if (!loaded) {
                loader.load();
                loaded = true;
                postLoaderService.submit(this);
            } else {
                loader.postLoad();

                if (loader.selfCompleting) {
                    loader.complete();
                } else {
                    complete.add(loader);
                }
            }
        }
    }
}
