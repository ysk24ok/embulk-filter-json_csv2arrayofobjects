package org.embulk.filter.json_csv2arrayofobjects;

import com.google.common.base.Optional;

import org.embulk.config.Config;
import org.embulk.config.ConfigDefault;
import org.embulk.config.ConfigException;
import org.embulk.config.ConfigSource;
import org.embulk.config.Task;
import org.embulk.config.TaskSource;
import org.embulk.spi.Column;
import org.embulk.spi.Exec;
import org.embulk.spi.FilterPlugin;
import org.embulk.spi.Page;
import org.embulk.spi.PageBuilder;
import org.embulk.spi.PageOutput;
import org.embulk.spi.PageReader;
import org.embulk.spi.Schema;
import org.embulk.spi.type.Type;

import java.util.List;

public class JsonCsv2arrayofobjectsFilterPlugin
        implements FilterPlugin
{
    public interface PluginTask
            extends Task
    {
        @Config("column")
        public String getColumn();

        @Config("key")
        public String getKey();

        @Config("delimiter")
        @ConfigDefault("\",\"")
        public Optional<String> getDelimiter();

        @Config("sub_delimiter")
        @ConfigDefault("\"-\"")
        public Optional<String> getSubDelimiter();

        @Config("sequence_name")
        @ConfigDefault("null")
        public Optional<String> getSequenceName();

        @Config("output_keys")
        public List<JsonKeyTask> getOutputKeys();
    }

    public interface JsonKeyTask
            extends Task
    {
        @Config("name")
        public String getName();

        @Config("type")
        public Type getType();
    }

    public void validate(PluginTask task, Schema inputSchema)
    {
        // throws exception when the column does not exist
        Column column = inputSchema.lookupColumn(task.getColumn());
        Type colType = column.getType();
        // delimiter and sub_delimtier should not be equal
        String delimiter = task.getDelimiter().get();
        String subDelimiter = task.getSubDelimiter().get();
        if (delimiter.equals(subDelimiter)) {
            String errMsg = "delimiter and sub_delimiter should not be equal";
            throw new ConfigException(errMsg);
        }
    }

    @Override
    public void transaction(ConfigSource config, Schema inputSchema,
            FilterPlugin.Control control)
    {
        PluginTask task = config.loadConfig(PluginTask.class);
        validate(task, inputSchema);
        Schema outputSchema = inputSchema;
        control.run(task.dump(), outputSchema);
    }

    @Override
    public PageOutput open(TaskSource taskSource, Schema inputSchema,
            Schema outputSchema, PageOutput output)
    {
        PluginTask task = taskSource.loadTask(PluginTask.class);
        PageBuilder pageBuilder = new PageBuilder(
            Exec.getBufferAllocator(), outputSchema, output);
        PageReader pageReader = new PageReader(inputSchema);
        Filter filter = new Filter(task);
        ColumnVisitorImpl visitor = new ColumnVisitorImpl(
            pageReader, pageBuilder, filter, task);
        return new PageOutputImpl(
            pageReader, pageBuilder, outputSchema, visitor);
    }

    public static class PageOutputImpl implements PageOutput
    {
        private PageReader pageReader;
        private PageBuilder pageBuilder;
        private Schema outputSchema;
        private ColumnVisitorImpl visitor;

        PageOutputImpl(PageReader pageReader, PageBuilder pageBuilder, Schema outputSchema, ColumnVisitorImpl visitor)
        {
            this.pageReader = pageReader;
            this.pageBuilder = pageBuilder;
            this.outputSchema = outputSchema;
            this.visitor = visitor;
        }

        @Override
        public void add(Page page)
        {
            pageReader.setPage(page);
            while (pageReader.nextRecord()) {
                outputSchema.visitColumns(visitor);
                pageBuilder.addRecord();
            }
        }

        @Override
        public void finish()
        {
            pageBuilder.finish();
        }

        @Override
        public void close()
        {
            pageBuilder.close();
        }
    };
}
