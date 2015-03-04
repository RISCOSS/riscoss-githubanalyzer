package eu.riscoss.datacollector.sonar;

import java.util.TreeMap;

/**
 * ClassInfo includes metrics of a Class of an OSS project in SONAR.
 * 
 * @author marc
 */
public class ClassInfo
{
    private final TreeMap<String, Object> metric = new TreeMap<String, Object>();;

    private String className;

    public String getClassName()
    {
        return className;
    }

    public void setClassName(String className)
    {
        this.className = className;
    }

    public Object getMetric(String metricName)
    {
        return metric.get(metricName);
    }

    public void setMetric(String metricName, Object value)
    {
        metric.put(metricName, value);
    }
}
