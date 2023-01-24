package com.solverlabs.worldcraft.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.solverlabs.worldcraft.R;

import java.util.ArrayList;

public class EntryAdapter extends ArrayAdapter<OptionItem> {
    private final Context context;
    private final ArrayList<OptionItem> items;
    private final LayoutInflater vi;

    public EntryAdapter(Context context, ArrayList<OptionItem> items) {
        super(context, 0, items);
        this.context = context;
        this.items = items;
        vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        OptionItem optionItem = items.get(position);
        if (optionItem == null) {
            return convertView;
        }
        if (optionItem.isSection()) {
            SectionItem si = (SectionItem) optionItem;
            View v = vi.inflate(R.layout.list_item_section, null);
            v.setOnClickListener(null);
            v.setOnLongClickListener(null);
            v.setLongClickable(false);
            TextView sectionView = v.findViewById(R.id.list_item_section_text);
            sectionView.setText(si.getTitle());
            return v;
        }
        EntryItem entryItem = (EntryItem) optionItem;
        View view = vi.inflate(R.layout.list_item_entry, null);
        TextView title = view.findViewById(R.id.list_item_entry_title);
        TextView subtitle = view.findViewById(R.id.list_item_entry_summary);
        if (entryItem.hasImage()) {
            ImageView image = view.findViewById(R.id.list_item_image);
            image.setImageResource(entryItem.resID);
        }
        if (title != null) {
            title.setText(entryItem.title);
        }
        if (subtitle != null) {
            subtitle.setText(entryItem.subtitle);
            return view;
        }
        return view;
    }
}
