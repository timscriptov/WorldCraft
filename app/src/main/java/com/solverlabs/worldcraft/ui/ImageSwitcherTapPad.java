package com.solverlabs.worldcraft.ui;

import com.solverlabs.droid.rugl.geom.TexturedShape;
import com.solverlabs.droid.rugl.gl.StackedRenderer;
import com.solverlabs.worldcraft.factories.ItemFactory;

public class ImageSwitcherTapPad extends ImageTapPad {
    private final TexturedShape shape;
    private boolean isOn;

    public ImageSwitcherTapPad(float x, float y, float width, float height, int w, int h, int w2, int h2) {
        super(x, y, width, height, w, h);
        this.isOn = false;
        shape = ItemFactory.Item.getShape(w2, h2);
        shape.scale(25.0f, 25.0f, 25.0f);
        shape.translate(x + 25.0f, 25.0f + y, 0.0f);
    }

    @Override
    public void renderShape(StackedRenderer sr) {
        if (isOn) {
            if (shape != null) {
                shape.render(sr);
                return;
            }
            return;
        }
        super.renderShape(sr);
    }

    public void switchState() {
        isOn = !isOn;
    }

    public boolean isOn() {
        return isOn;
    }
}
