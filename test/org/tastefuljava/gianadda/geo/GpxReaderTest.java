package org.tastefuljava.gianadda.geo;

import java.io.IOException;
import java.io.InputStream;
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
            try (InputStream in = getClass().getResourceAsStream(name)) {
                TrackPoint[] pts = GpxReader.readTrack(in);
            }
        }
    }
}
