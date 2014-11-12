package org.tastefuljava.gianadda.geo;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestLatLngBounds {
    
    public TestLatLngBounds() {
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
    public void testCenter() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(new LatLng(45, -179));
        builder.include(new LatLng(48, 179));
        LatLngBounds bounds = builder.build();
        LatLng center = bounds.getCenter();
        assertEquals(46.5, center.getLat(), 0.01);
        assertTrue(Math.abs(center.getLng()-180) < 0.01 ||
                Math.abs(center.getLng()+180) < 0.01);
    }

    @Test
    public void testContains() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(new LatLng(45, -179));
        builder.include(new LatLng(48, 179));
        LatLngBounds bounds = builder.build();
        assertTrue(bounds.contains(46, -180));
        assertTrue(bounds.contains(46, 180));
        assertFalse(bounds.contains(44, -180));
        assertFalse(bounds.contains(49, 180));
        assertFalse(bounds.contains(46, -178));
        assertFalse(bounds.contains(46, 178));
        assertFalse(bounds.contains(0, 0));
    }
}
