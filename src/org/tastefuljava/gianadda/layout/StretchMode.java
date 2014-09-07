package org.tastefuljava.gianadda.layout;

import java.awt.Rectangle;

public enum StretchMode {
    FIT("Fit") {
        @Override
        public void adjustRect(Rectangle src, Rectangle dst,
                HorizontalPosition hpos, VerticalPosition vpos,
                int srcDpi, int dstDpi) {
            int sw = src.width;
            int sh = src.height;
            int swdh = sw * dst.height;
            int shdw = sh * dst.width;
            if (swdh < shdw) {
                hpos.adjustWidth(dst, swdh / sh);
            } else {
                vpos.adjustHeight(dst, shdw / sw);
            }
        }

        @Override
        public int cost(int sw, int sh, int dw, int dh,
                int srcDpi, int dstDpi) {
            int swdh = sw * dh;
            int shdw = sh * dw;
            if (swdh < shdw) {
                return swdh-shdw;
            } else {
                return shdw-swdh;
            }
        }
    },

    FILL("Fill") {
        @Override
        public void adjustRect(Rectangle src, Rectangle dst,
                HorizontalPosition hpos, VerticalPosition vpos,
                int srcDpi, int dstDpi) {
            int dw = dst.width;
            int dh = dst.height;
            int swdh = src.width * dh;
            int shdw = src.height * dw;
            if (swdh < shdw) {
                vpos.adjustHeight(src, swdh / dw);
            } else {
                hpos.adjustWidth(src, shdw / dh);
            }
        }

        @Override
        public int cost(int sw, int sh, int dw, int dh,
                int srcDpi, int dstDpi) {
            int swdh = sw * dh;
            int shdw = sh * dw;
            if (swdh < shdw) {
                return swdh-shdw;
            } else {
                return shdw-swdh;
            }
        }
    },
    NONE("None") {
        @Override
        public void adjustRect(Rectangle src, Rectangle dst,
                HorizontalPosition hpos, VerticalPosition vpos,
                int srcDpi, int dstDpi) {
            {
                int swi = src.width * dstDpi;
                int dwi = dst.width * srcDpi;
                if (swi < dwi) {
                    hpos.adjustWidth(dst, swi / srcDpi);
                } else {
                    hpos.adjustWidth(src, dwi / dstDpi);
                }
            }
            {
                int shi = src.height * dstDpi;
                int dhi = dst.height * srcDpi;
                if (shi < dhi) {
                    vpos.adjustHeight(dst, shi / srcDpi);
                } else {
                    vpos.adjustHeight(src, dhi / dstDpi);
                }
            }
        }

        @Override
        public int cost(int sw, int sh, int dw, int dh,
                int srcDpi, int dstDpi) {
            int cost = 0;
            {
                int swi = sw * dstDpi;
                int dwi = dw * srcDpi;
                if (swi < dwi) {
                    cost += dwi-swi;
                } else {
                    cost += swi-dwi;
                }
            }
            {
                int shi = sh * dstDpi;
                int dhi = dh * srcDpi;
                if (shi < dhi) {
                    cost += dhi-shi;
                } else {
                    cost += shi-dhi;
                }
            }
            return cost;
        }
    };

    private final String title;

    private StretchMode(String title) {
        this.title = title;
    }

    abstract public void adjustRect(Rectangle src, Rectangle dst,
            HorizontalPosition hpos, VerticalPosition vpos,
            int srcDpi, int dstDpi);
    abstract public int cost(int sw, int sh, int dw, int dh,
                int srcDpi, int dstDpi);

    @Override
    public String toString() {
        return title;
    }
}
