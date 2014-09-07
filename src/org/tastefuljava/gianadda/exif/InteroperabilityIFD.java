package org.tastefuljava.gianadda.exif;

import java.nio.ByteOrder;
import java.util.Map;
import static org.tastefuljava.gianadda.exif.IFD.buildTagMap;

public class InteroperabilityIFD extends IFD {
    private static final Map<Integer,IFD.Tag> TAG_MAP
            = buildTagMap(Tag.values());

    public static enum Tag implements IFD.Tag {
        InteroperabilityIndex(0x0001),
        InteroperabilityVersion(0x0002),
        RelatedImageFileFormat(0x1000),
        RelatedImageWidth(0x1001),
        RelatedImageLength(0x1002);

        private final int tag;

        private Tag(int tag) {
            this.tag = tag;
        }

        @Override
        public int getTag() {
            return tag;
        }

        @Override
        public String getName() {
            return name();
        }
    }

    public InteroperabilityIFD(byte[] data, int start, int position,
            ByteOrder order) {
        super(data, start, position, order);
    }

    @Override
    protected IFD.Tag getTag(int tag) {
        return TAG_MAP.get(tag);
    }
}
