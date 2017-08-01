package org.embulk.filter.json_csv2arrayofobjects;

import org.embulk.EmbulkTestRuntime;
import org.embulk.filter.json_csv2arrayofobjects.JsonCsv2arrayofobjectsFilterPlugin.PluginTask;
import org.embulk.spi.DataException;
import org.junit.Rule;
import org.junit.Test;

import static org.embulk.filter.json_csv2arrayofobjects.TestJsonCsv2arrayofobjectsFilterPlugin.taskFromYamlString;
import static org.junit.Assert.assertEquals;

public class TestFilter
{
    @Rule
    public EmbulkTestRuntime runtime = new EmbulkTestRuntime();

    @Test
    public void oneOutputKey()
    {
        PluginTask task = taskFromYamlString(
            "type: json_csv2arrayofobjects",
            "column: json_payload",
            "key: key",
            "output_keys:",
            "  - {name: name, type: string}"
        );
        Filter filter = new Filter(task);
        String inputValue = null;
        String got = null;
        String expected = null;
        // element size > 1
        inputValue = "{\"key\": \"a,b,c\"}";
        got = filter.doFilter(inputValue);
        expected = "{\"key\":[{\"name\":\"a\"},{\"name\":\"b\"},{\"name\":\"c\"}]}";
        assertEquals(expected, got);
        // element size = 1
        inputValue = "{\"key\": \"a\"}";
        got = filter.doFilter(inputValue);
        expected = "{\"key\":[{\"name\":\"a\"}]}";
        assertEquals(expected, got);
    }

    @Test
    public void multipleOutputKeys()
    {
        PluginTask task = taskFromYamlString(
            "type: json_csv2arrayofobjects",
            "column: json_payload",
            "key: key",
            "output_keys:",
            "  - {name: name, type: string}",
            "  - {name: number, type: long}"
        );
        Filter filter = new Filter(task);
        String inputValue = null;
        String got = null;
        String expected = null;
        // element size > 1
        inputValue = "{\"key\": \"a-1,b-2,c-3\"}";
        got = filter.doFilter(inputValue);
        expected = "{\"key\":[{\"number\":1,\"name\":\"a\"},{\"number\":2,\"name\":\"b\"},{\"number\":3,\"name\":\"c\"}]}";
        assertEquals(expected, got);
        // element size = 1
        inputValue = "{\"key\": \"a-1\"}";
        got = filter.doFilter(inputValue);
        expected = "{\"key\":[{\"number\":1,\"name\":\"a\"}]}";
        assertEquals(expected, got);
    }

    @Test
    public void nestedJson()
    {
        PluginTask task = taskFromYamlString(
            "type: json_csv2arrayofobjects",
            "column: json_payload",
            "key: key1.key2",
            "output_keys:",
            "  - {name: name, type: string}",
            "  - {name: number, type: double}"
        );
        Filter filter = new Filter(task);
        String inputValue = "{\"key1\": {\"key2\": \"a-1.5\"}}";
        String got = filter.doFilter(inputValue);
        String expected = "{\"key1\":{\"key2\":[{\"number\":1.5,\"name\":\"a\"}]}}";
        assertEquals(expected, got);
    }

    @Test
    public void skipEmptyElement()
    {
        PluginTask task = taskFromYamlString(
            "type: json_csv2arrayofobjects",
            "column: json_payload",
            "key: key",
            "output_keys:",
            "  - {name: name, type: string}",
            "  - {name: number, type: long}"
        );
        Filter filter = new Filter(task);
        String inputValue = "{\"key\": \",a-1,,,b-2,\"}";
        String got = filter.doFilter(inputValue);
        String expected = "{\"key\":[{\"number\":1,\"name\":\"a\"},{\"number\":2,\"name\":\"b\"}]}";
        assertEquals(expected, got);
    }

    @Test
    public void explicitDelimiter()
    {
        PluginTask task = taskFromYamlString(
            "type: json_csv2arrayofobjects",
            "column: json_payload",
            "key: key",
            "delimiter: \":\"",
            "output_keys:",
            "  - {name: name, type: string}",
            "  - {name: tf, type: boolean}"
        );
        Filter filter = new Filter(task);
        String inputValue = "{\"key\": \"a-t:b-f\"}";
        String got = filter.doFilter(inputValue);
        String expected = "{\"key\":[{\"tf\":true,\"name\":\"a\"},{\"tf\":false,\"name\":\"b\"}]}";
        assertEquals(expected, got);
    }

    @Test
    public void explicitSubDelimiter()
    {
        PluginTask task = taskFromYamlString(
            "type: json_csv2arrayofobjects",
            "column: json_payload",
            "key: key",
            "sub_delimiter: \"_\"",
            "output_keys:",
            "  - {name: name, type: string}",
            "  - {name: number, type: long}"
        );
        Filter filter = new Filter(task);
        String inputValue = "{\"key\": \"a_1,b_2\"}";
        String got = filter.doFilter(inputValue);
        String expected = "{\"key\":[{\"number\":1,\"name\":\"a\"},{\"number\":2,\"name\":\"b\"}]}";
        assertEquals(expected, got);
    }

    @Test
    public void explicitSequenceName()
    {
        PluginTask task = taskFromYamlString(
            "type: json_csv2arrayofobjects",
            "column: json_payload",
            "key: key",
            "sequence_name: seq",
            "output_keys:",
            "  - {name: name, type: string}",
            "  - {name: number, type: long}"
        );
        Filter filter = new Filter(task);
        String inputValue = "{\"key\": \"a-1,b-2\"}";
        filter.doFilter(inputValue);
        String got = filter.doFilter(inputValue);
        String expected = "{\"key\":[{\"number\":1,\"name\":\"a\",\"seq\":0},{\"number\":2,\"name\":\"b\",\"seq\":1}]}";
        assertEquals(expected, got);
    }

    @Test(expected = DataException.class)
    public void invalidKey()
    {
        PluginTask task = taskFromYamlString(
            "type: json_csv2arrayofobjects",
            "column: json_payload",
            "key: key1.key2",
            "output_keys:",
            "  - {name: name, type: string}",
            "  - {name: number, type: long}"
        );
        Filter filter = new Filter(task);
        String inputValue = "{\"key\": \"a-1,b-2,c-3\"}";
        filter.doFilter(inputValue);
    }
}
