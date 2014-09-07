package org.tastefuljava.gallerygen.exif;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class IFD {
    private static final Logger LOG = Logger.getLogger(IFD.class.getName());

    private final byte[] data;
    private final int start;
    private final int position;
    private final ByteOrder order;

    public static interface Tag {
        public int getTag();
        public String getName();
    }

    public static interface IFDTag extends Tag {
        public IFD create(byte[] data, int start, int position,
                ByteOrder order);
    }

    IFD(byte[] data, int start, int position, ByteOrder order) {
        this.data = data;
        this.start = start;
        this.position = position;
        this.order = order;
    }

    protected abstract Tag getTag(int tag);

    public void accept(IFDVisitor visitor) {
        ByteBuffer buf = getBuffer();
        int size = 65535 & buf.getShort();
        for (int i = 0; i < size; ++i) {
            final int tagValue = 65535 & buf.getShort();
            Tag tag = getTag(tagValue);
            if (tag == null) {
                tag = new Tag() {
                    @Override
                    public int getTag() {
                        return tagValue;
                    }

                    @Override
                    public String getName() {
                        return Integer.toHexString(getTag());
                    }
                };
            }
            Type type = Type.forValue(65535 & buf.getShort());
            int count = buf.getInt();
            int next = buf.position() + 4;
            switch (type) {
                case BYTE:
                    if (count > 4) {
                        buf.position(start + buf.getInt());
                    }
                    visitor.handleBytes(i, tag, type, getBytes(buf, count));
                    break;
                case SHORT:
                    if (count > 2) {
                        buf.position(start + buf.getInt());
                    }
                    visitor.handleShorts(i, tag, type, getShorts(buf, count));
                    break;
                case LONG:
                    if (count > 1) {
                        buf.position(start + buf.getInt());
                    }
                    visitor.handleInts(i, tag, type, getInts(buf, count));
                    break;
                case RATIONAL:
                    buf.position(start + buf.getInt());
                    visitor.handleRationals(i, tag, type,
                            getRationals(buf, count));
                    break;
                case SBYTE:
                    if (count > 4) {
                        buf.position(start + buf.getInt());
                    }
                    visitor.handleSBytes(i, tag, type, getBytes(buf, count));
                    break;
                case SSHORT:
                    if (count > 2) {
                        buf.position(start + buf.getInt());
                    }
                    visitor.handleSShorts(i, tag, type, getShorts(buf, count));
                    break;
                case SLONG:
                    if (count > 1) {
                        buf.position(start + buf.getInt());
                    }
                    visitor.handleSInts(i, tag, type, getInts(buf, count));
                    break;
                case SRATIONAL:
                    buf.position(start + buf.getInt());
                    visitor.handleSRationals(i, tag, type,
                            getRationals(buf, count));
                    break;
                case ASCII:
                    if (count > 4) {
                        buf.position(start + buf.getInt());
                    }
                    byte[] bytes = new byte[count];
                    buf.get(bytes);
                    while (count > 0 && bytes[count - 1] == 0) {
                        --count;
                    }
                    String s = utf8(bytes, 0, count);
                    visitor.handleString(i, tag, type, s);
                    break;
                case UNDEFINED:
                    if (count > 4) {
                        buf.position(start + buf.getInt());
                    }
                    byte[] undef = new byte[count];
                    buf.get(undef);
                    visitor.handleUndefineds(i, tag, type, undef);
                    break;
                default:
                    break;
            }
            buf.position(next);
        }
    }

    private static String utf8(byte[] buf, int offs, int len) {
        try {
            return new String(buf, offs, len, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            // UTF-8 not supported? very unlikely
            LOG.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex.getMessage());
        }
    }

    public Rational getRational(Tag tag, Rational def) {
        ByteBuffer buf = locateTag(tag);
        if (buf == null) {
            return def;
        }
        Type type = Type.forValue(buf.getShort() & 65535);
        if (type != Type.RATIONAL && type != Type.SRATIONAL) {
            throw new IllegalArgumentException("Invalid Exif field type: "
                    + type + " expected: " + Type.RATIONAL);
        }
        int count = buf.getInt();
        if (count != 1) {
            throw new IllegalArgumentException("Invalid Exif field size: "
                    + count + " expected: 1");
        }
        buf.position(start + buf.getInt());
        int n = buf.getInt();
        int d = buf.getInt();
        return new Rational(n, d);
    }

    public Rational[] getRationals(Tag tag, Rational[] def) {
        ByteBuffer buf = locateTag(tag);
        if (buf == null) {
            return def;
        }
        Type type = Type.forValue(buf.getShort() & 65535);
        if (type != Type.RATIONAL && type != Type.SRATIONAL) {
            throw new IllegalArgumentException("Invalid Exif field type: "
                    + type + " expected: " + Type.RATIONAL);
        }
        int count = buf.getInt();
        buf.position(start + buf.getInt());
        return getRationals(buf, count);
    }

    public int getInt(Tag tag, int def) {
        ByteBuffer buf = locateTag(tag);
        if (buf == null) {
            return def;
        }
        Type type = Type.forValue(buf.getShort() & 65535);
        int count = buf.getInt();
        if (count != 1) {
            throw new IllegalArgumentException("Invalid Exif field size: "
                    + count + " expected: 1");
        }
        switch (type) {
            case BYTE:
                return buf.get();
            case SHORT:
                return buf.getShort() & 65535;
            case SLONG:
                return buf.getInt();
            case LONG:
                int value = buf.getInt();
                if (value < 0) {
                    throw new ArithmeticException("Integer overflow");
                }
                return value;
            default:
                throw new IllegalArgumentException("Invalid Exif field type: "
                        + type);
        }
    }

    public void setInt(Tag tag, int value) {
        ByteBuffer buf = locateTag(tag);
        if (buf == null) {
            return;
        }
        Type type = Type.forValue(buf.getShort() & 65535);
        int count = buf.getInt();
        if (count != 1) {
            throw new IllegalArgumentException("Invalid Exif field size: "
                    + count + " expected: 1");
        }
        switch (type) {
            case BYTE:
                if (value < 0 || value > 255) {
                    throw new IllegalArgumentException(
                            "Byte overflow: " + value);
                }
                buf.put((byte) value);
                break;
            case SHORT:
                if (value < 0 || value > 65535) {
                    throw new IllegalArgumentException("Short overflow: "
                            + value);
                }
                buf.putShort((short) value);
                break;
            case SLONG:
            case LONG:
                buf.putInt(value);
                break;
            default:
                throw new IllegalArgumentException("Invalid Exif field type: "
                        + type);
        }
    }

    public String getString(Tag tag) {
        ByteBuffer buf = locateTag(tag);
        if (buf == null) {
            return null;
        }
        Type type = Type.forValue(buf.getShort() & 65535);
        if (type != Type.ASCII) {
            throw new IllegalArgumentException("Invalid Exif field type: "
                    + type + " expected: " + Type.ASCII);
        }
        int count = buf.getInt();
        if (count > 4) {
            buf.position(start + buf.getInt());
        }
        byte[] bytes = new byte[count];
        buf.get(bytes);
        while (count > 0 && bytes[count - 1] == 0) {
            --count;
        }
        return utf8(bytes, 0, count);
    }

    public Date getDateTime(Tag tag) {
        String s = getString(tag);
        if (s == null) {
            return null;
        }
        try {
            DateFormat format = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            return format.parse(s);
        } catch (ParseException e) {
            throw new NumberFormatException("Invalid date format: " + s);
        }
    }

    public IFD getIFD(IFDTag tag) {
        ByteBuffer buf = locateTag(tag);
        if (buf == null) {
            return null;
        }
        Type type = Type.forValue(buf.getShort() & 65535);
        if (type != Type.LONG) {
            throw new IllegalArgumentException("Invalid Exif field type: "
                    + type + " expected: " + Type.LONG);
        }
        int count = buf.getInt();
        if (count != 1) {
            throw new IllegalArgumentException("Invalid Exif field size: "
                    + count + " expected: 1");
        }
        return tag.create(data, start, buf.getInt(), order);
    }

    public IFD getNext() {
        ByteBuffer buf = getBuffer();
        int count = 65535 & buf.getShort();
        buf.position(buf.position() + count * 12);
        int next = buf.getInt();
        return next == 0
                ? null
                : newInstance(getClass(), data, start, next, order);
    }

    protected static Map<Integer,Tag> buildTagMap(Tag[]... tagLists) {
        Map<Integer,Tag> result = new HashMap<>();
        for (Tag[] tagList: tagLists) {
            for (Tag t: tagList) {
                result.put(t.getTag(), t);
            }
        }
        return result;
    }

    protected static <T extends IFD> T newInstance(Class<T> clazz, byte[] data,
            int start, int position, ByteOrder order) {
        try {
            Constructor<T> cons = clazz.getConstructor(
                    byte[].class, int.class, int.class, ByteOrder.class);
            return cons.newInstance(data, start, position, order);
        } catch (NoSuchMethodException | SecurityException
                | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException ex) {
            LOG.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex.getMessage());
        }
    }

    private ByteBuffer getBuffer() {
        ByteBuffer buf = ByteBuffer.wrap(data, start, data.length - start);
        buf.order(order);
        buf.position(start + position);
        return buf;
    }

    public ByteBuffer locateTag(Tag tag) {
        ByteBuffer buf = getBuffer();
        int count = 65535 & buf.getShort();
        for (int i = 0; i < count; ++i) {
            int tg = 65535 & buf.getShort();
            if (tg == tag.getTag()) {
                return buf;
            }
            buf.position(buf.position() + 10);
        }
        return null;
    }

    protected static byte[] getBytes(ByteBuffer buf, int count) {
        byte[] result = new byte[count];
        for (int i = 0; i < count; ++i) {
            result[i] = buf.get();
        }
        return result;
    }

    protected static short[] getShorts(ByteBuffer buf, int count) {
        short[] result = new short[count];
        for (int i = 0; i < count; ++i) {
            result[i] = buf.getShort();
        }
        return result;
    }

    protected static int[] getInts(ByteBuffer buf, int count) {
        int[] result = new int[count];
        for (int i = 0; i < count; ++i) {
            result[i] = buf.getInt();
        }
        return result;
    }

    protected static Rational[] getRationals(ByteBuffer buf, int count) {
        Rational[] result = new Rational[count];
        for (int i = 0; i < count; ++i) {
            int n = buf.getInt();
            int d = buf.getInt();
            result[i] = new Rational(n, d);
        }
        return result;
    }
}
