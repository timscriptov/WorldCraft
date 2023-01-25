package com.mcal.droid.rugl.gl.facets;

import android.opengl.GLES10;

import androidx.annotation.NonNull;

import com.mcal.droid.rugl.gl.Facet;
import com.mcal.droid.rugl.gl.enums.MagFilter;
import com.mcal.droid.rugl.gl.enums.MinFilter;
import com.mcal.droid.rugl.gl.enums.TextureWrap;

import org.apache.commons.compress.archivers.tar.TarConstants;


public class TextureState extends Facet<TextureState> {
    public static final TextureState disabled = new TextureState();
    public final Filters filter;
    public final int id;
    public final WrapParameters wrap;

    private TextureState() {
        this.id = -1;
        this.filter = new Filters();
        this.wrap = new WrapParameters();
    }

    public TextureState(int id, Filters filter, WrapParameters wrap) {
        this.id = id;
        this.filter = filter;
        this.wrap = wrap;
    }

    public TextureState with(int id) {
        return new TextureState(id, this.filter, this.wrap);
    }

    public TextureState with(Filters filter) {
        return new TextureState(this.id, filter, this.wrap);
    }

    public TextureState with(WrapParameters wrap) {
        return new TextureState(this.id, this.filter, wrap);
    }

    @Override
    public void transitionFrom(@NonNull TextureState t) {
        if (this.id != t.id) {
            if (t.id == -1) {
                GLES10.glEnable(3553);
            }
            if (this.id == -1) {
                GLES10.glDisable(3553);
            } else {
                GLES10.glBindTexture(3553, this.id);
            }
        }
        if (this.id != -1) {
            if (this.id == t.id) {
                this.filter.transitionFrom(t.filter);
                this.wrap.transitionFrom(t.wrap);
                return;
            }
            this.filter.force();
            this.wrap.force();
        }
    }

    @Override
    public int compareTo(TextureState t) {
        if (this.id == -1 && t.id == -1) {
            return 0;
        }
        int d = this.id - t.id;
        if (d == 0) {
            int d2 = this.filter.compareTo(this.filter);
            if (d2 == 0) {
                return this.wrap.compareTo(this.wrap);
            }
            return d2;
        }
        return d;
    }

    @NonNull
    public String toString() {
        return "Texture id = " + this.id +
                " " + this.filter +
                " " + this.wrap;
    }


    public static class Filters extends Facet<Filters> {
        public final MagFilter mag;
        public final MinFilter min;

        public Filters() {
            this.min = MinFilter.NEAREST_MIPMAP_LINEAR;
            this.mag = MagFilter.LINEAR;
        }

        public Filters(MinFilter min, MagFilter mag) {
            this.min = min;
            this.mag = mag;
        }

        public Filters with(MinFilter min) {
            return new Filters(min, this.mag);
        }

        public Filters with(MagFilter mag) {
            return new Filters(this.min, mag);
        }

        @Override
        public int compareTo(@NonNull Filters t) {
            int d = this.min.ordinal() - t.min.ordinal();
            if (d == 0) {
                return this.mag.ordinal() - t.mag.ordinal();
            }
            return d;
        }

        @Override
        public void transitionFrom(@NonNull Filters t) {
            if (this.min != t.min) {
                GLES10.glTexParameterx(3553, 10241, this.min.value);
            }
            if (this.mag != t.mag) {
                GLES10.glTexParameterx(3553, TarConstants.DEFAULT_BLKSIZE, this.mag.value);
            }
        }

        public void force() {
            GLES10.glTexParameterx(3553, 10241, this.min.value);
            GLES10.glTexParameterx(3553, TarConstants.DEFAULT_BLKSIZE, this.mag.value);
        }

        @NonNull
        public String toString() {
            return "filters min = " + this.min + " mag = " + this.mag;
        }
    }


    public static class WrapParameters extends Facet<WrapParameters> {
        public final TextureWrap s;
        public final TextureWrap t;

        public WrapParameters() {
            this.s = TextureWrap.REPEAT;
            this.t = TextureWrap.REPEAT;
        }

        public WrapParameters(TextureWrap s, TextureWrap t) {
            this.s = s;
            this.t = t;
        }

        @Override
        public void transitionFrom(@NonNull WrapParameters w) {
            if (this.s != w.s) {
                GLES10.glTexParameterx(3553, 10242, this.s.value);
            }
            if (this.t != w.t) {
                GLES10.glTexParameterx(3553, 10243, this.t.value);
            }
        }

        public void force() {
            GLES10.glTexParameterx(3553, 10242, this.s.value);
            GLES10.glTexParameterx(3553, 10243, this.t.value);
        }

        @Override
        public int compareTo(@NonNull WrapParameters w) {
            int d = this.s.ordinal() - w.s.ordinal();
            if (d == 0) {
                return this.t.ordinal() - w.t.ordinal();
            }
            return d;
        }

        @NonNull
        public String toString() {
            return "Wrap s = " + this.s + " t = " + this.t;
        }
    }
}
