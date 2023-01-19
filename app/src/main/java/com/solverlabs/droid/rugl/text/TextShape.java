package com.solverlabs.droid.rugl.text;

import com.solverlabs.droid.rugl.geom.TexturedShape;


public class TextShape extends TexturedShape {
    public final Font font;
    public final String string;

    public TextShape(TexturedShape shape, Font font, String string) {
        super(shape);
        this.font = font;
        this.string = string;
    }
}
