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
        paint = new Paint();
        this.worldName = worldName;
        deleteSave = new Button(context);
        deleteSave.setText("DELETE");
        addView(deleteSave);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        deleteSave.layout(0, 0, 0, 0);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        canvas.drawText(worldName, 0.0f, 0.0f, paint);
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        canvas.drawText(worldName, 0.0f, 0.0f, paint);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);
        canvas.drawText(worldName, 0.0f, 0.0f, paint);
    }

    public String getWorldName() {
        return worldName;
    }

    @NonNull
    @Override
    public String toString() {
        return worldName;
    }
}
