package org.tastefuljava.gianadda.geo;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ElevationService {
    private static final Logger LOG
            = Logger.getLogger(ElevationService.class.getName());

    private static final int CONNECT_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 10000;
    private static final int MAX_COUNT = 100;
    private static final String SERVICE_URL
            = "http://maps.google.com/maps/api/elevation/xml";

    public static void getElevations(TrackPoint[] results)
            throws IOException {
        int pos = 0;
        while (pos+MAX_COUNT <= results.length) {
            getElevations(results, pos, MAX_COUNT);
            pos += MAX_COUNT;
        }
        if (pos < results.length) {
            getElevations(results, pos, results.length-pos);
        }
    }

    private static void getElevations(TrackPoint trkpts[], int start,
            int count) throws IOException {
        for (int i = 0; i < 20; ++i) {
            String status = getElevations1(trkpts, start, count);
            switch (status) {
                case "OK":
                    return;
                case "OVER_QUERY_LIMIT":
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        LOG.log(Level.SEVERE, null, ex);
                    }   break;
                default:
                    throw new IOException("Error getting elevations: " + status);
            }
        }
        throw new IOException("Error getting elevations: too many retries");
    }
        
    private static String getElevations1(TrackPoint trkpts[], int start,
            int count) throws IOException {
        String enc = GMapEncoder.encodePoints(trkpts, start, count);
        String url = SERVICE_URL + "?locations=enc:"
                + URLEncoder.encode(enc, "ASCII")
                + "&sensor=false";
        URL uploadUrl = new URL(url);
        HttpURLConnection con = (HttpURLConnection)uploadUrl.openConnection();
        try {
            con.setRequestMethod("GET");
            con.addRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            con.setConnectTimeout(CONNECT_TIMEOUT);
            con.setReadTimeout(READ_TIMEOUT);
            int code = con.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                String msg = con.getResponseMessage();
                throw new IOException("HTTP Error " + code + ": " + msg);
            }
            try (InputStream stream = con.getInputStream()) {
                return parseResults(trkpts, start, stream);
            }
        } catch (ParserConfigurationException | SAXException e) {
            LOG.log(Level.SEVERE, "Error parsing results", e);
            throw new IOException(e.getMessage());
        } finally {
            con.disconnect();
        }
    }

    private static String parseResults(TrackPoint trkpts[], int start,
            InputStream in)
            throws IOException, SAXException, ParserConfigurationException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        SAXParser parser = factory.newSAXParser();

        Handler handler = new Handler(trkpts, start);
        parser.parse(in, handler);
        return handler.status;
    }

    private static class Handler extends DefaultHandler {
        private final TrackPoint[] trkpts;
        private int current = 0;
        private final StringBuilder buf = new StringBuilder();
        private String status = "";

        private Handler(TrackPoint[] trkpts, int start) {
            this.trkpts = trkpts;
            current = start;
        }

        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            buf.setLength(0);
        }

        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            switch (localName) {
                case "elevation":
                    double ele = Double.parseDouble(buf.toString());
                    TrackPoint src = trkpts[current];
                    TrackPoint dst = new TrackPoint(src.getLat(), src.getLng(),
                            ele, src.getTime());
                    trkpts[current] = dst;
                    ++current;
                    break;
                case "status":
                    status = buf.toString();
                    if (!status.equals("OK")) {
                        LOG.log(Level.SEVERE, "Status: {0}", status);
                    }
                    break;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            buf.append(ch, start, length);
        }
    }
}
