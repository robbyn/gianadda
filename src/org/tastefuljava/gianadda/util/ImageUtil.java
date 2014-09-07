package org.tastefuljava.gianadda.util;

import org.tastefuljava.gianadda.layout.VerticalPosition;
import org.tastefuljava.gianadda.layout.HorizontalPosition;
import org.tastefuljava.gianadda.layout.StretchMode;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class ImageUtil {
    private static final ResizeAlgorithm RESIZER = ResizeAlgorithm.valueOf(
            System.getProperty("resize-algorithm",
                    ResizeAlgorithm.PROGRESSIVE_BILINEAR.name()));

    private ImageUtil() {
        // Private constructor to prevent instanciation
    }

    public static BufferedImage resize(BufferedImage img, int width,
            int height) {
        return RESIZER.resize(img, width, height);
    }

    public static BufferedImage rotate(BufferedImage img, int angle) {
        AffineTransform trans;
        BufferedImage newImg;
        int w = img.getWidth();
        int h = img.getHeight();
        if (angle == 0) {
            newImg = new BufferedImage(w, h, img.getType());
            trans = null;
        } else if (angle == 90) {
            newImg = new BufferedImage(h, w, img.getType());
            trans = new AffineTransform(0.0, 1.0, -1.0, 0.0, h, 0.0);
        } else if (angle == 180) {
            newImg = new BufferedImage(w, h, img.getType());
            trans = new AffineTransform(-1.0, 0.0, 0.0, -1.0, w, h);
        } else if (angle == 270) {
            newImg = new BufferedImage(h, w, img.getType());
            trans = new AffineTransform(0.0, -1.0, 1.0, 0.0, 0.0, w);
        } else {
            throw new RuntimeException("Invalid angle " + angle);
        }
        Graphics2D g = newImg.createGraphics();
        try {
            if (trans != null) {
                g.transform(trans);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            }
            g.drawImage(img, 0, 0, w, h, null);
        } finally {
            g.dispose();
        }
        return newImg;
    }

    public static BufferedImage rotateAndResize(BufferedImage img, int angle,
            int width, int height) {
        int w, h;
        if (angle == 0 || angle == 180) {
            w = width;
            h = height;
        } else if (angle == 90 || angle == 270) {
            w = height;
            h = width;
        } else {
            throw new RuntimeException("Invalid angle " + angle);
        }
        Rectangle src = new Rectangle(0, 0,
                img.getWidth(), img.getHeight());
        Rectangle dst = new Rectangle(0, 0, w, h);
        StretchMode.FIT.adjustRect(src, dst, HorizontalPosition.LEFT,
                VerticalPosition.TOP, 0, 0);
        img = resize(img, dst.width, dst.height);
        return angle == 0 ? img : rotate(img, angle);
    }
}
