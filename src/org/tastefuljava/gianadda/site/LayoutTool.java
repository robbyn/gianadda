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

    public Dimension previewSize(Dimension size) {
        Dimension dim = ImageType.PREVIEW.getSizeFrom(conf);
        Rectangle src = new Rectangle(0, 0,
                size.width, size.height);
        Rectangle dst = new Rectangle(0, 0, dim.width, dim.height);
        StretchMode.FIT.adjustRect(src, dst, HorizontalPosition.LEFT,
                VerticalPosition.TOP, 0, 0);
        return dst.getSize();
    }
}
