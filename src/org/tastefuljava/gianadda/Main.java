package org.tastefuljava.gianadda;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.tastefuljava.gianadda.site.SiteBuilder;
import org.tastefuljava.gianadda.util.Files;

public class Main {
    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    enum Flag {
        SYNCHRONIZE, FORCE_HTML;
    }

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
                        case "-s":
                        case "--sync":
                            flags.add(Flag.SYNCHRONIZE);
                            break;
                        case "-f":
                        case "--force-html":
                            flags.add(Flag.SYNCHRONIZE);
                            flags.add(Flag.FORCE_HTML);
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
                default:
                    break loop;
            }
        }
        if (st != -1 || dir == null) {
            usage();
            return false;
        }
        return true;
    }

    private void process() throws IOException {
        try (SiteBuilder builder = new SiteBuilder(dir)) {
            if (createTheme != null) {
                builder.create(createTheme);
            } else {
                builder.open();
            }
            if (changeTheme != null) {
                builder.changeTheme(changeTheme);
            }
            if (flags.contains(Flag.SYNCHRONIZE)) {
                builder.synchronize(flags.contains(Flag.FORCE_HTML));
            }
        }
    }
}
