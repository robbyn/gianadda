package org.tastefuljava.gianadda.exif;

import org.tastefuljava.gianadda.exif.IFD.Tag;

public interface IFDVisitor {
    public void handleBytes(int index, Tag tag, Type type, byte[] value);
    public void handleShorts(int index, Tag tag, Type type, short[] value);
    public void handleInts(int index, Tag tag, Type type, int[] value);
    public void handleRationals(int index, Tag tag, Type type, Rational[] value);
    public void handleSBytes(int index, Tag tag, Type type, byte[] value);
    public void handleSShorts(int index, Tag tag, Type type, short[] value);
    public void handleSInts(int index, Tag tag, Type type, int[] value);
    public void handleSRationals(int index, Tag tag, Type type, Rational[] value);
    public void handleString(int index, Tag tag, Type type, String value);
    public void handleUndefineds(int index, Tag tag, Type type, byte[] data);
    public void handleFloats(int index, Tag tag, Type type, float[] value);
    public void handleDoubles(int index, Tag tag, Type type, double[] value);
}
