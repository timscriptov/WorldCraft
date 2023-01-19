package com.solverlabs.worldcraft.dialog.component;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;


public class MolotButton extends Button {
    public MolotButton(Context context) {
        super(context);
        setMolotTypeFace();
    }

    public MolotButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setMolotTypeFace();
    }

    public MolotButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setMolotTypeFace();
    }

    private void setMolotTypeFace() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/molot.otf");
            setTypeface(tf);
        }
    }
}
