package org.tastefuljava.gianadda.exif;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Exif {
    // JPEG markers
    private static final int SOI = 0xFFD8;
    private static final int EOI = 0xFFD9;
    private static final int APP1 = 0xFFE1;

    private static final String EXIF_ID_CODE = "Exif\0\0";
    private static final String LITTLE_ENDIAN = "II";
    private static final String BIG_ENDIAN = "MM";
    private static final int TIFF_HEADER_START = 6;

    private final byte data[];
    private ByteOrder byteOrder;
    private int firstIFDPosition;

    public static Exif fromJPEG(DataInput din) throws IOException {
        int soi = din.readShort() & 0xFFFF;
        if (soi != SOI) {
            throw new IOException("This is not a jpeg file");
        }
        int marker = din.readShort() & 0xFFFF;
        while (marker != APP1 && marker != EOI) {
            if ((marker & 0xFF00) != 0xFF00) {
                throw new IOException("Invalid marker");
            }
            int toskip = (din.readShort() & 0xFFFF)-2;
            if (din.skipBytes(toskip) != toskip) {
                throw new IOException("Invalid length");
            }
            marker = din.readShort() & 0xFFFF;
        }
        if (marker == EOI) {
            throw new IOException("No Exif data");
        }
        int length = (din.readShort() & 0xFFFF)-2;
        byte buf[] = new byte[length];
        din.readFully(buf);
        return new Exif(buf);
    }

    public static Exif fromJPEG(InputStream in) throws IOException {
        DataInput din = new DataInputStream(in);
        return fromJPEG(din);
    }

    public static Exif fromJPEG(File file) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            return fromJPEG(in);
        }
    }

    public static Exif fromJPEG(byte[] data) throws IOException {
        try (InputStream in = new ByteArrayInputStream(data)) {
            return fromJPEG(in);
        }
    }

    public Exif(byte data[]) throws IOException {
        this.data = data;
        ByteBuffer buf = ByteBuffer.wrap(data);
        String s = getAscii(buf, 6);
        if (!s.equals(EXIF_ID_CODE)) {
            throw new IOException("Invalid exif id code " + s);
        }
        s = getAscii(buf, 2);
        if (s.equals(LITTLE_ENDIAN)) {
            byteOrder = ByteOrder.LITTLE_ENDIAN;
        } else if (s.equals(BIG_ENDIAN)) {
            byteOrder = ByteOrder.BIG_ENDIAN;
        } else {
            throw new IOException("Invalid byte order " + s);
        }
        buf.order(byteOrder);
        if (buf.getShort() != 0x002A) {
            throw new IOException("Invalid TIFF header: expecting 0x002A");
        }
        firstIFDPosition = buf.getInt();
    }

    public RootIFD getRootIFD() {
        return new RootIFD(data, TIFF_HEADER_START, firstIFDPosition,
                byteOrder);
    }

    private static String getAscii(ByteBuffer b, int length) throws UnsupportedEncodingException {
        return getString(b, length, "ASCII");
    }

    private static String getString(ByteBuffer b, int length, String encoding)
            throws UnsupportedEncodingException {
        byte bytes[] = new byte[length];
        b.get(bytes, 0, length);
        return new String(bytes, 0, length, encoding);
    }
}
