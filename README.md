# Json Csv2arrayofobjects filter plugin for Embulk

An Embulk filter plugin to convert csv in JSON to array of objects.

## Overview

* **Plugin type**: filter

## Configuration

* **column**: input column name (string, required)
* **key**: json path (string, required)
* **delimiter**: delimiter (string, default: `,`)
* **sub_delimiter**: sub-delimiter (string, default: `-`)
* **sequence_name**: key name for sequence of the elements (string, default: `null`)
* **output_keys**: 
  - **name**: name (required)
  - **type**: embulk type (required)

## Example

* a simple example

input:

```json
{"csv1": "1-a,2-b,3-c"}
{"csv1": "10-o,11-p,12-q"}
```

yaml:

```yaml
filters:
  - type: json_csv2arrayofobjects
    column: json1
    key: csv1
    output_keys:
      - {name: number, type: long}
      - {name: code, type: string}
```

output:

```json
{"csv1":[{"code":"a","number":1},{"code":"b","number":2},{"code":"c","number":3}]}
{"csv1":[{"code":"o","number":10},{"code":"p","number":11},{"code":"q","number":12}]}
```

* an another example

input:

```json
{"key": {"csv2": "10.0:x:f-11.5:y:t"}}
{"key": {"csv2": "50.0:A:t-51.5:B:t-52.0:C:t"}}
```

yaml:

```yaml
filters
  - type: json_csv2arrayofobjects
    column: json2
    key: key.csv2
    delimiter: "-"
    sub_delimiter: ":"
    sequence_name: "seq"
    output_keys:
      - {name: decimal, type: double}
      - {name: code, type: string}
      - {name: bool, type: boolean}
```

output:

```json
{"key":{"csv2":[{"code":"x","bool":false,"decimal":10.0,"seq":0},{"code":"y","bool":true,"decimal":11.5,"seq":1}]}}
{"key":{"csv2":[{"code":"A","bool":true,"decimal":50.0,"seq":0},{"code":"B","bool":true,"decimal":51.5,"seq":1},{"code":"C","bool":true,"decimal":52.0,"seq":2}]}}
```

## Thanks

Implementation of [StringCast.java](https://github.com/ysk24ok/embulk-filter-json_csv2arrayofobjects/blob/master/src/main/java/org/embulk/filter/json_csv2arrayofobjects/StringCast.java) use that of [embulk-filter-typecast](https://github.com/sonots/embulk-filter-typecast) as a reference.


## Build

```
$ ./gradlew gem  # -t to watch change of files and rebuild continuously
```
