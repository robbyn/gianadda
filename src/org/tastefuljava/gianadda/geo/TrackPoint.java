package org.tastefuljava.gianadda.geo;

import java.util.Date;

public class TrackPoint extends LatLng {
    private final Double h;
    private final Date time;

    public TrackPoint(double lat, double lng, Double h, Date time) {
        super(lat, lng);
        this.h = h;
        this.time = time;
    }

    public Double getElevation() {
        return h;
    }

    public double getH() {
        return h == null ? 0 : h;
    }

    public Date getTime() {
        return time;
    }
}
