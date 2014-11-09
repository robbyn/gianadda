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

    public Double getH() {
        return h;
    }

    public Date getTime() {
        return time;
    }
}
