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

public class SiteService implements Closeable {
    private static final Logger LOG
            = Logger.getLogger(SiteService.class.getName());

    private final GalleryDirs dirs;
    private Catalog catalog;
    private Configuration conf;

    public SiteService(File dir) {
        this.dirs = new GalleryDirs(dir);
    }

    public void open() throws IOException {
        LOG.log(Level.INFO, "Open gallery {0}", dirs.getBaseDir());
        catalog = Catalog.open(dirs.getCatalogDir(), null);
    }

    public void create(String theme) throws IOException {
        LOG.log(Level.INFO, "Create gallery {0}", dirs.getBaseDir());
        Files.mkdirs(dirs.getBaseDir());
        Files.deleteIfExists(dirs.getCatalogDir());
        Files.deleteIfExists(dirs.getThemeDir());
        Files.deleteIfExists(dirs.getSiteDir());
        initTheme(theme);
        open();
    }

    @Override
    public void close() {
        if (catalog != null) {
            try {
                catalog.close();
            } finally {
                catalog = null;
            }
        }
    }

    public Configuration getConf() {
        return conf;
    }

    public void changeTheme(String theme) throws IOException {
        LOG.log(Level.INFO, "Change theme of gallery {0} to {1}",
                new Object[] {dirs.getBaseDir(), theme});
        boolean ok = false;
        File themeDir = dirs.getThemeDir();
        File backupDir = new File(
                themeDir.getParentFile(), themeDir.getName() + ".backup");
        Files.deleteIfExists(backupDir);
        if (themeDir.exists()) {
            Files.rename(themeDir, backupDir);
        }
        try {
            initTheme(theme);
            ok = true;
        } finally {
            if (!ok) {
                // restore backup
                Files.deleteIfExists(themeDir);
                if (backupDir.exists()) {
                    Files.rename(backupDir, themeDir);
                }
            }
        }
    }

    public void synchronize(boolean forceHtml) throws IOException {
        LOG.log(Level.INFO, "Synchronize gallery {0}", dirs.getBaseDir());
        CatalogSession sess = openSession();
        try {
            Properties props = new Properties();
            props.put(Synchronizer.PROP_FORCE_HTML, Boolean.toString(forceHtml));
            Configuration cfg = new Configuration(props, conf);
            Synchronizer syn = new Synchronizer(cfg, sess, dirs);
            syn.synchronize();
        } finally {
            closeSession(sess);
        }
    }

    public CatalogSession openSession() {
        boolean ok = false;
        CatalogSession sess = catalog.openSession();
        try {
            Mapper map = sess.getMapper(Mapper.class);
            CurrentMapper.set(map);
            ok = true;
        } finally {
            if (!ok) {
                closeSession(sess);
            }
        }
        return sess;
    }

    public void closeSession(CatalogSession sess) {
        CurrentMapper.set(null);
        sess.close();
    }

    private File getResourceDir() {
        String s = System.getProperty("resource-base");
        File dir;
        if (s != null) {
            dir = new File(s);
        } else {
            dir = Files.getProgramDir();
        }
        if (!dir.isDirectory()) {
            LOG.log(Level.SEVERE, "Invalid resource dir {0}", dir);
            throw new RuntimeException("Invalid resource dir " + dir);
        }
        return dir;
    }


    private void initTheme(String theme) throws IOException {
        File dir = new File(getResourceDir(), "themes");
        if (!dir.isDirectory()) {
            throw new IOException("Invalid theme dir " + dir);
        }
        boolean ok = initTheme(new File(dir, theme))
                || initTheme(new File(dir, theme + ".ggt"))
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
}
