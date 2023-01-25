package com.mcal.droid.rugl.text;

import androidx.annotation.NonNull;

import com.mcal.droid.rugl.geom.TexturedShape;
import com.mcal.droid.rugl.util.math.Range;

import java.math.BigDecimal;


public class Readout extends TexturedShape {
    private final int colour;
    private final int fracDigits;
    private final int intDigits;
    private final int prefixLength;
    private final int valueOffset;
    private final float[][] digitTexCoords = new float[11][];
    private final float minValue;
    private float currentValue = -1.0f;
    private float maxValue;

    /**
     * @param font
     * @param colour
     * @param prefix
     * @param signed
     * @param intDigits
     * @param fracDigits
     */
    public Readout(Font font, int colour, String prefix, boolean signed, int intDigits, int fracDigits) {
        super(build(font, colour, prefix, signed, intDigits, fracDigits));
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

    @NonNull
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

    /**
     * @param value
     */
    public void updateValue(float value) {
        if (this.currentValue != value) {
            this.texCoordsDirty = true;
            this.currentValue = value;
            value = Range.limit(value, minValue, maxValue);

            // set minus sign colour
            for (int i = (prefixLength * 4) - 4; i < prefixLength * 4; i++) {
                colours[i] = value < 0.0f ? colour : 0;
            }
            value = (float) (value / Math.pow(10.0d, intDigits - 1));

            int index = valueOffset;
            for (int i = 0; i < intDigits; i++) {
                // shift up
                int digit = (int) value;
                BigDecimal bigDec = new BigDecimal(value - digit);
                value = bigDec.setScale(2, 4).floatValue() * 10.0f;
                if (i == 0 && intDigits > 1 && digit == 0) {
                    System.arraycopy(digitTexCoords[10], 0, texCoords, index, 8);
                } else {
                    System.arraycopy(digitTexCoords[digit], 0, texCoords, index, 8);
                }
                index += 8;
            }
            index += 8;
            for (int i3 = 0; i3 < fracDigits; i3++) {
                int digit = (int) value;
                value = (value - digit) * 10.0f;
                System.arraycopy(digitTexCoords[digit], 0, texCoords, index, 8);
                index += 8;
            }
        }
    }

    public void setMaxValue(int i) {
        maxValue = 99.0f;
    }
}
