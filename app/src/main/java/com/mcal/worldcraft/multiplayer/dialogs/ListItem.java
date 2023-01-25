package com.mcal.worldcraft.multiplayer.dialogs;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

public class ListItem {
    public final int actionTag;
    public final Drawable image;
    public final CharSequence text;

    @SuppressLint("UseCompatLoadingForDrawables")
    public ListItem(@NonNull Resources res, int textResourceId, int imageResourceId, int actionTag) {
        this.text = res.getString(textResourceId);
        if (imageResourceId != -1) {
            this.image = res.getDrawable(imageResourceId);
        } else {
            this.image = null;
        }
        this.actionTag = actionTag;
    }
}
