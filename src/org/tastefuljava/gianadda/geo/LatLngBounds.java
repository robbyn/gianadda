package org.tastefuljava.gianadda.geo;

import java.util.Arrays;
import static org.tastefuljava.gianadda.geo.LatLng.normalizeLat;
import static org.tastefuljava.gianadda.geo.LatLng.normalizeLng;

public class LatLngBounds {
    private double southLat;
    private double westLng;
    private double northLat;
    private double eastLng;

    public LatLngBounds() {
    }

    public LatLngBounds(LatLng sw, LatLng ne) {
        if (sw.getLat() > ne.getLat()) {
            throw new IllegalArgumentException(
                    "Invalid bounds: south latitude larger than north");
        }
        init(sw.getLat(), sw.getLng(), ne.getLat(), ne.getLng());
    }

    public LatLngBounds(double south, double west,
            double north, double east) {
        south = normalizeLat(south);
        west = normalizeLng(west);
        north = normalizeLat(north);
        east = normalizeLng(east);
        if (south > north) {
            throw new IllegalArgumentException(
                    "Invalid bounds: south latitude larger than north");
        }
        init(south, west, north, east);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + hashDouble(southLat);
        hash = 37 * hash + hashDouble(westLng);
        hash = 37 * hash + hashDouble(northLat);
        hash = 37 * hash + hashDouble(eastLng);
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
        return southLat == other.southLat
                && westLng == other.westLng
                && northLat == other.northLat
                && eastLng == other.eastLng;
    }

    @Override
    public String toString() {
        return "LatLngBounds{south:" + southLat + ", west:" + westLng
                + ", north:" + northLat + ", eastLng:" + eastLng + '}';
    }

    private LatLngBounds(Builder builder) {
        init(builder.southLat, builder.westLng,
                builder.northLat, builder.eastLng);
    }

    private void init(double southLat, double westLng,
            double northLat, double eastLng) {
        this.southLat = southLat;
        this.northLat = northLat;
        this.westLng = westLng;
        this.eastLng = eastLng;
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
        double east = eastLng;
        if (east < 0) {
            east += 360;
        }
        double west = westLng;
        if (west < 0) {
            west += 360;
        }
        return new LatLng((northLat + southLat)/2,
                normalizeLng((east + west)/2));
    }

    public boolean contains(LatLng pt) {
        return privateContains(pt.getLat(), pt.getLng());
    }

    public boolean contains(double lat, double lng) {
        return privateContains(normalizeLat(lat), normalizeLng(lng));
    }

    private boolean privateContains(double lat, double lng) {
        if (lat > northLat) {
            return false;
        } else if (lat < southLat) {
            return false;
        }
        double east = normalizeLng(lng-eastLng);
        double west = normalizeLng(lng-westLng);
        if (west < 0 && east < 0) {
            return false;
        } else if (west > 0 && east > 0) {
            return false;
        }
        return true;
    }

    public LatLngBounds including(LatLng pt) {
        Builder builder = new Builder();
        builder.privateInclude(northLat, eastLng);
        builder.privateInclude(southLat, westLng);
        builder.include(pt);
        return builder.build();
    }

    private static int hashDouble(double val) {
        long bits = Double.doubleToLongBits(val);
        return (int)(bits ^ bits >>> 32);
    }

    public static class Builder {
        private boolean valid = false;
        private double northLat;
        private double southLat;
        private double eastLng;
        private double westLng;

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
                northLat = southLat = normalizeLat(lat);
                eastLng = westLng = normalizeLng(lng);
                valid = true;
            } else {
                if (lat > northLat) {
                    northLat = lat;
                } else if (lat < southLat) {
                    southLat = lat;
                }
                double east = normalizeLng(lng-eastLng);
                double west = normalizeLng(lng-westLng);
                double angle = eastLng-westLng;
                if (west < 0 && east < 0) {
                    westLng = lng;
                } else if (west > 0 && east < 0) {
                    eastLng = lng;
                }
            }
        }
    }
}
