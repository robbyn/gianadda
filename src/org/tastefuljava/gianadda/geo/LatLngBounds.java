package org.tastefuljava.gianadda.geo;

import static org.tastefuljava.gianadda.geo.LatLng.normalizeLat;
import static org.tastefuljava.gianadda.geo.LatLng.normalizeLng;

public class LatLngBounds {
    private double northLat;
    private double southLat;
    private double eastLng;
    private double westLng;

    public LatLngBounds() {
    }

    private LatLngBounds(Builder builder) {
        northLat = builder.northLat;
        southLat = builder.southLat;
        eastLng = builder.eastLng;
        westLng = builder.westLng;
    }

    public static LatLngBounds build(LatLng... points) {
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
                if (west < 0 && east < 0) {
                    eastLng = lng;
                } else if (west > 0 && east > 0) {
                    westLng = lng;
                }
            }
        }
    }
}
