package org.tastefuljava.gianadda.site;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.tastefuljava.gianadda.domain.Track;
import org.tastefuljava.gianadda.geo.EarthGeometry;
import org.tastefuljava.gianadda.geo.GMapEncoder;
import org.tastefuljava.gianadda.geo.LatLng;
import org.tastefuljava.gianadda.geo.LatLngBounds;
import org.tastefuljava.gianadda.geo.TrackPoint;
import org.tastefuljava.gianadda.util.Configuration;
import org.tastefuljava.gianadda.util.Files;
import org.tastefuljava.gianadda.util.QueryBuilder;

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

    public boolean writeMapFile(String fileName, Iterable<Track> tracks) throws IOException {
        String key = conf.getString("google.api.key", null);
        if (key != null) {
            QueryBuilder qry = new QueryBuilder(
                    "https://maps.googleapis.com/maps/api/staticmap");
            qry.param("size", "320x320");
            for (Track track: tracks) {
                TrackPoint[] pts = EarthGeometry.reduce(track.getPoints(), 50);
                qry.rawParam("path", "color:0xFF000080"
                        + "|weight:5"
                        + "|enc:"+GMapEncoder.encodePoints(pts));
            }
            qry.param("key", key);
            qry.param("format", "png8");
            qry.param("maptype", "terrain");
            URL url = new URL(qry.toString());
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            try {
                con.setRequestMethod("GET");
                int code = con.getResponseCode();
                if (code != HttpURLConnection.HTTP_OK) {
                    String msg = con.getResponseMessage();
                    throw new IOException("HTTP Error " + code + ": " + msg);
                }
                try (InputStream stream = con.getInputStream()) {
                    Files.save(stream, new File(fileName));
                    return true;
                }
            } finally {
                con.disconnect();
            }
        }
        return false;
    }
}
