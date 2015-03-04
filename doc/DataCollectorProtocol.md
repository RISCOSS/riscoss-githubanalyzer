# RISCOSS data collectors

For use with the RISCOSS Data Collector Manager

## DataCollector Input

These Data Collectors are executable files or scripts which take their input
values as a JSON string piped to standard input. There is a single standard
input value known as "targetEntity", this value is set to the entity name.
All other values in the JSON object corrispond to configuration parameters
which are displayed in the user interface.

An example of running a data collector is as follows.

    echo '{"targetEntity":"xx",'\
        '"url":"http://fossology.ow2.org/?mod=nomoslicense&upload=40&item=306775"}' | \
        java -jar ./riscoss-platform-rdc-fossology-0.24-SNAPSHOT-jar-with-dependencies.jar

You can see the "targetEntity" is known as "xx" and there is a single
parameter called "url", when developing a DataCollector, you must simply
document the names of the parameters which your DataCollector requires as well
as their meanings. When including a DataCollector in the DomainManager, you
must create an XWiki Class with a field for each parameter in your DataCollector.

The DataCollectorManager will take each field from the DataCollector XWiki Object
and include the value in the JSON input when starting the DataCollector.


## DataCollector Output

When a DataCollector runs, it outputs it's result to standard output. The output
begins with `-----BEGIN RISK DATA-----` and ends with `-----END RISK DATA-----`
anything not between these two markers is ignored by the DomainManager.

The content between these markers is a JSON Array structure which is placed
verbatum into the Risk Data Respository. The content of the array is a set of
structures, each structure has three elements. An "id" which is the identifier of
the data point, a "target" which is equal to the input "targetEntity", a "type"
which is one of `NUMBER`, `DISTRIBUTION`, or `EVIDENCE`. An example of output
is below:

```json
[
    {"id":"copyleft-licenses","target":"xx","type":"NUMBER","value":0.0},
    {"id":"files-with-unknown-license","target":"xx","type":"NUMBER","value":0.7352941176470589},
    {"id":"copyleft-licenses-with-linking","target":"xx","type":"NUMBER","value":0.23529411764705882},
    {"id":"percentage-of-files-with-permissive-license","target":"xx","type":"NUMBER","value":0.029411764705882353},
    {"id":"percentage-of-files-without-license","target":"xx","type":"NUMBER","value":0.0},
    {"id":"files-with-commercial-license","target":"xx","type":"NUMBER","value":0.0},
    {"id":"number-of-different-licenses","target":"xx","type":"NUMBER","value":31.0}
]
```
