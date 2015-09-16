package org.tastefuljava.gianadda.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QueryBuilder {
    private static final Logger LOG
            = Logger.getLogger(QueryBuilder.class.getName());

    private final StringBuilder buf;
    private boolean hasParms = false;

    public QueryBuilder(String baseUrl) {
        buf = new StringBuilder(baseUrl);
    }

    public QueryBuilder rawParam(String name, String value) {
        if (hasParms) {
            buf.append('&');
        } else {
            buf.append('?');
            hasParms = true;
        }
        buf.append(name);
        buf.append('=');
        buf.append(value);
        return this;
    }

    public QueryBuilder param(String name, String value) {
        return rawParam(name, encode(value));
    }

    @Override
    public String toString() {
        return buf.toString();
    }

    private static String encode(String s) {
        try {
            return s == null ? null
                    : URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException ex) {
            LOG.log(Level.SEVERE, null, ex);
            throw new IllegalArgumentException(ex.getMessage());
        }
    }
}
