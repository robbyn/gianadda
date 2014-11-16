package org.tastefuljava.gianadda.geo;

import java.util.Date;
import java.util.Objects;

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

    @Override
    public String toString() {
        return "TrackPoint{lat=" + lat + ", lng=" + lng + ", h=" + h
                + ", time=" + time + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.h);
        hash = 29 * hash + Objects.hashCode(this.time);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TrackPoint other = (TrackPoint) obj;
        if (!Objects.equals(this.h, other.h)) {
            return false;
        }
        return Objects.equals(this.time, other.time);
    }
}
