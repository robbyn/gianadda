package org.tastefuljava.gallerygen.util;

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuration {
    private final Properties props = new Properties();
    private final Configuration link;

    public Configuration() {
        this(null, null);
    }

    public Configuration(Properties props) {
        this(props, null);
    }

    public Configuration(Properties props, Configuration link) {
        if (props != null) {
            this.props.putAll(props);
        }
        this.link = link;
    }

    public static Configuration load(File file) throws IOException {
        return load(file, null);
    }

    public static Configuration load(File file, Configuration link)
            throws IOException {
        Properties props = new Properties();
        if (file.isFile()) {
            try (InputStream in = new FileInputStream(file)) {
                props.load(in);
            }
        }
        return new Configuration(props, link);
    }

    public String getString(String name, String def) {
        String s = props.getProperty(name, null);
        if (s != null) {
            return s;
        } else if (link == null) {
            return System.getProperty(name, def);
        } else {
            return link.getString(name, def);
        }
    }

    public int getInt(String name, int def) {
        String s = getString(name, null);
        return Util.isBlank(s) ? def : Integer.parseInt(s);
    }

    public Dimension getDimension(String name, Dimension def) {
        String s = getString(name, null);
        return Util.isBlank(s) ? def : Util.parseDimension(s);
    }
}
