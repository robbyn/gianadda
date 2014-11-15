package org.tastefuljava.gianadda.geo;

import java.util.Arrays;
import static org.tastefuljava.gianadda.geo.LatLng.diffLng;
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
        privateInit(sw.getLat(), sw.getLng(), ne.getLat(), ne.getLng());
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
        privateInit(s, w, n, e);
    }

    private LatLngBounds(Builder builder) {
        privateInit(builder.south, builder.west,
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

    public double getSouth() {
        return south;
    }

    public double getWest() {
        return west;
    }

    public double getNorth() {
        return north;
    }

    public double getEast() {
        return east;
    }

    public LatLng getSouthWest() {
        return new LatLng(south, west);
    }

    public LatLng getNorthEast() {
        return new LatLng(north, east);
    }

    public LatLng getCenter() {
        double angle = diffLng(west, east);
        return new LatLng((north + south)/2,
                normalizeLng(west + angle/2));
    }

    public boolean contains(LatLng pt) {
        return privateContains(pt.getLat(), pt.getLng());
    }

    public boolean contains(double lat, double lng) {
        return privateContains(normalizeLat(lat), normalizeLng(lng));
    }

    public boolean intersects(LatLngBounds other) {
        if (other.south > north || other.north < south) {
            return false;
        }
        return diffLng(west, other.west) < diffLng(east, west)
                || diffLng(west, other.east) < diffLng(east, west);
    }

    public LatLngBounds including(LatLng pt) {
        Builder builder = new Builder();
        builder.privateInclude(north, east);
        builder.privateInclude(south, west);
        builder.include(pt);
        return builder.build();
    }

    private void privateInit(double s, double w, double n, double e) {
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
        return diffLng(west, lng) <= diffLng(west, east);
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
                double current = diffLng(west, east);
                double westToLng = diffLng(west, lng);
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
