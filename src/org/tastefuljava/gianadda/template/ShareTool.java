package org.tastefuljava.gianadda.template;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShareTool {
    private static final Logger LOG = Logger.getLogger(ShareTool.class.getName());
    private static final Pattern VAR_PATTERN
            = Pattern.compile("\\{([-_A-Za-z0-9]+)\\}");
    private final Properties services = loadServices();

    public long getTimestamp() {
        return System.currentTimeMillis()/1000;
    }

    public Set<String> getServices() {
        return services.stringPropertyNames();
    }

    public String makeUrl(String service, Map<String,String> vars)
            throws IOException {
        try {
            String templ = services.getProperty(service);
            Matcher matcher = VAR_PATTERN.matcher(templ);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String var = matcher.group(1);
                String val = vars.get(var);
                matcher.appendReplacement(sb, val == null 
                        ? var : URLEncoder.encode(val, "UTF-8"));
            }
            matcher.appendTail(sb);
            return sb.toString();
        } catch (UnsupportedEncodingException ex) {
            LOG.log(Level.SEVERE, "Error encoding URL", ex);
            throw new IOException(ex.getMessage());
        }
    }

    private static Properties loadServices() {
        try (InputStream in = ShareTool.class.getResourceAsStream(
                "services.properties")) {
            Properties props = new Properties();
            props.load(in);
            return props;
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex.getMessage());
        }
    }
}
