package com.solverlabs.droid.rugl.input;

import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.solverlabs.droid.rugl.Game;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Touch {
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

    public static void onTouchEvent(MotionEvent me) {
        touchEvents.offer(me);
    }

    public static void processTouches() {
        while (!touchEvents.isEmpty()) {
            MotionEvent me = touchEvents.poll();
            if (me.getAction() == 1) {
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
            for (int i3 = 0; i3 < pointers.length; i3++) {
                if (pointers[i3].active && !wasActive[i3]) {
                    for (int j = 0; j < listeners.size(); j++) {
                        listeners.get(j).pointerAdded(pointers[i3]);
                    }
                } else if (!pointers[i3].active && wasActive[i3]) {
                    for (int j2 = 0; j2 < listeners.size(); j2++) {
                        listeners.get(j2).pointerRemoved(pointers[i3]);
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

    public static void setScreenSize(float desiredWidth, float desiredHeight, int actualWidth, int actualHeight) {
        xScale = desiredWidth / actualWidth;
        yScale = desiredHeight / actualHeight;
    }

    public static void addListener(TouchListener l) {
        listeners.add(l);
    }

    public static void removeListener(TouchListener l) {
        listeners.remove(l);
    }

    public static void reset() {
        for (int i = 0; i < pointers.length; i++) {
            pointers[i].active = false;
        }
        for (TouchListener l : listeners) {
            l.reset();
        }
    }

    public interface TouchListener {
        boolean pointerAdded(Pointer pointer);

        void pointerRemoved(Pointer pointer);

        void reset();
    }

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