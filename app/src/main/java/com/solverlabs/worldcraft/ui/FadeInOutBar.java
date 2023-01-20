package com.solverlabs.worldcraft.ui;

import com.solverlabs.droid.rugl.geom.ColouredShape;
import com.solverlabs.droid.rugl.geom.ShapeUtil;
import com.solverlabs.droid.rugl.gl.GLUtil;
import com.solverlabs.droid.rugl.gl.StackedRenderer;
import com.solverlabs.droid.rugl.input.Touch;

public class FadeInOutBar implements Touch.TouchListener {
    private static final int ALPHA_OFFSET = 24;
    private static final int MAX_ALPHA_VALUE = 256;
    private final int color;
    private int currentAlpha;
    private final long duration;
    private final FadingType fadingType;
    private FadingType lastFadeType;
    private final float maxFading;
    protected OnChangedListener onChangedListener;
    private ColouredShape shape;
    private long startedAt;

    public enum FadingType {
        FadeOut,
        FadeIn,
        FadeOutThanFadeIn
    }

    public interface OnChangedListener {
        void fadeOutDone();
    }

    public FadeInOutBar(int color, long duration, float maxFading, FadingType fadingType) {
        this.fadingType = fadingType;
        this.color = color;
        this.duration = duration;
        this.maxFading = 256.0f * maxFading;
        recreateShape();
    }

    public void setOnChangedListener(OnChangedListener onChangedListener) {
        this.onChangedListener = onChangedListener;
    }

    private void recreateShape() {
        this.shape = new ColouredShape(ShapeUtil.innerQuad(0.0f, 0.0f, 800.0f, 480.0f, 400.0f, 0.1f), this.color, GLUtil.typicalState);
    }

    public void startFadingNow() {
        this.startedAt = System.currentTimeMillis();
    }

    public void setFadingStartedAt(long startedAt) {
        this.startedAt = startedAt;
        if (this.startedAt == 0 && isFirstCircle()) {
            resetFirstCircle();
        }
    }

    public void cancel() {
        this.startedAt = 0L;
    }

    public void advance(float delta) {
        if (this.startedAt + this.duration > System.currentTimeMillis()) {
            float fdelta = ((float) (System.currentTimeMillis() - this.startedAt)) / ((float) this.duration);
            if (this.fadingType == FadingType.FadeIn) {
                fadeIn(fdelta);
            } else if (this.fadingType == FadingType.FadeOut) {
                fadeOut(fdelta);
            } else if (this.fadingType == FadingType.FadeOutThanFadeIn) {
                if (fdelta <= 0.5f) {
                    fadeOut(fdelta * 2.0f);
                } else {
                    fadeIn((fdelta - 0.5f) * 2.0f);
                }
            }
            updateShapeAlpha();
        } else if (this.currentAlpha != 0) {
            this.currentAlpha = 0;
            recreateShape();
        }
    }

    private void fadeOut(float fdelta) {
        this.currentAlpha = (int) (this.maxFading * fdelta);
        setFadingType(FadingType.FadeOut);
    }

    private void fadeIn(float fdelta) {
        this.currentAlpha = (int) (this.maxFading - ((int) (this.maxFading * fdelta)));
        setFadingType(FadingType.FadeIn);
    }

    private void setFadingType(FadingType fadingType) {
        if (this.lastFadeType != fadingType && this.fadingType == FadingType.FadeOutThanFadeIn && this.onChangedListener != null && this.lastFadeType == FadingType.FadeOut) {
            this.onChangedListener.fadeOutDone();
        }
        this.lastFadeType = fadingType;
    }

    public void draw(StackedRenderer sr) {
        if (this.currentAlpha > 0) {
            this.shape.render(sr);
        }
    }

    private void updateShapeAlpha() {
        for (int i = 0; i < this.shape.colours.length; i++) {
            this.shape.colours[i] = (this.shape.colours[i] & 16777215) | (this.currentAlpha << 24);
        }
    }

    public boolean isFirstCircle() {
        return this.lastFadeType == FadingType.FadeOut;
    }

    public void resetFirstCircle() {
        this.lastFadeType = FadingType.FadeIn;
    }

    @Override 
    public boolean pointerAdded(Touch.Pointer p) {
        return false;
    }

    @Override 
    public void pointerRemoved(Touch.Pointer p) {
    }

    @Override 
    public void reset() {
    }
}
