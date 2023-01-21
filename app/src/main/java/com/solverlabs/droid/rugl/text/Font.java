package com.solverlabs.droid.rugl.text;

import android.graphics.Point;

import androidx.annotation.NonNull;

import com.solverlabs.droid.rugl.geom.ColouredShape;
import com.solverlabs.droid.rugl.geom.Shape;
import com.solverlabs.droid.rugl.geom.ShapeUtil;
import com.solverlabs.droid.rugl.geom.TexturedShape;
import com.solverlabs.droid.rugl.gl.GLUtil;
import com.solverlabs.droid.rugl.gl.State;
import com.solverlabs.droid.rugl.gl.enums.MagFilter;
import com.solverlabs.droid.rugl.gl.enums.MinFilter;
import com.solverlabs.droid.rugl.gl.facets.TextureState;
import com.solverlabs.droid.rugl.texture.Image;
import com.solverlabs.droid.rugl.texture.TextureFactory;
import com.solverlabs.droid.rugl.util.RectanglePacker;
import com.solverlabs.droid.rugl.util.geom.BoundingRectangle;
import com.solverlabs.droid.rugl.util.geom.Vector2f;
import com.solverlabs.worldcraft.factories.DescriptionFactory;

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
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


public final class Font {
    static final boolean assertionsDisabled = !Font.class.desiredAssertionStatus();
    private static final BoundingRectangle tempBounds;
    private static final Vector2f tempExtent;
    private static final Vector2f tempOrigin;
    private static final Vector2f tempPoint;

    static {
        tempBounds = new BoundingRectangle();
        tempPoint = new Vector2f();
        tempOrigin = new Vector2f();
        tempExtent = new Vector2f();
    }

    public final int ascent;
    public final boolean bold;
    public final int descent;
    public final boolean distanceField;
    public final boolean italic;
    public final int leading;
    public final String name;
    public final int size;
    public KerningSource kerningSource = null;
    private Glyph[] map;
    private final List<Glyph> glyphs = new LinkedList<>();
    private final Set<GlyphImage> glyphImages = new HashSet<>();
    private transient TextureFactory.GLTexture texture = null;

    public Font(String name, boolean bold, boolean italic, int size, int ascent, int descent, int leading, boolean distanceField) {
        this.name = name;
        this.bold = bold;
        this.italic = italic;
        this.size = size;
        this.ascent = ascent;
        this.descent = descent;
        this.leading = leading;
        this.distanceField = distanceField;
    }

    public Font(@NonNull ByteBuffer data) {
        boolean z = true;
        byte[] nd = new byte[data.getInt()];
        data.get(nd);
        this.name = new String(nd);
        this.bold = data.get() != 0;
        this.italic = data.get() != 0;
        this.size = data.getInt();
        this.ascent = data.getInt();
        this.descent = data.getInt();
        this.leading = data.getInt();
        this.distanceField = data.get() != 0 && z;
        int gi = data.getInt();
        for (int i = 0; i < gi; i++) {
            this.glyphImages.add(new GlyphImage(data));
        }
        GlyphImage[] imageArray = this.glyphImages.toArray(new GlyphImage[this.glyphImages.size()]);
        int g = data.getInt();
        for (int i2 = 0; i2 < g; i2++) {
            addGlyph(new Glyph(data, imageArray));
        }
    }

    public Font(InputStream is) throws IOException {
        boolean z = true;
        DataInputStream dis = new DataInputStream(is);
        int nd = dis.readInt();
        byte[] nb = new byte[nd];
        dis.readFully(nb);
        this.name = new String(nb);
        this.bold = dis.readByte() != 0;
        this.italic = dis.readByte() != 0;
        this.size = dis.readInt();
        this.ascent = dis.readInt();
        this.descent = dis.readInt();
        this.leading = dis.readInt();
        this.distanceField = dis.readByte() != 0 && z;
        int gi = dis.readInt();
        for (int i = 0; i < gi; i++) {
            this.glyphImages.add(new GlyphImage(is));
        }
        GlyphImage[] imageArray = this.glyphImages.toArray(new GlyphImage[this.glyphImages.size()]);
        int g = dis.readInt();
        for (int i2 = 0; i2 < g; i2++) {
            addGlyph(new Glyph(is, imageArray));
        }
    }

    @NonNull
    public static Font readFont(String fileName) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(fileName, "r");
        FileChannel ch = raf.getChannel();
        MappedByteBuffer buffer = ch.map(FileChannel.MapMode.READ_ONLY, 0L, raf.length());
        Font f = new Font(buffer);
        ch.close();
        return f;
    }

    public void write(@NonNull ByteBuffer data) {
        int i = 1;
        byte[] nd = this.name.getBytes();
        data.putInt(nd.length);
        data.put(nd);
        data.put((byte) (this.bold ? 1 : 0));
        data.put((byte) (this.italic ? 1 : 0));
        data.putInt(this.size);
        data.putInt(this.ascent);
        data.putInt(this.descent);
        data.putInt(this.leading);
        if (!this.distanceField) {
            i = 0;
        }
        data.put((byte) i);
        data.putInt(this.glyphImages.size());
        for (GlyphImage gi : this.glyphImages) {
            gi.write(data);
        }
        data.putInt(this.glyphs.size());
        for (Glyph g : this.glyphs) {
            g.write(data);
        }
    }

    public void write(String fileName) throws IOException {
        RandomAccessFile rf = new RandomAccessFile(fileName, "rw");
        FileChannel ch = rf.getChannel();
        int fileLength = dataSize();
        rf.setLength(fileLength);
        MappedByteBuffer buffer = ch.map(FileChannel.MapMode.READ_WRITE, 0L, fileLength);
        write(buffer);
        buffer.force();
        ch.close();
    }

    public int dataSize() {
        int i = 0 + 4;
        int bytes = this.name.getBytes().length + 4;
        int bytes2 = bytes + 2 + 16 + 4;
        for (GlyphImage gi : this.glyphImages) {
            bytes2 += gi.dataSize();
        }
        int bytes3 = bytes2 + 4;
        for (Glyph g : this.glyphs) {
            bytes3 += g.dataSize();
        }
        return bytes3 + 1;
    }

    public void addGlyph(Glyph g) {
        if (this.map == null || g.character >= this.map.length) {
            Glyph[] newMap = new Glyph[g.character + '\n'];
            if (this.map != null) {
                System.arraycopy(this.map, 0, newMap, 0, this.map.length);
            }
            this.map = newMap;
        }
        if (this.map[g.character] == null) {
            this.map[g.character] = g;
            if (this.kerningSource != null) {
                for (Glyph gl : this.glyphs) {
                    gl.updateKerning(g.character, this.kerningSource.computeKerning(g.character, gl.character));
                    g.updateKerning(gl.character, this.kerningSource.computeKerning(gl.character, g.character));
                }
            }
            this.glyphs.add(g);
            this.glyphImages.add(g.image);
        }
    }

    public boolean init(boolean mipmap) {
        boolean z = true;
        if (this.texture == null) {
            List<GlyphImage> gImages = new ArrayList<>(this.glyphImages);
            Collections.sort(gImages, (o1, o2) -> {
                int left = o1.image.width * o1.image.height;
                int right = o2.image.width * o2.image.height;
                return -(left - right);
            });
            Point p = calculateTextureSize();
            TextureFactory.GLTexture glt = TextureFactory.createTexture(p.x, p.y, Image.Format.LUMINANCE_ALPHA, mipmap, 1);
            if (glt != null) {
                this.texture = glt;
                boolean success = true;
                for (GlyphImage gi : gImages) {
                    if (!gi.init(glt)) {
                        success = false;
                    }
                }
                for (Glyph g : this.glyphs) {
                    if (!g.image.init(glt)) {
                        this.map[g.character] = this.map[48];
                        success = false;
                    }
                }
                return success;
            }
        }
        if (this.texture == null) {
            z = false;
        }
        return z;
    }

    @NonNull
    private Point calculateTextureSize() {
        boolean success;
        float mean = 0.0f;
        for (GlyphImage g : this.glyphImages) {
            mean = mean + g.image.width + g.image.height;
        }
        int dim = Math.max(2, GLUtil.nextPowerOf2((int) ((mean / (this.glyphs.size() * 2)) * Math.sqrt(this.glyphs.size()))) / 2);
        Point d = new Point(dim / 2, dim);
        do {
            d.x *= 2;
            d.y *= 2;
            RectanglePacker<GlyphImage> packer = new RectanglePacker<>(d.x, d.y, 1);
            Iterator<GlyphImage> iter = this.glyphImages.iterator();
            boolean fit = true;
            while (fit && iter.hasNext()) {
                GlyphImage g2 = iter.next();
                fit = packer.insert(g2.image.width, g2.image.height, g2) != null;
            }
            success = fit;
        } while (!success);
        return d;
    }

    public Glyph map(char c) {
        if (c >= this.map.length) {
            c = 0;
        }
        if (c == '\t') {
            c = ' ';
        }
        Glyph g = this.map[c];
        if (g == null) {
            if (!assertionsDisabled && this.map[0] == null) {
                throw new AssertionError();
            }
            return this.map[0];
        }
        return g;
    }

    public float getStringLength(@NonNull CharSequence text) {
        Glyph last = null;
        float length = 0.0f;
        for (int i = 0; i < text.length(); i++) {
            Glyph next = map(text.charAt(i));
            float kerning = last == null ? 0.0f : next.getKerningAfter(last.character);
            length += next.advance + kerning;
            last = next;
        }
        return length;
    }

    @NonNull
    public float[] getVertices(CharSequence text, float[] dest, int start) {
        int index = start;
        if (dest == null) {
            dest = new float[text.length() * 12];
            index = 0;
        }
        Glyph last = null;
        float penX = 0.0f;
        float penY = 0.0f;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            boolean nl = c == '\n';
            if (nl) {
                penY -= this.size;
                c = ' ';
                penX = 0.0f;
            }
            Glyph next = map(c);
            next.image.getSize(tempBounds);
            next.getGlyphOffset(tempPoint);
            float kerning = last == null ? 0.0f : next.getKerningAfter(last.character);
            tempBounds.translate(tempPoint.getX() + penX + kerning, tempPoint.getY() + penY);
            int index2 = index + 1;
            dest[index] = tempBounds.x.getMin();
            int index3 = index2 + 1;
            dest[index2] = tempBounds.y.getMin();
            int index4 = index3 + 1;
            dest[index3] = 0.0f;
            int index5 = index4 + 1;
            dest[index4] = tempBounds.x.getMin();
            int index6 = index5 + 1;
            dest[index5] = tempBounds.y.getMax();
            int index7 = index6 + 1;
            dest[index6] = 0.0f;
            int index8 = index7 + 1;
            dest[index7] = tempBounds.x.getMax();
            int index9 = index8 + 1;
            dest[index8] = tempBounds.y.getMin();
            int index10 = index9 + 1;
            dest[index9] = 0.0f;
            int index11 = index10 + 1;
            dest[index10] = tempBounds.x.getMax();
            int index12 = index11 + 1;
            dest[index11] = tempBounds.y.getMax();
            index = index12 + 1;
            dest[index12] = 0.0f;
            if (!nl) {
                penX += next.advance + kerning;
            }
            last = next;
        }
        return dest;
    }

    @NonNull
    @Contract("_, _ -> new")
    public TextShape buildTextShape(CharSequence text, int colour) {
        if (assertionsDisabled || this.texture != null) {
            if (!assertionsDisabled && text.length() == 0) {
                throw new AssertionError("Empty string");
            }
            float[] verts = getVertices(text, null, 0);
            float[] texcoords = getTexCoords(text, null, 0);
            short[] indices = ShapeUtil.makeQuads(verts.length / 3, 0, null, 0);
            TexturedShape ts = new TexturedShape(new ColouredShape(new Shape(verts, indices), colour, null), texcoords, this.texture.getTexture());
            ts.state = ts.state.with(ts.state.texture.with(new TextureState.Filters(this.texture.mipmap ? MinFilter.LINEAR_MIPMAP_LINEAR : MinFilter.LINEAR, MagFilter.LINEAR)));
            return new TextShape(ts, this, text.toString());
        }
        throw new AssertionError("Font " + this.name + " not initialised");
    }

    @NonNull
    public float[] getTexCoords(CharSequence text, float[] dest, int start) {
        int index = start;
        if (dest == null) {
            dest = new float[text.length() * 8];
            index = 0;
        }
        for (int i = 0; i < text.length(); i++) {
            Glyph g = map(text.charAt(i));
            g.image.getOrigin(tempOrigin);
            g.image.getExtent(tempExtent);
            int index2 = index + 1;
            dest[index] = tempOrigin.x;
            int index3 = index2 + 1;
            dest[index2] = tempOrigin.y;
            int index4 = index3 + 1;
            dest[index3] = tempOrigin.x;
            int index5 = index4 + 1;
            dest[index4] = tempExtent.y;
            int index6 = index5 + 1;
            dest[index5] = tempExtent.x;
            int index7 = index6 + 1;
            dest[index6] = tempOrigin.y;
            int index8 = index7 + 1;
            dest[index7] = tempExtent.x;
            index = index8 + 1;
            dest[index8] = tempExtent.y;
        }
        return dest;
    }

    public TextureFactory.GLTexture getFontTexture() {
        return this.texture;
    }

    public boolean isPlain() {
        return !this.bold && !this.italic;
    }

    @NonNull
    public List<Glyph> getGlyphs() {
        return Collections.unmodifiableList(this.glyphs);
    }

    @NonNull
    @Contract(pure = true)
    public String toString() {
        return "Font \"" + this.name + "\" " + this.size + " " + (this.bold ? "bold" : DescriptionFactory.emptyText) + " " + (this.italic ? "italic" : DescriptionFactory.emptyText) + " ascent = " + this.ascent + " descent = " + this.descent + " leading = " + this.leading;
    }
}
