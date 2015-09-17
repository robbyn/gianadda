package org.tastefuljava.gianadda.geo;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import org.tastefuljava.gianadda.util.Files;
import org.tastefuljava.gianadda.util.QueryBuilder;
import org.tastefuljava.gianadda.util.Util;

public class StaticMap {
    private final QueryBuilder qry = new QueryBuilder(
            "https://maps.googleapis.com/maps/api/staticmap");
    private LatLngBounds.Builder visibleBoundsBuilder;


    public static enum MapType {
        ROADMAP("roadmap"), SATELLITE("satellite"), TERRAIN("terrain"),
        HYBRID("hybrid");

        private final String label;

        private MapType(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    };

    public static enum Format {
        PNG8("png8"), PNG32("png32"), GIF("gif"), JPG("jpg"),
        JPG_BASELINE("jpg-baseline");

        private final String label;

        private Format(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public StaticMap setKey(String key) {
        if (key != null) {
            qry.param("key", key);
        }
        return this;
    }

    public StaticMap setSize(int width, int height) {
        qry.param("size", width + "x" + height);
        return this;
    }

    public StaticMap setMapType(MapType type) {
        if (type != null) {
            qry.param("maptype", type.toString());
        }
        return this;
    }

    public StaticMap setFormat(Format format) {
        if (format != null) {
            qry.param("format", format.toString());
        }
        return this;
    }

    public StaticMap addPath(Color color, int weight, TrackPoint... pts) {
        qry.rawParam("path", "color:0x" + hex(color)
                + "|weight:" + weight
                + "|enc:"+GMapEncoder.encodePoints(pts));
        return this;
    }

    public StaticMap setVisible(LatLng... pts) {
        if (visibleBoundsBuilder == null) {
            visibleBoundsBuilder = new LatLngBounds.Builder();
        }
        for (LatLng pt: pts) {
            visibleBoundsBuilder.include(pt);
        }
        return this;
    }

    @Override
    public String toString() {
        appendVisibleBounds();
        return qry.toString();
    }

    public URL toURL() throws MalformedURLException {
        return new URL(toString());
    }

    public void saveToFile(File file) throws IOException {
        try (InputStream in = toURL().openStream()) {
            Files.save(in, file);
        }
    }

    private static String hex(Color color) {
        return Util.hex(color.getRed(), 2)
                + Util.hex(color.getGreen(), 2)
                + Util.hex(color.getBlue(), 2)
                + Util.hex(color.getAlpha(), 2);
    }

    private void appendVisibleBounds() {
        if (visibleBoundsBuilder != null) {
            if (visibleBoundsBuilder.isValid()) {
                LatLngBounds bounds = visibleBoundsBuilder.build();
                qry.rawParam("visible", pt2str(bounds.getNorthEast())
                        + '|' + pt2str(bounds.getSouthWest()));
            }
            visibleBoundsBuilder = null;
        }
    }

    private String pt2str(LatLng pt) {
        return Util.formatNumber(pt.getLat(), "0.######")
                + ',' + Util.formatNumber(pt.getLng(), "0.######");
    }
}
