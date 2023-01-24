package com.solverlabs.worldcraft.ui;

import com.solverlabs.droid.rugl.geom.ColouredShape;
import com.solverlabs.droid.rugl.geom.Shape;
import com.solverlabs.droid.rugl.geom.ShapeUtil;
import com.solverlabs.droid.rugl.gl.StackedRenderer;
import com.solverlabs.droid.rugl.input.TapPad;
import com.solverlabs.droid.rugl.text.Font;
import com.solverlabs.droid.rugl.text.TextShape;
import com.solverlabs.droid.rugl.util.Colour;

import org.apache.commons.compress.archivers.cpio.CpioConstants;

public class CustomButton extends TapPad {
    private final Font font;
    private final float height;
    private final String text;
    private final float width;
    private final float x;
    private final float y;
    public boolean drawText;
    public boolean isStroke;
    private ColouredShape buttonBottomBound;
    private ColouredShape buttonLeftBound;
    private ColouredShape buttonRightBound;
    private ColouredShape buttonUpBound;
    private ColouredShape innerShape;
    private TextShape textShape;

    public CustomButton(float x, float y, float width, float height, String text) {
        super(x, y, width, height);
        drawText = true;
        isStroke = false;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        font = GUI.getFont();
        if (font != null) {
            textShape = font.buildTextShape(text, Colour.white);
            textShape.translate(((width - font.getStringLength(text)) / 2.0f) + x, ((height - font.size) / 2.0f) + y, 0.0f);
        }
        this.text = text;
    }

    @Override
    public void draw(StackedRenderer sr) {
        if (buttonBottomBound == null) {
            Shape s = ShapeUtil.line(3.0f, 0.0f, 0.0f, width, 0.0f);
            buttonUpBound = new ColouredShape(s, Colour.white, null);
            buttonBottomBound = new ColouredShape(s, Colour.darkgrey, null);

            s = ShapeUtil.line(3.0f, 0.0f, 0.0f, 0.0f, height);
            buttonLeftBound = new ColouredShape(s, Colour.withAlphai(Colour.white, CpioConstants.C_IWUSR), null);
            buttonRightBound = new ColouredShape(s, Colour.withAlphai(Colour.darkgrey, CpioConstants.C_IWUSR), null);
        }
        sr.pushMatrix();
        sr.translate(x, y, 0.0f);
        buttonBottomBound.render(sr);
        sr.translate(0.0f, height, 0.0f);
        buttonUpBound.render(sr);
        sr.popMatrix();
        sr.pushMatrix();
        sr.translate(x, y, 0.0f);
        buttonLeftBound.render(sr);
        sr.translate(width, 0.0f, 0.0f);
        buttonRightBound.render(sr);
        sr.popMatrix();
        sr.render();
        if (textShape != null && drawText) {
            textShape.render(sr);
        }
        if (isSelected || touch != null) {
            drawInnerBound(sr);
        }
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public String getText() {
        return text;
    }

    private void drawInnerBound(StackedRenderer sr) {
        if (innerShape == null) {
            if (!isStroke) {
                Shape is = ShapeUtil.innerQuad(x, y, x + width, y + height, height, 0.0f);
                innerShape = new ColouredShape(is, Colour.withAlphai(Colour.white, CpioConstants.C_IWUSR), null);
            } else {
                Shape is2 = ShapeUtil.innerQuad(x, y, x + width, y + height, 4.0f, 0.0f);
                innerShape = new ColouredShape(is2, Colour.white, null);
            }
        }
        innerShape.render(sr);
    }

    public void setSelected(boolean select) {
        isSelected = select;
    }
}
