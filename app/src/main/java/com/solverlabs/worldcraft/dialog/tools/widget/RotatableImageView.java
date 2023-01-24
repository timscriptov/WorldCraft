package com.solverlabs.worldcraft.dialog.tools.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

public class RotatableImageView extends AppCompatImageView {
    private int mRotation = 0;
    private float mXPivot = 0.0f;
    private float mYPivot = 0.0f;

    public RotatableImageView(Context context) {
        super(context);
    }

    public RotatableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setRotation(int rotation, float xPivot, float yPivot) {
        mRotation = rotation;
        mXPivot = xPivot;
        mYPivot = yPivot;
    }

    @Override
    public float getRotation() {
        return mRotation;
    }

    public void setRotation(int rotation) {
        setRotation(rotation, 0.5f, 0.5f);
    }

    public float getXPivot() {
        return mXPivot;
    }

    public float getYPivot() {
        return mYPivot;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        canvas.save();
        canvas.rotate(mRotation, getWidth() * mXPivot, getHeight() * mYPivot);
        super.onDraw(canvas);
        canvas.restore();
    }
}
