package com.mcal.droid.rugl.text;


/**
 * Interface for objects that can compute kerning values
 *
 * @author ryanm
 */
public interface KerningSource {
    /**
     * Computes the kerning value to be applied when two characters are
     * laid out side by side in the aforementioned {@link Font}
     *
     * @param prev The first character
     * @param next The following character
     * @return The kerning value
     */
    public float computeKerning(char prev, char next);
}