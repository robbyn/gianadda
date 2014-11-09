package org.tastefuljava.gianadda.geo;

public class LatLng {
    private final double lat;
    private final double lng;

    public static double normalizeLat(double lat) {
        if (lat < -90) {
            return -90;
        } else if (lat > 90) {
            return 90;
        } else {
            return lat;
        }
    }

    public static double normalizeLng(double lng) {
        if (lng < -180) {
            return 180 - ((180 - lng) % 360);
        } else {
            return ((lng + 180) % 360) - 180;
        }
    }

    public LatLng(double lat, double lng) {
        this.lat = normalizeLat(lat);
        this.lng = normalizeLng(lng);
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    @Override
    public String toString() {
        return "LatLng{" + "lat=" + lat + ", lng=" + lng + '}';
    }
}
