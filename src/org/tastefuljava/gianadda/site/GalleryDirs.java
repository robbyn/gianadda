package org.tastefuljava.gianadda.site;

import java.io.File;

public class GalleryDirs {
    public static final String CATALOG_PATH = "_catalog";
    public static final String THEME_PATH = "_theme";
    public static final String SITE_PATH = "_site";

    private final File baseDir;

    public GalleryDirs(File baseDir) {
        this.baseDir = baseDir;
    }

    public File getBaseDir() {
        return baseDir;
    }

    public File getCatalogDir() {
        return new File(baseDir, CATALOG_PATH);
    }

    public File getThemeDir() {
        return new File(baseDir, THEME_PATH);
    }

    public File getSiteDir() {
        return new File(baseDir, SITE_PATH);
    }
}
