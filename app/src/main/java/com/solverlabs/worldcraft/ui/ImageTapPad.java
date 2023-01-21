package com.solverlabs.worldcraft.ui;

import com.solverlabs.droid.rugl.geom.ColouredShape;
import com.solverlabs.droid.rugl.geom.ShapeUtil;
import com.solverlabs.droid.rugl.geom.TexturedShape;
import com.solverlabs.droid.rugl.gl.StackedRenderer;
import com.solverlabs.droid.rugl.gl.State;
import com.solverlabs.droid.rugl.input.TapPad;
import com.solverlabs.worldcraft.factories.ItemFactory;

public class ImageTapPad extends TapPad {
    private final TexturedShape shape;
    private ColouredShape outlineBlack;
    private ColouredShape outlineWhite;

    public ImageTapPad(float x, float y, float width, float height, int w, int h) {
        super(x, y, width, height);
        this.shape = ItemFactory.Item.getShape(w, h);
        this.shape.scale(25.0f, 25.0f, 25.0f);
        this.shape.translate(x + 25.0f, 25.0f + y, 0.0f);
    }

    @Override
    public void draw(StackedRenderer sr) {
        if (this.isVisible) {
            if (this.outlineWhite == null) {
                this.outlineWhite = new ColouredShape(ShapeUtil.innerQuad(this.pad.x.getMin(), this.pad.y.getMin(), this.pad.x.getMax(), this.pad.y.getMax(), 5.0f, 0.0f), this.boundsWhiteColour, (State) null);
            }
            if (this.outlineBlack == null) {
                this.outlineBlack = new ColouredShape(ShapeUtil.innerQuad(this.pad.x.getMin() + 2.5f, this.pad.y.getMin() + 2.5f, this.pad.x.getMax() - 2.5f, this.pad.y.getMax() - 2.5f, this.pad.x.getSpan(), 0.0f), this.boundsBlackColour, (State) null);
            }
            this.outlineWhite.render(sr);
            if (this.touch != null) {
                this.outlineBlack.colours = ShapeUtil.expand(this.boundsWhiteColour, this.outlineBlack.vertexCount());
            } else {
                this.outlineBlack.colours = ShapeUtil.expand(this.boundsBlackColour, this.outlineBlack.vertexCount());
            }
            this.outlineBlack.render(sr);
            sr.render();
            renderShape(sr);
        }
    }

    public void renderShape(StackedRenderer sr) {
        if (this.shape != null) {
            this.shape.render(sr);
        }
    }
}
