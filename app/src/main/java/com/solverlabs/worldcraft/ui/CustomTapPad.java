package com.solverlabs.worldcraft.ui;

import com.solverlabs.droid.rugl.geom.ColouredShape;
import com.solverlabs.droid.rugl.geom.ShapeUtil;
import com.solverlabs.droid.rugl.gl.StackedRenderer;
import com.solverlabs.droid.rugl.gl.State;
import com.solverlabs.droid.rugl.input.TapPad;
import com.solverlabs.droid.rugl.text.Font;
import com.solverlabs.droid.rugl.text.TextShape;
import com.solverlabs.droid.rugl.util.Colour;

public class CustomTapPad extends TapPad {
    private final Font font;
    private final float height;
    private final float width;
    private final float x;
    private final float y;
    private ColouredShape outlineBlack;
    private ColouredShape outlineWhite;
    private TextShape textShape;

    public CustomTapPad(float x, float y, float width, float height, Font font, String text) {
        this(x, y, width, height, font, text, false);
    }

    public CustomTapPad(float x, float y, float width, float height, Font font, String text, boolean top) {
        super(x, y, width, height);
        this.font = font;
        if (font != null) {
            this.textShape = font.buildTextShape(text, Colour.white);
            this.textShape.translate(x + ((width - font.getStringLength(text)) / 2.0f), y + ((height - font.size) / 2.0f), top ? 0.1f : 0.0f);
        }
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
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
            if (this.textShape != null) {
                this.textShape.render(sr);
            }
        }
    }

    public void setText(String text) {
        if (this.font != null) {
            this.textShape = this.font.buildTextShape(text, Colour.packFloat(1.0f, 1.0f, 1.0f, 1.0f));
            this.textShape.translate(this.x + ((this.width - this.font.getStringLength(text)) / 2.0f), this.y + ((this.height - this.font.size) / 2.0f), 0.0f);
        }
    }
}
