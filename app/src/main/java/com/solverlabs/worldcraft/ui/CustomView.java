package com.solverlabs.worldcraft.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;

@SuppressLint("ViewConstructor")
public class CustomView extends ViewGroup {
    private final Button deleteSave;
    private final Paint paint;
    private final String worldName;

    public CustomView(Context context, String worldName) {
        super(context);
        this.paint = new Paint();
        this.worldName = worldName;
        this.deleteSave = new Button(context);
        this.deleteSave.setText("DELETE");
        addView(this.deleteSave);
    }

    @Override 
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        this.deleteSave.layout(0, 0, 0, 0);
    }

    @Override 
    protected void onDraw(@NonNull Canvas canvas) {
        canvas.drawText(this.worldName, 0.0f, 0.0f, this.paint);
    }

    @Override 
    protected void dispatchDraw(@NonNull Canvas canvas) {
        canvas.drawText(this.worldName, 0.0f, 0.0f, this.paint);
    }

    @Override 
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);
        canvas.drawText(this.worldName, 0.0f, 0.0f, this.paint);
    }

    public String getWorldName() {
        return this.worldName;
    }

    @NonNull
    @Override
    public String toString() {
        return this.worldName;
    }
}
