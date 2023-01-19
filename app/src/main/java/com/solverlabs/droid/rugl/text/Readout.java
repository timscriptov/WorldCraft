package com.solverlabs.droid.rugl.text;

import com.solverlabs.droid.rugl.geom.TexturedShape;
import com.solverlabs.droid.rugl.util.math.Range;

import java.math.BigDecimal;


public class Readout extends TexturedShape {
    private static final int EMPTY_SPACE_INDEX = 10;
    private final int colour;
    private final int fracDigits;
    private final int intDigits;
    private final int prefixLength;
    private final int valueOffset;
    private float currentValue;
    private float[][] digitTexCoords;
    private float maxValue;
    private float minValue;

    public Readout(Font font, int colour, String prefix, boolean signed, int intDigits, int fracDigits) {
        super(build(font, colour, prefix, signed, intDigits, fracDigits));
        this.digitTexCoords = new float[11][];
        this.currentValue = -1.0f;
        this.colour = colour;
        this.intDigits = intDigits;
        this.fracDigits = fracDigits;
        this.prefixLength = prefix.length();
        this.maxValue = (float) (((Math.pow(10.0d, intDigits) - 1.0d) + 1.0d) - Math.pow(10.0d, -fracDigits));
        this.minValue = signed ? -this.maxValue : 0.0f;
        this.valueOffset = ((signed ? 1 : 0) + prefix.length()) * 4 * 2;
        for (int i = 0; i < 10; i++) {
            this.digitTexCoords[i] = font.getTexCoords(String.valueOf(i), null, 0);
        }
        this.digitTexCoords[10] = font.getTexCoords(" ", null, 0);
    }

    private static TexturedShape build(Font font, int colour, String prefix, boolean signed, int intDigits, int fracDigits) {
        StringBuilder buff = new StringBuilder(prefix);
        if (signed) {
            buff.append('-');
        }
        for (int i = 0; i < intDigits; i++) {
            buff.append('0');
        }
        if (fracDigits > 0) {
            buff.append('/');
        }
        for (int i2 = 0; i2 < fracDigits; i2++) {
            buff.append('0');
        }
        return font.buildTextShape(buff, colour);
    }

    public void updateValue(float value) {
        if (this.currentValue != value) {
            this.mTexCoordsDirty = true;
            this.currentValue = value;
            float value2 = Range.limit(value, this.minValue, this.maxValue);
            for (int i = (this.prefixLength * 4) - 4; i < this.prefixLength * 4; i++) {
                this.colours[i] = value2 < 0.0f ? this.colour : 0;
            }
            float value3 = (float) (value2 / Math.pow(10.0d, this.intDigits - 1));
            int index = this.valueOffset;
            for (int i2 = 0; i2 < this.intDigits; i2++) {
                int digit = (int) value3;
                BigDecimal bigDec = new BigDecimal(value3 - digit);
                value3 = bigDec.setScale(2, 4).floatValue() * 10.0f;
                if (i2 == 0 && this.intDigits > 1 && digit == 0) {
                    System.arraycopy(this.digitTexCoords[10], 0, this.mTexCoords, index, 8);
                } else {
                    System.arraycopy(this.digitTexCoords[digit], 0, this.mTexCoords, index, 8);
                }
                index += 8;
            }
            int index2 = index + 8;
            for (int i3 = 0; i3 < this.fracDigits; i3++) {
                int digit2 = (int) value3;
                value3 = (value3 - digit2) * 10.0f;
                System.arraycopy(this.digitTexCoords[digit2], 0, this.mTexCoords, index2, 8);
                index2 += 8;
            }
        }
    }

    public void setMaxValue(int i) {
        this.maxValue = 99.0f;
    }
}
