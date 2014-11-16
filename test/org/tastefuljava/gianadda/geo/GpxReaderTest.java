package org.tastefuljava.gianadda.geo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class GpxReaderTest {
    
    public GpxReaderTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testReader() throws IOException {
        for (int i = 0; i < 10; ++i) {
            String name = "gpx/sample" + i + ".gpx";
            String gpx;
            TrackPoint[] pts;
            try (InputStream in = getClass().getResourceAsStream(name)) {
                pts = GpxReader.readTrack(in);
                assertNotNull(pts);
                assertTrue(pts.length > 0);
                for (TrackPoint pt: pts) {
                    assertNotNull(pt.getTime());
                }
                StringWriter out = new StringWriter();
                GpxWriter.writeTrack(pts, out);
                gpx = out.toString();
            }
            try (InputStream in
                    = new ByteArrayInputStream(gpx.getBytes("UTF-8"))) {
                TrackPoint[] pts2 = GpxReader.readTrack(in);
                assertArrayEquals(pts, pts2);
            }
        }
    }
}
