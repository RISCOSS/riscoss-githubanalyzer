package eu.riscoss.datacollector.sonar;

/**
 * @author Mirko Morandini
 */

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;

import eu.riscoss.datacollector.RiskDataCollector;
import eu.riscoss.datacollector.common.IndicatorsMap;
import eu.riscoss.rdc.model.Distribution;
import eu.riscoss.rdc.model.RiskDataType;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class SonarRiskDataCollector implements RiskDataCollector
{
    public static String[] singleMetrics; // = { "ncloc", "complexity" };

    public static String[] historyMetrics; // = { "ncloc", "comment_lines" };

    public static String[] fileMetrics; // = { "ncloc", "coverage" };

    public static class SonarStatistics
    {
        public TreeMap<String, Double> singleValues = new TreeMap<String, Double>();

        public TreeMap<String, List<Double>> historyValues = new TreeMap<String, List<Double>>();
    }

    /**
     * extracts and stores Sonar indicators.
     * @param im
     * @param properties needs Sonar_host and Sonar_resourceKey
     */
    /**
     *
     */
    public void createIndicators(IndicatorsMap im, Properties properties)
    {

        String resourceKey = properties.getProperty("Sonar_resourceKey"); // "org.ow2.bonita:bonita-server";

        String host = properties.getProperty("Sonar_host"); // "org.ow2.bonita:bonita-server";

        singleMetrics = properties.getProperty("Sonar_singleMetrics", "ncloc, complexity").split("\\s*\\,\\s*");
        historyMetrics = properties.getProperty("Sonar_historyMetrics", "ncloc, comment_lines").split("\\s*\\,\\s*");
        //fileMetrics takes ncloc, complexity as default if not defined:
        fileMetrics = properties.getProperty("Sonar_by_file_Metrics", "ncloc, complexity").split("\\s*\\,\\s*");

        //retrieve data for single values and for history
        SonarStatistics statistics = getStatistics(host, resourceKey);
        //retrieve data per class
        ArrayList<ClassInfo> classvalueslist = ClassInfoManager.getStatisticsPerClass(host, resourceKey, fileMetrics);
        //elaborate per-class data to get the needed indicators

        //*****************************
        //calculate specific indicators
        SonarQuality.createIndicators(statistics, fileMetrics, classvalueslist);
        //*****************************

        if (statistics != null) {
            for (String key : statistics.singleValues.keySet()) {
                im.add("Sonar " + key, statistics.singleValues.get(key));
            }

            for (String key : statistics.historyValues.keySet()) {
                List<Double> l = statistics.historyValues.get(key);
                Distribution d = new Distribution();
                d.setValues(l);
                im.add("Sonar History " + key, RiskDataType.DISTRIBUTION, d);
            }
        }
        //implement also for list

    }

    /**
     * Gets the measures for every metric in the lists singleMetric and historyMetric
     */
    protected SonarStatistics getStatistics(String host, String resourceKey)
    {

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Date from = null;
        Date to = null;
        try {
            from = df.parse("01/01/2012");
            Date today = new Date();
            to = today;
        } catch (Exception e) {
            e.printStackTrace();
        }

        SonarStatistics stats = new SonarStatistics();

        for (String m : singleMetrics) {
            //value stored inside the method. If not found, no value is stored
            RetrieveStats.getStoreCurrentValue(host, resourceKey, m, stats);
            //stats.singleValues.put(m, RetrieveStats.getCurrentValue(host,resourceKey, m));
        }

        for (String m : historyMetrics) {
            ArrayList<Double> val;
            if ((val = RetrieveStats.getHistoricValues(host, resourceKey, m, from, to)) != null) {
                stats.historyValues.put(m, val);
            }
        }

        return stats;
    }
}
