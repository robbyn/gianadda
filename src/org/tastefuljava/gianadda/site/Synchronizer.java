package org.tastefuljava.gianadda.site;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import org.tastefuljava.gianadda.catalog.CatalogSession;
import org.tastefuljava.gianadda.domain.Folder;
import org.tastefuljava.gianadda.domain.GpsData;
import org.tastefuljava.gianadda.domain.Picture;
import org.tastefuljava.gianadda.exif.Exif;
import org.tastefuljava.gianadda.exif.ExifIFD;
import org.tastefuljava.gianadda.exif.GPSIFD;
import org.tastefuljava.gianadda.exif.RootIFD;
import org.tastefuljava.gianadda.template.TemplateEngine;
import org.tastefuljava.gianadda.util.Configuration;
import org.tastefuljava.gianadda.util.Files;
import org.tastefuljava.gianadda.util.ImageUtil;

public class Synchronizer {
    private static final Logger LOG
            = Logger.getLogger(Synchronizer.class.getName());

    public static final String PROP_FORCE_HTML = "force-html";

    private static final String CONF_FILENAME = "settings.properties";
    private static final String THEME_CONF_PATH
            = GalleryDirs.THEME_PATH + "/" + CONF_FILENAME;

    static enum ImageType {
        PREVIEW, THUMB;

        public Dimension getSizeFrom(Configuration conf) {
            return conf.getDimension(
                this + "-size", null);
        }

        public File directory(File baseDir) {
            return new File(baseDir, toString());
        }

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    private final Configuration conf;
    private final CatalogSession sess;
    private final GalleryDirs dirs;
    private final Pattern templateNamePattern;
    private final TemplateEngine engine;
    private final Folder rootFolder;
    private final Pattern picNamePattern;
    private final Pattern dirNamePattern;
    private final String previewPath;

    Synchronizer(Configuration link, CatalogSession sess,
            GalleryDirs dirs) throws IOException {
        this.rootFolder = getRootFolder(sess);
        this.engine = new TemplateEngine(
                dirs.getBaseDir(), createParams(link));
        this.conf = buildConf(engine, dirs, link);
        this.sess = sess;
        this.dirs = dirs;
        this.picNamePattern = getConfPattern("pic-name-pattern", false);
        this.dirNamePattern = getConfPattern("dir-name-pattern", false);
        this.templateNamePattern = getConfPattern(
                "template-name-pattern", false);
        this.previewPath = GalleryDirs.THEME_PATH
                + "/" + conf.getString("preview-template", null);
    }

    public void synchronize() throws IOException {
        boolean changed = syncDir(rootFolder, dirs.getBaseDir());
        if (changed || getForceHtml()) {
            LOG.log(Level.INFO, "Applying site-level theme");
            applyTemplates(GalleryDirs.THEME_PATH + "/site", dirs.getSiteDir(),
                    createFolderParams(rootFolder, 0));
        }
    }

    private static Folder getRootFolder(CatalogSession sess) {
        Folder folder = Folder.getRoot("/");
        if (folder == null) {
            folder = new Folder();
            folder.setName("/");
            folder.setTitle("Root");
            folder.setDescription("Root folder");
            folder.insert();
            sess.commit();
        }
        return folder;
    }

    private static Map<String,Object> createParams(Configuration conf) {
        Map<String,Object> parms = new HashMap<>();
        parms.put("conf", new ConfigurationTool(conf));
        return parms;
    }

    private static Configuration buildConf(TemplateEngine engine, 
            GalleryDirs dirs, Configuration conf) throws IOException {
        Properties props = new Properties();
        try (InputStream in = Synchronizer.class.getResourceAsStream(
                "default-" + CONF_FILENAME)) {
            if (in != null) {
                props.load(in);
            }
        }
        if (new File(dirs.getBaseDir(), THEME_CONF_PATH).isFile()) {
            String text = engine.process(THEME_CONF_PATH, createParams(conf));
            props.load(new StringReader(text));
        }
        return new Configuration(props, conf);
    }

    private static int getAngle(RootIFD root) {
        int orientation = root.getInt(RootIFD.Tag.Orientation, 0);
        switch (orientation) {
            case 3:
                return 180;
            case 6:
                return 90;
            case 8:
                return 270;
            default:
                return 0;
        }
    }

    private boolean getForceHtml() {
        return conf.getBoolean(PROP_FORCE_HTML, false);
    }

    private Pattern getConfPattern(String name, boolean cs) {
        String s = conf.getString(name, null);
        if (s == null) {
            return null;
        }
        int flags = cs ? 0 : Pattern.CASE_INSENSITIVE;
        return Pattern.compile(s, flags);
    }

    private boolean syncDir(Folder folder, File dir) throws IOException {
        LOG.log(Level.INFO, "Synchronizing folder {0}", folder.getPath());
        boolean changed = syncPics(folder, dir);
        changed |= synSubdirs(folder, dir);
        if (changed || getForceHtml()) {
            LOG.log(Level.INFO, "Applying folder-level theme to {0}",
                    folder.getPath());
            Map<String,Object> parms = createFolderParams(folder, 0);
            applyTemplates("_theme/folder", folderSiteDir(folder), parms);
        }
        return changed;
    }

    private boolean syncPics(Folder folder,
            File dir) throws IOException {
        boolean changed = false;
        String[] picNames = Files.listFiles(dir, picNamePattern);
        for (String name: picNames) {
            boolean picChanged = false;
            File file = new File(dir, name);
            Date timeStamp = new Date(file.lastModified());
            Picture pic = folder.getPicture(name);
            if (pic == null) {
                pic = new Picture();
                pic.setFolder(folder);
                pic.setName(name);
                LOG.log(Level.INFO, "New picture found: {0}", pic.getPath());
                if (!tryProcessPic(pic, file)) {
                    continue;
                }
                pic.insert();
                sess.commit();
                picChanged = true;
            } else if (!timeStamp.equals(pic.getDateTime())) {
                LOG.log(Level.INFO, "Picture has changed: {0}", pic.getPath());
                if (!tryProcessPic(pic, file)) {
                    continue;
                }
                pic.update();
                sess.commit();
                picChanged = true;
            }
            if (picChanged) {
                changed = true;
            }
            if (picChanged || getForceHtml()) {
                generatePreviewHtml(pic);
            }
        }
        return changed;
    }

    private boolean synSubdirs(Folder folder, File dir) throws IOException {
        boolean changed = false;
        String[] dirNames = Files.listDirs(dir, dirNamePattern);
        for (String name: dirNames) {
            Folder sub = folder.getSubfolder(name);
            if (sub == null) {
                sub = new Folder();
                sub.setParent(folder);
                sub.setName(name);
                sub.setTitle(name);
                sub.insert();
                sess.commit();
                changed = true;
            }
            changed |= syncDir(sub, new File(dir, name));
        }
        return changed;
    }

    private File folderSiteDir(Folder folder) {
        return new File(dirs.getSiteDir(), folder.getPath());
    }

    private File imageFile(Picture pic, ImageType type) {
        File folderDir = folderSiteDir(pic.getFolder());
        File dir = type.directory(folderDir);
        return new File(dir, pic.getName());
    }

    private BufferedImage generateImage(Picture pic, BufferedImage img,
            int angle, ImageType type) throws IOException {
        Dimension dim = type.getSizeFrom(conf);
        img = ImageUtil.rotateAndResize(img, angle, dim.width, dim.height);
        File file = imageFile(pic, type);
        Files.mkdirs(file.getParentFile());
        ImageIO.write(img, "jpeg", file);
        return img;
    }

    private boolean tryProcessPic(Picture pic, File file) {
        try {
            processPic(pic, file);
            return true;
        } catch (IOException e) {
            LOG.log(Level.WARNING,
                    "Error while processing " + file + " - skipping", e);
            return false;
        }
    }

    private void processPic(Picture pic, File file) throws IOException {
        Exif exif;
        BufferedImage img;
        boolean ok = false;
        ImageInputStream in = new FileImageInputStream(file);
        try {
            in.mark();
            exif = Exif.fromJPEG(in);
            in.reset();
            img = ImageIO.read(in);
            ok = true;
        } finally {
            if (!ok) { // otherwise, ImageIO.read(in) would have closed it
                in.close();
            }
        }
        Date timestamp = new Date(file.lastModified());
        int angle = 0;
        if (exif != null) {
            RootIFD root = exif.getRootIFD();
            pic.setDescription(root.getString(RootIFD.Tag.ImageDescription));
            pic.setArtist(root.getString(RootIFD.Tag.Artist));
            pic.setCopyright(root.getString(RootIFD.Tag.Copyright));
            ExifIFD ifd = root.getExifIFD();
            if (ifd == null) {
                throw new IOException("Not ExifIFD found in " + pic.getPath());
            }
            Date ts = ifd.getDateTime(ExifIFD.Tag.DateTimeOriginal);
            if (ts != null) {
                timestamp = ts;
                file.setLastModified(ts.getTime());
            }
            GPSIFD gps = root.getGPSIFD();
            if (gps != null) {
                Double latitude = gps.getLatitude();
                Double longitude = gps.getLongitude();
                if (latitude == null && longitude == null) {
                    LOG.log(Level.WARNING,
                            "latitude/longitude missing in {0}", pic.getPath());
                } else {
                    LOG.log(Level.FINE,
                            "GPS data found in {0}", pic.getPath());
                    GpsData data = new GpsData();
                    data.setLatitude(latitude);
                    data.setLongitude(longitude);
                    data.setAltitude(gps.getAltitude());
                    pic.setGpsData(data);
                }
            }
            angle = getAngle(root);
        }
        pic.setDateTime(timestamp);
        int width = img.getWidth();
        int height = img.getHeight();
        pic.setWidth(width);
        pic.setHeight(height);
        img = generateImage(pic, img, angle, ImageType.PREVIEW);        
        generateImage(pic, img, 0, ImageType.THUMB);
    }

    private void generatePreviewHtml(Picture pic) throws IOException {
        if (new File(dirs.getBaseDir(), previewPath).exists()) {
            LOG.log(Level.FINE, "Generate preview page for {0}", pic.getPath());
            File folderDir = folderSiteDir(pic.getFolder());
            File dir = ImageType.PREVIEW.directory(folderDir);
            Files.mkdirs(dir);
            File outFile = new File(dir, pic.getName() + ".html");
            applyTemplate(previewPath, outFile, createPictureParams(pic, 1));
        }
    }

    private void applyTemplates(String path, File dest, Map<String,?> parms)
            throws IOException {
        File source = new File(dirs.getBaseDir(), path);
        applyTemplates(path, source, dest, parms);
    }

    private void applyTemplates(String path, File source, File dest,
            Map<String,?> parms) throws IOException {
        Files.mkdirs(dest);
        for (String name: Files.list(source, null)) {
            File file = new File(source, name);
            String subpath = path + "/" + name;
            if (file.isDirectory()) {
                applyTemplates(subpath, file, new File(dest, name), parms);
            } else {
                applyTemplate(subpath, file, new File(dest, name), parms);
            }
        }
    }

    private void applyTemplate(String path, File source, File dest,
            Map<String,?> parms) throws IOException {
        if (templateNamePattern.matcher(source.getName()).matches()) {
            applyTemplate(path, dest, parms);
        } else {
            Files.copy(source, dest);
        }
    }

    private void applyTemplate(String path, File dest, Map<String,?> parms)
            throws IOException {
        engine.process(path, parms, dest);
    }

    private Map<String,Object> createSiteParams() {
        return createParams(conf);
    }

    private Map<String,Object> createFolderParams(Folder folder, int depth) {
        int level = folder.getLevel() + depth;
        String base = "";
        for (int i = 0; i < level; ++i) {
            base += "../";
        }
        Map<String,Object> parms = createSiteParams();
        parms.put("folder", folder);
        parms.put("base", base);
        return parms;
    }

    private Map<String,Object> createPictureParams(Picture pic, int depth) {
        Map<String,Object> parms = createFolderParams(pic.getFolder(), depth);
        parms.put("pic", pic);
        return parms;
    }
}
