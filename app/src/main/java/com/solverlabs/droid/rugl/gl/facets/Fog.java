package com.solverlabs.droid.rugl.gl.facets;

import android.opengl.GLES10;
import android.util.Log;

import com.solverlabs.droid.rugl.gl.Facet;
import com.solverlabs.droid.rugl.gl.enums.FogMode;
import com.solverlabs.droid.rugl.util.Colour;


public class Fog extends Facet<Fog> {
    public static final Fog disabled = new Fog();
    public static float[] fa = {0.7f, 0.7f, 0.9f, 1.0f};
    public final float density;
    public final boolean enabled;
    public final FogMode mode;
    public int colour;
    public float end;
    public float start;

    private Fog() {
        this.enabled = false;
        this.mode = FogMode.EXP;
        this.density = 1.0f;
        this.start = 0.0f;
        this.end = 1.0f;
        this.colour = Colour.packFloat(0.0f, 0.0f, 0.0f, 0.0f);
    }

    public Fog(FogMode mode, float density, float start, float end, int colour) {
        this.enabled = true;
        this.mode = mode;
        this.density = density;
        this.start = start;
        this.end = end;
        this.colour = colour;
    }

    @Override
    public int compareTo(Fog another) {
        int i = 1;
        int i2 = this.enabled ? 1 : 0;
        if (!another.enabled) {
            i = 0;
        }
        float d = i2 - i;
        if (d == 0.0f) {
            d = this.mode.mode - another.mode.mode;
            if (d == 0.0f) {
                d = this.density - another.density;
                if (d == 0.0f) {
                    d = this.start - another.start;
                    if (d == 0.0f) {
                        d = this.end - another.end;
                        if (d == 0.0f) {
                            d = this.colour - another.colour;
                        }
                    }
                }
            }
        }
        return (int) Math.signum(d);
    }

    @Override
    public void transitionFrom(Fog facet) {
        if (this.enabled != facet.enabled) {
            if (this.enabled) {
                GLES10.glEnable(2912);
                GLES10.glFogx(2917, this.mode.mode);
                GLES10.glFogf(2914, this.density);
                GLES10.glFogf(2915, this.start);
                GLES10.glFogf(2916, this.end);
                GLES10.glFogfv(2918, fa, 0);
                return;
            }
            GLES10.glDisable(2912);
        } else if (this.enabled) {
            if (this.mode != facet.mode) {
                GLES10.glFogx(2917, this.mode.mode);
            }
            if (this.density != facet.density) {
                GLES10.glFogf(2914, this.density);
            }
            if (this.start != facet.start) {
                GLES10.glFogf(2915, this.start);
            }
            if (this.end != facet.end) {
                GLES10.glFogf(2916, this.end);
            }
            if (this.colour != facet.colour) {
                Colour.toArray(this.colour, fa);
                GLES10.glFogfv(2918, fa, 0);
                Log.d("FOG", "minimal transition" + this.colour);
            }
        }
    }

    public void setFA(int colour) {
        Colour.toArray(colour, fa);
    }

    public String toString() {
        return "enabled " + this.enabled;
    }
}
