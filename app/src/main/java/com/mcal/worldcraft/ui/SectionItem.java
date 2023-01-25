package com.mcal.worldcraft.ui;

public class SectionItem implements OptionItem {
    private final String title;

    public SectionItem(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    @Override
    public boolean isSection() {
        return true;
    }

    @Override
    public boolean hasImage() {
        return false;
    }
}
