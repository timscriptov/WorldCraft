package com.mcal.droid.rugl.input;

import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mcal.droid.rugl.Game;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Provides a polling-style interface to multitouch pointers. Note that this
 * flips the y-axis so the the origin is at the bottom-left of the screen
 */
public class Touch {
    /**
     * An array of pointers. They can be active or not
     */
    public static final Pointer[] pointers = new Pointer[2];
    private static final Queue<MotionEvent> touchEvents;
    private static final ArrayList<TouchListener> listeners = new ArrayList<>();
    private static final boolean[] wasActive = new boolean[2];
    private static float xScale = 1.0f;
    private static float yScale = 1.0f;

    static {
        for (int i = 0; i < pointers.length; i++) {
            pointers[i] = new Pointer(i);
        }
        touchEvents = new ConcurrentLinkedQueue<>();
    }

    /**
     * @param me
     */
    public static void onTouchEvent(MotionEvent me) {
        touchEvents.offer(me);
    }

    /**
     * Call this once per frame to process touch events on the main thread
     */
    public static void processTouches() {
        while (!touchEvents.isEmpty()) {
            MotionEvent me = touchEvents.poll();
            // final touch has left
            if (me.getAction() == MotionEvent.ACTION_UP) {
                for (int i = 0; i < pointers.length; i++) {
                    onPointerUp(pointers[i]);
                }
            } else if ((me.getAction() & 255) == 6) {
                Pointer p = getPoinerByEvent(me);
                if (p != null) {
                    onPointerUp(p);
                }
            } else {
                updatePointers(me);
            }
        }
    }

    private static void onPointerUp(@NonNull Pointer pointer) {
        if (pointer.active) {
            pointer.active = false;
            for (int j = 0; j < listeners.size(); j++) {
                listeners.get(j).pointerRemoved(pointer);
            }
        }
    }

    @Nullable
    private static Pointer getPoinerByEvent(MotionEvent event) {
        try {
            return pointers[event.getPointerId(event.getAction() >> 8)];
        } catch (Exception e) {
            return null;
        }
    }

    private static void updatePointers(MotionEvent me) {
        try {
            int pointerCount = me.getPointerCount();
            for (int i = 0; i < pointers.length; i++) {
                wasActive[i] = pointers[i].active;
                pointers[i].active = false;
            }
            for (int i2 = 0; i2 < pointerCount; i2++) {
                int pointerIndex = me.getPointerId(i2);
                if (pointerIndex < pointers.length) {
                    Pointer p = pointers[pointerIndex];
                    p.active = true;
                    p.x = me.getX(i2) * xScale;
                    p.y = (Game.screenHeight - me.getY(i2)) * yScale;
                    p.size = me.getSize(i2);
                } else {
                    return;
                }
            }
            for (int i = 0; i < pointers.length; i++) {
                if (pointers[i].active && !wasActive[i]) {
                    // added
                    for (int j = 0; j < listeners.size(); j++) {
                        listeners.get(j).pointerAdded(pointers[i]);
                    }
                } else if (!pointers[i].active && wasActive[i]) {
                    // removed
                    for (int j = 0; j < listeners.size(); j++) {
                        listeners.get(j).pointerRemoved(pointers[i]);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void resetTouch() {
        touchEvents.clear();
        for (int i = 0; i < pointers.length; i++) {
            pointers[i] = new Pointer(i);
            wasActive[i] = pointers[i].active;
        }
    }

    /**
     * Sets scaling factors for translation between physical and desired
     * coordinate systems
     *
     * @param desiredWidth
     * @param desiredHeight
     * @param actualWidth
     * @param actualHeight
     */
    public static void setScreenSize(float desiredWidth, float desiredHeight, int actualWidth, int actualHeight) {
        xScale = desiredWidth / actualWidth;
        yScale = desiredHeight / actualHeight;
    }

    /**
     * @param l The object to inform of pointer changes
     */
    public static void addListener(TouchListener l) {
        listeners.add(l);
    }

    /**
     * @param l The object to stop informing of pointer changes
     */
    public static void removeListener(TouchListener l) {
        listeners.remove(l);
    }

    /**
     * Called at startup
     */
    public static void reset() {
        for (int i = 0; i < pointers.length; i++) {
            pointers[i].active = false;
        }
        for (TouchListener l : listeners) {
            l.reset();
        }
    }

    public interface TouchListener {
        /**
         * Called when a new pointer is added to the screen
         *
         * @param pointer This object's fields will be updated as the pointer changes
         * @return <code>true</code> if the touch should be consumed. No other
         * listeners will be notified
         */
        boolean pointerAdded(Pointer pointer);

        /**
         * Called when a pointer is removed from the screen
         *
         * @param pointer This object will no longer be updated
         */
        void pointerRemoved(Pointer pointer);

        /**
         * Called when the Touch system is initiated
         */
        void reset();
    }

    /**
     * Information on one pointer
     */
    public static class Pointer {
        public final int id;
        public boolean active;
        public boolean isUse;
        public float size;
        public float x;
        public float y;

        private Pointer(int id) {
            this.active = false;
            this.isUse = false;
            this.id = id;
        }

        @NonNull
        public String toString() {
            if (!this.active) {
                return "Inactive: " + this.x + ", " + this.y;
            }
            return this.id + " ( " + this.x + ", " + this.y + " ) " + this.size;
        }
    }
}