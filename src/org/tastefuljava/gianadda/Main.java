package org.tastefuljava.gianadda;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.EnumSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.tastefuljava.gianadda.catalog.CatalogSession;
import org.tastefuljava.gianadda.domain.Folder;
import org.tastefuljava.gianadda.domain.GpsData;
import org.tastefuljava.gianadda.domain.Picture;
import org.tastefuljava.gianadda.site.SiteService;
import org.tastefuljava.gianadda.util.Files;

public class Main {
    static {
        // This is especially useful on Mac OS to avoid the default app to be
        // Launched and appear in the dock and in the menu bar
        System.setProperty("java.awt.headless", "true");
    }

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    enum Flag {
        SYNCHRONIZE, FORCE_HTML, DELETE, VERBOSE, QUIET, DEBUG, HELP, TEST,
        ELEVATION_SERVICE, FORCE_ELEVATION_SERVICE, GEOTAG, FORCE_GEOTAG,
        SIMPLIFY;

        public String getName() {
            return name().toLowerCase().replace('_', '-');
        }

        @Override
        public String toString() {
            return "--" + getName();
        }
    }

    private double tolerance = 1; // meter
    private File dir = null;
    private String createTheme = null;
    private String changeTheme = null;
    private final Set<Flag> flags = EnumSet.noneOf(Flag.class);

    public static void main(String[] args) {
        try {
            Main main = new Main();
            if (!main.processArgs(args)) {
                return;
            }
            main.process();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void usage() {
        try (InputStream in = Main.class.getResourceAsStream("usage.txt")) {
            Files.copyText(in, "UTF-8", System.err,
                    Charset.defaultCharset().name());
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private boolean processArgs(String[] args) {
        int st = 0;
        loop: 
        for (String arg: args) {
            switch (st) {
                case -1:
                    st = 9999;
                    break loop;
                case 0:
                    switch (arg) {
                        case "-c":
                        case "--create":
                            st = 1;
                            break;
                        case "-t":
                        case "--change-theme":
                            st = 2;
                            break;
                        case "--tolerance":
                            st = 3;
                            break;
                        case "-?":
                        case "-h":
                        case "--help":
                            flags.add(Flag.HELP);
                            break;
                        case "-s":
                        case "--sync":
                            flags.add(Flag.SYNCHRONIZE);
                            break;
                        case "-f":
                        case "--force-html":
                            flags.add(Flag.SYNCHRONIZE);
                            flags.add(Flag.FORCE_HTML);
                            break;
                        case "-d":
                        case "--delete":
                            flags.add(Flag.SYNCHRONIZE);
                            flags.add(Flag.FORCE_HTML);
                            flags.add(Flag.DELETE);
                            break;
                        case "-e":
                        case "--elevation-service":
                            flags.add(Flag.SYNCHRONIZE);
                            flags.add(Flag.ELEVATION_SERVICE);
                            break;
                        case "-E":
                        case "--force-elevation-service":
                            flags.add(Flag.SYNCHRONIZE);
                            flags.add(Flag.ELEVATION_SERVICE);
                            flags.add(Flag.FORCE_ELEVATION_SERVICE);
                            break;
                        case "-g":
                        case "--geotag":
                            flags.add(Flag.SYNCHRONIZE);
                            flags.add(Flag.GEOTAG);
                            break;
                        case "-G":
                        case "--force-geotag":
                            flags.add(Flag.SYNCHRONIZE);
                            flags.add(Flag.GEOTAG);
                            flags.add(Flag.FORCE_GEOTAG);
                            break;
                        case "--simplify":
                            flags.add(Flag.SIMPLIFY);
                            break;
                        case "-v":
                        case "--verbose":
                            flags.add(Flag.VERBOSE);
                            break;
                        case "--quiet":
                            flags.add(Flag.QUIET);
                            break;
                        case "--debug":
                            flags.add(Flag.DEBUG);
                            break;
                        case "---test":
                            flags.add(Flag.TEST);
                            break;
                        default:
                            dir = new File(arg);
                            st = -1;
                            break;
                    }
                    break;
                case 1:
                    st = 0;
                    createTheme = arg;
                    flags.add(Flag.SYNCHRONIZE);
                    flags.add(Flag.FORCE_HTML);
                    break;
                case 2:
                    st = 0;
                    changeTheme = arg;
                    flags.add(Flag.SYNCHRONIZE);
                    flags.add(Flag.FORCE_HTML);
                    break;
                case 3:
                    try {
                        tolerance = Double.parseDouble(arg);
                    } catch (NumberFormatException e) {
                        LOG.log(Level.SEVERE,
                                "Invalid value for tolerance: {0}", arg);
                        break loop;
                    }
                    st = 0;
                    flags.add(Flag.SIMPLIFY);
                    break;
                default:
                    break loop;
            }
        }
        if (!isTerminalState(st)) {
            usage();
            return false;
        } else if (flags.contains(Flag.HELP)) {
            usage();
        }
        return true;
    }

    private boolean isTerminalState(int st) {
        return st == -1 || st == 0 && flags.contains(Flag.HELP);
    }

    private void process() throws IOException {
        if (dir == null) {
            return;
        }
        initLogging();
        LOG.log(Level.INFO, "Gallery: {0}", dir);
        if (createTheme != null) {
            LOG.log(Level.INFO, "Create gallery using theme {0}", createTheme);
        }
        if (changeTheme != null) {
            LOG.log(Level.INFO, "Change theme to {0}", changeTheme);
        }
        LOG.log(Level.INFO, "Option flags: {0}", flags);
        try (SiteService builder = new SiteService(dir)) {
            if (createTheme != null) {
                builder.create(createTheme);
            } else {
                builder.open();
            }
            if (changeTheme != null) {
                builder.changeTheme(changeTheme);
            }
            if (flags.contains(Flag.SYNCHRONIZE)) {
                Properties props = new Properties();
                for (Flag flag: Flag.values()) {
                    boolean isset = flags.contains(flag);
                    props.put(flag.getName(), isset ? "true" : "false");
                }
                if (flags.contains(Flag.SIMPLIFY)) {
                    props.put("tolerance", Double.toString(tolerance));
                }
                builder.synchronize(props);
            }
            if (flags.contains(Flag.TEST)) {
                try (CatalogSession sess = builder.openSession()) {
                    test(sess);
                }
            }
        }
    }

    private void test(CatalogSession sess) {
        LOG.info("Starting tests");
        Folder root = Folder.getRoot("/");
        test(root);
    }

    private void test(Folder folder) {
        System.out.println();
        System.out.println("Folder: " + folder.getPath());
        for (Picture pic: folder.getPictures()) {
            GpsData gps = pic.getGpsData();
            if (gps == null) {
                System.out.println(pic.getName() + " has no GPS data");
            } else {
                System.out.println(pic.getName() + " " + gps.getLatitude()
                        + " " + gps.getLongitude() + " " + gps.getAltitude());
            }
        }
        for (Folder child: folder.getSubfolders()) {
            test(child);
        }
    }

    private void initLogging() {
        if (System.getProperty("java.util.logging.config.file") == null) {
            // Use default logging configuration
            try (InputStream inputStream = Main.class.getResourceAsStream(
                    "default-logging.properties")) {
                LogManager.getLogManager().readConfiguration(inputStream);
            } catch (final IOException e) {
                LOG.severe(e.getMessage());
            }
        }
        Logger log = LogManager.getLogManager().getLogger("");
        if (log != null) {
            Level level = Level.WARNING;
            if (flags.contains(Flag.QUIET)) {
                level = Level.SEVERE;
            }
            if (flags.contains(Flag.VERBOSE)) {
                level = Level.INFO;
            }
            if (flags.contains(Flag.DEBUG)) {
                level = Level.FINEST;
            }
            log.setLevel(level);
        }
    }
}
