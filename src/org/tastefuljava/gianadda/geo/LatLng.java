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
            lng = 180 - ((180 - lng) % 360);
            return lng == 180 ? -180 : lng;
        } else {
            return ((lng + 180) % 360) - 180;
        }
    }

    // gives the angle from longituda a to longitude be travelling eastward.
    // result comprise in [0;360[
    public static double lngDiff(double a, double b) {
        double d = b - a;
        if (d >= 0) {
            return d % 360;
        } else {
            d = 360 - (-d) % 360;
            return d < 360 ? d : 0;
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
