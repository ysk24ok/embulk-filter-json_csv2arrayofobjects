package org.embulk.filter.json_csv2arrayofobjects;

import com.google.common.collect.ImmutableSet;
import org.embulk.config.ConfigException;
import org.embulk.spi.DataException;
import org.embulk.spi.type.BooleanType;
import org.embulk.spi.type.DoubleType;
import org.embulk.spi.type.JsonType;
import org.embulk.spi.type.LongType;
import org.embulk.spi.type.StringType;
import org.embulk.spi.type.TimestampType;
import org.embulk.spi.type.Type;

public class StringCast
{
    public static final ImmutableSet<String> TRUE_STRINGS =
        ImmutableSet.of(
                "true", "True", "TRUE",
                "yes", "Yes", "YES",
                "t", "T", "y", "Y",
                "on", "On", "ON",
                "1");

    public static final ImmutableSet<String> FALSE_STRINGS =
        ImmutableSet.of(
                "false", "False", "FALSE",
                "no", "No", "NO",
                "f", "F", "n", "N",
                "off", "Off", "OFF",
                "0");

    private StringCast() {}

    public static String buildErrorMessage(String as, String value)
    {
        return String.format("Cannot cast String to %s: \"%s\"", as, value);
    }

    public static boolean asBoolean(String value)
    {
        if (TRUE_STRINGS.contains(value)) {
            return true;
        }
        else if (FALSE_STRINGS.contains(value)) {
            return false;
        }
        else {
            throw new DataException(buildErrorMessage("boolean", value));
        }
    }

    public static double asDouble(String value)
    {
        try {
            return Double.parseDouble(value);
        }
        catch (NumberFormatException ex) {
            throw new DataException(buildErrorMessage("double", value), ex);
        }
    }

    public static long asLong(String value)
    {
        try {
            return Long.parseLong(value);
        }
        catch (NumberFormatException ex) {
            throw new DataException(buildErrorMessage("long", value), ex);
        }
    }

    public static String asString(String value)
    {
        return value;
    }

    public static Object cast(String value, Type outputType)
    {
        if (outputType instanceof BooleanType) {
            return asBoolean(value);
        }
        else if (outputType instanceof DoubleType) {
            return asDouble(value);
        }
        else if (outputType instanceof LongType) {
            return asLong(value);
        }
        else if (outputType instanceof StringType) {
            return asString(value);
        }
        else if (outputType instanceof JsonType) {
            throw new ConfigException("Casting to json is not supported.");
        }
        else if (outputType instanceof TimestampType) {
            throw new ConfigException("Casting to timestamp is not supported.");
        }
        else {
            throw new ConfigException(String.format("Invalid type: %s", outputType));
        }
    }
}
