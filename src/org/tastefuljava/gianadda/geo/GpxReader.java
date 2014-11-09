package org.tastefuljava.gianadda.geo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class GpxReader extends DefaultHandler {
    private static final DateFormat TIME_FORMAT
            = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private final List<TrackPoint> track = new ArrayList<>();
    private final StringBuffer buf = new StringBuffer();
    private double lat;
    private double lon;
    private Double ele;
    private Date time;

    public static TrackPoint[] readTrack(InputStream in) throws IOException {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(true);
            SAXParser parser = factory.newSAXParser();
            GpxReader reader = new GpxReader();
            parser.parse(in, reader);
            return reader.getTrack();
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException(e.getMessage());
        }
    }

    public static TrackPoint[] readTrack(File file) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            return readTrack(in);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        buf.setLength(0);
        switch (qName) {
            case "trkpt":
                lat = Double.parseDouble(attributes.getValue("lat"));
                lon = Double.parseDouble(attributes.getValue("lon"));
                ele = null;
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        switch (qName) {
            case "trkpt":
                track.add(new TrackPoint(lat, lon, ele, time));
                break;
            case "ele":
                ele = Double.parseDouble(buf.toString());
                break;
            case "":
                try {
                    time = TIME_FORMAT.parse(buf.toString());
                } catch (ParseException e) {
                    throw new SAXException("Invalid time " + buf.toString());
                }
                break;
        }
    }

    @Override
    public void characters(char[] chars, int start, int length)
            throws SAXException {
        buf.append(chars, start, length);
    }

    private TrackPoint[] getTrack() {
        return track.toArray(new TrackPoint[track.size()]);
    }
}
