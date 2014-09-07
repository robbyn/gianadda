package org.tastefuljava.gianadda;

import java.io.IOException;
import java.lang.reflect.Array;
import org.tastefuljava.gianadda.exif.IFD;
import org.tastefuljava.gianadda.exif.IFD.Tag;
import org.tastefuljava.gianadda.exif.IFDVisitor;
import org.tastefuljava.gianadda.exif.Rational;
import org.tastefuljava.gianadda.exif.Type;

public enum Dumper implements IFDVisitor {
    INSTANCE;

    public static void visit(IFD ifd) throws IOException {
        if (ifd == null) {
            System.out.println("NULL");
        } else {
            ifd.accept(INSTANCE);
        }
    }

    @Override
    public void handleBytes(int index, Tag tag, Type type, byte[] value) {
        System.out.print(index + " " + tag.getName() + " " + type.getName() + ": ");
        dumpValue(value);
        System.out.println();
    }

    @Override
    public void handleShorts(int index, Tag tag, Type type, short[] value) {
        System.out.print(index + " " + tag.getName() + " " + type.getName() + ": ");
        dumpValue(value);
        System.out.println();
    }

    @Override
    public void handleInts(int index, Tag tag, Type type, int[] value) {
        System.out.print(index + " " + tag.getName() + " " + type.getName() + ": ");
        dumpValue(value);
        System.out.println();
    }

    @Override
    public void handleRationals(int index, Tag tag, Type type, Rational[] value) {
        System.out.print(index + " " + tag.getName() + " " + type.getName() + ": ");
        dumpValue(value);
        System.out.println();
    }

    @Override
    public void handleSBytes(int index, Tag tag, Type type, byte[] value) {
        System.out.print(index + " " + tag.getName() + " " + type.getName() + ": ");
        dumpValue(value);
        System.out.println();
    }

    @Override
    public void handleSShorts(int index, Tag tag, Type type, short[] value) {
        System.out.print(index + " " + tag.getName() + " " + type.getName() + ": ");
        dumpValue(value);
        System.out.println();
    }

    @Override
    public void handleSInts(int index, Tag tag, Type type, int[] value) {
        System.out.print(index + " " + tag.getName() + " " + type.getName() + ": ");
        dumpValue(value);
        System.out.println();
    }

    @Override
    public void handleSRationals(int index, Tag tag, Type type,
            Rational[] value) {
        System.out.print(index + " " + tag.getName() + " " + type.getName() + ": ");
        dumpValue(value);
        System.out.println();
    }

    @Override
    public void handleString(int index, Tag tag, Type type, String value) {
        System.out.print(index + " " + tag.getName() + " " + type.getName() + ": ");
        dumpValue(value);
        System.out.println();
    }

    @Override
    public void handleUndefineds(int index, Tag tag, Type type, byte[] data) {
        System.out.print(index + " " + tag.getName() + " " + type.getName() + ": ");
        dumpValue(data);
        System.out.println();
    }

    @Override
    public void handleFloats(int index, Tag tag, Type type, float[] value) {
        System.out.print(index + " " + tag.getName() + " " + type.getName() + ": ");
        dumpValue(value);
        System.out.println();
    }

    @Override
    public void handleDoubles(int index, Tag tag, Type type, double[] value) {
        System.out.print(index + " " + tag.getName() + " " + type.getName() + ": ");
        dumpValue(value);
        System.out.println();
    }

    private static void dumpValue(Object val) {
        if (val == null) {
            System.out.print("null");
        } else if (val.getClass().isArray()) {
            System.out.print("[");
            int len = Array.getLength(val);
            int limit = Math.min(len, 5);
            for (int i = 0; i < limit; ++i) {
                if (i > 0) {
                    System.out.print(",");
                }
                System.out.print(Array.get(val, i));
            }
            if (limit < len) {
                System.out.print("...");
            }
            System.out.print("] (" + len + ")");
        } else {
            System.out.print(val);
        }
    }
}
