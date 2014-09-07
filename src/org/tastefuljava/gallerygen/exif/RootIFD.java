package org.tastefuljava.gallerygen.exif;

import java.nio.ByteOrder;
import java.util.Map;

public class RootIFD extends IFD {
    private static final Map<Integer,IFD.Tag> TAG_MAP
            = buildTagMap(Tag.values(), IFDTag.values());

    public static enum Tag implements IFD.Tag {
        ProcessingSoftware(0x000b),
        NewSubfileType(0x00fe),
        SubfileType(0x00ff),
        ImageWidth(0x0100),
        ImageLength(0x0101),
        BitsPerSample(0x0102),
        Compression(0x0103),
        PhotometricInterpretation(0x0106),
        Threshholding(0x0107),
        CellWidth(0x0108),
        CellLength(0x0109),
        FillOrder(0x010a),
        DocumentName(0x010d),
        ImageDescription(0x010e),
        Make(0x010f),
        Model(0x0110),
        StripOffsets(0x0111),
        Orientation(0x0112),
        SamplesPerPixel(0x0115),
        RowsPerStrip(0x0116),
        StripByteCounts(0x0117),
        XResolution(0x011a),
        YResolution(0x011b),
        PlanarConfiguration(0x011c),
        GrayResponseUnit(0x0122),
        GrayResponseCurve(0x0123),
        T4Options(0x0124),
        T6Options(0x0125),
        ResolutionUnit(0x0128),
        TransferFunction(0x012d),
        Software(0x0131),
        DateTime(0x0132),
        Artist(0x013b),
        HostComputer(0x013c),
        Predictor(0x013d),
        WhitePoint(0x013e),
        PrimaryChromaticities(0x013f),
        ColorMap(0x0140),
        HalftoneHints(0x0141),
        TileWidth(0x0142),
        TileLength(0x0143),
        TileOffsets(0x0144),
        TileByteCounts(0x0145),
        SubIFDs(0x014a),
        InkSet(0x014c),
        InkNames(0x014d),
        NumberOfInks(0x014e),
        DotRange(0x0150),
        TargetPrinter(0x0151),
        ExtraSamples(0x0152),
        SampleFormat(0x0153),
        SMinSampleValue(0x0154),
        SMaxSampleValue(0x0155),
        TransferRange(0x0156),
        ClipPath(0x0157),
        XClipPathUnits(0x0158),
        YClipPathUnits(0x0159),
        Indexed(0x015a),
        JPEGTables(0x015b),
        OPIProxy(0x015f),
        JPEGProc(0x0200),
        JPEGInterchangeFormat(0x0201),
        JPEGInterchangeFormatLength(0x0202),
        JPEGRestartInterval(0x0203),
        JPEGLosslessPredictors(0x0205),
        JPEGPointTransforms(0x0206),
        JPEGQTables(0x0207),
        JPEGDCTables(0x0208),
        JPEGACTables(0x0209),
        YCbCrCoefficients(0x0211),
        YCbCrSubSampling(0x0212),
        YCbCrPositioning(0x0213),
        ReferenceBlackWhite(0x0214),
        XMLPacket(0x02bc),
        Rating(0x4746),
        RatingPercent(0x4749),
        ImageID(0x800d),
        CFARepeatPatternDim(0x828d),
        CFAPattern(0x828e),
        BatteryLevel(0x828f),
        Copyright(0x8298),
        ExposureTime(0x829a),
        FNumber(0x829d),
        IPTCNAA(0x83bb),
        ImageResources(0x8649),
//        ExifTag(0x8769),
        InterColorProfile(0x8773),
        ExposureProgram(0x8822),
        SpectralSensitivity(0x8824),
//        GPSTag(0x8825),
        ISOSpeedRatings(0x8827),
        OECF(0x8828),
        Interlace(0x8829),
        TimeZoneOffset(0x882a),
        SelfTimerMode(0x882b),
        DateTimeOriginal(0x9003),
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
        FlashEnergy(0x920b),
        SpatialFrequencyResponse(0x920c),
        Noise(0x920d),
        FocalPlaneXResolution(0x920e),
        FocalPlaneYResolution(0x920f),
        FocalPlaneResolutionUnit(0x9210),
        ImageNumber(0x9211),
        SecurityClassification(0x9212),
        ImageHistory(0x9213),
        SubjectLocation(0x9214),
        ExposureIndex(0x9215),
        TIFFEPStandardID(0x9216),
        SensingMethod(0x9217),
        XPTitle(0x9c9b),
        XPComment(0x9c9c),
        XPAuthor(0x9c9d),
        XPKeywords(0x9c9e),
        XPSubject(0x9c9f),
        PrintImageMatching(0xc4a5),
        DNGVersion(0xc612),
        DNGBackwardVersion(0xc613),
        UniqueCameraModel(0xc614),
        LocalizedCameraModel(0xc615),
        CFAPlaneColor(0xc616),
        CFALayout(0xc617),
        LinearizationTable(0xc618),
        BlackLevelRepeatDim(0xc619),
        BlackLevel(0xc61a),
        BlackLevelDeltaH(0xc61b),
        BlackLevelDeltaV(0xc61c),
        WhiteLevel(0xc61d),
        DefaultScale(0xc61e),
        DefaultCropOrigin(0xc61f),
        DefaultCropSize(0xc620),
        ColorMatrix1(0xc621),
        ColorMatrix2(0xc622),
        CameraCalibration1(0xc623),
        CameraCalibration2(0xc624),
        ReductionMatrix1(0xc625),
        ReductionMatrix2(0xc626),
        AnalogBalance(0xc627),
        AsShotNeutral(0xc628),
        AsShotWhiteXY(0xc629),
        BaselineExposure(0xc62a),
        BaselineNoise(0xc62b),
        BaselineSharpness(0xc62c),
        BayerGreenSplit(0xc62d),
        LinearResponseLimit(0xc62e),
        CameraSerialNumber(0xc62f),
        LensInfo(0xc630),
        ChromaBlurRadius(0xc631),
        AntiAliasStrength(0xc632),
        ShadowScale(0xc633),
        DNGPrivateData(0xc634),
        MakerNoteSafety(0xc635),
        CalibrationIlluminant1(0xc65a),
        CalibrationIlluminant2(0xc65b),
        BestQualityScale(0xc65c),
        RawDataUniqueID(0xc65d),
        OriginalRawFileName(0xc68b),
        OriginalRawFileData(0xc68c),
        ActiveArea(0xc68d),
        MaskedAreas(0xc68e),
        AsShotICCProfile(0xc68f),
        AsShotPreProfileMatrix(0xc690),
        CurrentICCProfile(0xc691),
        CurrentPreProfileMatrix(0xc692),
        ColorimetricReference(0xc6bf),
        CameraCalibrationSignature(0xc6f3),
        ProfileCalibrationSignature(0xc6f4),
        AsShotProfileName(0xc6f6),
        NoiseReductionApplied(0xc6f7),
        ProfileName(0xc6f8),
        ProfileHueSatMapDims(0xc6f9),
        ProfileHueSatMapData1(0xc6fa),
        ProfileHueSatMapData2(0xc6fb),
        ProfileToneCurve(0xc6fc),
        ProfileEmbedPolicy(0xc6fd),
        ProfileCopyright(0xc6fe),
        ForwardMatrix1(0xc714),
        ForwardMatrix2(0xc715),
        PreviewApplicationName(0xc716),
        PreviewApplicationVersion(0xc717),
        PreviewSettingsName(0xc718),
        PreviewSettingsDigest(0xc719),
        PreviewColorSpace(0xc71a),
        PreviewDateTime(0xc71b),
        RawImageDigest(0xc71c),
        OriginalRawFileDigest(0xc71d),
        SubTileBlockSize(0xc71e),
        RowInterleaveFactor(0xc71f),
        ProfileLookTableDims(0xc725),
        ProfileLookTableData(0xc726),
        OpcodeList1(0xc740),
        OpcodeList2(0xc741),
        OpcodeList3(0xc74e),
        NoiseProfile(0xc761);

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

    public static enum IFDTag implements IFD.IFDTag {
        Exif(0x8769, ExifIFD.class),
        GPS(0x8825, GPSIFD.class),
        Interoperability(0xA005, InteroperabilityIFD.class);

        private final int tag;
        private final Class<? extends IFD> clazz;

        private IFDTag(int tag, Class<? extends IFD> clazz) {
            this.tag = tag;
            this.clazz = clazz;
        }

        @Override
        public int getTag() {
            return tag;
        }

        @Override
        public String getName() {
            return name();
        }

        @Override
        public IFD create(byte[] data, int start, int position,
                ByteOrder order) {
            return newInstance(clazz, data, start, position, order);
        }
    }

    public RootIFD(byte[] data, int start, int position, ByteOrder order) {
        super(data, start, position, order);
    }

    @Override
    protected IFD.Tag getTag(int tag) {
        return TAG_MAP.get(tag);
    }

    public ExifIFD getExifIFD() {
        return (ExifIFD)getIFD(IFDTag.Exif);
    }

    public GPSIFD getGPSIFD() {
        return (GPSIFD)getIFD(IFDTag.GPS);
    }

    public InteroperabilityIFD getInteroperabilityIFD() {
        return (InteroperabilityIFD)getIFD(IFDTag.Interoperability);
    }
}
