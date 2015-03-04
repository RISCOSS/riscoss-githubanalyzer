package eu.riscoss.datacollector.jira;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.TreeMap;

import org.joda.time.DateTime.Property;
import org.joda.time.Interval;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.ProjectRestClient;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.atlassian.jira.rest.client.auth.AnonymousAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

import eu.riscoss.datacollector.RiskDataCollector;
import eu.riscoss.datacollector.common.IndicatorsMap;
import eu.riscoss.rdc.model.RiskDataType;

/**
 * @author Mirko Morandini, Fabio Mancinelli
 */

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class JiraRiskDataCollector implements RiskDataCollector
{
    private final static int maxBufferIssue = 250;

    private final long secondsOfDay = 86400;

    private final double security_factor = 0.03;

    public static class WeekYear implements Comparable<WeekYear>
    {
        int week;

        int year;

        public WeekYear(int week, int year)
        {
            this.week = week;
            this.year = year;
        }

        public int compareTo(WeekYear o)
        {
            if (year == o.year) {
                return week - o.week;
            } else {
                return year - o.year;
            }
        }
    }

    public static class JiraLogStatistics
    {
        public int openBugs;

        public double timeToResolveABug;

        public double timeToResolveABlockingOrCriticalBug;

        public int numberOfFeatureRequests;

        public int numberOfOpenFeatureRequests;

        public double numberOfClosedFeatureRequestsPerUpdate;

        public double numberOfClosedBugsPerUpdate;

        public boolean presenceOfBugsCorrected;

        public int numberOfSecurityBugs;

        public double timeToResolveASecurityBug;

        public int counterSecurityBugs;

        public double totalSecurityBugFixTime;

        public int counterCriticalBugs;

        public double totalCriticalBugFixTime;

        public int counterCloseBugs;

        public double totalBugFixTime;

        public int totalBugs;

        public ArrayList<Double> list_bugFixTime = new ArrayList<Double>(); //bug fix times in days

        //		public ArrayList<Double> distribution_bugFixTime = new ArrayList<Double>(); //bug fix times in days, divided by ........
        public ArrayList<Double> list_bugFixTimeCriticalBlocker = new ArrayList<Double>();
        //critical bug fix times in days

        public ArrayList<String> list_bugFixDayOfWeek = new ArrayList<String>();
        //weekdays for closing bug (1:Mon,.., 7:Sun)

        public ArrayList<String> list_GeneralFixDayOfWeek = new ArrayList<String>();
        //weekdays for closing bug or feature (1:Mon,.., 7:Sun)

        public TreeMap<WeekYear, Integer> list_commit_frequency_week = new TreeMap<WeekYear, Integer>();
        //critical bug fix times in days

        public ArrayList<Integer> list_commit_hour = new ArrayList<Integer>();
    }

    /**
     * extracts and stores Jira indicators.
     */
    public void createIndicators(IndicatorsMap im, Properties properties) throws IOException
    {

        JiraRiskDataCollector.JiraLogStatistics statistics;
        try {
            statistics = retrieveJiraData(properties);

            //List<Distribution>
            JiraDistributions jdist = new JiraDistributions(statistics);
            jdist.calculateDistributionsKPAActiveness();

            jdist.storeAllDistributions(im);

            storeAllMeasures(im, statistics);
        } catch (RuntimeException e) {
            System.err.println("Error in retrieving Jira data.");
            e.printStackTrace();
        }

        //	} catch (Exception e) {
        //		// LOGGER.error(String.format("Error while executing analysis on %s", markmailURI), e);
        //		System.err.println("Error while executing analysis on " + markmailURI);
        //		e.printStackTrace();
        //	}
    }

    /**
     * Prepares login to Jira and gets the statsitics
     */
    private JiraRiskDataCollector.JiraLogStatistics retrieveJiraData(Properties properties)
            throws RuntimeException, IOException
    {
        //LOGGER.info(String.format("Running %s on %s", JiraMeasurementsToolFactory.TOOL_ID, scope));

        String jiraURL = properties.getProperty("JIRA_URL");
        if (jiraURL == null) {
            System.out.println("Jira URI is null. Stopping.");
            throw new RuntimeException("Jira URI is null.");
        }

        String jiraProject = properties.getProperty("JIRA_Project");
        if (jiraProject == null) {
            jiraProject = "";
        }

        boolean anonymousAuthentication = Boolean.parseBoolean(properties.getProperty("JIRA_AnonymousAuthentication"));
        String username = properties.getProperty("JIRA_Username");
        String password = properties.getProperty("JIRA_Password");
        String initialDate = properties.getProperty("JIRA_InitialDate");

        //	        if (jiraURL == null) {
        //	            LOGGER.error("jira URL is null.");
        //	        }

        JiraRiskDataCollector.JiraLogStatistics statistics = null;
        try {
            statistics = getStatistics(jiraURL, jiraProject, anonymousAuthentication, username, password, initialDate);
        } catch (URISyntaxException e) {
            System.out.printf("Error creating retrieving data from URI %s", jiraURL);
            throw new RuntimeException(e);
//			e.printStackTrace();
//		} catch (RuntimeException e) {
//			System.out.printf("Error while executing analysis on %s", jiraURL);
//			e.printStackTrace();
        }
        return statistics;
    }

    /**
     * Calculates the statistic of jira
     *
     * @param jiraURL url of Jira
     * @param anonymousAuthentication true if the authentication is anonymous, false otherwise.
     * @param username the username to log in Jira
     * @param password the password to log in Jira
     * @param initialDate the initial date to perform the measures.
     * @throws URISyntaxException the url of Jira is not a valid URI.
     */
    protected JiraRiskDataCollector.JiraLogStatistics getStatistics(String jiraURL, String jiraProject,
            boolean anonymousAuthentication, String username, String password, String initialDate)
            throws URISyntaxException, IOException
    {

        int issueIndex = 0;
        int totalIssues;
        Issue is;
        String project = ""; //default: no project, all the JIRA repo is scanned

        final JiraRestClientFactory jiraRestFactory = new AsynchronousJiraRestClientFactory();
        JiraRestClient restClient = null;

        try {
    /*
     * Type Authentication instance
	 */

            if (anonymousAuthentication) {
                restClient = jiraRestFactory.create(new URI(jiraURL), new AnonymousAuthenticationHandler());
            } else {
                restClient = jiraRestFactory.createWithBasicHttpAuthentication(new URI(jiraURL), username,
                        password); //note: factory version is 0.1, no javadoc available
            }

//		System.out.println("FACTORY "+jiraURL + "  " + username + "  " + password + "  " + restClient);

            final IssueRestClient client = restClient.getIssueClient();
            SearchRestClient searchClient = restClient.getSearchClient();

            if (jiraProject != null && !jiraProject.isEmpty())  //default: no project, all the JIRA repo is scanned
            {
                project = "project = " + jiraProject + " AND";
            }

            String jql = project + " created >= \"" + initialDate +
                    "\"";  //TODO need to adapt for project, e.g. project = WEBLAB AND ...

            SearchResult results = null;
            try {
                results = searchClient.searchJql(jql).claim();
            } catch (RuntimeException e) {
                //System.err.println("Error in executing query on Jira " + jiraURL);
                //e.printStackTrace();
                throw e;
            }

	/*
     * Calculate Total Issues, Retrieve Bug and Feature data
	 */
            totalIssues = results.getTotal();
            System.out.println("Total issues to scan (from " + initialDate + "): " + totalIssues);

            JiraRiskDataCollector.JiraLogStatistics stats = new JiraRiskDataCollector.JiraLogStatistics();
            while (issueIndex < totalIssues) {
                results = searchClient.searchJql(jql, maxBufferIssue, issueIndex, null).claim();

                for (final BasicIssue issue : results.getIssues()) {
                    is = client.getIssue(issue.getKey()).claim();

                    if (is != null) {
                        //string matching for bugs and features, data extraction
                        retrieveDataBugsFeatures(is, stats);
                        issueIndex++;
                    }
                }// for end
            } // while end

            /**
             * Metrics about all issues
             */
            extractGeneralMeasures(restClient, searchClient, stats);

            return stats;
        } finally {
            restClient.close(); //Added this to avoid endless looping in case of error.
        }
    }

    /**

     * @param restClient
     * @param searchClient
     * @param stats
     * @return
     */
    private void extractGeneralMeasures(JiraRestClient restClient, SearchRestClient searchClient,
            JiraLogStatistics stats)
    {
//	double numberOfClosedFeatureRequestsPerUpdate;
//	double numberOfClosedBugsPerUpdate;
        String jql;
        SearchResult results;
        // Number of Versions
        ProjectRestClient projectClient = restClient.getProjectClient();
        Project project;
        int countOfVersions = 0;
//	int totalBugs = 0;
//	int numberOfOpenBugs = 0;
//	double totalCriticalBugFixTime = 0;
//	int counterCriticalBugs = 0;
//	double totalBugFixTime = 0;
//	double totalSecurityBugFixTime = 0;
//	int counterSecurityBugs = 0;
//	int counterCloseBugs = 0;
//	int numberOfFeatureRequests = 0;
//	int numberOfOpenFeatureRequests = 0;
//	boolean presenceOfSecurityBugsCorrected = false;

        Iterable<BasicProject> basicProjects = projectClient.getAllProjects().claim();
        for (BasicProject basicP : basicProjects) {

            project = projectClient.getProject(basicP.getKey()).claim();
            for (Version ver : project.getVersions()) {
                countOfVersions++;
            }
        }

        jql = "issuetype = Bug AND status in (Closed, Resolved)";
        results = searchClient.searchJql(jql).claim();
        stats.numberOfClosedBugsPerUpdate = (double) results.getTotal() / (double) countOfVersions;

        jql = "issuetype = \"New Feature\" AND status in (Closed, Resolved)";
        results = searchClient.searchJql(jql).claim();
        stats.numberOfClosedFeatureRequestsPerUpdate = (double) results.getTotal() / (double) countOfVersions;

//	stats.openBugs = numberOfOpenBugs;
        stats.timeToResolveABug = stats.totalBugFixTime / stats.counterCloseBugs;
        stats.timeToResolveABlockingOrCriticalBug = stats.totalCriticalBugFixTime / stats.counterCriticalBugs;
        stats.numberOfFeatureRequests = stats.numberOfFeatureRequests;
        stats.numberOfOpenFeatureRequests = stats.numberOfOpenFeatureRequests;

        stats.numberOfSecurityBugs = (int) (stats.totalBugs * security_factor);
        if (stats.counterCloseBugs > 0) { //TODO: check why this should express security bugs!
            stats.presenceOfBugsCorrected = true; //TODO: check: was presenceOfSecurityBugsCorrected before!??
        }
        if (stats.counterSecurityBugs > 0) {
            stats.timeToResolveASecurityBug = stats.totalSecurityBugFixTime / stats.counterSecurityBugs;
        }

//	return stats;
    }

    /**
     *
     * @param is
     * @param stats
     */
    private void retrieveDataBugsFeatures(Issue is, JiraLogStatistics stats)
    {
        String issueState;
        Interval interval;

//	int issueIndex = 0;
//	int totalIssues;

        System.out.println(is.getIssueType().getName() + " " + is.getSummary() + ".   Updated: " +
                is.getUpdateDate()); //.dayOfWeek().getAsText());

        issueState = is.getStatus().getName().toUpperCase();

	/*
     * general measures for all types
	 */
        if ((issueState.equals(IssueStatus.CLOSED.toString())) || (issueState.equals(IssueStatus.RESOLVED.toString()))
                || (issueState.equals(IssueStatus.DONE.toString())))
        {

            Property dayOfWeek = is.getUpdateDate().dayOfWeek();
            stats.list_GeneralFixDayOfWeek.add(dayOfWeek.getAsString());

            int week = is.getUpdateDate().getWeekOfWeekyear();
            int year = is.getUpdateDate().getWeekyear();
            WeekYear w = new WeekYear(week, year);
            Integer n = stats.list_commit_frequency_week.get(w);
            if (n == null) {
                n = new Integer(0);
            }
            stats.list_commit_frequency_week.put(w, n + 1);

            stats.list_commit_hour.add(is.getUpdateDate().getHourOfDay()); //1..24
        }

	/*
     * Measures for BUGs
	 */

        if (is.getIssueType().getName().toUpperCase().equals("BUG")) {
//		issueState = is.getStatus().getName().toUpperCase();
            stats.totalBugs++;

		/*
         * Bug open if -->status !closed and ! done
		 */
            if ((!issueState.equals(IssueStatus.CLOSED.toString()))
                    && (!issueState.equals(IssueStatus.DONE.toString())))
            {
                stats.openBugs++;
            }

		/*
         * Bug close if--> status closed or status done or status resolved Number of days=
		 * date of bug create - date of bug last update
		 */
            if ((issueState.equals(IssueStatus.CLOSED.toString()))
                    || (issueState.equals(IssueStatus.RESOLVED.toString()))
                    || (issueState.equals(IssueStatus.DONE.toString())))
            {

                interval = new Interval(is.getCreationDate(), is.getUpdateDate());
                double bugFixTimeDays = interval.toDuration().getStandardSeconds() / secondsOfDay;
                stats.list_bugFixTime.add(bugFixTimeDays);
                stats.totalBugFixTime += (double) bugFixTimeDays;
                stats.counterCloseBugs++;

			/*
             * closed bug with Priority CRITICAL OR BLOCKER
			 */
                if (is.getPriority() != null) {
                    if ((is.getPriority().getName().toUpperCase().equals(IssuePriority.CRITICAL.toString()))
                            || (is.getPriority().getName().toUpperCase().equals(IssuePriority.BLOCKER.toString())))
                    {
                        double bugFixTimeBlockDays = interval.toDuration().getStandardSeconds() / secondsOfDay;
                        stats.list_bugFixTimeCriticalBlocker.add(bugFixTimeBlockDays);
                        stats.totalCriticalBugFixTime += (double) bugFixTimeBlockDays;
                        stats.counterCriticalBugs++;
                    }
                }

			/*
             * "commit frequency week": retrieve weekday of closed/resolved/done
			 */

                Property dayOfWeek = is.getUpdateDate().dayOfWeek();
                //weekdays for closing bug (1:Mon,.., 7:Sun)
//			System.err.println(dayOfWeek.getAsString());
                stats.list_bugFixDayOfWeek.add(dayOfWeek.getAsString());  //NOTE: currently not used

			/*
             * close bug is a security issue
			 */
                if (isSecurityIssue(is)) {
                    stats.totalSecurityBugFixTime += (double) interval.toDuration().getStandardSeconds()
                            / secondsOfDay;
                    stats.counterSecurityBugs++;
                }
            } else {
            /*
             * Other BUGs Measures
			 */
            }
        }
    /*
     * Measures for FEATUREs
	 */
        else {
		/*
		 * Issue is feature if type = "NEW FEATURE"
		 */
            if (is.getIssueType().getName().toUpperCase().equals("NEW FEATURE")) {
                issueState = is.getStatus().getName().toUpperCase();
                stats.numberOfFeatureRequests++;
			/*
			 * Feature open if -->status !closed and ! done
			 */
                if ((!issueState.equals(IssueStatus.CLOSED.toString()))
                        && (!issueState.equals(IssueStatus.DONE.toString())))
                {
                    stats.numberOfOpenFeatureRequests++;
                }
            }
        }
    }

    /**
     * Returns if a given issue is related to security
     *
     * @param is Issue
     * @return true if it is a security issue.
     */
    private boolean isSecurityIssue(Issue is)
    {
        String[] securityTerms = { "security", "secure", "attack", "vulnerability", "exploit",
                "sql injection", "cross-site scripting" };
        String issueSummary = is.getSummary() == null ? "" : is.getSummary().toLowerCase();
        String issueDescription = is.getDescription() == null ? "" : is.getDescription().toLowerCase();

        boolean found = false;
        for (int i = 0; i < securityTerms.length && !found; i++) {
            found = issueSummary.contains(securityTerms[i]) || issueDescription.contains(securityTerms[i]);
        }

        return found;
    }

    /**
     * Stores all the measures obtained from monitoring.
     *
     * @param im target of the measures.
     * @param statistics results of monitoring.
     */
    private void storeAllMeasures(IndicatorsMap im, JiraRiskDataCollector.JiraLogStatistics statistics)
    {
        im.add("open-bugs", statistics.openBugs); //Integer.toString(statistics.openBugs));
        im.add("time-to-resolve-a-bug", statistics.timeToResolveABug);
        im.add("time-to-resolve-a-blocker-or-critical-bug", statistics.timeToResolveABlockingOrCriticalBug);
        im.add("number-of-feature-requests", statistics.numberOfFeatureRequests);
        im.add("number-of-closed-feature-requests-per-date",
                statistics.numberOfClosedFeatureRequestsPerUpdate);
        im.add("number-of-open-feature-requests",
                statistics.numberOfOpenFeatureRequests);
        im.add("number-of-closed-bugs-per-update",
                statistics.numberOfClosedBugsPerUpdate);

        //to add a boolean, for now we cast it to a number (0/1)
        im.add("presence-of-bugs-corrected", RiskDataType.NUMBER, statistics.presenceOfBugsCorrected ? 1.0 : 0.0);
        im.add("number-of-security-bugs", statistics.numberOfSecurityBugs);
        im.add("time-to-resolve-a-security-bug", statistics.timeToResolveASecurityBug);

        //		im.add(type, RiskDataType.DISTRIBUTION, statistics.postsPerMonth_monthly);

        System.out.printf(
                // LOGGER.info(String.format(
                "JIRA Analysis completed [%d, %f, %f,%d,%f,%d,%f,%b,%d,%f\n]. Results stored", statistics.openBugs,
                statistics.timeToResolveABug, statistics.timeToResolveABlockingOrCriticalBug,
                statistics.numberOfFeatureRequests, statistics.numberOfClosedFeatureRequestsPerUpdate,
                statistics.numberOfOpenFeatureRequests, statistics.numberOfClosedBugsPerUpdate,
                statistics.presenceOfBugsCorrected, statistics.numberOfSecurityBugs,
                statistics.timeToResolveASecurityBug);
    }
}