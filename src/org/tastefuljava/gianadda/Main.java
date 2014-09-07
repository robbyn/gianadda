package org.tastefuljava.gianadda;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.tastefuljava.gianadda.site.SiteBuilder;

public class Main {
    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    private boolean sync = false;
    private File dir = null;
    private String theme = null;
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
                        case "-create":
                            st = 1;
                            break;
                        case "-sync":
                            sync = true;
                            break;
                        case "-force-html":
                            forceHtml = true;
                            sync = true;
                            break;
                        default:
                            dir = new File(arg);
                            break;
                    }
                    break;
                case 1:
                    theme = arg;
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
            if (theme == null) {
                builder.open();
                if (sync) {
                    builder.synchronize(forceHtml);
                }
            } else {
                builder.create(theme);
                builder.synchronize(true);
            }
        }
    }
}
