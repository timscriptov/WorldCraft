package com.solverlabs.worldcraft.nbt;

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

public class Tag {
    private final String name;
    private final Type type;
    private Type listType;
    private Object value;

    public Tag(Type type, String name, Tag[] value) {
        this(type, name, (Object) value);
    }

    public Tag(String name, Type listType) {
        this(Type.TAG_List, name, listType);
    }

    public Tag(Type type, String name, Object value) {
        this.listType = null;
        if (type == Type.TAG_Compound && !(value instanceof Tag[])) {
            throw new IllegalArgumentException();
        }
        switch (type) {
            case TAG_End:
                if (value != null) {
                    throw new IllegalArgumentException();
                }
                break;
            case TAG_Byte:
                if (!(value instanceof Byte)) {
                    throw new IllegalArgumentException();
                }
                break;
            case TAG_Short:
                if (!(value instanceof Short)) {
                    throw new IllegalArgumentException();
                }
                break;
            case TAG_Int:
                if (!(value instanceof Integer)) {
                    throw new IllegalArgumentException();
                }
                break;
            case TAG_Long:
                if (!(value instanceof Long)) {
                    throw new IllegalArgumentException();
                }
                break;
            case TAG_Float:
                if (!(value instanceof Float)) {
                    throw new IllegalArgumentException();
                }
                break;
            case TAG_Double:
                if (!(value instanceof Double)) {
                    throw new IllegalArgumentException();
                }
                break;
            case TAG_Byte_Array:
                if (!(value instanceof byte[])) {
                    throw new IllegalArgumentException();
                }
                break;
            case TAG_String:
                if (!(value instanceof String)) {
                    throw new IllegalArgumentException();
                }
                break;
            case TAG_List:
                if (value instanceof Type) {
                    this.listType = (Type) value;
                    value = new Tag[0];
                    break;
                } else if (!(value instanceof Tag[])) {
                    throw new IllegalArgumentException();
                } else {
                    this.listType = ((Tag[]) value)[0].getType();
                    break;
                }
            case TAG_Compound:
                if (!(value instanceof Tag[])) {
                    throw new IllegalArgumentException();
                }
                break;
            default:
                throw new IllegalArgumentException();
        }
        this.type = type;
        this.name = name;
        this.value = value;
    }

    @NonNull
    @Contract("_, _ -> new")
    public static Tag readFrom(InputStream is, boolean compressed) throws IOException {
        if (compressed) {
            is = new GZIPInputStream(is);
        }
        DataInputStream dis = new DataInputStream(is);
        byte type = dis.readByte();
        if (type == 0) {
            return new Tag(Type.TAG_End, (String) null, (Tag[]) null);
        }
        String disUTF = dis.readUTF();
        Object object = readPayload(dis, type);
        return new Tag(Type.values()[type], disUTF, object);
    }

    private static Object readPayload(DataInputStream dis, byte type) throws IOException {
        byte stt;
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
                int length = dis.readInt();
                byte[] ba = new byte[length];
                dis.readFully(ba);
                return ba;
            case 8:
                return dis.readUTF();
            case 9:
                byte lt = dis.readByte();
                int ll = dis.readInt();
                Tag[] lo = new Tag[ll];
                for (int i = 0; i < ll; i++) {
                    lo[i] = new Tag(Type.values()[lt], (String) null, readPayload(dis, lt));
                }
                return lo.length == 0 ? Type.values()[lt] : lo;
            case 10:
                Tag[] tags = new Tag[0];
                do {
                    stt = dis.readByte();
                    String name = null;
                    if (stt != 0) {
                        name = dis.readUTF();
                    }
                    Tag[] newTags = new Tag[tags.length + 1];
                    System.arraycopy(tags, 0, newTags, 0, tags.length);
                    newTags[tags.length] = new Tag(Type.values()[stt], name, readPayload(dis, stt));
                    tags = newTags;
                } while (stt != 0);
                return tags;
            default:
                return null;
        }
    }

    @Nullable
    @Contract(pure = true)
    private static String getTypeString(@NonNull Type type) {
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
            default:
                return null;
        }
    }

    private static void indent(int indent) {
        for (int i = 0; i < indent; i++) {
            System.out.print("   ");
        }
    }

    public Type getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }

    public Object getValue() {
        return this.value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Type getListType() {
        return this.listType;
    }

    public void addTag(Tag tag) {
        if (this.type != Type.TAG_List && this.type != Type.TAG_Compound) {
            throw new RuntimeException();
        }
        Tag[] subtags = (Tag[]) this.value;
        insertTag(tag, subtags.length);
    }

    public void insertTag(Tag tag, int index) {
        if (this.type != Type.TAG_List && this.type != Type.TAG_Compound) {
            throw new RuntimeException();
        }
        Tag[] subtags = (Tag[]) this.value;
        if (subtags.length > 0 && this.type == Type.TAG_List && tag.getType() != getListType()) {
            throw new IllegalArgumentException();
        }
        if (index > subtags.length) {
            throw new IndexOutOfBoundsException();
        }
        Tag[] newValue = new Tag[subtags.length + 1];
        System.arraycopy(subtags, 0, newValue, 0, index);
        newValue[index] = tag;
        System.arraycopy(subtags, index, newValue, index + 1, subtags.length - index);
        this.value = newValue;
    }

    public Tag removeTag(int index) {
        if (this.type != Type.TAG_List && this.type != Type.TAG_Compound) {
            throw new RuntimeException();
        }
        Tag[] subtags = (Tag[]) this.value;
        Tag victim = subtags[index];
        Tag[] newValue = new Tag[subtags.length - 1];
        System.arraycopy(subtags, 0, newValue, 0, index);
        int index2 = index + 1;
        System.arraycopy(subtags, index2, newValue, index2 - 1, subtags.length - index2);
        this.value = newValue;
        return victim;
    }

    public void removeSubTag(Tag tag) {
        if (this.type != Type.TAG_List && this.type != Type.TAG_Compound) {
            throw new RuntimeException();
        }
        if (tag != null) {
            Tag[] subtags = (Tag[]) this.value;
            for (int i = 0; i < subtags.length; i++) {
                if (subtags[i] == tag) {
                    removeTag(i);
                    return;
                }
                if (subtags[i].type == Type.TAG_List || subtags[i].type == Type.TAG_Compound) {
                    subtags[i].removeSubTag(tag);
                }
            }
        }
    }

    public Tag findTagByName(String name) {
        return findNextTagByName(name, null);
    }

    public Tag findNextTagByName(String name, Tag found) {
        if (this.type == Type.TAG_List || this.type == Type.TAG_Compound) {
            Tag[] subtags = (Tag[]) this.value;
            for (Tag subtag : subtags) {
                if ((subtag.name == null && name == null) || (subtag.name != null && subtag.name.equals(name))) {
                    return subtag;
                }
                Tag newFound = subtag.findTagByName(name);
                if (newFound != null && !newFound.equals(found)) {
                    return newFound;
                }
            }
            return null;
        }
        return null;
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

    private void writePayload(DataOutputStream dos) throws IOException {
        switch (this.type) {
            case TAG_End:
            default:
                return;
            case TAG_Byte:
                dos.writeByte((Byte) this.value);
                return;
            case TAG_Short:
                dos.writeShort((Short) this.value);
                return;
            case TAG_Int:
                dos.writeInt((Integer) this.value);
                return;
            case TAG_Long:
                dos.writeLong((Long) this.value);
                return;
            case TAG_Float:
                dos.writeFloat((Float) this.value);
                return;
            case TAG_Double:
                dos.writeDouble((Double) this.value);
                return;
            case TAG_Byte_Array:
                byte[] ba = (byte[]) this.value;
                dos.writeInt(ba.length);
                dos.write(ba);
                return;
            case TAG_String:
                dos.writeUTF((String) this.value);
                return;
            case TAG_List:
                Tag[] list = (Tag[]) this.value;
                dos.writeByte(getListType().ordinal());
                dos.writeInt(list.length);
                for (Tag tt : list) {
                    tt.writePayload(dos);
                }
                return;
            case TAG_Compound:
                Tag[] subtags = (Tag[]) this.value;
                for (Tag st : subtags) {
                    Type type = st.getType();
                    dos.writeByte(type.ordinal());
                    if (type != Type.TAG_End) {
                        dos.writeUTF(st.getName());
                        st.writePayload(dos);
                    }
                }
                return;
        }
    }

    public void print() {
        print(this, 0);
    }

    private void print(@NonNull Tag t, int indent) {
        Type type = t.getType();
        if (type != Type.TAG_End) {
            String name = t.getName();
            indent(indent);
            System.out.print(getTypeString(t.getType()));
            if (name != null) {
                System.out.print("(\"" + t.getName() + "\")");
            }
            if (type == Type.TAG_Byte_Array) {
                byte[] b = (byte[]) t.getValue();
                System.out.println(": [" + b.length + " bytes]");
            } else if (type == Type.TAG_List) {
                Tag[] subtags = (Tag[]) t.getValue();
                System.out.println(": " + subtags.length + " entries of type " + getTypeString(t.getListType()));
                for (Tag st : subtags) {
                    print(st, indent + 1);
                }
                indent(indent);
                System.out.println("}");
            } else if (type == Type.TAG_Compound) {
                Tag[] subtags2 = (Tag[]) t.getValue();
                System.out.println(": " + (subtags2.length - 1) + " entries");
                indent(indent);
                System.out.println("{");
                for (Tag st2 : subtags2) {
                    print(st2, indent + 1);
                }
                indent(indent);
                System.out.println("}");
            } else {
                System.out.println(": " + t.getValue());
            }
        }
    }

    @NonNull
    public String toString() {
        StringBuilder buff = new StringBuilder("Tag ");
        buff.append(this.name);
        buff.append(" ").append(this.type).append(" ");
        if (this.type == Type.TAG_List) {
            Tag[] tl = (Tag[]) this.value;
            buff.append(this.listType).append(" ").append(tl.length);
            buff.append("\n[");
            for (Tag tag : tl) {
                buff.append(tag.toString()).append(", ");
            }
            buff.append("]");
        } else {
            buff.append(this.value);
        }
        return buff.toString();
    }

    public void saveTagValue(Type type, String name, Object value) {
        removeSubTag(findTagByName(name));
        Tag tag = new Tag(type, name, value);
        addTag(tag);
    }

    public Object getTagValue(String name) {
        Tag tag = findTagByName(name);
        if (tag != null) {
            return tag.getValue();
        }
        return null;
    }

    public enum Type {
        TAG_End,
        TAG_Byte,
        TAG_Short,
        TAG_Int,
        TAG_Long,
        TAG_Float,
        TAG_Double,
        TAG_Byte_Array,
        TAG_String,
        TAG_List,
        TAG_Compound
    }
}
