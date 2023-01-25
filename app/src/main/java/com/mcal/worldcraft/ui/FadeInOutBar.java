package com.mcal.worldcraft.ui;

import com.mcal.droid.rugl.geom.ColouredShape;
import com.mcal.droid.rugl.geom.ShapeUtil;
import com.mcal.droid.rugl.gl.GLUtil;
import com.mcal.droid.rugl.gl.StackedRenderer;
import com.mcal.droid.rugl.input.Touch;

public class FadeInOutBar implements Touch.TouchListener {
    private static final int ALPHA_OFFSET = 24;
    private static final int MAX_ALPHA_VALUE = 256;
    private final int color;
    private final long duration;
    private final FadingType fadingType;
    private final float maxFading;
    protected OnChangedListener onChangedListener;
    private int currentAlpha;
    private FadingType lastFadeType;
    private ColouredShape shape;
    private long startedAt;

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
        shape = new ColouredShape(ShapeUtil.innerQuad(0.0f, 0.0f, 800.0f, 480.0f, 400.0f, 0.1f), color, GLUtil.typicalState);
    }

    public void startFadingNow() {
        startedAt = System.currentTimeMillis();
    }

    public void setFadingStartedAt(long startedAt) {
        this.startedAt = startedAt;
        if (startedAt == 0 && isFirstCircle()) {
            resetFirstCircle();
        }
    }

    public void cancel() {
        startedAt = 0L;
    }

    public void advance(float delta) {
        if (startedAt + duration > System.currentTimeMillis()) {
            float fdelta = ((float) (System.currentTimeMillis() - startedAt)) / ((float) duration);
            if (fadingType == FadingType.FadeIn) {
                fadeIn(fdelta);
            } else if (fadingType == FadingType.FadeOut) {
                fadeOut(fdelta);
            } else if (fadingType == FadingType.FadeOutThanFadeIn) {
                if (fdelta <= 0.5f) {
                    fadeOut(fdelta * 2.0f);
                } else {
                    fadeIn((fdelta - 0.5f) * 2.0f);
                }
            }
            updateShapeAlpha();
        } else if (currentAlpha != 0) {
            currentAlpha = 0;
            recreateShape();
        }
    }

    private void fadeOut(float fdelta) {
        currentAlpha = (int) (maxFading * fdelta);
        setFadingType(FadingType.FadeOut);
    }

    private void fadeIn(float fdelta) {
        currentAlpha = (int) (maxFading - ((int) (maxFading * fdelta)));
        setFadingType(FadingType.FadeIn);
    }

    private void setFadingType(FadingType fadingType) {
        if (lastFadeType != fadingType && fadingType == FadingType.FadeOutThanFadeIn && onChangedListener != null && lastFadeType == FadingType.FadeOut) {
            onChangedListener.fadeOutDone();
        }
        lastFadeType = fadingType;
    }

    public void draw(StackedRenderer sr) {
        if (currentAlpha > 0) {
            shape.render(sr);
        }
    }

    private void updateShapeAlpha() {
        for (int i = 0; i < shape.colours.length; i++) {
            shape.colours[i] = (shape.colours[i] & 16777215) | (currentAlpha << ALPHA_OFFSET);
        }
    }

    public boolean isFirstCircle() {
        return lastFadeType == FadingType.FadeOut;
    }

    public void resetFirstCircle() {
        lastFadeType = FadingType.FadeIn;
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

    public enum FadingType {
        FadeOut,
        FadeIn,
        FadeOutThanFadeIn
    }

    public interface OnChangedListener {
        void fadeOutDone();
    }
}
