package com.mcal.worldcraft.ui;

import com.mcal.droid.rugl.geom.ColouredShape;
import com.mcal.droid.rugl.geom.ShapeUtil;
import com.mcal.droid.rugl.geom.TexturedShape;
import com.mcal.droid.rugl.gl.StackedRenderer;
import com.mcal.droid.rugl.gl.State;
import com.mcal.droid.rugl.input.TapPad;
import com.mcal.worldcraft.factories.ItemFactory;

public class ImageTapPad extends TapPad {
    private final TexturedShape shape;
    private ColouredShape outlineBlack;
    private ColouredShape outlineWhite;

    public ImageTapPad(float x, float y, float width, float height, int w, int h) {
        super(x, y, width, height);
        shape = ItemFactory.Item.getShape(w, h);
        shape.scale(25.0f, 25.0f, 25.0f);
        shape.translate(x + 25.0f, 25.0f + y, 0.0f);
    }

    @Override
    public void draw(StackedRenderer sr) {
        if (isVisible) {
            if (outlineWhite == null) {
                outlineWhite = new ColouredShape(ShapeUtil.innerQuad(pad.x.getMin(), pad.y.getMin(), pad.x.getMax(), pad.y.getMax(), 5.0f, 0.0f), boundsWhiteColour, (State) null);
            }
            if (outlineBlack == null) {
                outlineBlack = new ColouredShape(ShapeUtil.innerQuad(pad.x.getMin() + 2.5f, pad.y.getMin() + 2.5f, pad.x.getMax() - 2.5f, pad.y.getMax() - 2.5f, pad.x.getSpan(), 0.0f), boundsBlackColour, (State) null);
            }
            outlineWhite.render(sr);
            if (touch != null) {
                outlineBlack.colours = ShapeUtil.expand(boundsWhiteColour, outlineBlack.vertexCount());
            } else {
                outlineBlack.colours = ShapeUtil.expand(boundsBlackColour, outlineBlack.vertexCount());
            }
            outlineBlack.render(sr);
            sr.render();
            renderShape(sr);
        }
    }

    public void renderShape(StackedRenderer sr) {
        if (shape != null) {
            shape.render(sr);
        }
    }
}
