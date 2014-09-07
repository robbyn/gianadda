package org.tastefuljava.gianadda;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.tastefuljava.gianadda.site.SiteBuilder;

public class Main {
    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    private File dir = null;
    private String createTheme = null;
    private String changeTheme = null;
    private boolean sync = false;
    private boolean forceHtml = false;

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
        System.out.println(
                "Usage: gianadda [-create <theme>] <directory>");
    }

    private boolean processArgs(String[] args) {
        int st = 0;
        for (String arg: args) {
            switch (st) {
                case 0:
                    switch (arg) {
                        case "-c":
                        case "--create":
                            st = 1;
                            break;
                        case "-s":
                        case "--sync":
                            sync = true;
                            break;
                        case "-f":
                        case "-force-html":
                            forceHtml = true;
                            sync = true;
                            break;
                        case "-t":
                        case "--change-theme":
                            st = 2;
                            break;
                        default:
                            dir = new File(arg);
                            break;
                    }
                    break;
                case 1:
                    createTheme = arg;
                    st = 0;
                    break;
                case 2:
                    changeTheme = arg;
                    st = 0;
                    break;
            }
        }
        if (st != 0 || dir == null) {
            usage();
            return false;
        }
        return true;
    }

    private void process() throws IOException {
        try (SiteBuilder builder = new SiteBuilder(dir)) {
            if (createTheme != null) {
                builder.create(createTheme);
                builder.synchronize(true);
            } else {
                builder.open();
                if (sync) {
                    builder.synchronize(forceHtml);
                }
            }
            if (changeTheme != null) {
                builder.changeTheme(changeTheme);
                builder.synchronize(true);
            }
        }
    }
}
