/**
 *
 */
package eu.riscoss.datacollector.sonar;

import java.util.ArrayList;
import java.util.Arrays;

import eu.riscoss.datacollector.sonar.SonarRiskDataCollector.SonarStatistics;

/**
 * @author marc, mirko
 */
public class SonarQuality
{
    private static final int L0_MAX = 10;

    private static final int L1_MAX = 20;

    private static final int L2_MAX = 50;

    /**
     * creates specific Quality indicators, stored in "statistics"
     */
    public static void createIndicators(SonarStatistics statistics, String[] fileMetrics,
            ArrayList<ClassInfo> classvalueslist)
    {

        createIndicators_Quality_PerClass(statistics, fileMetrics, classvalueslist);
        createIndicators_Quality_Java(statistics);
    }

    /**
     * Puts specific per_class indicators into stats, if classvalueslist not empty.
     */
    public static void createIndicators_Quality_PerClass(SonarStatistics stats, String[] metricNames,
            ArrayList<ClassInfo> classvalueslist)
    {

        // ArrayList<ClassInfo> list = getClassInfos(host, resourcekey, metricNames);
        if (classvalueslist != null && classvalueslist.size() != 0) {
            if (Arrays.asList(metricNames).contains("complexity") && Arrays.asList(metricNames).contains("ncloc")) {
                double[] complexityPercentage = getComplexityPercentage(classvalueslist);

                for (int i = 0; i < 3; i++) {
                    System.err.println("complexityPercentage[" + i + "]=" + complexityPercentage[i]);
                }

                double cRank = 0;

                // complexity rank table. Note: "< 1" instead of "0" to avoid outliers and imprecisions with
                // double
                if (complexityPercentage[0] <= 0.25 && complexityPercentage[1] < 0.01 &&
                        complexityPercentage[2] < 0.01)
                {
                    cRank = 2;
                } else if (complexityPercentage[0] <= 0.30 && complexityPercentage[1] <= 0.05
                        && complexityPercentage[2] < 0.01)
                {
                    cRank = 1;
                } else if (complexityPercentage[0] <= 0.40 && complexityPercentage[1] <= 0.10
                        && complexityPercentage[2] < 0.01)
                {
                    cRank = 0;
                } else if (complexityPercentage[0] <= 0.50 && complexityPercentage[1] <= 0.15
                        && complexityPercentage[2] <= 0.05)
                {
                    cRank = -1;
                } else {
                    cRank = -2;
                }

                stats.singleValues.put("complexity_Rank_Java", cRank);
            }
        }
        // return list;
    }

    /**
     * Calculates quality indicators based on single measures.
     */
    public static void createIndicators_Quality_Java(SonarStatistics stats)
    {
        // MY Measure (software productivity research, Feb. 2006)
        // For Java, it is kLOC / 8.2
        // range: (+2) 0-8, (+1) 8-30, (0) 30-80, (-1) 80-160, (-2) >160 (volume rank in parentheses)

        double ncloc_norm = 0;

        if (stats.singleValues.containsKey("ncloc")) {

            ncloc_norm = stats.singleValues.get("ncloc") / 8200;

            stats.singleValues.put("ncloc_normalised_Java", ncloc_norm);

            double volume_Rank_Java = 0;
            if (ncloc_norm <= 8) {
                volume_Rank_Java = 2;
            } else if (ncloc_norm <= 30) {
                volume_Rank_Java = 1;
            } else if (ncloc_norm <= 80) {
                volume_Rank_Java = 0;
            } else if (ncloc_norm <= 160) {
                volume_Rank_Java = -1;
            } else {
                volume_Rank_Java = -2;
            }

            stats.singleValues.put("volume_Rank_Java", volume_Rank_Java);
        }
        if (stats.singleValues.containsKey("duplicated_lines_density")) {

            double dd = 0; //duplication density
            double duplication_Rank_Java = 0;

            if ((dd = stats.singleValues.get("duplicated_lines_density")) >= 0) {
                if (dd <= 3) {
                    duplication_Rank_Java = 2;
                } else if (dd <= 5) {
                    duplication_Rank_Java = 1;
                } else if (dd <= 10) {
                    duplication_Rank_Java = 0;
                } else if (dd <= 20) {
                    duplication_Rank_Java = -1;
                } else {
                    duplication_Rank_Java = -2;
                }

                stats.singleValues.put("duplication_Rank", duplication_Rank_Java);
            }
        }

        if (stats.singleValues.containsKey("coverage")) {

            Double dd = stats.singleValues.get("line_coverage"); //coverage
            double coverage_rank = 0;

            /* Added by FM : I removed the assignment from the if because it could cause a NPE */
            if (dd != null) {
                if (dd >= 0) {

                    if (dd > 95) {
                        coverage_rank = 2;
                    } else if (dd > 80) {
                        coverage_rank = 1;
                    } else if (dd > 60) {
                        coverage_rank = 0;
                    } else if (dd >= 20) {
                        coverage_rank = -1;
                    } else //0..20
                    {
                        coverage_rank = -2;
                    }

                    stats.singleValues.put("coverage_Rank", coverage_rank);
                }
            }
        }
        // - Complex_Units_in_System --> Marc Oriol
    }

    /**
     * Computes the percentage of lines in different degrees of complexity. Note: Changed to calc values on single files
     * instead of ncloc. Files with high complexity have often high ncloc, so results are much more negative with the
     * tables applied.
     *
     * @param classList the list of Classes to analyze
     * @return The percentages of lines having different degree of complexity <P1, P2, P3>. Output range: 0..1.
     * @author marc, mirko
     */
    public static double[] getComplexityPercentage(ArrayList<ClassInfo> classList)
    {
        int[] linesPerComplexity = new int[3];
        int totalLines = 0;
        for (ClassInfo classInfo : classList) {
            // System.out.println(classInfo.getClassName());System.out.flush();

            Double complexity = (Double) classInfo.getMetric("complexity");
            Integer nclocs = ((Double) classInfo.getMetric("ncloc")).intValue();

            if (complexity >= 0 && nclocs >= 0) { //attention: -1 = no value found
                if (complexity < L0_MAX) { //also consider low complexity
                    //System.out.println(nclocs);
                } else if (L0_MAX < complexity && complexity < L1_MAX) {
                    linesPerComplexity[0]++;//= nclocs;
                } else if (complexity < L2_MAX) {
                    linesPerComplexity[1]++;//= nclocs;
                } else { // if (complexity >= L2_MAX) {
                    linesPerComplexity[2]++;//= nclocs;
                }
                totalLines++;//= nclocs;
            }
        }
        double[] complexityPercentage = new double[3];
        for (int i = 0; i < 3; i++) {
            complexityPercentage[i] = ((double) linesPerComplexity[i]) / totalLines;
        }
        return complexityPercentage;
    }

    /**
     * Computes the percentage of lines in different degrees of complexity.
     * Note: Can be changed to calc values on single files instead of ncloc.
     * Files with high complexity have often high ncloc, so results are much more negative with the tables applied.
     * @author marc, mirko
     * @param classList
     *            the list of Classes to analyze
     * @return The percentages of lines having different degree of complexity <P1, P2, P3>. Output range: 0..1.
     */
//	public static double[] getComplexityPercentage(ArrayList<ClassInfo> classList) {
//		int[] linesPerComplexity = new int[3];
//		int totalLines = 0;
//		for (ClassInfo classInfo : classList) {
//			// System.out.println(classInfo.getClassName());System.out.flush();
//
//			Double complexity = (Double) classInfo.getMetric("complexity");
//			Integer nclocs = ((Double) classInfo.getMetric("ncloc")).intValue();
//
//			if (complexity >= 0 && nclocs >= 0) { //attention: -1 = no value found
//				if (complexity < L0_MAX) {
//					//System.out.println(nclocs);
//				} else if (L0_MAX < complexity && complexity < L1_MAX) {
//					linesPerComplexity[0] += nclocs;
//				} else if (complexity < L2_MAX) {
//					linesPerComplexity[1] += nclocs;
//				} else { // if (complexity >= L2_MAX) {
//					linesPerComplexity[2] += nclocs;
//				}
//				totalLines += nclocs; //also if complexity low
//			}
//
//		}
//		double[] complexityPercentage = new double[3];
//		for (int i = 0; i < 3; i++) {
//			complexityPercentage[i] = ((double) linesPerComplexity[i]) / totalLines;
//		}
//		return complexityPercentage;
//	}
}
