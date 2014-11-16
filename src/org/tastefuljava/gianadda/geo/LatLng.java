package org.tastefuljava.gianadda.geo;

import org.tastefuljava.gianadda.util.Util;

public class LatLng {
    protected final double lat;
    protected final double lng;

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
            lng = 180 - (180 - lng)%360;
            return lng == 180 ? -180 : lng;
        } else {
            return (lng + 180)%360 - 180;
        }
    }

    // gives the angle from longituda a to longitude b, moving eastward.
    // result comprise in [0;360[
    public static double diffLng(double a, double b) {
        double d = b - a;
        if (d >= 0) {
            return d%360;
        } else {
            d = 360 - (-d)%360;
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

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Util.hashDouble(lat);
        hash = 89 * hash + Util.hashDouble(lng);
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
        final LatLng other = (LatLng) obj;
        if (lat != other.lat) {
            return false;
        }
        if (lng != other.lng) {
            return false;
        }
        return true;
    }
}
