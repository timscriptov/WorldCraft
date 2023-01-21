package com.solverlabs.droid.rugl.input;

import android.view.MotionEvent;
import com.solverlabs.droid.rugl.Game;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Touch {
    private static final Queue<MotionEvent> touchEvents;
    private static ArrayList<TouchListener> listeners = new ArrayList<>();
    private static float xScale = 1.0f;
    private static float yScale = 1.0f;
    public static final Pointer[] pointers = new Pointer[2];
    private static final boolean[] wasActive = new boolean[2];

    /* loaded from: classes.dex */
    public interface TouchListener {
        boolean pointerAdded(Pointer pointer);

        void pointerRemoved(Pointer pointer);

        void reset();
    }

    static {
        for (int i = 0; i < pointers.length; i++) {
            pointers[i] = new Pointer(i);
        }
        touchEvents = new ConcurrentLinkedQueue();
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

    private static void onPointerUp(Pointer pointer) {
        if (pointer.active) {
            pointer.active = false;
            for (int j = 0; j < listeners.size(); j++) {
                listeners.get(j).pointerRemoved(pointer);
            }
        }
    }

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

    /* loaded from: classes.dex */
    public static class Pointer {
        public boolean active;
        public final int id;
        public boolean isUse;
        public float size;
        public float x;
        public float y;

        private Pointer(int id) {
            this.active = false;
            this.isUse = false;
            this.id = id;
        }

        public String toString() {
            if (!this.active) {
                StringBuilder buff = new StringBuilder();
                buff.append("Inactive: ").append(this.x).append(", ").append(this.y);
                return buff.toString();
            }
            StringBuilder buff2 = new StringBuilder();
            buff2.append(this.id).append(" ( ").append(this.x).append(", ");
            buff2.append(this.y).append(" ) ").append(this.size);
            return buff2.toString();
        }
    }

    public static void reset() {
        for (int i = 0; i < pointers.length; i++) {
            pointers[i].active = false;
        }
        for (TouchListener l : listeners) {
            l.reset();
        }
    }
}