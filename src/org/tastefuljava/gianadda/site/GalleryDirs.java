package org.tastefuljava.gianadda.site;

import java.io.File;

public class GalleryDirs {
    private final File baseDir;

    public GalleryDirs(File baseDir) {
        this.baseDir = baseDir;
    }

    public File getBaseDir() {
        return baseDir;
    }

    public File getCatalogDir() {
        return new File(baseDir, "_catalog");
    }

    public File getThemeDir() {
        return new File(baseDir, "_theme");
    }

    public File getSiteDir() {
        return new File(baseDir, "_site");
    }
}
