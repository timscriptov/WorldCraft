package com.mcal.worldcraft.nbt;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.Contract;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Loads NBT files, nabbed from the minecraft wiki
 */
public class Tag {
    private final Type type;
    private final String name;
    private Type listType = null;
    private Object value;

    /**
     * Create a new TAG_List or TAG_Compound NBT tag.
     *
     * @param type  either TAG_List or TAG_Compound
     * @param name  name for the new tag or null to create an unnamed tag.
     * @param value list of tags to add to the new tag.
     */
    public Tag(final Type type, final String name, final Tag[] value) {
        this(type, name, (Object) value);
    }

    /**
     * Create a new TAG_List with an empty list. Use {@link Tag#addTag(Tag)} to
     * add tags later.
     *
     * @param name     name for this tag or null to create an unnamed tag.
     * @param listType type of the elements in this empty list.
     */
    public Tag(final String name, final Type listType) {
        this(Type.TAG_List, name, listType);
    }

    /**
     * Create a new NBT tag.
     *
     * @param type  any value from the {@link Type} enum.
     * @param name  name for the new tag or null to create an unnamed tag.
     * @param value an object that fits the tag type or a {@link Type} to create an
     *              empty TAG_List with this list type.
     */
    public Tag(final Type type, final String name, Object value) {
        if (type == Type.TAG_Compound)
            if (!(value instanceof Tag[]))
                throw new IllegalArgumentException();
        switch (type) {
            case TAG_End:
                if (value != null)
                    throw new IllegalArgumentException();
                break;
            case TAG_Byte:
                if (!(value instanceof Byte))
                    throw new IllegalArgumentException();
                break;
            case TAG_Short:
                if (!(value instanceof Short))
                    throw new IllegalArgumentException();
                break;
            case TAG_Int:
                if (!(value instanceof Integer))
                    throw new IllegalArgumentException();
                break;
            case TAG_Long:
                if (!(value instanceof Long))
                    throw new IllegalArgumentException();
                break;
            case TAG_Float:
                if (!(value instanceof Float))
                    throw new IllegalArgumentException();
                break;
            case TAG_Double:
                if (!(value instanceof Double))
                    throw new IllegalArgumentException();
                break;
            case TAG_Byte_Array:
                if (!(value instanceof byte[]))
                    throw new IllegalArgumentException();
                break;
            case TAG_String:
                if (!(value instanceof String))
                    throw new IllegalArgumentException();
                break;
            case TAG_List:
                if (value instanceof Type) {
                    listType = (Type) value;
                    value = new Tag[0];
                } else {
                    if (!(value instanceof Tag[]))
                        throw new IllegalArgumentException();
                    listType =
                            ((Tag[]) value)[0].getType();
                }
                break;
            case TAG_Compound:
                if (!(value instanceof Tag[]))
                    throw new IllegalArgumentException();
                break;
            case TAG_Int_Array:
                if (!(value instanceof int[]))
                    throw new IllegalArgumentException();
                break;
            case TAG_Long_Array:
                if (!(value instanceof long[]))
                    throw new IllegalArgumentException();
                break;
            default:
                throw new IllegalArgumentException();
        }
        this.type = type;
        this.name = name;
        this.value = value;
    }

    /**
     * Read a tag and its nested tags from an InputStream.
     *
     * @param is         stream to read from, like a FileInputStream
     * @param compressed <code>true</code> if the stream is gzipped
     * @return NBT tag or structure read from the InputStream
     * @throws IOException if there was no valid NBT structure in the InputStream or if
     *                     another IOException occurred.
     */
    @NonNull
    @Contract("_, _ -> new")
    public static Tag readFrom(final InputStream is, final boolean compressed)
            throws IOException {
        final DataInputStream dis =
                new DataInputStream(compressed ? new GZIPInputStream(is) : is);

        final byte type = dis.readByte();
        if (type == 0)
            return new Tag(Type.TAG_End, null, null);
        else
            return new Tag(Type.values()[type], dis.readUTF(), readPayload(
                    dis, type));
    }

    @Nullable
    private static Object
    readPayload(final DataInputStream dis, final byte type)
            throws IOException {
        switch (type) {
            case 0:
                return null;
            case 1:
                return dis.readByte();
            case 2:
                return dis.readShort();
            case 3:
                return dis.readInt();
            case 4:
                return dis.readLong();
            case 5:
                return dis.readFloat();
            case 6:
                return dis.readDouble();
            case 7:
                final int length = dis.readInt();
                final byte[] ba = new byte[length];
                dis.readFully(ba);
                return ba;
            case 8:
                return dis.readUTF();
            case 9:
                final byte lt = dis.readByte();
                final int ll = dis.readInt();
                final Tag[] lo = new Tag[ll];
                for (int i = 0; i < ll; i++)
                    lo[i] =
                            new Tag(Type.values()[lt], null, readPayload(dis, lt));
                if (lo.length == 0)
                    return Type.values()[lt];
                else
                    return lo;
            case 10:
                byte stt;
                Tag[] tags = new Tag[0];
                do {
                    stt = dis.readByte();
                    String name = null;
                    if (stt != 0)
                        name = dis.readUTF();
                    final Tag[] newTags = new Tag[tags.length + 1];
                    System.arraycopy(tags, 0, newTags, 0, tags.length);
                    newTags[tags.length] =
                            new Tag(Type.values()[stt], name,
                                    readPayload(dis, stt));
                    tags = newTags;
                }
                while (stt != 0);
                return tags;
        }
        return null;
    }

    @Nullable
    @Contract(pure = true)
    private static String getTypeString(@NonNull final Type type) {
        switch (type) {
            case TAG_End:
                return "TAG_End";
            case TAG_Byte:
                return "TAG_Byte";
            case TAG_Short:
                return "TAG_Short";
            case TAG_Int:
                return "TAG_Int";
            case TAG_Long:
                return "TAG_Long";
            case TAG_Float:
                return "TAG_Float";
            case TAG_Double:
                return "TAG_Double";
            case TAG_Byte_Array:
                return "TAG_Byte_Array";
            case TAG_String:
                return "TAG_String";
            case TAG_List:
                return "TAG_List";
            case TAG_Compound:
                return "TAG_Compound";
        }
        return null;
    }

    private static void indent(final int indent) {
        for (int i = 0; i < indent; i++)
            System.out.print("   ");
    }

    /**
     * @return tag type
     */
    public Type getType() {
        return type;
    }

    /**
     * @return tag name
     */
    public String getName() {
        return name;
    }

    /**
     * @return tag value
     */
    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Object getTagValue(String name) {
        Tag tag = findTagByName(name);
        if (tag != null) {
            return tag.getValue();
        }
        return null;
    }

    public void saveTagValue(Type type, String name, Object value) {
        removeSubTag(findTagByName(name));
        Tag tag = new Tag(type, name, value);
        addTag(tag);
    }

    /**
     * @return list tag type
     */
    public Type getListType() {
        return listType;
    }

    /**
     * Add a tag to a TAG_List or a TAG_Compound.
     *
     * @param tag
     */
    public void addTag(final Tag tag) {
        if (type != Type.TAG_List && type != Type.TAG_Compound)
            throw new RuntimeException();
        final Tag[] subtags = (Tag[]) value;
        insertTag(tag, subtags.length);
    }

    /**
     * Add a tag to a TAG_List or a TAG_Compound at the specified index.
     *
     * @param tag
     * @param index
     */
    public void insertTag(final Tag tag, final int index) {
        if (type != Type.TAG_List && type != Type.TAG_Compound)
            throw new RuntimeException();
        final Tag[] subtags = (Tag[]) value;
        if (subtags.length > 0)
            if (type == Type.TAG_List && tag.getType() != getListType())
                throw new IllegalArgumentException();
        if (index > subtags.length)
            throw new IndexOutOfBoundsException();
        final Tag[] newValue = new Tag[subtags.length + 1];
        System.arraycopy(subtags, 0, newValue, 0, index);
        newValue[index] = tag;
        System.arraycopy(subtags, index, newValue, index + 1, subtags.length
                - index);
        value = newValue;
    }

    /**
     * Remove a tag from a TAG_List or a TAG_Compound at the specified index.
     *
     * @param index
     * @return the removed tag
     */
    public Tag removeTag(int index) {
        if (type != Type.TAG_List && type != Type.TAG_Compound)
            throw new RuntimeException();
        final Tag[] subtags = (Tag[]) value;
        final Tag victim = subtags[index];
        final Tag[] newValue = new Tag[subtags.length - 1];
        System.arraycopy(subtags, 0, newValue, 0, index);
        index++;
        System.arraycopy(subtags, index, newValue, index - 1, subtags.length
                - index);
        value = newValue;
        return victim;
    }

    /**
     * Remove a tag from a TAG_List or a TAG_Compound. If the tag is not a child
     * of this tag then nested tags are searched.
     *
     * @param tag tag to look for
     */
    public void removeSubTag(final Tag tag) {
        if (type != Type.TAG_List && type != Type.TAG_Compound)
            throw new RuntimeException();
        if (tag == null)
            return;
        final Tag[] subtags = (Tag[]) value;
        for (int i = 0; i < subtags.length; i++)
            if (subtags[i] == tag) {
                removeTag(i);
                return;
            } else if (subtags[i].type == Type.TAG_List
                    || subtags[i].type == Type.TAG_Compound)
                subtags[i].removeSubTag(tag);
    }

    /**
     * Find the first nested tag with specified name in a TAG_Compound.
     *
     * @param name the name to look for. May be null to look for unnamed tags.
     * @return the first nested tag that has the specified name.
     */
    public Tag findTagByName(final String name) {
        return findNextTagByName(name, null);
    }

    /**
     * Find the first nested tag with specified name in a TAG_List or
     * TAG_Compound after a tag with the same name.
     *
     * @param name  the name to look for. May be null to look for unnamed tags.
     * @param found the previously found tag with the same name.
     * @return the first nested tag that has the specified name after the
     * previously found tag.
     */
    public Tag findNextTagByName(final String name, final Tag found) {
        if (type != Type.TAG_List && type != Type.TAG_Compound)
            return null;
        final Tag[] subtags = (Tag[]) value;
        for (final Tag subtag : subtags)
            if (subtag.name == null && name == null || subtag.name != null
                    && subtag.name.equals(name))
                return subtag;
            else {
                final Tag newFound = subtag.findTagByName(name);
                if (newFound != null)
                    if (newFound == found)
                        continue;
                    else
                        return newFound;
            }
        return null;
    }

    /**
     * Read a tag and its nested tags from an InputStream.
     *
     * @param os stream to write to, like a FileOutputStream
     * @throws IOException if this is not a valid NBT structure or if any IOException
     *                     occurred.
     */
    public void writeTo(final OutputStream os) throws IOException {
        GZIPOutputStream gzos;
        final DataOutputStream dos =
                new DataOutputStream(gzos = new GZIPOutputStream(os));
        dos.writeByte(type.ordinal());
        if (type != Type.TAG_End) {
            dos.writeUTF(name);
            writePayload(dos);
        }
        gzos.flush();
        gzos.close();
    }

    public void writeTo(OutputStream os, boolean compressed) throws IOException {
        GZIPOutputStream gzip = null;
        if (compressed) {
            gzip = new GZIPOutputStream(os);
        }
        DataOutputStream dos = new DataOutputStream(compressed ? gzip : os);
        dos.writeByte(this.type.ordinal());
        if (this.type != Type.TAG_End) {
            dos.writeUTF(this.name);
            writePayload(dos);
        }
        if (gzip != null) {
            gzip.finish();
            gzip.close();
        }
        os.flush();
        os.close();
    }

    private void writePayload(final DataOutputStream dos) throws IOException {
        switch (type) {
            case TAG_End:
                break;
            case TAG_Byte:
                dos.writeByte((Byte) value);
                break;
            case TAG_Short:
                dos.writeShort((Short) value);
                break;
            case TAG_Int:
                dos.writeInt((Integer) value);
                break;
            case TAG_Long:
                dos.writeLong((Long) value);
                break;
            case TAG_Float:
                dos.writeFloat((Float) value);
                break;
            case TAG_Double:
                dos.writeDouble((Double) value);
                break;
            case TAG_Byte_Array:
                final byte[] ba = (byte[]) value;
                dos.writeInt(ba.length);
                dos.write(ba);
                break;
            case TAG_String:
                dos.writeUTF((String) value);
                break;
            case TAG_List:
                final Tag[] list = (Tag[]) value;
                dos.writeByte(getListType().ordinal());
                dos.writeInt(list.length);
                for (final Tag tt : list)
                    tt.writePayload(dos);
                break;
            case TAG_Compound:
                final Tag[] subtags = (Tag[]) value;
                for (final Tag st : subtags) {
                    final Type type = st.getType();
                    dos.writeByte(type.ordinal());
                    if (type != Type.TAG_End) {
                        dos.writeUTF(st.getName());
                        st.writePayload(dos);
                    }
                }
                break;
            case TAG_Int_Array:
                final int[] ints = (int[]) value;
                dos.writeInt(ints.length);
                for (int anInt : ints) {
                    dos.writeInt(anInt);
                }
                break;
            case TAG_Long_Array:
                final long[] longs = (long[]) value;
                dos.writeInt(longs.length);
                for (long aLong : longs) {
                    dos.writeLong(aLong);
                }
                break;
        }
    }

    /**
     * Print the NBT structure to System.out
     */
    public void print() {
        print(this, 0);
    }

    private void print(@NonNull final Tag t, final int indent) {
        final Type type = t.getType();
        if (type == Type.TAG_End)
            return;
        final String name = t.getName();
        indent(indent);
        System.out.print(getTypeString(t.getType()));
        if (name != null) {
            System.out.print("(\"" + t.getName() + "\")");
        }
        if (type == Type.TAG_Byte_Array) {
            final byte[] b = (byte[]) t.getValue();
            System.out.println(": [" + b.length + " bytes]");
        } else if (type == Type.TAG_List) {
            final Tag[] subtags = (Tag[]) t.getValue();
            System.out.println(": " + subtags.length + " entries of type "
                    + getTypeString(t.getListType()));
            for (final Tag st : subtags)
                print(st, indent + 1);
            indent(indent);
            System.out.println("}");
        } else if (type == Type.TAG_Compound) {
            final Tag[] subtags = (Tag[]) t.getValue();
            System.out.println(": " + (subtags.length - 1) + " entries");
            indent(indent);
            System.out.println("{");
            for (final Tag st : subtags)
                print(st, indent + 1);
            indent(indent);
            System.out.println("}");
        } else if (type == Type.TAG_Int_Array) {
            final int[] b = (int[]) t.getValue();
            System.out.println(": [" + b.length + " ints]");
        } else if (type == Type.TAG_Long_Array) {
            final long[] b = (long[]) t.getValue();
            System.out.println(": [" + b.length + " longs]");
        } else {
            System.out.println(": " + t.getValue());
        }
    }

    @NonNull
    @Override
    public String toString() {
        final StringBuilder buff = new StringBuilder("Tag ");
        buff.append(name);
        buff.append(" ").append(type).append(" ");

        if (type == Type.TAG_List) {
            final Tag[] tl = (Tag[]) value;
            buff.append(listType).append(" ").append(tl.length);
            buff.append("\n[");

            for (final Tag tag : tl)
                buff.append(tag.toString()).append(", ");
            buff.append("]");
        } else
            buff.append(value);

        return buff.toString();
    }


    /**
     * Enum for the tag types.
     */
    public enum Type {
        /***/
        TAG_End,
        /***/
        TAG_Byte,
        /***/
        TAG_Short,
        /***/
        TAG_Int,
        /***/
        TAG_Long,
        /***/
        TAG_Float,
        /***/
        TAG_Double,
        /***/
        TAG_Byte_Array,
        /***/
        TAG_String,
        /***/
        TAG_List,
        /***/
        TAG_Compound,
        /***/
        TAG_Int_Array,
        /***/
        TAG_Long_Array
    }
}
