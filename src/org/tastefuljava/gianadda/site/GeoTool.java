package org.tastefuljava.gianadda.site;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import org.tastefuljava.gianadda.domain.Track;
import org.tastefuljava.gianadda.geo.EarthGeometry;
import org.tastefuljava.gianadda.geo.LatLng;
import org.tastefuljava.gianadda.geo.LatLngBounds;
import org.tastefuljava.gianadda.geo.StaticMap;
import org.tastefuljava.gianadda.geo.StaticMap.Format;
import org.tastefuljava.gianadda.geo.StaticMap.MapType;
import org.tastefuljava.gianadda.geo.TrackPoint;
import org.tastefuljava.gianadda.util.Configuration;

public class GeoTool {
    private final Configuration conf;

    GeoTool(Configuration conf) {
        this.conf = conf;
    }

    public LatLng latLng(double lat, double lng) {
        return new LatLng(lat, lng);
    }

    public LatLngBounds bounds(Iterable<LatLng> points) {
        return LatLngBounds.build(points);
    }

    public boolean writeMapFile(String fileName, Iterable<Track> tracks,
            Iterable<Track> subtracks) throws IOException {
        return this.writeMapFile(fileName, null, tracks, subtracks);
    }

    public boolean writeMapFile(String fileName, LatLngBounds bounds,
            Iterable<Track> tracks) throws IOException {
        return this.writeMapFile(fileName, null, tracks, null);
    }

    public boolean writeMapFile(String fileName, LatLngBounds bounds,
            Iterable<Track> tracks, Iterable<Track> subtracks)
            throws IOException {
        String key = conf.getString("google.browser.key", null);
        if (key == null) {
            return false;
        }
        StaticMap map = new StaticMap()
                .setKey(key)
                .setSize(320, 320)
                .setFormat(Format.PNG8)
                .setMapType(MapType.TERRAIN);
        for (Track track: tracks) {
            TrackPoint[] pts = EarthGeometry.reduce(track.getPoints(), 50);
            map.addPath(new Color(255,0,0,128), 5, pts);
        }
        if (bounds != null) {
            map.setVisible(bounds.getNorthEast(), bounds.getSouthWest());
        } else if (subtracks != null) {
            for (Track track: subtracks) {
                map.setVisible(track.getPoints());
            }
        }
        map.saveToFile(new File(fileName));
        return true;
    }
}
