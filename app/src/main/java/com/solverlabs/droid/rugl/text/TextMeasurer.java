package com.solverlabs.droid.rugl.text;

/**
 * Utility for translating between the length along a line and the
 * character at that position
 *
 * @author ryanm
 */
public class TextMeasurer {

    /**
     * Returns the index in the string of the character that occupies
     * the specified space
     *
     * @param font The font to measure
     * @param text The text to measure
     * @param f    The distance from the start of the text to query
     * @return The index of the character that is at distance f from
     * the start of the text, or -1 if the text finishes before
     * that point, or if f is negative
     */
    public static int indexOfCharAt(Font font, CharSequence text, float f) {
        if (f >= 0) {
            Glyph last = null;
            float penX = 0;
            int i;
            for (i = 0; i < text.length() && penX < f; i++) {
                Glyph next = font.map(text.charAt(i));
                float kerning = last == null ? 0 : next.getKerningAfter(last.character);

                penX += next.advance + kerning;

                last = next;
            }

            if (penX >= f) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Returns the index in the string of the character that occupies
     * the specified point
     *
     * @param font       The font to measure
     * @param text       The text to measure
     * @param f          The distance from the origin of the starting character
     * @param startIndex The distance from the starting character. Negative
     *                   values will measure backwards in the string
     * @return The index of the character at the specified point, or -1
     * if the text does not occupy that point
     */
    public static int indexOfCharAt(Font font, CharSequence text, float f, int startIndex) {
        if (f >= 0) { // get the easy case
            return indexOfCharAt(font, text.subSequence(startIndex, text.length()), f);
        }

        // measure backwards
        Glyph last = font.map(text.charAt(startIndex));
        float penX = 0;
        int i;

        for (i = startIndex; i >= 0 && penX > f; i--) {
            Glyph next = font.map(text.charAt(i));
            float kerning = last.getKerningAfter(next.character);

            penX -= next.advance - kerning;

            last = next;
        }

        if (penX <= 0) {
            return i;
        } else {
            return -1;
        }
    }

}
