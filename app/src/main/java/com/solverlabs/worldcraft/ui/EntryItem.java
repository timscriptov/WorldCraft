package com.solverlabs.worldcraft.ui;

public class EntryItem implements OptionItem {
    public final int ID;
    public final String title;
    public boolean hasImage;
    public int resID;
    public String subtitle;

    public EntryItem(String title, String subtitle, int Id, boolean hasImage, int resID) {
        this.title = title;
        this.subtitle = subtitle;
        this.ID = Id;
        this.hasImage = hasImage;
        this.resID = resID;
    }

    public EntryItem(String title, String subtitle, int Id) {
        this.title = title;
        this.subtitle = subtitle;
        this.ID = Id;
        this.hasImage = false;
    }

    @Override
    public boolean isSection() {
        return false;
    }

    @Override
    public boolean hasImage() {
        return this.hasImage;
    }
}
