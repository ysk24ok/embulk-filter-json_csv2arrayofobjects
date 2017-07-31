package org.embulk.filter.json_csv2arrayofobjects;

import org.embulk.filter.json_csv2arrayofobjects.JsonCsv2arrayofobjectsFilterPlugin.PluginTask;
import org.embulk.spi.Column;
import org.embulk.spi.ColumnVisitor;
import org.embulk.spi.DataException;
import org.embulk.spi.PageBuilder;
import org.embulk.spi.PageReader;
import org.embulk.spi.json.JsonParseException;
import org.embulk.spi.json.JsonParser;
import org.msgpack.value.Value;

public class ColumnVisitorImpl implements ColumnVisitor
{
    private final PageReader pageReader;
    private final PageBuilder pageBuilder;
    private final Filter filter;
    private final PluginTask task;
    private final JsonParser jsonParser;

    ColumnVisitorImpl(PageReader reader, PageBuilder builder, Filter filter, PluginTask task)
    {
        this.pageReader = reader;
        this.pageBuilder = builder;
        this.filter = filter;
        this.task = task;
        this.jsonParser = new JsonParser();
    }

    @Override
    public void booleanColumn(Column column)
    {
        if (pageReader.isNull(column)) {
            pageBuilder.setNull(column);
        }
        else {
            pageBuilder.setBoolean(column, pageReader.getBoolean(column));
        }
    }

    @Override
    public void longColumn(Column column)
    {
        if (pageReader.isNull(column)) {
            pageBuilder.setNull(column);
        }
        else {
            pageBuilder.setLong(column, pageReader.getLong(column));
        }
    }

    @Override
    public void doubleColumn(Column column)
    {
        if (pageReader.isNull(column)) {
            pageBuilder.setNull(column);
        }
        else {
            pageBuilder.setDouble(column, pageReader.getDouble(column));
        }
    }

    @Override
    public void stringColumn(Column column)
    {
        if (pageReader.isNull(column)) {
            pageBuilder.setNull(column);
        }
        else {
            if (column.getName().equals(task.getColumn())) {
                String inputValue = pageReader.getString(column);
                String outputValue = filter.doFilter(inputValue);
                pageBuilder.setString(column, outputValue);
            // do nothing if this column is not the target one
            }
            else {
                pageBuilder.setString(column, pageReader.getString(column));
            }
        }
    }

    @Override
    public void timestampColumn(Column column)
    {
        if (pageReader.isNull(column)) {
            pageBuilder.setNull(column);
        }
        else {
            pageBuilder.setTimestamp(column, pageReader.getTimestamp(column));
        }
    }

    @Override
    public void jsonColumn(Column column)
    {
        if (pageReader.isNull(column)) {
            pageBuilder.setNull(column);
        }
        else {
            if (column.getName().equals(task.getColumn())) {
                String inputValue = pageReader.getJson(column).toString();
                String outputValue = filter.doFilter(inputValue);
                Value outputValueAsJson = null;
                try {
                    outputValueAsJson = jsonParser.parse(outputValue);
                }
                catch (JsonParseException ex) {
                    throw new DataException(ex);
                }
                pageBuilder.setJson(column, outputValueAsJson);
            }
            // do nothing if this column is not the target one
            else {
                pageBuilder.setJson(column, pageReader.getJson(column));
            }
        }
    }
}
