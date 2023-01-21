package com.solverlabs.droid.rugl.util.io;

/**
 * A read-only interface for a source of data
 *
 * @author ryanm
 */
public interface DataSource {
    /**
     * Gets a boolean from the source
     *
     * @return The next byte of the source, as a boolean
     */
    public boolean getBoolean();

    /**
     * Gets a byte from the source
     *
     * @return the next byte from the source
     */
    public byte getByte();

    /**
     * Gets a char from the source
     *
     * @return the next two bytes of the source, as a char
     */
    public char getChar();

    /**
     * Gets a double from the source
     *
     * @return the next eight bytes of the source, as a double
     */
    public double getDouble();

    /**
     * Gets a float from the source
     *
     * @return the next four bytes of the source, as a float
     */
    public float getFloat();

    /**
     * Gets an int from the source
     *
     * @return the next four bytes of the source, as an int
     */
    public int getInt();

    /**
     * Gets a long from the source
     *
     * @return the next eight bytes of the source, as a long
     */
    public long getLong();

    /**
     * Gets a short from the source
     *
     * @return The next two bytes of the source, as a short
     */
    public short getShort();
}