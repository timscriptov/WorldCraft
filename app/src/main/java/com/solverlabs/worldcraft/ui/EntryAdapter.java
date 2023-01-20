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
        this.vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override 
    public View getView(int position, View convertView, ViewGroup parent) {
        OptionItem i = this.items.get(position);
        if (i == null) {
            return convertView;
        }
        if (i.isSection()) {
            SectionItem si = (SectionItem) i;
            View v = this.vi.inflate(R.layout.list_item_section, null);
            v.setOnClickListener(null);
            v.setOnLongClickListener(null);
            v.setLongClickable(false);
            TextView sectionView = v.findViewById(R.id.list_item_section_text);
            sectionView.setText(si.getTitle());
            return v;
        }
        EntryItem ei = (EntryItem) i;
        View v2 = this.vi.inflate(R.layout.list_item_entry, null);
        TextView title = v2.findViewById(R.id.list_item_entry_title);
        TextView subtitle = v2.findViewById(R.id.list_item_entry_summary);
        if (ei.hasImage()) {
            ImageView image = v2.findViewById(R.id.list_item_image);
            image.setImageResource(ei.resID);
        }
        if (title != null) {
            title.setText(ei.title);
        }
        if (subtitle != null) {
            subtitle.setText(ei.subtitle);
            return v2;
        }
        return v2;
    }
}
