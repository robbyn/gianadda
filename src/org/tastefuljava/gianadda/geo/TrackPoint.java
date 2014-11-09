package org.tastefuljava.gianadda.geo;

import java.io.Serializable;
import java.util.Date;

public class TrackPoint implements Serializable {
    private static final long serialVersionUID = 2125368054686681737L;

    private double lat;
    private double lng;
    private Double h;
    private Date time;

    public TrackPoint() {
    }

    public TrackPoint(double lat, double lng, Double h, Date time) {
        this.lat = lat;
        this.lng = lng;
        this.h = h;
        this.time = time;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public Double getH() {
        return h;
    }

    public Date getTime() {
        return time;
    }
}
