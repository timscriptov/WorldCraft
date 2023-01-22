package com.solverlabs.droid.rugl.text;

import android.graphics.Point;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.geom.ColouredShape;
import com.solverlabs.droid.rugl.geom.Shape;
import com.solverlabs.droid.rugl.geom.ShapeUtil;
import com.solverlabs.droid.rugl.geom.TexturedShape;
import com.solverlabs.droid.rugl.gl.GLUtil;
import com.solverlabs.droid.rugl.gl.enums.MagFilter;
import com.solverlabs.droid.rugl.gl.enums.MinFilter;
import com.solverlabs.droid.rugl.gl.facets.TextureState;
import com.solverlabs.droid.rugl.texture.Image;
import com.solverlabs.droid.rugl.texture.TextureFactory;
import com.solverlabs.droid.rugl.util.Colour;
import com.solverlabs.droid.rugl.util.RectanglePacker;
import com.solverlabs.droid.rugl.util.geom.BoundingRectangle;
import com.solverlabs.droid.rugl.util.geom.Vector2f;

import org.jetbrains.annotations.Contract;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Has some global characteristics, a number of {@link Glyph} s, and a
 * mapping of characters to {@link Glyph}s. A {@link Font} can be
 * constructed from various sources. This class started life in the
 * SPGL, but has been fairly extensively jiggered around - converted
 * to floating point etc
 */
public final class Font {
    /**
     * Handy bounds
     */
    private static final BoundingRectangle tempBounds = new BoundingRectangle();

    private static final Vector2f tempPoint = new Vector2f();

    private static final Vector2f tempOrigin = new Vector2f();

    private static final Vector2f tempExtent = new Vector2f();
    /**
     * The font's name
     */
    public final String name;
    /**
     * If the font is bold or not
     */
    public final boolean bold;
    /**
     * If the font is italic or not
     */
    public final boolean italic;
    /**
     * The distance between the line and the top of the font
     */
    public final int ascent;
    /**
     * The distance that this font descends below the line
     */
    public final int descent;
    /**
     * The spacing between one line's descent and the next line's
     * ascent
     */
    public final int leading;
    /**
     * The distance between lines
     */
    public final int size;
    /**
     * Indicates if this font uses distance field textures
     */
    public final boolean distanceField;
    /**
     * The font's glyphs
     */
    private final List<Glyph> glyphs = new LinkedList<>();

    /**
     * The font's images
     */
    private final Set<GlyphImage> glyphImages = new HashSet<>();
    /**
     * The source of kerning information that will be consulted
     * whenever a glyph is added to the font. If <code>null</code>,
     * kerning will be ignored
     */
    public KerningSource kerningSource = null;
    /**
     * Maps Unicode characters to glyphs. This approach is OK for ASCII
     * characters, since they live at the bottom of the unicode tables,
     * but something more clever needs to be done for higher characters
     * to avoid allocating enormous mostly empty arrays
     */
    private Glyph[] map;
    /**
     * The texture in which the glyphs reside
     */
    private transient TextureFactory.GLTexture texture = null;

    /**
     * Builds an initially empty font
     *
     * @param name          The font name
     * @param bold          if the font is bold or not
     * @param italic        If the font is italic or not
     * @param size          The size in points of the font
     * @param ascent        The font's maximum ascent, in points
     * @param descent       The font's maximum descent, in points
     * @param leading       The distance between one line's descent and the next's
     *                      ascent
     * @param distanceField
     */
    public Font(String name, boolean bold, boolean italic, int size, int ascent,
                int descent, int leading, boolean distanceField) {
        this.name = name;
        this.bold = bold;
        this.italic = italic;
        this.size = size;
        this.ascent = ascent;
        this.descent = descent;
        this.leading = leading;
        this.distanceField = distanceField;
    }

    /**
     * Reads a {@link Font} from a buffer
     *
     * @param data the buffer
     */
    public Font(@NonNull ByteBuffer data) {
        byte[] nd = new byte[data.getInt()];
        data.get(nd);
        name = new String(nd);

        bold = data.get() != 0;
        italic = data.get() != 0;
        size = data.getInt();
        ascent = data.getInt();
        descent = data.getInt();
        leading = data.getInt();
        distanceField = data.get() != 0;

        int gi = data.getInt();
        for (int i = 0; i < gi; i++) {
            glyphImages.add(new GlyphImage(data));
        }

        GlyphImage[] imageArray = glyphImages.toArray(new GlyphImage[glyphImages.size()]);

        int g = data.getInt();
        for (int i = 0; i < g; i++) {
            addGlyph(new Glyph(data, imageArray));
        }
    }

    /**
     * Reads a {@link Font} from a stream
     *
     * @param is
     * @throws IOException
     */
    public Font(InputStream is) throws IOException {
        DataInputStream dis = new DataInputStream(is);

        int nd = dis.readInt();
        byte[] nb = new byte[nd];
        dis.readFully(nb);
        name = new String(nb);

        bold = dis.readByte() != 0;
        italic = dis.readByte() != 0;
        size = dis.readInt();
        ascent = dis.readInt();
        descent = dis.readInt();
        leading = dis.readInt();
        distanceField = dis.readByte() != 0;

        int gi = dis.readInt();
        for (int i = 0; i < gi; i++) {
            glyphImages.add(new GlyphImage(is));
        }

        GlyphImage[] imageArray = glyphImages.toArray(new GlyphImage[glyphImages.size()]);

        int g = dis.readInt();
        for (int i = 0; i < g; i++) {
            addGlyph(new Glyph(is, imageArray));
        }
    }

    /**
     * Reads a font from a file
     *
     * @param fileName
     * @return A {@link Font}
     * @throws IOException
     */
    @NonNull
    public static Font readFont(String fileName) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(fileName, "r");
        FileChannel ch = raf.getChannel();
        MappedByteBuffer buffer = ch.map(FileChannel.MapMode.READ_ONLY, 0, raf.length());

        Font f = new Font(buffer);

        ch.close();

        return f;
    }

    /**
     * Writes a Font to a buffer
     *
     * @param data the buffer to write to
     */
    public void write(@NonNull ByteBuffer data) {
        byte[] nd = name.getBytes();
        data.putInt(nd.length);
        data.put(nd);

        data.put((byte) (bold ? 1 : 0));
        data.put((byte) (italic ? 1 : 0));
        data.putInt(size);
        data.putInt(ascent);
        data.putInt(descent);
        data.putInt(leading);
        data.put((byte) (distanceField ? 1 : 0));

        data.putInt(glyphImages.size());
        for (GlyphImage gi : glyphImages) {
            gi.write(data);
        }

        data.putInt(glyphs.size());
        for (Glyph g : glyphs) {
            g.write(data);
        }
    }

    /**
     * Write the {@link Font} to a file
     *
     * @param fileName
     * @throws IOException
     */
    public void write(String fileName) throws IOException {
        RandomAccessFile rf = new RandomAccessFile(fileName, "rw");
        FileChannel ch = rf.getChannel();
        int fileLength = dataSize();
        rf.setLength(fileLength);
        MappedByteBuffer buffer = ch.map(FileChannel.MapMode.READ_WRITE, 0, fileLength);

        write(buffer);

        buffer.force();
        ch.close();
    }

    /**
     * Calculates the size of buffer needed to store this {@link Font}
     *
     * @return The number of bytes used to store this font
     */
    public int dataSize() {
        int bytes = 0;

        // name length
        bytes += 4;

        // name
        bytes += name.getBytes().length;

        // bold/italic
        bytes += 2;

        // size, ascent, descent, leading
        bytes += 4 * 4;

        // glyph image count
        bytes += 4;
        for (GlyphImage gi : glyphImages) {
            bytes += gi.dataSize();
        }

        // glyph count
        bytes += 4;
        for (Glyph g : glyphs) {
            // glyphs
            bytes += g.dataSize();
        }

        // distance field
        bytes += 1;

        return bytes;
    }

    /**
     * Adds a glyph to this font
     *
     * @param g the glyph to add
     */
    public void addGlyph(Glyph g) {
        if (map == null || g.character >= map.length) {
            Glyph[] newMap = new Glyph[(g.character + 10)];

            if (map != null) {
                System.arraycopy(map, 0, newMap, 0, map.length);
            }

            map = newMap;
        }

        if (map[g.character] == null) {
            map[g.character] = g;

            // update kerning?
            if (kerningSource != null) {
                for (Glyph gl : glyphs) {
                    gl.updateKerning(g.character,
                            kerningSource.computeKerning(g.character, gl.character));

                    g.updateKerning(gl.character,
                            kerningSource.computeKerning(gl.character, g.character));
                }
            }

            // add glyph
            glyphs.add(g);

            glyphImages.add(g.image);
        }
    }

    /**
     * Loads the image into an OpenGL texture and initialises the glyph
     * texcoords
     *
     * @param mipmap If you're going to be zooming text, you might want to
     *               build mipmaps
     * @return <code>true</code> if successful, <code>false</code>
     * otherwise
     */
    public boolean init(boolean mipmap) {
        if (texture == null) {
            List<GlyphImage> gImages = new ArrayList<>();
            gImages.addAll(glyphImages);

            // sort the glyphs into descending order of size
            Collections.sort(gImages, (o1, o2) -> {
                int left = o1.image.width * o1.image.height;
                int right = o2.image.width * o2.image.height;

                // remember we want descending order
                return -(left - right);
            });

            // determine how big the texture should be
            Point p = calculateTextureSize();

            TextureFactory.GLTexture glt =
                    TextureFactory.createTexture(p.x, p.y, Image.Format.LUMINANCE_ALPHA, mipmap, 1);

            if (glt != null) {
                texture = glt;

                boolean success = true;

                for (GlyphImage gi : gImages) {
                    if (!gi.init(glt)) {
                        success = false;
                    }
                }

                for (Glyph g : glyphs) {
                    if (!g.image.init(glt)) { // a glyph has failed to init, remap it to the WTF
                        map[g.character] = map['0'];
                        success = false;
                    }
                }

                // this will be done for us when we render, but it doesn't
                // hurt to do it in init time
                // glt.regenerateMipmaps();

                return success;
            }
        }

        return texture != null;
    }

    @NonNull
    private Point calculateTextureSize() {
        // mean glyph size
        float mean = 0;
        for (GlyphImage g : glyphImages) {
            mean += g.image.width;
            mean += g.image.height;
        }
        mean /= glyphs.size() * 2;

        // initial guess
        int dim = GLUtil.nextPowerOf2((int) (mean * Math.sqrt(glyphs.size()))) / 2;
        dim = Math.max(2, dim);

        Point d = new Point(dim / 2, dim);

        boolean success = false;

        do {
            d.x *= 2;
            d.y *= 2;

            RectanglePacker<GlyphImage> packer =
                    new RectanglePacker<GlyphImage>(d.x, d.y, 1);
            Iterator<GlyphImage> iter = glyphImages.iterator();
            boolean fit = true;
            while (fit && iter.hasNext()) {
                GlyphImage g = iter.next();

                fit &= packer.insert(g.image.width, g.image.height, g) != null;
            }

            success = fit;
        }
        while (!success);

        return d;
    }

    /**
     * Map a character to a glyph
     *
     * @param c The character
     * @return The corresponding {@link Glyph}, or null
     */
    public Glyph map(char c) {
        if (c >= map.length) { // we don't have this char, use the WTF instead
            c = 0;
        }
        if (c == '\t') { // convert tabs to spaces
            c = ' ';
        }

        Glyph g = map[c];

        if (g != null) {
            return g;
        } else {
            assert map[(char) 0] != null;

            return map[(char) 0];
        }
    }

    /**
     * Calculates the rendered length of a string
     *
     * @param text The text to measure
     * @return The length of the rendered text
     */
    public float getStringLength(@NonNull CharSequence text) {
        Glyph last = null;
        float length = 0;
        for (int i = 0; i < text.length(); i++) {
            Glyph next = map(text.charAt(i));
            float kerning = last == null ? 0 : next.getKerningAfter(last.character);

            length += next.advance + kerning;

            last = next;
        }

        return length;
    }

    /**
     * Calculates the glyph vertices if the string is being rendered at
     * the origin. The dest array will have 4 * text.length elements
     * written to it, in bl tl br tr format
     *
     * @param text  The text to render
     * @param dest  A destination vertex array, or null. If non-null, all
     *              elements must also be non-null
     * @param start The element to start writing to in the dest array.
     *              Ignored in dest is null;
     * @return an array of glyph vertices
     */
    @NonNull
    public float[] getVertices(CharSequence text, float[] dest, int start) {
        int index = start;
        if (dest == null) {
            dest = new float[4 * 3 * text.length()];
            index = 0;
        }

        Glyph last = null;
        float penX = 0;
        float penY = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            boolean nl = c == '\n';
            if (nl) {
                penY -= size;
                c = ' ';
                penX = 0;
            }

            Glyph next = map(c);

            next.image.getSize(tempBounds);
            next.getGlyphOffset(tempPoint);
            float kerning = last == null ? 0 : next.getKerningAfter(last.character);

            tempBounds
                    .translate(tempPoint.getX() + penX + kerning, tempPoint.getY() + penY);

            dest[index++] = tempBounds.x.getMin();
            dest[index++] = tempBounds.y.getMin();
            dest[index++] = 0;
            dest[index++] = tempBounds.x.getMin();
            dest[index++] = tempBounds.y.getMax();
            dest[index++] = 0;
            dest[index++] = tempBounds.x.getMax();
            dest[index++] = tempBounds.y.getMin();
            dest[index++] = 0;
            dest[index++] = tempBounds.x.getMax();
            dest[index++] = tempBounds.y.getMax();
            dest[index++] = 0;

            if (!nl) {
                penX += next.advance + kerning;
            }
            last = next;

        }

        return dest;
    }

    /**
     * Builds a textured shape that will render some text. Text is
     * rendered starting at the origin and advancing along the x-axis.
     * See {@link TextLayout} for line-wrapping and such
     *
     * @param text   The text to render
     * @param colour The {@link Colour} of the text
     * @return A {@link TexturedShape} that will render the text
     */
    @NonNull
    @Contract("_, _ -> new")
    public TextShape buildTextShape(@NonNull CharSequence text, int colour) {
//        assert texture != null : "Font " + name + " not initialised";
//        assert text.length() != 0 : "Empty string";

        float[] verts = getVertices(text, null, 0);
        float[] texcoords = getTexCoords(text, (float[]) null, 0);
        short[] indices = ShapeUtil.makeQuads(verts.length / 3, 0, null, 0);

        TexturedShape ts =
                new TexturedShape(new ColouredShape(new Shape(verts, indices), colour,
                        null), texcoords, texture.getTexture());

        ts.state =
                ts.state.with(ts.state.texture.with(new TextureState.Filters(
                        texture.mipmap ? MinFilter.LINEAR_MIPMAP_LINEAR : MinFilter.LINEAR,
                        MagFilter.LINEAR)));

        return new TextShape(ts, this, text.toString());
    }

    /**
     * Calculates the glyph texture coordinates. The dest array will
     * have 4 * 2 * text.length elements written to it, in bl tl br tr
     * format
     *
     * @param text  The text to render
     * @param dest  A destination texcoord array, or null.
     * @param start The element to start writing to in the dest array.
     *              Ignored if dest is <code>null</code>;
     * @return an array of glyph texture coordinates
     */
    @NonNull
    public float[] getTexCoords(CharSequence text, float[] dest, int start) {
        int index = start;
        if (dest == null) {
            dest = new float[4 * 2 * text.length()];
            index = 0;
        }

        for (int i = 0; i < text.length(); i++) {
            Glyph g = map(text.charAt(i));

            g.image.getOrigin(tempOrigin);
            g.image.getExtent(tempExtent);

            dest[index++] = tempOrigin.x;
            dest[index++] = tempOrigin.y;
            dest[index++] = tempOrigin.x;
            dest[index++] = tempExtent.y;
            dest[index++] = tempExtent.x;
            dest[index++] = tempOrigin.y;
            dest[index++] = tempExtent.x;
            dest[index++] = tempExtent.y;
        }

        return dest;
    }

    /**
     * @return The font's texture
     */
    public TextureFactory.GLTexture getFontTexture() {
        return texture;
    }

    /**
     * @return true if this font is not bold or italic
     */
    public boolean isPlain() {
        return !(bold || italic);
    }

    /**
     * Gets an unmodifiable list of the glyphs in this font
     *
     * @return A list of glyphs
     */
    @NonNull
    public List<Glyph> getGlyphs() {
        return Collections.unmodifiableList(glyphs);
    }

    @NonNull
    @Override
    public String toString() {
        return "Font \"" + name + "\" " + size + " " + (bold ? "bold" : "") + " "
                + (italic ? "italic" : "") + " ascent = " + ascent + " descent = "
                + descent + " leading = " + leading;
    }
}
