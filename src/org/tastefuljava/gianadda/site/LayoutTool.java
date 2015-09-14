package org.tastefuljava.gianadda.site;

import java.awt.Dimension;
import java.awt.Rectangle;
import org.tastefuljava.gianadda.layout.HorizontalPosition;
import org.tastefuljava.gianadda.layout.StretchMode;
import org.tastefuljava.gianadda.layout.VerticalPosition;
import org.tastefuljava.gianadda.site.Synchronizer.ImageType;
import org.tastefuljava.gianadda.util.Configuration;

public class LayoutTool {
    private final Configuration conf;

    public LayoutTool(Configuration conf) {
        this.conf = conf;
    }

    public Dimension previewSize(Dimension size, int angle) {
        Dimension dim = ImageType.PREVIEW.getSizeFrom(conf);
        int w, h;
        switch (angle) {
            case 0:
            case 180:
                w = dim.width;
                h = dim.height;
                break;
            case 90:
            case 270:
                w = dim.height;
                h = dim.width;
                break;
            default:
                throw new RuntimeException("Invalid angle " + angle);
        }
        Rectangle src = new Rectangle(0, 0,
                size.width, size.height);
        Rectangle dst = new Rectangle(0, 0, w, h);
        StretchMode.FIT.adjustRect(src, dst, HorizontalPosition.LEFT,
                VerticalPosition.TOP, 0, 0);
        return dst.getSize();
    }
}
