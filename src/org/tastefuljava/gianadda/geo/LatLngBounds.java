package org.tastefuljava.gianadda.geo;

import java.util.Arrays;
import static org.tastefuljava.gianadda.geo.LatLng.lngDiff;
import static org.tastefuljava.gianadda.geo.LatLng.normalizeLat;
import static org.tastefuljava.gianadda.geo.LatLng.normalizeLng;

public class LatLngBounds {
    private double south;
    private double west;
    private double north;
    private double east;

    public LatLngBounds() {
    }

    public LatLngBounds(LatLng sw, LatLng ne) {
        if (sw.getLat() > ne.getLat()) {
            throw new IllegalArgumentException(
                    "Invalid bounds: south latitude larger than north");
        }
        init(sw.getLat(), sw.getLng(), ne.getLat(), ne.getLng());
    }

    public LatLngBounds(double s, double w, double n, double e) {
        s = normalizeLat(s);
        w = normalizeLng(w);
        n = normalizeLat(n);
        e = normalizeLng(e);
        if (s > n) {
            throw new IllegalArgumentException(
                    "Invalid bounds: south latitude larger than north");
        }
        init(s, w, n, e);
    }

    private LatLngBounds(Builder builder) {
        init(builder.south, builder.west,
                builder.north, builder.east);
    }

    public static LatLngBounds build(LatLng... points) {
        return build(Arrays.asList(points));
    }

    public static LatLngBounds build(Iterable<LatLng> points) {
        Builder builder = new Builder();
        for (LatLng pt: points) {
            builder.include(pt);
        }
        return builder.build();
    }

    public LatLng getCenter() {
        double angle = lngDiff(west, east);
        return new LatLng((north + south)/2,
                normalizeLng(west + angle/2));
    }

    public boolean contains(LatLng pt) {
        return privateContains(pt.getLat(), pt.getLng());
    }

    public boolean contains(double lat, double lng) {
        return privateContains(normalizeLat(lat), normalizeLng(lng));
    }

    public LatLngBounds including(LatLng pt) {
        Builder builder = new Builder();
        builder.privateInclude(north, east);
        builder.privateInclude(south, west);
        builder.include(pt);
        return builder.build();
    }

    private void init(double s, double w, double n, double e) {
        south = s;
        north = n;
        west = w;
        east = e;
    }

    private boolean privateContains(double lat, double lng) {
        if (lat > north) {
            return false;
        } else if (lat < south) {
            return false;
        }
        return lngDiff(west, lng) <= lngDiff(west, east);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + hashDouble(south);
        hash = 37 * hash + hashDouble(west);
        hash = 37 * hash + hashDouble(north);
        hash = 37 * hash + hashDouble(east);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LatLngBounds other = (LatLngBounds) obj;
        return south == other.south
                && west == other.west
                && north == other.north
                && east == other.east;
    }

    @Override
    public String toString() {
        return "LatLngBounds{south:" + south + ", west:" + west
                + ", north:" + north + ", east:" + east + '}';
    }

    private static int hashDouble(double val) {
        long bits = Double.doubleToLongBits(val);
        return (int)(bits ^ bits >>> 32);
    }

    public static class Builder {
        private boolean valid = false;
        private double north;
        private double south;
        private double east;
        private double west;

        public boolean isValid() {
            return valid;
        }

        public LatLngBounds build() {
            if (!valid) {
                throw new IllegalStateException("No points were included");
            }
            return new LatLngBounds(this);
        }

        public void include(LatLng pt) {
            privateInclude(pt.getLat(), pt.getLng());
        }

        public void include(double lat, double lng) {
            privateInclude(normalizeLat(lat), normalizeLng(lng));
        }

        private void privateInclude(double lat, double lng) {
            if (!valid) {
                north = south = normalizeLat(lat);
                east = west = normalizeLng(lng);
                valid = true;
            } else {
                if (lat > north) {
                    north = lat;
                } else if (lat < south) {
                    south = lat;
                }
                double current = lngDiff(west, east);
                double westToLng = lngDiff(west, lng);
                if (westToLng <= current) {
                    // lng is inside the bounds
                } else if (360 - westToLng < westToLng - current) {
                    this.west = lng;
                } else {
                    east = lng;
                }
            }
        }
    }
}
