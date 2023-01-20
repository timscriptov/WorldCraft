package com.solverlabs.worldcraft.ui;

import com.solverlabs.droid.rugl.geom.TexturedShape;
import com.solverlabs.droid.rugl.gl.StackedRenderer;
import com.solverlabs.worldcraft.factories.ItemFactory;

public class ImageSwitcherTapPad extends ImageTapPad {
    private boolean isOn;
    private final TexturedShape shape;

    public ImageSwitcherTapPad(float x, float y, float width, float height, int w, int h, int w2, int h2) {
        super(x, y, width, height, w, h);
        this.isOn = false;
        this.shape = ItemFactory.Item.getShape(w2, h2);
        this.shape.scale(25.0f, 25.0f, 25.0f);
        this.shape.translate(x + 25.0f, 25.0f + y, 0.0f);
    }

    @Override
    public void renderShape(StackedRenderer sr) {
        if (this.isOn) {
            if (this.shape != null) {
                this.shape.render(sr);
                return;
            }
            return;
        }
        super.renderShape(sr);
    }

    public void switchState() {
        this.isOn = !this.isOn;
    }

    public boolean isOn() {
        return this.isOn;
    }
}
