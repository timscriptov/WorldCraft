package com.mcal.worldcraft.multiplayer.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.mcal.worldcraft.R;

import java.util.ArrayList;

public class AddAdapter extends BaseAdapter {
    public static final int ITEM_APPWIDGET = 1;
    public static final int ITEM_LIVE_FOLDER = 2;
    public static final int ITEM_SHORTCUT = 0;
    public static final int ITEM_WALLPAPER = 3;
    private final LayoutInflater mInflater;
    private final ArrayList<ListItem> mItems = new ArrayList<>();

    public AddAdapter(@NonNull Activity launcher) {
        this.mInflater = (LayoutInflater) launcher.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Resources res = launcher.getResources();
        this.mItems.add(new ListItem(res, R.string.red_man, R.drawable.man1, 0));
        this.mItems.add(new ListItem(res, R.string.sensei_man, R.drawable.man2, 1));
        this.mItems.add(new ListItem(res, R.string.military_man, R.drawable.man3, 2));
        this.mItems.add(new ListItem(res, R.string.pirate, R.drawable.man4, 2));
        this.mItems.add(new ListItem(res, R.string.cop, R.drawable.man5, 2));
        this.mItems.add(new ListItem(res, R.string.eagle_eye, R.drawable.man6, 2));
        this.mItems.add(new ListItem(res, R.string.flash, R.drawable.man7, 2));
        this.mItems.add(new ListItem(res, R.string.bee, R.drawable.man8, 2));
        this.mItems.add(new ListItem(res, R.string.robo, R.drawable.man9, 2));
        this.mItems.add(new ListItem(res, R.string.tux, R.drawable.man10, 2));
        this.mItems.add(new ListItem(res, R.string.emily, R.drawable.woman1, 0));
        this.mItems.add(new ListItem(res, R.string.diana, R.drawable.woman2, 1));
        this.mItems.add(new ListItem(res, R.string.jessica, R.drawable.woman3, 2));
        this.mItems.add(new ListItem(res, R.string.victoria, R.drawable.woman4, 2));
        this.mItems.add(new ListItem(res, R.string.rachel, R.drawable.woman5, 2));
        this.mItems.add(new ListItem(res, R.string.jennifer, R.drawable.woman6, 2));
        this.mItems.add(new ListItem(res, R.string.lily, R.drawable.woman7, 2));
        this.mItems.add(new ListItem(res, R.string.mary, R.drawable.woman8, 2));
        this.mItems.add(new ListItem(res, R.string.caroline, R.drawable.woman9, 2));
        this.mItems.add(new ListItem(res, R.string.vanessa, R.drawable.woman10, 2));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ListItem item = (ListItem) getItem(position);
        if (convertView == null) {
            convertView = this.mInflater.inflate(R.layout.add_list_item, parent, false);
        }
        TextView textView = (TextView) convertView;
        textView.setTag(item);
        textView.setText(item.text);
        textView.setCompoundDrawablesWithIntrinsicBounds(item.image, null, null, null);
        return convertView;
    }

    @Override
    public int getCount() {
        return this.mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return this.mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public static class ListItem {
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
}
