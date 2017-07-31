Embulk::JavaPlugin.register_filter(
  "json_csv2arrayofobjects", "org.embulk.filter.json_csv2arrayofobjects.JsonCsv2arrayofobjectsFilterPlugin",
  File.expand_path('../../../../classpath', __FILE__))
