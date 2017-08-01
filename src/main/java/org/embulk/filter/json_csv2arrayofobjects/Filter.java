package org.embulk.filter.json_csv2arrayofobjects;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.embulk.filter.json_csv2arrayofobjects.JsonCsv2arrayofobjectsFilterPlugin.JsonKeyTask;
import org.embulk.filter.json_csv2arrayofobjects.JsonCsv2arrayofobjectsFilterPlugin.PluginTask;
import org.embulk.spi.DataException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Filter
{
    private PluginTask task;

    Filter(PluginTask task)
    {
        this.task = task;
    }

    public String doFilter(String json)
    {
        String jsonPath = String.format("$.%s", task.getKey());
        String val = null;
        try {
            val = JsonPath.parse(json).read(jsonPath);
        }
        // when the key does not exist in json
        catch (PathNotFoundException ex) {
            throw new DataException(ex);
        }
        String delimiter = task.getDelimiter().get();
        String subDelimiter = task.getSubDelimiter().get();
        List<JsonKeyTask> outputKeys = task.getOutputKeys();
        List<Map> l = new ArrayList<Map>();
        String[] vals = val.split(delimiter);
        for (int i = 0; i < vals.length; i++) {
            // skip empty element
            if (vals[i].isEmpty()) {
                continue;
            }
            String[] subvals = vals[i].split(subDelimiter);
            Map<String, Object> m = new HashMap<String, Object>();
            for (int j = 0; j < subvals.length; j++) {
                JsonKeyTask jsonKey = outputKeys.get(j);
                String k = jsonKey.getName();
                m.put(k, StringCast.cast(subvals[j], jsonKey.getType()));
            }
            // add sequence
            if (task.getSequenceName().isPresent()) {
                m.put(task.getSequenceName().get(), i);
            }
            l.add(m);
        }
        return JsonPath.parse(json).set(jsonPath, l).jsonString();
    }
}
