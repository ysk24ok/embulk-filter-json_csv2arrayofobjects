package org.embulk.filter.json_csv2arrayofobjects;

import org.embulk.EmbulkTestRuntime;
import org.embulk.config.ConfigException;
import org.embulk.config.ConfigLoader;
import org.embulk.config.ConfigSource;
import org.embulk.filter.json_csv2arrayofobjects.JsonCsv2arrayofobjectsFilterPlugin.PluginTask;
import org.embulk.spi.Exec;
import org.embulk.spi.Schema;
import org.embulk.spi.SchemaConfigException;
import org.embulk.spi.type.Types;
import org.junit.Rule;
import org.junit.Test;

public class TestJsonCsv2arrayofobjectsFilterPlugin
{
    @Rule
    public EmbulkTestRuntime runtime = new EmbulkTestRuntime();

    public JsonCsv2arrayofobjectsFilterPlugin plugin = new JsonCsv2arrayofobjectsFilterPlugin();

    public static PluginTask taskFromYamlString(String... lines)
    {
        StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            builder.append(line).append("\n");
        }
        String yamlString = builder.toString();

        ConfigLoader loader = new ConfigLoader(Exec.getModelManager());
        ConfigSource config = loader.fromYamlString(yamlString);
        return config.loadConfig(PluginTask.class);
    }

    @Test(expected = SchemaConfigException.class)
    public void validateColumnExists()
    {
        PluginTask task = taskFromYamlString(
            "type: json_csv2arrayofobjects",
            "column: json_payload1",
            "key: key",
            "output_keys:",
            "  - {name: name, type: string}"
        );
        Schema inputSchema = Schema.builder()
            .add("json_payload", Types.STRING)
            .build();
        plugin.validate(task, inputSchema);
    }

    @Test(expected = ConfigException.class)
    public void validateDelimiterAndSubDelimiterShouldNotBeEqual()
    {
        PluginTask task = taskFromYamlString(
            "type: json_csv2arrayofobjects",
            "column: json_payload",
            "key: key",
            "delimiter: \"-\"",
            "output_keys:",
            "  - {name: name, type: string}"
        );
        Schema inputSchema = Schema.builder()
            .add("json_payload", Types.STRING)
            .build();
        plugin.validate(task, inputSchema);
    }
}
