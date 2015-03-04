package eu.riscoss.datacollector.sonar;

/**
 * @author Marc Oriol, Mirko Morandini
 */

import java.util.ArrayList;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.Measure;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.services.ResourceQuery;

import eu.riscoss.datacollector.sonar.ClassInfo;

public class ClassInfoManager
{

    // private static String _host ="http://nemo.sonarqube.org";

    /**
     * Obtains the list of all classes of a project (or module) with their metrics per class.
     * 
     * @param resourceKey the project or module to analyse.
     * @return the list of classes of such project or module with their metrics, -1 if metric value not found.
     */
    public static ArrayList<ClassInfo> getStatisticsPerClass(String host, String resourceKey, String[] metricNames)
    {
        ArrayList<ClassInfo> list = new ArrayList<ClassInfo>();
        Sonar sonar = Sonar.create(host);
        ResourceQuery query = new ResourceQuery(resourceKey);
        query.setMetrics(metricNames);
        query.setScopes("FIL");
        query.setDepth(-1);
        //**************************************************put above and control!
        for (Resource resource : sonar.findAll(query)) {
            ClassInfo classInfo = new ClassInfo();
            classInfo.setClassName(resource.getName());

            for (String metricName : metricNames) {
                Measure measure = resource.getMeasure(metricName);
//                System.err.println(measure);
                if (measure != null) {
                    classInfo.setMetric(metricName, measure.getValue());
                } else {
                    System.err.print("Warning[getClassInfos]: " + metricName + " of " + resource.getName()
                        + " is empty. ");
                    classInfo.setMetric(metricName, new Double(-1.0));
                }
            }
            list.add(classInfo);
        }
        System.err.println();
        return list;
    }



    

}
