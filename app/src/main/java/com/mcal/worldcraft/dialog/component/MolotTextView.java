package com.mcal.worldcraft.dialog.component;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

public class MolotTextView extends AppCompatTextView {
    public MolotTextView(Context context) {
        super(context);
        setMolotTypeFace();
    }

    public MolotTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setMolotTypeFace();
    }

    public MolotTextView(Context context, AttributeSet attrs, int defStyle) {
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
