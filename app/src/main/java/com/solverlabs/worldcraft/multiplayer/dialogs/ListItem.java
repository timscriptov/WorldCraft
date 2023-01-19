package com.solverlabs.worldcraft.multiplayer.dialogs;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;


public class ListItem {
    public final int actionTag;
    public final Drawable image;
    public final CharSequence text;

    public ListItem(Resources res, int textResourceId, int imageResourceId, int actionTag) {
        this.text = res.getString(textResourceId);
        if (imageResourceId != -1) {
            this.image = res.getDrawable(imageResourceId);
        } else {
            this.image = null;
        }
        this.actionTag = actionTag;
    }
}
