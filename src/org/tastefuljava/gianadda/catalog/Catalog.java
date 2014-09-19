package org.tastefuljava.gianadda.catalog;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.tastefuljava.gianadda.util.Configuration;
import org.tastefuljava.jedo.Session;
import org.tastefuljava.jedo.mapping.Mapper;

public class Catalog implements Closeable {
    private final File dir;
    private final Configuration conf;
    private final Mapper mapper;

    public static Catalog open(File dir, Configuration link) throws IOException {
        return new CatalogBuilder(dir, link).open();
    }

    Catalog(File dir, Configuration conf, Mapper mapper) {
        this.dir = dir;
        this.conf = conf;
        this.mapper = mapper;
    }

    @Override
    public void close() {
        // nothing
    }

    public File getDirectory() {
        return dir;
    }

    public Configuration getConf() {
        return conf;
    }

    public CatalogSession openSession()
            throws SQLException, ClassNotFoundException {
        boolean ok = false;
        Connection cnt = openConnection();
        try {
            cnt.setAutoCommit(false);
            Session session = new Session(cnt, mapper);
            try {
                CatalogSession csess = new CatalogSession(session);
                ok = true;
                return csess;
            } finally {
                if (!ok) {
                    session.close();
                }
            }
        } finally {
            if (!ok) {
                cnt.close();
            }
        }
    }

    private Connection openConnection()
            throws ClassNotFoundException, SQLException {
        Class.forName(conf.getString("jdbc.driver", null));
        return DriverManager.getConnection(conf.getString("jdbc.url", null),
                conf.getString("jdbc.username", null),
                conf.getString("jdbc.password", null));
    }
}
