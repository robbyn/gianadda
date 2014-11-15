package org.tastefuljava.gianadda.template;

import org.tastefuljava.gianadda.geo.LatLng;
import org.tastefuljava.gianadda.geo.LatLngBounds;

public class GeoTool {
    public LatLng latLng(double lat, double lng) {
        return new LatLng(lat, lng);
    }

    public LatLngBounds bounds(Iterable<LatLng> points) {
        return LatLngBounds.build(points);
    }
}
