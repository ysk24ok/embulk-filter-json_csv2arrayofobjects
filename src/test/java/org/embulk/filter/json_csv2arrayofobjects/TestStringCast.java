package org.embulk.filter.json_csv2arrayofobjects;

import org.embulk.EmbulkTestRuntime;
import org.embulk.config.ConfigException;
import org.embulk.spi.DataException;
import org.embulk.spi.type.Types;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestStringCast
{
    @Rule
    public EmbulkTestRuntime runtime = new EmbulkTestRuntime();

    @Test
    public void asBoolean()
    {
        for (String str : StringCast.TRUE_STRINGS) {
            assertEquals(true, StringCast.asBoolean(str));
        }
        for (String str : StringCast.FALSE_STRINGS) {
            assertEquals(false, StringCast.asBoolean(str));
        }
        try {
            StringCast.asBoolean("foo");
            fail();
        }
        catch (Throwable t) {
            assertTrue(t instanceof DataException);
        }
    }

    @Test
    public void asDouble()
    {
        assertEquals(1.0, StringCast.asDouble("1"), 0.0);
        assertEquals(1.5, StringCast.asDouble("1.5"), 0.0);
        try {
            StringCast.asDouble("foo");
            fail();
        }
        catch (Throwable t) {
            assertTrue(t instanceof DataException);
        }
    }

    @Test
    public void asLong()
    {
        assertEquals(1, StringCast.asLong("1"));
        try {
            StringCast.asLong("1.5");
            fail();
        }
        catch (Throwable t) {
            assertTrue(t instanceof DataException);
        }
        try {
            StringCast.asLong("foo");
            fail();
        }
        catch (Throwable t) {
            assertTrue(t instanceof DataException);
        }
    }

    @Test
    public void asString()
    {
        assertEquals("1", StringCast.asString("1"));
        assertEquals("1.5", StringCast.asString("1.5"));
        assertEquals("foo", StringCast.asString("foo"));
    }

    @Test
    public void cast()
    {
        assertTrue((boolean) StringCast.cast("true", Types.BOOLEAN));
        assertEquals(1.5, (Double) StringCast.cast("1.5", Types.DOUBLE), 0.0);
        assertEquals(1L, StringCast.cast("1", Types.LONG));
        assertEquals("foo", StringCast.cast("foo", Types.STRING));
    }

    @Test(expected = ConfigException.class)
    public void castToJson()
    {
        StringCast.cast("{\"a\":1}", Types.JSON);
    }

    @Test(expected = ConfigException.class)
    public void castToTimestamp()
    {
        StringCast.cast("2012-12-12 12:12:12.121212", Types.TIMESTAMP);
    }
}
