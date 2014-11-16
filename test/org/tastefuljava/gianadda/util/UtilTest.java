package org.tastefuljava.gianadda.util;

import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class UtilTest {
    
    public UtilTest() {
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
    public void testXsd() {
        TimeZone gmt = TimeZone.getTimeZone("GMT");
        TimeZone tz = new SimpleTimeZone(1000*(60*(2*60+0)), "+02:00");
        Calendar cal = Calendar.getInstance(gmt);
        cal.set(Calendar.YEAR, 2014);
        cal.set(Calendar.MONTH, 10);
        cal.set(Calendar.DAY_OF_MONTH, 16);
        cal.set(Calendar.HOUR_OF_DAY, 19);
        cal.set(Calendar.MINUTE, 18);
        cal.set(Calendar.SECOND, 34);
        cal.set(Calendar.MILLISECOND, 317);
        Date date = cal.getTime();
        cal.set(Calendar.MILLISECOND, 0);
        Date rounded = cal.getTime();

        assertEquals("2014-11-16T19:18:34.317Z",
                Util.formatXsdDateTime(date, gmt));
        assertEquals("2014-11-16T21:18:34.317+02:00",
                Util.formatXsdDateTime(date, tz));

        assertEquals(date,
                Util.parseXsdDateTime("2014-11-16T19:18:34.317Z"));
        assertEquals(date,
                Util.parseXsdDateTime("2014-11-16T21:18:34.317056+02:00"));
        assertEquals(rounded,
                Util.parseXsdDateTime("2014-11-16T19:18:34Z"));
    }
}
