package org.tastefuljava.gianadda.site;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.tastefuljava.gianadda.util.Configuration;
import org.tastefuljava.gianadda.util.Util;

public class FormatTool {
    private final Configuration conf;

    FormatTool(Configuration conf) {
        this.conf = conf;
    }

    public String formatNumber(Double value, String format) {
        return Util.formatNumber(value, format);
    }

    public String formatDate(Date date, String pattern) {
        DateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }

    public String formatGMT(Date date, String pattern) {
        DateFormat format = new SimpleDateFormat(pattern);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format.format(date);
    }
}
