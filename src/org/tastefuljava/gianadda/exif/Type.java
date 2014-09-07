package org.tastefuljava.gianadda.exif;

import java.util.HashMap;
import java.util.Map;

public enum Type {
    BYTE(1),
    ASCII(2),
    SHORT(3),
    LONG(4),
    RATIONAL(5),
    SBYTE(6),
    UNDEFINED(7),
    SSHORT(8),
    SLONG(9),
    SRATIONAL(10),
    FLOAT(11),
    DOUBLE(12);

    private static final Map<Integer,Type> VALUE_MAP = buildMap();

    private final int value;

    private Type(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name();
    }

    public static Type forValue(int value) {
        return VALUE_MAP.get(value);
    }

    private static Map<Integer,Type> buildMap() {
        Map<Integer,Type> result = new HashMap<>();
        for (Type dt: Type.values()) {
            result.put(dt.getValue(), dt);
        }
        return result;
    }
}
