package com.mcal.droid.rugl.text;

import com.mcal.droid.rugl.geom.TexturedShape;
import com.mcal.droid.rugl.geom.TexturedShapeWelder;
import com.mcal.droid.rugl.util.geom.Vector2f;
import com.mcal.droid.rugl.util.geom.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a block of line-wrapped text
 *
 * @author ryanm
 */
public class TextLayout {
    /**
     * The full text of this {@link TextLayout}
     */
    public final String fullText;
    /**
     * Array of separate lines
     */
    public final CharSequence[] lines;
    /**
     * The alignment used
     */
    public final Alignment alignment;
    /**
     * The font used
     */
    public final Font font;
    /**
     * The generated layout. The origin of the first character is at the (0,0)
     */
    public final TexturedShape textShape;

    /**
     * Constructs a new {@link TextLayout}
     *
     * @param text
     * @param font
     * @param alignment
     * @param lineLength
     * @param colour     <code>null</code> for white
     */
    public TextLayout(final CharSequence text, final Font font,
                      Alignment alignment, final float lineLength, final int colour) {
        fullText = text.toString();
        this.font = font;
        this.alignment = alignment;

        final List<CharSequence> linesList = new ArrayList<CharSequence>();

        if (alignment == null) {
            alignment = Alignment.LEFT;
        }

        final TexturedShapeWelder tsb = new TexturedShapeWelder();

        float yOffset = 0;

        int start = 0;
        int end = 0;
        do {
            while (end < text.length() && text.charAt(end) != '\n') {
                end++;
            }

            final CharSequence[] lineArray =
                    splitParagraph(text.subSequence(start, end), font, lineLength);

            Collections.addAll(linesList, lineArray);

            // add lines
            for (int i = 0; i < lineArray.length; i++) {
                final TexturedShape ts =
                        font.buildTextShape(lineArray[i], colour);

                ts.translate(0, yOffset, 0);

                if (alignment != Alignment.JUSTIFY || i < lineArray.length - 1) {
                    alignment.layoutLine(lineArray[i], lineLength, ts.vertices);
                }

                tsb.addShape(ts);

                yOffset -= font.size;
            }

            end++;
            start = end;
        }
        while (end < text.length());

        textShape = tsb.fuse();

        lines = linesList.toArray(new CharSequence[linesList.size()]);
    }

    /**
     * Splits a paragraph into lines
     *
     * @param text
     * @param f
     * @param lineLength
     */
    private static CharSequence[] splitParagraph(final CharSequence text,
                                                 final Font f, final float lineLength) {
        final List<CharSequence> lines = new ArrayList<CharSequence>();

        int prev = 0;
        int increment;
        int next = 0;

        do {
            prev = next;
            increment =
                    TextMeasurer.indexOfCharAt(f,
                            text.subSequence(prev, text.length()), lineLength);

            if (increment == -1) {
                next = text.length();
            } else {
                increment = Math.max(1, increment);

                next += increment;

                next = Math.min(next, text.length() - 1);

                // rewind till the last whitespace
                while (next > prev && !Character.isWhitespace(text.charAt(next))) {
                    next--;
                }

                if (next == prev) { // we have no choice but to split a word
                    next += increment;
                }
            }

            if (prev != next) {
                String l = text.subSequence(prev, next).toString();

                if (prev == 0 && next == text.length() - 1) {
                    l = l.trim();
                }

                lines.add(l);
            }
        }
        while (increment > 0);

        return lines.toArray(new CharSequence[lines.size()]);
    }

    /**
     * Computes the caret position
     *
     * @param caretPosition The index of the character that the caret is in front of.
     * @param dest          The {@link Vector2f} array in which to store the result, or null
     *                      to construct a new {@link Vector2f}[4]
     * @return <code>dest</code>, or a new {@link Vector2f}[4] containing the
     * result
     */
    public Vector2f getCaretPosition(int caretPosition, Vector2f dest) {
        if (dest == null) {
            dest = new Vector2f();
        }

        if (caretPosition < 0) {
            caretPosition = 0;
        }

        if (caretPosition < fullText.length()) {
            // 4 vertices per character, so...
            final Vector3f v = textShape.getVertex(4 * caretPosition, null);

            // the offset from the char baseline to the bl vertex of the
            // quad

            final Glyph glyph = font.map(fullText.charAt(caretPosition));
            final Vector2f glyphOffset = glyph.getGlyphOffset(null);

            dest.set(v.x - glyphOffset.x, v.y - glyphOffset.y);
        } else {
            final Vector3f v =
                    textShape.getVertex(textShape.vertexCount() - 4, null);

            final Glyph glyph =
                    font.map(fullText.charAt(fullText.length() - 1));
            final Vector2f glyphOffset = glyph.getGlyphOffset(null);

            dest.set(v.x - glyphOffset.x + glyph.advance, v.y - glyphOffset.y);
        }

        return dest;
    }

    /**
     * Defines how text is laid out on each line
     *
     * @author ryanm
     */
    public static enum Alignment {
        /**
         * Text is placed against the left side of the line
         */
        LEFT {
            @Override
            public void layoutLine(final CharSequence line,
                                   final float lineLength, final float[] vertices) {
                // no op
            }
        },
        /**
         * Text is placed against the right side of the line
         */
        RIGHT {
            @Override
            public void layoutLine(final CharSequence line,
                                   final float lineLength, final float[] vertices) {
                final float whiteSpace =
                        lineLength - vertices[vertices.length - 3];

                for (int i = 0; i < vertices.length; i += 3) {
                    vertices[i] += whiteSpace;
                }
            }
        },
        /**
         * Text is placed in the center of the line
         */
        CENTER {
            @Override
            public void layoutLine(final CharSequence line,
                                   final float lineLength, final float[] vertices) {
                float whiteSpace = lineLength - vertices[vertices.length - 3];

                whiteSpace /= 2;

                for (int i = 0; i < vertices.length; i += 3) {
                    vertices[i] += whiteSpace;
                }
            }
        },
        /**
         * The whitespace in the text is expanded to fill the line
         */
        JUSTIFY {
            @Override
            public void layoutLine(final CharSequence line,
                                   final float lineLength, final float[] vertices) {
                float whiteSpace = lineLength - vertices[vertices.length - 3];

                int spaces = 0;
                for (int i = 0; i < line.length(); i++) {
                    if (line.charAt(i) == ' ') {
                        spaces++;
                    }
                }
                whiteSpace /= spaces;

                float offset = 0;
                for (int i = 0; i < line.length(); i++) {
                    vertices[3 * 4 * i] += offset;
                    vertices[3 * (4 * i + 1)] += offset;

                    if (line.charAt(i) == ' ') {
                        offset += whiteSpace;
                    }

                    vertices[3 * (4 * i + 2)] += offset;
                    vertices[3 * (4 * i + 3)] += offset;
                }
            }
        };

        /**
         * Lays out the line of text.
         *
         * @param line       The text to lay out
         * @param lineLength The length of the line
         * @param vertices   The vertices of the characters, in bl tl, br tr order
         */
        public abstract void layoutLine(CharSequence line, float lineLength,
                                        float[] vertices);
    }
}
