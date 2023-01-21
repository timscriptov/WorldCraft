package com.solverlabs.droid.rugl.util;

import androidx.annotation.NonNull;

import java.nio.ByteOrder;


public class Colour {
    public static final int black;
    public static final int blue;
    public static final int cyan;
    public static final int darkgrey;
    public static final int green;
    public static final int grey;
    public static final int lightgrey;
    public static final int magenta;
    public static final int ocean;
    public static final int orange;
    public static final int raspberry;
    public static final int red;
    public static final int springGreen;
    public static final int turquoise;
    public static final int violet;
    public static final int white;
    public static final int yellow;
    private static final int alphaOffset;
    private static final int blueOffset;
    private static final int greenOffset;
    private static final int redOffset;
    /**
     * Mask to get only the alpha bits
     */
    private static final int alphaMask;
    /**
     * Mask to get only the colour bits
     */
    private static final int colourmask;

    static {
        boolean big = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
        redOffset = big ? 24 : 0;
        greenOffset = big ? 16 : 8;
        blueOffset = big ? 8 : 16;
        alphaOffset = big ? 0 : 24;
        white = packFloat(1.0f, 1.0f, 1.0f, 1.0f);
        black = packFloat(0.0f, 0.0f, 0.0f, 1.0f);
        grey = packFloat(0.5f, 0.5f, 0.5f, 1.0f);
        darkgrey = packFloat(0.25f, 0.25f, 0.25f, 1.0f);
        lightgrey = packFloat(0.75f, 0.75f, 0.75f, 1.0f);
        red = packFloat(1.0f, 0.0f, 0.0f, 1.0f);
        green = packFloat(0.0f, 1.0f, 0.0f, 1.0f);
        blue = packFloat(0.0f, 0.0f, 1.0f, 1.0f);
        yellow = packFloat(1.0f, 1.0f, 0.0f, 1.0f);
        cyan = packFloat(0.0f, 1.0f, 1.0f, 1.0f);
        magenta = packFloat(1.0f, 0.0f, 1.0f, 1.0f);
        orange = packFloat(1.0f, 0.5f, 0.0f, 1.0f);
        springGreen = packFloat(0.5f, 1.0f, 0.0f, 1.0f);
        turquoise = packFloat(0.0f, 1.0f, 0.5f, 1.0f);
        ocean = packFloat(0.0f, 0.5f, 1.0f, 1.0f);
        violet = packFloat(0.5f, 0.0f, 1.0f, 1.0f);
        raspberry = packFloat(1.0f, 0.0f, 0.5f, 1.0f);
        alphaMask = 255 << alphaOffset;
        colourmask = ~alphaMask;
    }

    private Colour() {
    }

    /**
     * Converts from a native-order packed colour int to a big-endian
     * packed colour int
     *
     * @param rgba
     * @return The big-endian int
     */
    public static int toBigEndian(int rgba) {
        return (redi(rgba) << 24) | (greeni(rgba) << 16) | (bluei(rgba) << 8) | alphai(rgba);
    }

    /**
     * Converts from a big-endian packed colouor int to a native-order
     * packed colour int
     *
     * @param rgba
     * @return the native-order int
     */
    public static int fromBigEndian(int rgba) {
        int r = (rgba >> 24) & 255;
        int g = (rgba >> 16) & 255;
        int b = (rgba >> 8) & 255;
        int a = (rgba >> 0) & 255;
        return packInt(r, g, b, a);
    }

    /**
     * @param rgba  packed colour int
     * @param array destination array, or <code>null</code> to allocate a
     *              new array
     * @return a float[]{ r, g, b, a } array, in ranges 0-1
     */
    @NonNull
    public static float[] toArray(int rgba, float[] array) {
        if (array == null) {
            array = new float[4];
        }
        array[0] = redf(rgba);
        array[1] = greenf(rgba);
        array[2] = bluef(rgba);
        array[3] = alphaf(rgba);
        return array;
    }

    /**
     * Packs colour components into an integer
     *
     * @param r range 0-255
     * @param g range 0-255
     * @param b range 0-255
     * @param a range 0-255
     * @return a packed colour integer
     */
    public static int packInt(int r, int g, int b, int a) {
        int r2 = (r & 255) << redOffset;
        int g2 = (g & 255) << greenOffset;
        int b2 = (b & 255) << blueOffset;
        return r2 | g2 | b2 | ((a & 255) << alphaOffset);
    }

    /**
     * Packs colour components into an integer
     *
     * @param r range 0-1
     * @param g range 0-1
     * @param b range 0-1
     * @param a range 0-1
     * @return a packed colour integer
     */
    public static int packFloat(float r, float g, float b, float a) {
        return packInt((int) (r * 255.0f), (int) (g * 255.0f), (int) (b * 255.0f), (int) (255.0f * a));
    }

    /**
     * Extracts the red component
     *
     * @param rgba packed colour value
     * @return The component 0-1
     */
    public static float redf(int rgba) {
        return redi(rgba) / 255.0f;
    }

    /**
     * Extracts the green component
     *
     * @param rgba packed colour value
     * @return The component 0-1
     */
    public static float greenf(int rgba) {
        return greeni(rgba) / 255.0f;
    }

    /**
     * Extracts the blue component
     *
     * @param rgba packed colour value
     * @return The component 0-1
     */
    public static float bluef(int rgba) {
        return bluei(rgba) / 255.0f;
    }

    /**
     * Extracts the alpha component
     *
     * @param rgba packed colour value
     * @return The component 0-1
     */
    public static float alphaf(int rgba) {
        return alphai(rgba) / 255.0f;
    }

    /**
     * Extracts the red component
     *
     * @param rgba packed colour value
     * @return The component 0-255
     */
    public static int redi(int rgba) {
        return (rgba >> redOffset) & 255;
    }

    /**
     * Extracts the green component
     *
     * @param rgba packed colour value
     * @return The component 0-255
     */
    public static int greeni(int rgba) {
        return (rgba >> greenOffset) & 255;
    }

    /**
     * Extracts the blue component
     *
     * @param rgba packed colour value
     * @return The component 0-255
     */
    public static int bluei(int rgba) {
        return (rgba >> blueOffset) & 255;
    }

    /**
     * Extracts the alpha component
     *
     * @param rgba packed colour value
     * @return The component 0-255
     */
    public static int alphai(int rgba) {
        return (rgba >> alphaOffset) & 255;
    }

    /**
     * Amends a colour by changing the alpha component
     *
     * @param colour source colour
     * @param alpha  new alpha component (0-255)
     * @return new colour
     */
    public static int withAlphai(int colour, int alpha) {
        return (colour & colourmask) | ((alpha & 255) << alphaOffset);
    }

    /**
     * Amends an array of colour ints by changing the alpha component
     *
     * @param colours source colours
     * @param alpha   new alpha component (0-255)
     */
    public static void withAlphai(@NonNull int[] colours, int alpha) {
        for (int i = 0; i < colours.length; i++) {
            colours[i] = withAlphai(colours[i], alpha);
        }
    }

    public static int withLightf(int colour, float light) {
        int red2 = (int) (redi(colour) * light);
        int green2 = (int) (greeni(colour) * light);
        int blue2 = (int) (bluei(colour) * light);
        return packInt(red2, green2, blue2, alphai(colour));
    }

    /**
     * @param rgba
     * @return a string in r:g:b:a format
     */
    @NonNull
    public static String toString(int rgba) {
        return redi(rgba) + ":" + greeni(rgba) + ":" + bluei(rgba) + ":" + alphai(rgba);
    }
}
