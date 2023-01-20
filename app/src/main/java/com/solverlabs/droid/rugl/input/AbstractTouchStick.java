package com.solverlabs.droid.rugl.input;

import com.solverlabs.droid.rugl.gl.StackedRenderer;


public abstract class AbstractTouchStick implements Touch.TouchListener {
    public float x;
    public float y;
    public long tapTime = 150;
    public long clickHoldDelay = 150;
    public ClickListener listener = null;
    protected Touch.Pointer touch = null;

    public abstract void advance();

    public abstract void draw(StackedRenderer stackedRenderer);


    public static abstract class ClickListener {
        public abstract void onClick();

        public abstract void onClickHold(boolean z);

        public abstract void onMove();

        public abstract void onUp();
    }
}
