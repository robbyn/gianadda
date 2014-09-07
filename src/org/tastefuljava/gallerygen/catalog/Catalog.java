package org.tastefuljava.gallerygen.catalog;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import org.apache.ibatis.session.SqlSessionFactory;
import org.tastefuljava.gallerygen.util.Configuration;

public class Catalog implements Closeable {
    private static final Logger LOG = Logger.getLogger(Catalog.class.getName());

    private final File dir;
    private final SqlSessionFactory factory;
    private final Configuration conf;

    public static Catalog open(File dir, Configuration link) throws IOException {
        return new CatalogBuilder(dir, link).open();
    }

    Catalog(File dir, SqlSessionFactory factory, Configuration conf) {
        this.dir = dir;
        this.factory = factory;
        this.conf = conf;
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

    public CatalogSession openSession() {
        return new CatalogSession(factory.openSession());
    }
}
