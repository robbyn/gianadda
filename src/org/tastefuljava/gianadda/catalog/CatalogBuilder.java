package org.tastefuljava.gianadda.catalog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.h2.tools.RunScript;
import org.tastefuljava.gianadda.util.Configuration;
import org.tastefuljava.jedo.mapping.Mapper;
import org.tastefuljava.jedo.mapping.MappingFileReader;

class CatalogBuilder {
    private static final Logger LOG
            = Logger.getLogger(CatalogBuilder.class.getName());

    private static final String DEFAULT_FILE = "default-conf.properties";
    private static final String CONF_FILE = "catalog-conf.properties";
    private static final String PROP_FORMAT_VERSION = "format-version";
    private static final int CURRENT_VERSION = 2;

    private final File dir;
    private final Configuration link;
    private final Properties props = new Properties();

    CatalogBuilder(File dir, Configuration link) throws IOException {
        this.dir = dir;
        this.link = link;
    }

    Catalog open() throws IOException {
        requireDir();
        loadProps();
        props.put("jdbc.url", jdbcURL());
        saveProps();
        if (getFormatVersion() == 0) {
            createDb();
        } else {
            upgradeDb();
        }
        Configuration conf = new Configuration(props, link);
        MappingFileReader reader = new MappingFileReader();
        reader.load(CatalogBuilder.class.getResource("mapping.xml"));
        Mapper mapper = reader.getMapper();
        return new Catalog(dir, conf, mapper);
    }

    private static InputStream getResourceAsStream(String name) {
        return CatalogBuilder.class.getResourceAsStream(name);
    }

    private String jdbcURL() {
        File db = new File(dir, "catalog");
        return "jdbc:h2:" + db.getAbsolutePath();
    }

    private void requireDir() throws IOException {
        if (!dir.isDirectory() && !dir.mkdirs()) {
            throw new IOException("Could not create directory " + dir);
        }
    }

    private void loadProps() throws IOException {
        props.clear();
        try (InputStream in = getResourceAsStream(DEFAULT_FILE)) {
            props.load(in);
        }
        File file = new File(dir, CONF_FILE);
        if (file.isFile()) {
            try (InputStream in = new FileInputStream(file)) {
                props.load(in);
            }
        }
    }

    private void saveProps() throws IOException {
        File file = new File(dir, CONF_FILE);
        try (OutputStream out = new FileOutputStream(file)) {
            props.store(out, "schema version info");
        }
    }

    private int getFormatVersion() {
        return Integer.parseInt(props.getProperty(PROP_FORMAT_VERSION, "0"));
    }

    private void saveFormatVersion(int version) throws IOException {
        props.setProperty(PROP_FORMAT_VERSION, Integer.toString(version));
        saveProps();
    }

    private Connection openConnection()
            throws ClassNotFoundException, SQLException {
        Class.forName(props.getProperty("jdbc.driver"));
        return DriverManager.getConnection(props.getProperty("jdbc.url"),
                props.getProperty("jdbc.username"),
                props.getProperty("jdbc.password"));
    }

    private void createDb() throws IOException {
        try {
            try (Connection conn = openConnection()) {
                runScript(conn, "scripts/schema.sql");
            }
            saveFormatVersion(CURRENT_VERSION);
        } catch (SQLException | ClassNotFoundException ex) {
            LOG.log(Level.SEVERE, "Error creating database in " + dir, ex);
            throw new IOException(ex.getMessage());
        }
    }

    private void upgradeDb() throws IOException {
        try {
            int version = getFormatVersion();
            if (version < CURRENT_VERSION) {
                LOG.log(Level.INFO, "Upgrading database from V{0} to V{1}",
                        new Object[] {version, CURRENT_VERSION});
                try (Connection conn = openConnection()) {
                    while (version < CURRENT_VERSION) {
                        ++version;
                        runScript(conn, "scripts/upgrade-" + version + ".sql");
                        saveFormatVersion(version);
                    }
                }
            }
        } catch (SQLException | ClassNotFoundException ex) {
            LOG.log(Level.SEVERE, "Error upgrading database in " + dir, ex);
            throw new IOException(ex.getMessage());
        }
    }

    private static void runScript(Connection conn, String name)
            throws IOException, SQLException {
        try (InputStream stream = Catalog.class.getResourceAsStream(name);
                Reader in = new InputStreamReader(stream, "UTF-8")) {
            LOG.log(Level.INFO, "Executing script {0}", name);
            RunScript.execute(conn, in);
        }
    }
}
