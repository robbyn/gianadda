package org.tastefuljava.gianadda.exif;

import java.nio.ByteOrder;
import java.util.Map;
import static org.tastefuljava.gianadda.exif.IFD.buildTagMap;

public class GPSIFD extends IFD {
    private static final Map<Integer,IFD.Tag> TAG_MAP
            = buildTagMap(Tag.values());

    public static enum Tag implements IFD.Tag {
        GPSVersionID(0x0000),
        GPSLatitudeRef(0x0001),
        GPSLatitude(0x0002),
        GPSLongitudeRef(0x0003),
        GPSLongitude(0x0004),
        GPSAltitudeRef(0x0005),
        GPSAltitude(0x0006),
        GPSTimeStamp(0x0007),
        GPSSatellites(0x0008),
        GPSStatus(0x0009),
        GPSMeasureMode(0x000a),
        GPSDOP(0x000b),
        GPSSpeedRef(0x000c),
        GPSSpeed(0x000d),
        GPSTrackRef(0x000e),
        GPSTrack(0x000f),
        GPSImgDirectionRef(0x0010),
        GPSImgDirection(0x0011),
        GPSMapDatum(0x0012),
        GPSDestLatitudeRef(0x0013),
        GPSDestLatitude(0x0014),
        GPSDestLongitudeRef(0x0015),
        GPSDestLongitude(0x0016),
        GPSDestBearingRef(0x0017),
        GPSDestBearing(0x0018),
        GPSDestDistanceRef(0x0019),
        GPSDestDistance(0x001a),
        GPSProcessingMethod(0x001b),
        GPSAreaInformation(0x001c),
        GPSDateStamp(0x001d),
        GPSDifferential(0x001e);

        private final int tag;

        private Tag(int tag) {
            this.tag = tag;
        }

        @Override
        public int getTag() {
            return tag;
        }

        @Override
        public String getName() {
            return name();
        }
    }

    public GPSIFD(byte[] data, int start, int position, ByteOrder order) {
        super(data, start, position, order);
    }

    public Double getLatitude() {
        Rational[] rvals = getRationals(Tag.GPSLatitude, null);
        if (rvals == null || rvals.length == 0) {
            return null;
        }
        String ref = getString(Tag.GPSLatitudeRef);
        if (ref == null) {
            return null;
        }
        double value = toDegrees(rvals);
        switch (ref) {
            case "N":
                return value;
            case "S":
                return -value;
            default:
                throw new RuntimeException("Invalid latitude reference");
        }
    }

    public Double getLongitude() {
        Rational[] rvals = getRationals(Tag.GPSLongitude, null);
        if (rvals == null || rvals.length == 0) {
            return null;
        }
        String ref = getString(Tag.GPSLongitudeRef);
        if (ref == null) {
            return null;
        }
        double value = toDegrees(rvals);
        switch (ref) {
            case "E":
                return value;
            case "W":
                return -value;
            default:
                throw new RuntimeException("Invalid latitude reference");
        }
    }

    public Double getAltitude() {
        int ref = getInt(Tag.GPSAltitudeRef, 0);
        Rational value = getRational(Tag.GPSAltitude, null);
        if (value == null) {
            return null;
        }
        double result = value.doubleValue();
        switch (ref) {
            case 0:
                return result;
            case 1:
                return -result;
            default:
                throw new RuntimeException("Invalid altitude reference " + ref);
        }
    }

    @Override
    protected IFD.Tag getTag(int tag) {
        return TAG_MAP.get(tag);
    }

    private static double toDegrees(Number[] values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException(
                    "toDegrees requires at least one value");
        }
        double result = values[0].doubleValue();
        if (values.length >= 2) {
            result += values[1].doubleValue()/60;
            if (values.length >= 3) {
                result += values[2].doubleValue()/3600;
            }
        }
        return result;
    }
}
