package org.tastefuljava.gianadda;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.tastefuljava.gianadda.exif.Exif;
import org.tastefuljava.gianadda.exif.IFD;
import org.tastefuljava.gianadda.exif.IFDVisitor;
import org.tastefuljava.gianadda.exif.Rational;
import org.tastefuljava.gianadda.exif.Type;

public class DumpExif {
    private static final Logger LOG
            = Logger.getLogger(DumpExif.class.getName());
    public static void main(String[] args) {
        for (String arg: args) {
            dumpExif(arg);
        }
    }

    private static void dumpExif(String arg) {
        try {
            System.out.println();
            System.out.println("--- File " + arg);
            Exif exif = Exif.fromJPEG(new File(arg));
            dumpIFD(exif.getRootIFD());
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private static void dumpIFD(final IFD ifd) {
        final List<IFD.IFDTag> later = new ArrayList<>();
        System.out.println();
        System.out.println("- " + ifd.getClass().getSimpleName());
        ifd.accept(new IFDVisitor() {
            @Override
            public void handleBytes(int index, IFD.Tag tag, Type type,
                    byte[] value) {
                System.out.print(tag + ": " + type + " ");
                dumpValue(value);
                System.out.println();
            }

            @Override
            public void handleShorts(int index, IFD.Tag tag, Type type,
                    short[] value) {
                System.out.print(tag + ": " + type + " ");
                dumpValue(value);
                System.out.println();
            }

            @Override
            public void handleInts(int index, IFD.Tag tag, Type type,
                    int[] value) {
                System.out.print(tag + ": " + type + " ");
                dumpValue(value);
                System.out.println();
                if (tag instanceof IFD.IFDTag && value.length == 1) {
                    later.add((IFD.IFDTag)tag);
                }
            }

            @Override
            public void handleRationals(int index, IFD.Tag tag, Type type,
                    Rational[] value) {
                System.out.print(tag + ": " + type + " ");
                dumpValue(value);
                System.out.println();
            }

            @Override
            public void handleSBytes(int index, IFD.Tag tag, Type type,
                    byte[] value) {
                System.out.print(tag + ": " + type + " ");
                dumpValue(value);
                System.out.println();
            }

            @Override
            public void handleSShorts(int index, IFD.Tag tag, Type type,
                    short[] value) {
                System.out.print(tag + ": " + type + " ");
                dumpValue(value);
                System.out.println();
            }

            @Override
            public void handleSInts(int index, IFD.Tag tag, Type type,
                    int[] value) {
                System.out.print(tag + ": " + type + " ");
                dumpValue(value);
                System.out.println();
            }

            @Override
            public void handleSRationals(int index, IFD.Tag tag, Type type,
                    Rational[] value) {
                System.out.print(tag + ": " + type + " ");
                dumpValue(value);
                System.out.println();
            }

            @Override
            public void handleString(int index, IFD.Tag tag, Type type,
                    String value) {
                System.out.print(tag + ": " + type + " ");
                dumpValue(value);
                System.out.println();
            }

            @Override
            public void handleUndefineds(int index, IFD.Tag tag, Type type,
                    byte[] data) {
                System.out.print(tag + ": " + type + " ");
                dumpValue(data);
                System.out.println();
            }

            @Override
            public void handleFloats(int index, IFD.Tag tag, Type type,
                    float[] value) {
                System.out.print(tag + ": " + type + " ");
                dumpValue(value);
                System.out.println();
            }

            @Override
            public void handleDoubles(int index, IFD.Tag tag, Type type,
                    double[] value) {
                System.out.print(tag + ": " + type + " ");
                dumpValue(value);
                System.out.println();
            }

            private void dumpValue(Object value) {
                if (value == null) {
                    System.out.print("null");
                } else {
                    Class<?> type = value.getClass();
                    if (!type.isArray()) {
                        System.out.print(value);
                    } else {
                        System.out.print("[");
                        int n = Array.getLength(value);
                        int limit = Math.min(n, 5);
                        for (int i = 0; i < limit; ++i) {
                            if (i > 0) {
                                System.out.print(" ");
                            }
                            System.out.print(Array.get(value, i));
                        }
                        if (limit < n) {
                            System.out.print("...");
                        }
                        System.out.print("]");
                    }
                }
            }
        });
        for (IFD.IFDTag tag: later) {
            dumpIFD(ifd.getIFD(tag));
        }
    }
}
