package org.tastefuljava.gianadda.site;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.tastefuljava.gianadda.catalog.Catalog;
import org.tastefuljava.gianadda.catalog.CatalogSession;
import org.tastefuljava.gianadda.domain.CurrentMapper;
import org.tastefuljava.gianadda.domain.Mapper;
import org.tastefuljava.gianadda.util.Configuration;
import org.tastefuljava.gianadda.util.Files;

public class SiteBuilder implements Closeable {
    private static final Logger LOG
            = Logger.getLogger(SiteBuilder.class.getName());

    private static final String CONF_FILENAME = "site.properties";

    private final GalleryDirs dirs;
    private Catalog catalog;
    private CatalogSession sess;
    private Configuration conf;

    public SiteBuilder(File dir) {
        this.dirs = new GalleryDirs(dir);
    }

    @Override
    public void close() {
        try {
            CurrentMapper.set(null);
            closeSession();
        } finally {
            closeCatalog();
        }
    }

    public void open() throws IOException {
        boolean ok = false;
        catalog = Catalog.open(dirs.getCatalogDir(), null);
        try {
            File file = new File(dirs.getSiteDir(), CONF_FILENAME);
            conf = Configuration.load(file, catalog.getConf());
            sess = catalog.openSession();
            try {
                Mapper map = sess.getMapper(Mapper.class);
                CurrentMapper.set(map);
                ok = true;
            } finally {
                if (!ok) {
                    closeSession();
                }
            }
        } finally {
            if (!ok) {
                closeCatalog();
            }
        }
    }

    public void create(String theme) throws IOException {
        Files.mkdirs(dirs.getBaseDir());
        Files.deleteIfExists(dirs.getCatalogDir());
        Files.deleteIfExists(dirs.getThemeDir());
        Files.deleteIfExists(dirs.getSiteDir());
        SiteBuilder.this.initTheme(theme);
        initSite();
        open();
    }

    public Configuration getConf() {
        return conf;
    }

    public void synchronize(boolean forceHtml) throws IOException {
        Properties props = new Properties();
        props.put(Synchronizer.PROP_FORCE_HTML, Boolean.toString(forceHtml));
        Configuration cfg = new Configuration(props, conf);
        Synchronizer syn = new Synchronizer(cfg, sess, dirs);
        syn.synchronize();
    }

    private File getResourceDir() {
        String s = System.getProperty("resource-base");
        File dir;
        if (s != null) {
            dir = new File(s);
        } else {
            dir = new File(Files.getProgramDir(), "res");
        }
        if (!dir.isDirectory()) {
            LOG.log(Level.SEVERE, "Invalid resource dir {0}", dir);
            throw new RuntimeException("Invalid resource dir " + dir);
        }
        return dir;
    }

    private void closeSession() {
        if (sess != null) {
            try {
                sess.close();
            } finally {
                sess = null;
            }
        }
    }

    private void closeCatalog() {
        if (catalog != null) {
            try {
                catalog.close();
            } finally {
                catalog = null;
            }
        }
    }

    private void initTheme(String theme) throws IOException {
        File dir = new File(getResourceDir(), "themes");
        if (!dir.isDirectory()) {
            throw new IOException("Invalid theme dir " + dir);
        }
        boolean ok = initTheme(new File(dir, theme))
                || initTheme(new File(dir, theme + ".jar"))
                || initTheme(new File(dir, theme + ".zip"));
        if (!ok) {
            throw new IOException("Theme does not exist " + theme);
        }
    }

    private boolean initTheme(File source) throws IOException {
        if (source.isDirectory()) {
            Files.copy(source, dirs.getThemeDir());
        } else if (source.isFile()) {
            Files.unzip(source, dirs.getThemeDir(), null);
        } else {
            return false;
        }
        return true;
    }

    private void initSite() throws IOException {
        File source = new File(dirs.getThemeDir(), CONF_FILENAME);
        if (source.isFile()) {
            File dest = new File(dirs.getSiteDir(), CONF_FILENAME);
            Files.copy(source, dest);
        }
    }
}
