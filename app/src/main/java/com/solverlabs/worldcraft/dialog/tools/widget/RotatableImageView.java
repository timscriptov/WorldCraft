package com.solverlabs.worldcraft.dialog.tools.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

public class RotatableImageView extends AppCompatImageView {
    private int mRotation;
    private float mXPivot;
    private float mYPivot;

    public RotatableImageView(Context context) {
        super(context);
        this.mRotation = 0;
        this.mXPivot = 0.0f;
        this.mYPivot = 0.0f;
    }

    public RotatableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mRotation = 0;
        this.mXPivot = 0.0f;
        this.mYPivot = 0.0f;
    }

    public void setRotation(int rotation, float xPivot, float yPivot) {
        this.mRotation = rotation;
        this.mXPivot = xPivot;
        this.mYPivot = yPivot;
    }

    @Override
    public float getRotation() {
        return this.mRotation;
    }

    public void setRotation(int rotation) {
        setRotation(rotation, 0.5f, 0.5f);
    }

    public float getXPivot() {
        return this.mXPivot;
    }

    public float getYPivot() {
        return this.mYPivot;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        canvas.save();
        canvas.rotate(this.mRotation, getWidth() * this.mXPivot, getHeight() * this.mYPivot);
        super.onDraw(canvas);
        canvas.restore();
    }
}
