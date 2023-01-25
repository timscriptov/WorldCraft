package com.mcal.worldcraft.ui;

import com.mcal.droid.rugl.geom.ColouredShape;
import com.mcal.droid.rugl.geom.ShapeUtil;
import com.mcal.droid.rugl.gl.StackedRenderer;
import com.mcal.droid.rugl.gl.State;
import com.mcal.droid.rugl.input.TapPad;
import com.mcal.droid.rugl.text.Font;
import com.mcal.droid.rugl.text.TextShape;
import com.mcal.droid.rugl.util.Colour;

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
            textShape = font.buildTextShape(text, Colour.white);
            textShape.translate(x + ((width - font.getStringLength(text)) / 2.0f), y + ((height - font.size) / 2.0f), top ? 0.1f : 0.0f);
        }
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
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
            if (textShape != null) {
                textShape.render(sr);
            }
        }
    }

    public void setText(String text) {
        if (font != null) {
            textShape = font.buildTextShape(text, Colour.packFloat(1.0f, 1.0f, 1.0f, 1.0f));
            textShape.translate(x + ((width - font.getStringLength(text)) / 2.0f), y + ((height - font.size) / 2.0f), 0.0f);
        }
    }
}
