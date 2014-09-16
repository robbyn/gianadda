package org.tastefuljava.gianadda.site;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.tastefuljava.gianadda.catalog.Catalog;
import org.tastefuljava.gianadda.catalog.CatalogSession;
import org.tastefuljava.gianadda.util.Configuration;
import org.tastefuljava.gianadda.util.Files;

public class SiteService implements Closeable {
    private static final Logger LOG
            = Logger.getLogger(SiteService.class.getName());

    private static final String[] THEME_EXTENSIONS
            = {"", ".ggt", ".jar", ".zip"};

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

    public void synchronize(boolean forceHtml, boolean delete) throws IOException {
        LOG.log(Level.INFO, "Synchronize gallery {0}", dirs.getBaseDir());
        try (CatalogSession sess = openSession()) {
            Properties props = new Properties();
            props.put(
                    Synchronizer.PROP_FORCE_HTML, Boolean.toString(forceHtml));
            props.put(Synchronizer.PROP_DELETE, Boolean.toString(delete));
            Configuration cfg = new Configuration(props, conf);
            Synchronizer syn = new Synchronizer(cfg, sess, dirs);
            syn.synchronize();
        }
    }

    public CatalogSession openSession() {
        try {
            return catalog.openSession();
        } catch (SQLException | ClassNotFoundException ex) {
            LOG.log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex.getMessage());
        }
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
        boolean ok = false;
        File dir = new File(getResourceDir(), "themes");
        if (dir.isDirectory()) {
            for (String ext: THEME_EXTENSIONS) {
                ok = initTheme(new File(dir, theme + ext));
                if (ok) {
                    break;
                }
            }
        }
        if (!ok) {
            // Try in the resources
            for (String ext: THEME_EXTENSIONS) {
                ok = initResourceTheme(theme + ext);
                if (ok) {
                    break;
                }
            }
            if (!ok) {
                throw new IOException("Theme not found: " + theme);
            }
        }
    }

    private boolean initResourceTheme(String name) throws IOException {
        File temp = extractResource("/" + name, "dgt", ".ggt");
        if (temp != null && initTheme(temp)) {
            Files.delete(temp);
            return true;
        }
        return false;
    }

    private File extractResource(String name, String pfx, String sfx)
            throws IOException {
        try (InputStream in = SiteService.class.getResourceAsStream(name)) {
            if (in != null) {
                File temp = File.createTempFile(pfx, sfx);
                Files.save(in, temp);
                return temp;
            }
        }
        return null;
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
