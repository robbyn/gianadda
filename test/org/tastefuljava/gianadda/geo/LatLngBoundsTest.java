package org.tastefuljava.gianadda.geo;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class LatLngBoundsTest {
    public LatLngBoundsTest() {
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
    public void testBuild() {
        LatLngBounds bounds = LatLngBounds.build(
                new LatLng(45, -100),
                new LatLng(48, 100));
        assertEquals(new LatLngBounds(45, 100, 48, -100), bounds);
        bounds = LatLngBounds.build(
                new LatLng(45, -100),
                new LatLng(46, 0),
                new LatLng(48, 100));
        assertEquals(new LatLngBounds(45, -100, 48, 100), bounds);
        bounds = LatLngBounds.build(
                new LatLng(45, -100),
                new LatLng(48, 100),
                new LatLng(46, 0));
        assertEquals(new LatLngBounds(45, 100, 48, 0), bounds);
    }

    @Test
    public void testCenter() {
        LatLngBounds bounds = LatLngBounds.build(
                new LatLng(45, -179),
                new LatLng(48, 179));
        LatLng center = bounds.getCenter();
        assertEquals(46.5, center.getLat(), 0.01);
        assertTrue(Math.abs(center.getLng()-180) < 0.01 ||
                Math.abs(center.getLng()+180) < 0.01);
    }

    @Test
    public void testContains() {
        LatLngBounds bounds = LatLngBounds.build(
                new LatLng(45, -179),
                new LatLng(48, 179));
        assertTrue(bounds.contains(46, -180));
        assertTrue(bounds.contains(46, 180));
        assertFalse(bounds.contains(44, -180));
        assertFalse(bounds.contains(49, 180));
        assertFalse(bounds.contains(46, -178));
        assertFalse(bounds.contains(46, 178));
        assertFalse(bounds.contains(0, 0));
        bounds = LatLngBounds.build(
                new LatLng(45, -100),
                new LatLng(48, 100));
        assertTrue(bounds.contains(46, 180));
        bounds = LatLngBounds.build(
                new LatLng(45, -100),
                new LatLng(46, 0),
                new LatLng(48, 100));
        assertFalse(bounds.contains(46, 180));
        bounds = LatLngBounds.build(
                new LatLng(45, -100),
                new LatLng(48, 100),
                new LatLng(46, 0));
        assertTrue(bounds.contains(46, 180));
    }
}
