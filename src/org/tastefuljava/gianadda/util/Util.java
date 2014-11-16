package org.tastefuljava.gianadda.util;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
    private static final Logger LOG = Logger.getLogger(Util.class.getName());

    public static final String XSD_DATETIME_FORMAT
            = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    private static final String NUM = "[+-]?(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)";
    private static final Pattern DIMENSION_PATTERN
            = Pattern.compile("^\\s*(" + NUM + ")\\s*,\\s*(" + NUM + ")\\s*$");
    private static final String DEFAULT_NUMBER_FORMAT = "0.####";

    private Util() {
        // Private constructor to prevent instanciation
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().length() == 0;
    }

    public static String formatNumber(Number num, String pattern) {
        return num == null ? null : getDecimalFormat(pattern).format(num);
    }

    public static String formatNumber(Number num) {
        return formatNumber(num, DEFAULT_NUMBER_FORMAT);
    }

    public static int parseInt(String s) {
        return isBlank(s) ? 0 : Integer.parseInt(s);
    }

    public static String formatInt(int value) {
        return Integer.toString(value);
    }

    public static double parseDouble(String s) {
        return isBlank(s) ? 0 : Double.parseDouble(s);
    }

    public static double parseDouble(String s, String pattern) {
        try {
            return isBlank(s)
                    ? 0 : getDecimalFormat(pattern).parse(s).doubleValue();
        } catch (ParseException ex) {
            LOG.log(Level.SEVERE, null, ex);
            throw new IllegalArgumentException(
                    "Invalid number format: " + s);
        }
    }

    public static <T extends Dimension2D> T parseDimension(Class<T> clazz,
            String s) {
        try {
            if (isBlank(s)) {
                return null;
            }
            Matcher matcher = DIMENSION_PATTERN.matcher(s);
            if (!matcher.matches()) {
                throw new IllegalArgumentException(
                        "Invalid dimension format: [" + s + "]");
            }
            T result = clazz.newInstance();
            result.setSize(parseDouble(matcher.group(1)),
                    parseDouble(matcher.group(2)));
            return result;
        } catch (InstantiationException | IllegalAccessException ex) {
            LOG.log(Level.SEVERE, null, ex);
            throw new IllegalArgumentException(
                    "Cannot instanciate " + clazz.getName());
        }
    }

    public static Dimension parseDimension(String s) {
        return parseDimension(Dimension.class, s);
    }

    public static String formatDimension(Dimension2D dim) {
        if (dim == null) {
            return null;
        }
        return formatNumber(dim.getWidth()) + ","
                + formatNumber(dim.getHeight());
    }

    public static Date parseXsdDateTime(String s) {
        return parseDate(s, XSD_DATETIME_FORMAT);
    }

    public static String formatXsdDateTime(Date d) {
        return formatDate(d, XSD_DATETIME_FORMAT);
    }

    public static String formatXsdDateTime(Date d, TimeZone tz) {
        return getDateFormat(XSD_DATETIME_FORMAT, tz).format(d);
    }

    public static Date parseDate(String s, String pattern) {
        if (isBlank(s)) {
            return null;
        }
        try {
            return getDateFormat(pattern).parse(s);
        } catch (ParseException ex) {
            LOG.log(Level.SEVERE, null, ex);
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    public static String formatDate(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        return getDateFormat(pattern).format(date);
    }

    public static DateFormat getDateFormat(String pattern) {
        return getDateFormat(pattern, TimeZone.getDefault());
    }

    public static DateFormat getDateFormat(String pattern, TimeZone tz) {
        DateFormat result = new SimpleDateFormat(pattern);
        result.setTimeZone(tz);
        return result;
    }

    public static DecimalFormat getDecimalFormat(String pattern) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        DecimalFormat format = new DecimalFormat(pattern);
        format.setDecimalFormatSymbols(symbols);
        return format;
    }

    public static final String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException ex) {
            // VERY unlikely
            LOG.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex.getMessage());
        }
    }
}
