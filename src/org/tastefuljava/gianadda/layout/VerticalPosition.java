package org.tastefuljava.gianadda.layout;

import java.awt.Rectangle;

public enum VerticalPosition {
    TOP("Top") {
        @Override
        void adjustHeight(Rectangle rc, int h) {
            rc.height = h;
        }
    },
    BOTTOM("Bottom") {
        @Override
        void adjustHeight(Rectangle rc, int h) {
            rc.y += rc.height - h;
            rc.height = h;
        }
    },
    MIDDLE("Middle") {
        @Override
        void adjustHeight(Rectangle rc, int h) {
            rc.y += (rc.height - h) / 2;
            rc.height = h;
        }
    };

    private String title;

    private VerticalPosition(String title) {
        this.title = title;
    }

    abstract void adjustHeight(Rectangle rc, int h);

    @Override
    public String toString() {
        return title;
    }
}
