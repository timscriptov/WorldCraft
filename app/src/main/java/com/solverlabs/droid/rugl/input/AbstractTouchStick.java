package com.solverlabs.droid.rugl.input;

import com.solverlabs.droid.rugl.gl.StackedRenderer;

/**
 * Remember to register these with
 * {@link Touch#addListener(Touch.TouchListener)} before use, and to
 * {@link Touch#removeListener(Touch.TouchListener)} when you're done
 */
public abstract class AbstractTouchStick implements Touch.TouchListener {
    /**
     * Current x value, in range -1 (left) to 1 (right)
     */
    public float x;
    /**
     * Current y value, in range -1 (bottom) to 1 (top)
     */
    public float y;
    /**
     * The maximum touch time for a click to be registered
     */
    public long tapTime = 150;
    public long clickHoldDelay = 150;
    /**
     * Listener to notify of a click
     */
    public ClickListener listener = null;
    /**
     * Use this to track the current touch
     */
    protected Touch.Pointer touch = null;

    /**
     * Update the {@link #x} and {@link #y} values according to input
     */
    public abstract void advance();

    /**
     * @param stackedRenderer
     */
    public abstract void draw(StackedRenderer stackedRenderer);


    public static abstract class ClickListener {
        /**
         * The stick has been tapped
         */
        public abstract void onClick();

        /**
         * The stick has been clicked, and then long-held
         *
         * @param active <code>true</code> when we start the click-hold,
         *               <code>false</code> when we end it
         */
        public abstract void onClickHold(boolean active);

        public abstract void onMove();

        public abstract void onUp();
    }
}
