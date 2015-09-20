package org.tastefuljava.gianadda.site;

import org.tastefuljava.gianadda.meta.MetaReader;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
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
import org.tastefuljava.gianadda.domain.Track;
import org.tastefuljava.gianadda.exif.Exif;
import org.tastefuljava.gianadda.exif.ExifIFD;
import org.tastefuljava.gianadda.exif.GPSIFD;
import org.tastefuljava.gianadda.exif.RootIFD;
import org.tastefuljava.gianadda.geo.EarthGeometry;
import org.tastefuljava.gianadda.geo.ElevationService;
import org.tastefuljava.gianadda.geo.GpxReader;
import org.tastefuljava.gianadda.geo.LatLngBounds;
import org.tastefuljava.gianadda.geo.TrackPoint;
import org.tastefuljava.gianadda.layout.StretchMode;
import org.tastefuljava.gianadda.template.TemplateEngine;
import org.tastefuljava.gianadda.util.Configuration;
import org.tastefuljava.gianadda.util.Files;
import org.tastefuljava.gianadda.util.ImageUtil;

public class Synchronizer {
    private static final Logger LOG
            = Logger.getLogger(Synchronizer.class.getName());

    public static final String PROP_FORCE_HTML = "force-html";
    public static final String PROP_DELETE = "delete";
    public static final String PROP_ELEVATION_SERVICE = "elevation-service";
    public static final String PROP_FORCE_ELEVATION_SERVICE
            = "force-elevation-service";
    public static final String PROP_GEOTAG = "geotag";
    public static final String PROP_FORCE_GEOTAG = "force-geotag";
    public static final String PROP_SIMPLIFY = "simplify";
    public static final String PROP_TOLERANCE = "tolerance";

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
    private final Pattern picNamePattern;
    private final Pattern trackNamePattern;
    private final Pattern dirNamePattern;
    private final String previewPath;
    private final String folderMetaName;
    private final boolean forceHtml;
    private final boolean delete;
    private final boolean useElevationService;
    private final boolean forceElevationService;
    private final boolean geotag;
    private final boolean forceGeotag;
    private final boolean simplify;
    private final double tolerance;

    Synchronizer(Configuration link, CatalogSession sess,
            GalleryDirs dirs) throws IOException {
        this.engine = new TemplateEngine(
                dirs.getBaseDir(), createParams(link));
        this.conf = buildConf(engine, dirs, link);
        this.sess = sess;
        this.dirs = dirs;
        this.picNamePattern = getConfPattern("pic-name-pattern", false);
        this.trackNamePattern = getConfPattern("track-name-pattern", false);
        this.dirNamePattern = getConfPattern("dir-name-pattern", false);
        this.templateNamePattern = getConfPattern(
                "template-name-pattern", false);
        this.previewPath = GalleryDirs.THEME_PATH
                + "/" + conf.getString("preview-template", null);
        this.folderMetaName = conf.getString("folder-meta-name", null);
        this.forceHtml = conf.getBoolean(PROP_FORCE_HTML, false);
        this.delete = conf.getBoolean(PROP_DELETE, false);
        this.useElevationService
                = conf.getBoolean(PROP_ELEVATION_SERVICE, false);
        this.forceElevationService
                = conf.getBoolean(PROP_FORCE_ELEVATION_SERVICE, false);
        this.geotag = conf.getBoolean(PROP_GEOTAG, false);
        this.forceGeotag = conf.getBoolean(PROP_FORCE_GEOTAG, false);
        this.simplify = conf.getBoolean(PROP_SIMPLIFY, false);
        this.tolerance = Double.parseDouble(
                conf.getString(PROP_TOLERANCE, "1"));
    }

    public void synchronize() throws IOException {
        boolean changed
                = syncDir(getRootFolder(), dirs.getBaseDir(), null, null);
        if (changed || forceHtml) {
            LOG.log(Level.INFO, "Applying site-level theme");
            applyTemplates(GalleryDirs.THEME_PATH + "/site", dirs.getSiteDir(),
                    createFolderParams(getRootFolder(), 0));
        }
    }

    private Folder getRootFolder() {
        Folder folder = Folder.getRoot("/");
        if (folder == null) {
            Date now = new Date();
            folder = new Folder();
            folder.setDateTime(now);
            folder.setPubDate(null);
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
        parms.put("geo", new GeoTool(conf));
        parms.put("fmt", new FormatTool(conf));
        parms.put("folders", new FolderTool(conf));
        parms.put("layout", new LayoutTool(conf));
        parms.put("profile", new ProfileTool(conf));
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

    private Pattern getConfPattern(String name, boolean cs) {
        String s = conf.getString(name, null);
        if (s == null) {
            return null;
        }
        int flags = cs ? 0 : Pattern.CASE_INSENSITIVE;
        return Pattern.compile(s, flags);
    }

    private boolean syncDir(Folder folder, File dir, String prev, String next)
            throws IOException {
        LOG.log(Level.INFO, "Synchronizing folder {0}", folder.getPath());
        File meta = new File(dir, folderMetaName);
        boolean changed = MetaReader.check(meta, folder);
        if (changed) {
            folder.update();
            sess.commit();            
        }
        changed |= synTracks(folder, dir);
        changed |= syncPics(folder, dir);
        changed |= synSubdirs(folder, dir);
        if (changed || forceHtml) {
            LOG.log(Level.INFO, "Applying folder-level theme to {0}",
                    folder.getPath());
            Map<String,Object> parms = createFolderParams(folder, 0);
            parms.put("prev", prev);
            parms.put("next", next);
            applyTemplates("_theme/folder", folderSiteDir(folder), parms);
        }
        return changed;
    }

    private boolean syncPics(Folder folder,
            File dir) throws IOException {
        boolean changed = false;
        String[] picNames = Files.listFiles(dir, picNamePattern);
        for (int i = 0; i < picNames.length; ++i) {
            String name = picNames[i];
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
            if (picChanged || forceHtml) {
                String prev = i == 0 ? null : picNames[i-1];
                String next = i+1 >= picNames.length ? null : picNames[i+1];
                generatePreviewHtml(pic, i, picNames.length, prev, next);
            }
        }
        if (delete) {
            Set<String> nameSet = new HashSet<>();
            nameSet.addAll(Arrays.asList(picNames));
            for (Picture pic: folder.getPictures()) {
                if (!nameSet.contains(pic.getName())) {
                    deletePic(pic);
                    changed = true;
                }
            }
            sess.commit();
        }
        return changed;
    }

    private boolean synTracks(Folder folder,
            File dir) throws IOException {
        boolean changed = false;
        String[] names = Files.listFiles(dir, trackNamePattern);
        for (int i = 0; i < names.length; ++i) {
            String name = names[i];
            boolean fileChanged = false;
            File file = new File(dir, name);
            Date timeStamp = new Date(file.lastModified());
            Track track = folder.getTrack(name);
            if (track == null) {
                track = new Track();
                track.setFolder(folder);
                track.setName(name);
                track.setDateTime(timeStamp);
                LOG.log(Level.INFO, "New track found: {0}", track.getPath());
                if (!tryProcessTrack(track, file)) {
                    continue;
                }
                track.insert();
                sess.commit();
                fileChanged = true;
            } else if (!timeStamp.equals(track.getDateTime())) {
                LOG.log(Level.INFO, "Track has changed: {0}", track.getPath());
                track.setDateTime(timeStamp);
                if (!tryProcessTrack(track, file)) {
                    continue;
                }
                track.update();
                sess.commit();
                fileChanged = true;
            }
            if (fileChanged) {
                changed = true;
            }
            if (fileChanged || forceHtml) {
                String prev = i == 0 ? null : names[i-1];
                String next = i+1 >= names.length ? null : names[i+1];
                generateTrackHtml(track, i, names.length, prev, next);
            }
        }
        if (delete) {
            Set<String> nameSet = new HashSet<>();
            nameSet.addAll(Arrays.asList(names));
            for (Track track: folder.getTracks()) {
                if (!nameSet.contains(track.getName())) {
                    deleteTrack(track);
                    changed = true;
                }
            }
            sess.commit();
        }
        return changed;
    }

    private boolean tryProcessTrack(Track track, File file) {
        try {
            return processTrack(track, file);
        } catch (IOException e) {
            LOG.log(Level.WARNING,
                    "Error while processing " + file + " - skipping", e);
            return false;
        }
    }

    private boolean processTrack(Track track, File file) throws IOException {
        TrackPoint[] points = GpxReader.readTrack(file);
        if (points != null && points.length > 1) {
            if (simplify) {
                int prev = points.length;
                points = EarthGeometry.simplify(points, tolerance);
                LOG.log(Level.INFO, "Simplified track - before: {0} points,"
                        + " after: {1} points",
                        new Object[]{prev, points.length});
            }
            if (forceElevationService
                    || useElevationService && !allElevationsPresent(points)) {
                LOG.info("Getting elevations from webservice");
                ElevationService.getElevations(points);
            }
            LatLngBounds bounds = LatLngBounds.build(points);
            track.setBounds(bounds);
            track.setPoints(points);
            return true;
        }
        return false;
    }

    private static boolean allElevationsPresent(TrackPoint[] points) {
        for (TrackPoint pt: points) {
            if (pt.getElevation() == null) {
                return false;
            }
        }
        return true;
    }

    private void generateTrackHtml(Track track, int i, int length, String prev,
            String next) {
    }

    private void deleteTrack(Track track) {
        LOG.log(Level.INFO, "Removing track {0}", track.getPath());
        track.delete();
    }

    private boolean synSubdirs(Folder folder, File dir) throws IOException {
        boolean changed = false;
        String[] dirNames = Files.listDirs(dir, dirNamePattern);
        for (int i = 0; i < dirNames.length; ++i) {
            String name = dirNames[i];
            String prev = i == 0 ? null : dirNames[i-1];
            String next = i+1 >= dirNames.length ? null : dirNames[i+1];
            File subdir = new File(dir, name);
            Folder sub = folder.getSubfolder(name);
            if (sub == null) {
                Date date = new Date(subdir.lastModified());
                sub = new Folder();
                sub.setParent(folder);
                sub.setName(name);
                sub.setDateTime(date);
                sub.setPubDate(null);
                sub.setTitle(name);
                sub.insert();
                sess.commit();
                changed = true;
            }
            changed |= syncDir(sub, subdir, prev, next);
        }
        if (delete) {
            Set<String> nameSet = new HashSet<>();
            nameSet.addAll(Arrays.asList(dirNames));
            for (Folder child: folder.getSubfolders()) {
                if (!nameSet.contains(child.getName())) {
                    deleteFolder(child);
                    changed = true;
                }
            }
            sess.commit();
        }
        return changed;
    }

    private void deletePic(Picture pic) {
        LOG.log(Level.INFO, "Removing picture {0}", pic.getPath());
        pic.delete();
    }

    private void deleteFolder(Folder folder) {
        for (Folder child: folder.getSubfolders()) {
            deleteFolder(child);
        }
        for (Picture pic: folder.getPictures()) {
            deletePic(pic);
        }
        for (Track track: folder.getTracks()) {
            deleteTrack(track);
        }
        LOG.log(Level.INFO, "removing folder {0}", folder.getPath());
        folder.delete();
        sess.commit();
    }

    private File folderSiteDir(Folder folder) {
        return folder.getFile(dirs.getSiteDir());
    }

    private File imageFile(Picture pic, ImageType type) {
        File folderDir = folderSiteDir(pic.getFolder());
        File dir = type.directory(folderDir);
        return new File(dir, pic.getName());
    }

    private BufferedImage generateImage(Picture pic, BufferedImage img,
            int angle, ImageType type, StretchMode mode) throws IOException {
        Dimension dim = type.getSizeFrom(conf);
        img = ImageUtil.rotateAndResize(img, angle, dim.width, dim.height,
                mode);
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
        Exif exif = null;
        BufferedImage img;
        boolean ok = false;
        ImageInputStream in = new FileImageInputStream(file);
        try {
            String ext = Files.extension(file);
            if (ext != null && (ext.equalsIgnoreCase("jpg")
                    || ext.equalsIgnoreCase("jpeg"))) {
                in.mark();
                try {
                    exif = Exif.fromJPEG((DataInput)in);
                } catch (IOException e) {
                    LOG.log(Level.WARNING,
                            "Could not load EXIF data from {0}", file);
                }
                in.reset();
            }
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
                LOG.log(Level.WARNING,
                        "Not ExifIFD found in {0}", pic.getPath());
            } else {
                Date ts = ifd.getDateTime(ExifIFD.Tag.DateTimeOriginal);
                if (ts != null) {
                    timestamp = ts;
                    file.setLastModified(ts.getTime());
                }
            }
            GPSIFD gps = root.getGPSIFD();
            if (gps != null) {
                Double latitude = gps.getLatitude();
                Double longitude = gps.getLongitude();
                if (latitude == null || longitude == null
                        || latitude == 0 && longitude == 0) {
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
        switch (angle) {
            case 0:
            case 180:
                pic.setWidth(img.getWidth());
                pic.setHeight(img.getHeight());
                break;
            case 90:
            case 270:
                pic.setWidth(img.getHeight());
                pic.setHeight(img.getWidth());
                break;
            default:
                throw new RuntimeException("Invalid angle " + angle);
        }
        pic.setAngle(angle);
        img = generateImage(pic, img, angle, ImageType.PREVIEW,
                StretchMode.FIT);        
        generateImage(pic, img, 0, ImageType.THUMB, StretchMode.FIT);
    }


    private void generatePreviewHtml(Picture pic, int ix, int count,
            String prev, String next) throws IOException {
        if (new File(dirs.getBaseDir(), previewPath).exists()) {
            LOG.log(Level.FINE, "Generate preview page for {0}", pic.getPath());
            File folderDir = folderSiteDir(pic.getFolder());
            File dir = ImageType.PREVIEW.directory(folderDir);
            Files.mkdirs(dir);
            File outFile = new File(dir, pic.getName() + ".html");
            Map<String,Object> parms = createPictureParams(pic, 1);
            parms.put("index", ix);
            parms.put("count", count);
            parms.put("prev", prev);
            parms.put("next", next);
            applyTemplate(previewPath, outFile, parms);
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
