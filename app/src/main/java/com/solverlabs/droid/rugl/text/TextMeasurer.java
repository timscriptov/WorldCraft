package com.solverlabs.droid.rugl.text;


public class TextMeasurer {
    public static int indexOfCharAt(Font font, CharSequence text, float f) {
        if (f >= 0.0f) {
            Glyph last = null;
            float penX = 0.0f;
            int i = 0;
            while (i < text.length() && penX < f) {
                Glyph next = font.map(text.charAt(i));
                float kerning = last == null ? 0.0f : next.getKerningAfter(last.character);
                penX += next.advance + kerning;
                last = next;
                i++;
            }
            if (penX >= f) {
                return i;
            }
        }
        return -1;
    }

    public static int indexOfCharAt(Font font, CharSequence text, float f, int startIndex) {
        if (f >= 0.0f) {
            return indexOfCharAt(font, text.subSequence(startIndex, text.length()), f);
        }
        Glyph last = font.map(text.charAt(startIndex));
        float penX = 0.0f;
        int i = startIndex;
        while (i >= 0 && penX > f) {
            Glyph next = font.map(text.charAt(i));
            float kerning = last.getKerningAfter(next.character);
            penX -= next.advance - kerning;
            last = next;
            i--;
        }
        if (penX > 0.0f) {
            return -1;
        }
        return i;
    }
}
