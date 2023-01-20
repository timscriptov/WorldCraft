package com.solverlabs.droid.rugl.text;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.geom.TexturedShape;
import com.solverlabs.droid.rugl.geom.TexturedShapeWelder;
import com.solverlabs.droid.rugl.util.geom.Vector2f;
import com.solverlabs.droid.rugl.util.geom.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class TextLayout {
    public final Alignment alignment;
    public final Font font;
    public final String fullText;
    public final CharSequence[] lines;
    public final TexturedShape textShape;

    public TextLayout(@NonNull CharSequence text, Font font, Alignment alignment, float lineLength, int colour) {
        this.fullText = text.toString();
        this.font = font;
        this.alignment = alignment;
        List<CharSequence> linesList = new ArrayList<>();
        alignment = alignment == null ? Alignment.LEFT : alignment;
        TexturedShapeWelder tsb = new TexturedShapeWelder();
        float yOffset = 0.0f;
        int start = 0;
        int end = 0;
        while (true) {
            if (end < text.length() && text.charAt(end) != '\n') {
                end++;
            } else {
                CharSequence[] lineArray = splitParagraph(text.subSequence(start, end), font, lineLength);
                Collections.addAll(linesList, lineArray);
                for (int i = 0; i < lineArray.length; i++) {
                    TexturedShape ts = font.buildTextShape(lineArray[i], colour);
                    ts.translate(0.0f, yOffset, 0.0f);
                    if (alignment != Alignment.JUSTIFY || i < lineArray.length - 1) {
                        alignment.layoutLine(lineArray[i], lineLength, ts.vertices);
                    }
                    tsb.addShape(ts);
                    yOffset -= font.size;
                }
                end++;
                start = end;
                if (end >= text.length()) {
                    this.textShape = tsb.fuse();
                    this.lines = linesList.toArray(new CharSequence[linesList.size()]);
                    return;
                }
            }
        }
    }

    @NonNull
    private static CharSequence[] splitParagraph(@NonNull CharSequence text, Font f, float lineLength) {
        int increment;
        List<CharSequence> lines = new ArrayList<>();
        int next = 0;
        do {
            int prev = next;
            increment = TextMeasurer.indexOfCharAt(f, text.subSequence(prev, text.length()), lineLength);
            if (increment == -1) {
                next = text.length();
            } else {
                increment = Math.max(1, increment);
                next = Math.min(next + increment, text.length() - 1);
                while (next > prev && !Character.isWhitespace(text.charAt(next))) {
                    next--;
                }
                if (next == prev) {
                    next += increment;
                }
            }
            if (prev != next) {
                String l = text.subSequence(prev, next).toString();
                if (prev == 0 && next == text.length() - 1) {
                    l = l.trim();
                }
                lines.add(l);
                continue;
            }
        } while (increment > 0);
        return lines.toArray(new CharSequence[lines.size()]);
    }

    public Vector2f getCaretPosition(int caretPosition, Vector2f dest) {
        if (dest == null) {
            dest = new Vector2f();
        }
        if (caretPosition < 0) {
            caretPosition = 0;
        }
        if (caretPosition < this.fullText.length()) {
            Vector3f v = this.textShape.getVertex(caretPosition * 4, null);
            Vector2f glyphOffset = this.font.map(this.fullText.charAt(caretPosition)).getGlyphOffset(null);
            dest.set(v.x - glyphOffset.x, v.y - glyphOffset.y);
        } else {
            Vector3f v2 = this.textShape.getVertex(this.textShape.vertexCount() - 4, null);
            Glyph glyph = this.font.map(this.fullText.charAt(this.fullText.length() - 1));
            Vector2f glyphOffset2 = glyph.getGlyphOffset(null);
            dest.set((v2.x - glyphOffset2.x) + glyph.advance, v2.y - glyphOffset2.y);
        }
        return dest;
    }


    public enum Alignment {
        LEFT {
            @Override
            public void layoutLine(CharSequence line, float lineLength, float[] vertices) {
            }
        },
        RIGHT {
            @Override
            public void layoutLine(CharSequence line, float lineLength, float[] vertices) {
                float whiteSpace = lineLength - vertices[vertices.length - 3];
                for (int i = 0; i < vertices.length; i += 3) {
                    vertices[i] = vertices[i] + whiteSpace;
                }
            }
        },
        CENTER {
            @Override
            public void layoutLine(CharSequence line, float lineLength, float[] vertices) {
                float whiteSpace = lineLength - vertices[vertices.length - 3];
                float whiteSpace2 = whiteSpace / 2.0f;
                for (int i = 0; i < vertices.length; i += 3) {
                    vertices[i] = vertices[i] + whiteSpace2;
                }
            }
        },
        JUSTIFY {
            @Override
            public void layoutLine(CharSequence line, float lineLength, float[] vertices) {
                float whiteSpace = lineLength - vertices[vertices.length - 3];
                int spaces = 0;
                for (int i = 0; i < line.length(); i++) {
                    if (line.charAt(i) == ' ') {
                        spaces++;
                    }
                }
                float whiteSpace2 = whiteSpace / spaces;
                float offset = 0.0f;
                for (int i2 = 0; i2 < line.length(); i2++) {
                    int i3 = i2 * 12;
                    vertices[i3] = vertices[i3] + offset;
                    int i4 = ((i2 * 4) + 1) * 3;
                    vertices[i4] = vertices[i4] + offset;
                    if (line.charAt(i2) == ' ') {
                        offset += whiteSpace2;
                    }
                    int i5 = ((i2 * 4) + 2) * 3;
                    vertices[i5] = vertices[i5] + offset;
                    int i6 = ((i2 * 4) + 3) * 3;
                    vertices[i6] = vertices[i6] + offset;
                }
            }
        };

        public abstract void layoutLine(CharSequence charSequence, float f, float[] fArr);
    }
}
