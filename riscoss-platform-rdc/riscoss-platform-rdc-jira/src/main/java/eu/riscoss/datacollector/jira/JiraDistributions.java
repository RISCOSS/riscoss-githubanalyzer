package eu.riscoss.datacollector.jira;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import eu.riscoss.datacollector.common.IndicatorsMap;
import eu.riscoss.datacollector.common.RiskDataUtils;
import eu.riscoss.datacollector.jira.JiraRiskDataCollector.JiraLogStatistics;
import eu.riscoss.rdc.model.Distribution;
import eu.riscoss.rdc.model.RiskDataType;

public class JiraDistributions
{
    Hashtable<String, Distribution> distributions;

    private final JiraLogStatistics stats;

    public JiraDistributions(JiraLogStatistics statistics)
    {
        distributions = new Hashtable<String, Distribution>();
        this.stats = statistics;
    }

    /**
     * Calculates various distributions and stores them in an internal table. Needed distributions to be added here.
     */
    @SuppressWarnings("serial")
    public void calculateDistributionsKPAActiveness()
    {
        /*
         * distributions implemented for Activeness risk drivers, KPA workshow results
		 */
        final double[] Levels_bugFixTime_Days = { 1, 2, 17, 56 };
        final double[] Levels_bugFixTimeCriticalBlocker_Days = { 1, 3, 15, 46 };
        final double[] Levels_commitFrequency_Week = { 0, 22, 45, 91 };
//		final String[] Levels_commit_Hours = {, "medium", "high"}; //Need to interpret "low/medium/high" for hours!
        final Map<String, String[]> Levels_commit_Hours = new LinkedHashMap<String, String[]>()
        {{//Need to interpret "low/medium/high" for hours!
                //note: strings used for compatibility to convenience methods
                put("low", new String[]{ "9", "10", "11", "12", "13", "14", "15", "16", "17" });
                put("medium", new String[]{ "7", "8", "18", "19", "20" });
                put("high", new String[]{ "21", "22", "23", "24", "0", "1", "2", "3", "4", "5",
                        "6" }); //format should be 1..24
            }};

        final Map<String, String[]> Levels_commit_Weekday = new LinkedHashMap<String, String[]>()
        {{//Need to interpret "low/medium/high" for hours!
                put("low", new String[]{ "1", "2", "3", "4" });// {"Monday", "Tuesday", "Wednesday", "Thursday"});
                put("medium", new String[]{ "5", "6" });// {"Friday", "Saturday"});
                put("high", new String[]{ "7" });//{"Sunday"});
            }};

//		final Map<String, String[]> Levels_commit_Holiday = new HashMap<String, String[]>() {{//Need to interpret "low/medium/high" for holidays!
//			put("low", new String[] {"Monday", "Tuesday", "Wednesday", "Thursday"});
//			put("medium", new String[] {"Friday", "Saturday"});
//			put("high", new String[] {"Sunday"});
//		}};

        distributions.put("VAverage_bug_fix_time__days_",
                RiskDataUtils.getNumberDistribution(stats.list_bugFixTime, Levels_bugFixTime_Days));

        distributions.put("VBug_fix_time_for_critical___blocker_level_bugs", RiskDataUtils
                .getNumberDistribution(stats.list_bugFixTimeCriticalBlocker, Levels_bugFixTimeCriticalBlocker_Days));

        distributions.put("VCommit_frequency___week", RiskDataUtils
                .getNumberDistributionInt(new ArrayList<Integer>(stats.list_commit_frequency_week.values()),
                        Levels_commitFrequency_Week));

        List<String> commit_hour = new ArrayList<String>(); //commodity since string input needed
        for (Integer n : stats.list_commit_hour) {
            commit_hour.add(n.toString());
        }

        Map<String, Double> hours = RiskDataUtils.getStringDistribution(commit_hour, Levels_commit_Hours);
        distributions.put("VHour__When_the_commit_was_made", new Distribution(hours.values().toArray(new Double[0])));

        //System.err.println(stats.list_GeneralFixDayOfWeek);
        Map<String, Double> weekdays =
                RiskDataUtils.getStringDistribution(stats.list_GeneralFixDayOfWeek, Levels_commit_Weekday);
        //NOTE: Commit times replaced by Bug fix issue closing times.
        //distributions.put("bugFixDayOfWeek", new Distribution(weekdays.values().toArray(new Double[0])));
        distributions
                .put("VWeekday__When_the_commit_was_made", new Distribution(weekdays.values().toArray(new Double[0])));
    }

    /**
     * Stores all distributions previously calculated, in im
     *
     * @param im the IndicatorsMap where distributions are stored
     */
    public void storeAllDistributions(IndicatorsMap im)
    {
        for (String key : distributions.keySet()) {
            im.add(key, RiskDataType.DISTRIBUTION, distributions.get(key));
        }
    }
}