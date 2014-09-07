package org.tastefuljava.gianadda.site;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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

    private static final String CONF_FILENAME = "conf.properties";
    private static final String THEME_CONF_PATH
            = GalleryDirs.THEME_PATH + "/" + CONF_FILENAME;
    private static final String SITE_CONF_PATH
            = GalleryDirs.SITE_PATH + "/" + CONF_FILENAME;
    private static final String PREVIEW_FILENAME = "preview.html";
    private static final String PREVIEW_PATH
            = GalleryDirs.THEME_PATH + "/" + PREVIEW_FILENAME;

    private static final Pattern PIC_NAME_PATTERN = Pattern.compile(
            "^[^.].*[.](jpg|jpeg)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern DIR_NAME_PATTERN = Pattern.compile(
            "^[^._].*$");
    private static final String DEFAULT_TEMPLNAME_RE
            = "^[^.].*[.](html|js|css)$";

    private static final String PREVIEW = "preview";
    private static final String THUMB = "thumb";
    private static final Map<String,Dimension> DEFAULT_SIZE = new HashMap<>();
    static {
        DEFAULT_SIZE.put(PREVIEW, new Dimension(800,600));
        DEFAULT_SIZE.put(THUMB, new Dimension(200,133));        
    }

    private final Configuration conf;
    private final CatalogSession sess;
    private final GalleryDirs dirs;
    private final Pattern templateNamePattern;
    private final TemplateEngine engine;

    Synchronizer(Configuration conf, CatalogSession sess,
            GalleryDirs dirs) throws IOException {
        this.engine = new TemplateEngine(
                dirs.getBaseDir(), createParams(conf));
        this.conf = buildConf(engine, dirs, conf);
        this.sess = sess;
        this.dirs = dirs;
        String re = conf.getString(
                "template-name-pattern", DEFAULT_TEMPLNAME_RE);
        this.templateNamePattern = Pattern.compile(
                re, Pattern.CASE_INSENSITIVE);
    }

    public void synchronize() throws IOException {
        Folder rootFolder = Folder.getRoot("/");
        if (rootFolder == null) {
            rootFolder = new Folder();
            rootFolder.setName("/");
            rootFolder.setTitle("Root");
            rootFolder.setDescription("Root folder");
            rootFolder.insert();
            sess.commit();
        } else {
            LOG.log(Level.INFO, "Listing pictures of {0}",
                    rootFolder.getName());
            for (Picture pic: rootFolder.getPictures()) {
                LOG.log(Level.INFO, "Picture {0}", pic.getPath());
            }
        }
        boolean changed = syncDir(rootFolder, dirs.getBaseDir());
        if (changed || getForceHtml()) {
            applyTemplates(GalleryDirs.THEME_PATH + "/site", dirs.getSiteDir(),
                    createFolderParams(rootFolder, 0));
        }
    }

    private boolean getForceHtml() {
        return conf.getBoolean(PROP_FORCE_HTML, false);
    }

    private boolean syncDir(Folder folder, File dir) throws IOException {
        boolean changed = syncPics(folder, dir);
        changed |= synSubdirs(folder, dir);
        if (changed || getForceHtml()) {
            Map<String,Object> parms = createFolderParams(folder, 0);
            applyTemplates("_theme/folder", folderSiteDir(folder), parms);
        }
        return changed;
    }

    private boolean syncPics(Folder folder,
            File dir) throws IOException {
        boolean changed = false;
        String[] picNames = Files.listFiles(dir, PIC_NAME_PATTERN);
        for (String name: picNames) {
            boolean picChanged = false;
            File file = new File(dir, name);
            Date timeStamp = new Date(file.lastModified());
            Picture pic = folder.getPicture(name);
            if (pic == null) {
                pic = new Picture();
                pic.setFolder(folder);
                pic.setName(name);
                processPic(pic, file);
                pic.insert();
                sess.commit();
                picChanged = true;
            } else if (!timeStamp.equals(pic.getDateTime())) {
                pic.update();
                processPic(pic, file);
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
        String[] dirNames = Files.listDirs(dir, DIR_NAME_PATTERN);
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

    private File folderSiteFile(Folder folder, String name) {
        return new File(folderSiteDir(folder), name);
    }

    private File imageFile(Picture pic, String type) {
        File dir = folderSiteFile(pic.getFolder(), type);
        return new File(dir, pic.getName());
    }

    private BufferedImage generateImage(Picture pic, BufferedImage img,
            int angle, String type) throws IOException {
        Dimension dim = conf.getDimension(
                type + "-size", DEFAULT_SIZE.get(type));
        img = ImageUtil.rotateAndResize(img, angle, dim.width, dim.height);
        File file = imageFile(pic, type);
        Files.mkdirs(file.getParentFile());
        ImageIO.write(img, "jpeg", file);
        return img;
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
        RootIFD root = exif.getRootIFD();
        ExifIFD ifd = root.getExifIFD();
        Date timestamp = ifd.getDateTime(ExifIFD.Tag.DateTimeOriginal);
        if (timestamp == null) {
            timestamp = new Date(file.lastModified());
        } else {
            file.setLastModified(timestamp.getTime());
        }
        pic.setDateTime(timestamp);
        int width = img.getWidth();
        int height = img.getHeight();
        pic.setWidth(width);
        pic.setHeight(height);
        GPSIFD gps = root.getGPSIFD();
        if (gps != null) {
            pic.setLatitude(gps.getLatitude());
            pic.setLongitude(gps.getLongitude());
            pic.setAltitude(gps.getAltitude());
        }
        int angle = getAngle(root);
        img = generateImage(pic, img, angle, PREVIEW);        
        generateImage(pic, img, 0, THUMB);
    }

    private void generatePreviewHtml(Picture pic) throws IOException {
        if (new File(dirs.getBaseDir(), PREVIEW_PATH).exists()) {
            File dir = folderSiteFile(pic.getFolder(), PREVIEW);
            Files.mkdirs(dir);
            File outFile = new File(dir, pic.getName() + ".html");
            applyTemplate(PREVIEW_PATH, outFile, createPictureParams(pic, 1));
        }
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

    private static Map<String,Object> createParams(Configuration conf) {
        Map<String,Object> parms = new HashMap<>();
        parms.put("conf", new ConfigurationTool(conf));
        return parms;
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

    private static Configuration buildConf(TemplateEngine engine, 
            GalleryDirs dirs, Configuration conf) throws IOException {
        if (new File(dirs.getBaseDir(), THEME_CONF_PATH).isFile()) {
            String text = engine.process(THEME_CONF_PATH, createParams(conf));
            Properties props = new Properties();
            props.load(new StringReader(text));
            conf = new Configuration(props, conf);
        }
        if (new File(dirs.getBaseDir(), SITE_CONF_PATH).isFile()) {
            String text = engine.process(SITE_CONF_PATH, createParams(conf));
            Properties props = new Properties();
            props.load(new StringReader(text));
            conf = new Configuration(props, conf);
        }
        return conf;
    }
}
