package org.tastefuljava.gianadda.exif;

import java.nio.ByteOrder;
import java.util.Map;
import static org.tastefuljava.gianadda.exif.IFD.buildTagMap;

public class ExifIFD extends IFD {
    private static final Map<Integer,IFD.Tag> TAG_MAP
            = buildTagMap(Tag.values());

    public static enum Tag implements IFD.Tag {
        ExposureTime(0x829a),
        FNumber(0x829d),
        ExposureProgram(0x8822),
        SpectralSensitivity(0x8824),
        ISOSpeedRatings(0x8827),
        OECF(0x8828),
        SensitivityType(0x8830),
        StandardOutputSensitivity(0x8831),
        RecommendedExposureIndex(0x8832),
        ISOSpeed(0x8833),
        ISOSpeedLatitudeyyy(0x8834),
        ISOSpeedLatitudezzz(0x8835),
        ExifVersion(0x9000),
        DateTimeOriginal(0x9003),
        DateTimeDigitized(0x9004),
        ComponentsConfiguration(0x9101),
        CompressedBitsPerPixel(0x9102),
        ShutterSpeedValue(0x9201),
        ApertureValue(0x9202),
        BrightnessValue(0x9203),
        ExposureBiasValue(0x9204),
        MaxApertureValue(0x9205),
        SubjectDistance(0x9206),
        MeteringMode(0x9207),
        LightSource(0x9208),
        Flash(0x9209),
        FocalLength(0x920a),
        SubjectArea(0x9214),
        MakerNote(0x927c),
        UserComment(0x9286),
        SubSecTime(0x9290),
        SubSecTimeOriginal(0x9291),
        SubSecTimeDigitized(0x9292),
        FlashpixVersion(0xa000),
        ColorSpace(0xa001),
        PixelXDimension(0xa002),
        PixelYDimension(0xa003),
        RelatedSoundFile(0xa004),
        InteroperabilityTag(0xa005),
        FlashEnergy(0xa20b),
        SpatialFrequencyResponse(0xa20c),
        FocalPlaneXResolution(0xa20e),
        FocalPlaneYResolution(0xa20f),
        FocalPlaneResolutionUnit(0xa210),
        SubjectLocation(0xa214),
        ExposureIndex(0xa215),
        SensingMethod(0xa217),
        FileSource(0xa300),
        SceneType(0xa301),
        CFAPattern(0xa302),
        CustomRendered(0xa401),
        ExposureMode(0xa402),
        WhiteBalance(0xa403),
        DigitalZoomRatio(0xa404),
        FocalLengthIn35mmFilm(0xa405),
        SceneCaptureType(0xa406),
        GainControl(0xa407),
        Contrast(0xa408),
        Saturation(0xa409),
        Sharpness(0xa40a),
        DeviceSettingDescription(0xa40b),
        SubjectDistanceRange(0xa40c),
        ImageUniqueID(0xa420),
        CameraOwnerName(0xa430),
        BodySerialNumber(0xa431),
        LensSpecification(0xa432),
        LensMake(0xa433),
        LensModel(0xa434),
        LensSerialNumber(0xa435);

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

    public ExifIFD(byte[] data, int start, int position, ByteOrder order) {
        super(data, start, position, order);
    }

    @Override
    protected IFD.Tag getTag(int tag) {
        return TAG_MAP.get(tag);
    }

}
