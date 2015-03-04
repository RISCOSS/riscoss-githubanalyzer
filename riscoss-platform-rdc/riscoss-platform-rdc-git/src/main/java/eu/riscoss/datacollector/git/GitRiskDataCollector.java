package eu.riscoss.datacollector.git;

/**
 * @author Marc Oriol, Mirko Morandini, Fabio Mancinelli
 */

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.Weeks;

import eu.riscoss.datacollector.RiskDataCollector;
import eu.riscoss.datacollector.common.IndicatorsMap;
import eu.riscoss.rdc.model.Distribution;
import eu.riscoss.rdc.model.RiskDataType;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class GitRiskDataCollector implements RiskDataCollector
{
    protected String gitPath;

    protected String repositoryURI;

    protected File repositoryLocalPath;

    protected String initialDate;

    protected static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

    public static class GitLogStatistics
    {
        public int totalLinesAdded = 0;

        public int totalLinesRemoved = 0;

        public int totalFilesChanged = 0;

        public int totalCommits = 0;

        public int totalCommitters = 0;

        public int[] commitsByHour = new int[24];

        public int[] commitsByWeekday = new int[7];

        public Date firstCommitDate;
    }

    /**
     * Extracts and stores the indicators for
     * 
     * @param im indicatorMap to store the indicators
     * @param properties the properties to get the required config information.
     */
    public void createIndicators(IndicatorsMap im, Properties properties) throws IOException
    {
        gitPath = properties.getProperty("GitPath");
        initialDate = properties.getProperty("Git_InitialDate");
        repositoryURI = properties.getProperty("GitRepositoryURI");
        String repositoryName = new File(repositoryURI).getName();
        repositoryLocalPath = new File(properties.getProperty("GitLocalDirectory"), repositoryName);
        GitLogStatistics statistics = getStatistics();
        storeAllMeasures(im, statistics);
    }

    /**
     * Returns the statistics of the git project.
     * 
     * @return the statistics of the git project.
     */
    protected GitRiskDataCollector.GitLogStatistics getStatistics()
    {
        GitLogStatistics statistics = new GitLogStatistics();

        try {

            // clone or update the repository if exists
            if (!repositoryLocalPath.exists()) {
                cloneRepository(repositoryURI, repositoryLocalPath);
            } else {
                updateRepository(repositoryLocalPath);
            }

            // obtain the list of commits with their details
            List<String> commitIds = getCommitsIds();
            ArrayList<CommitDetails> commitDetails = new ArrayList<CommitDetails>();
            for (String commitId : commitIds) {
                CommitDetails commitDetail = getCommitDetails(commitId);
                if (commitDetail != null) {
                    commitDetails.add(commitDetail);
                }
            }

            // aggregate the information of each commit into Statistics
            HashSet<String> committers = new HashSet<String>();
            for (CommitDetails commitDetail : commitDetails) {
                committers.add(commitDetail.getCommitter());
                statistics.totalLinesAdded += commitDetail.getLinesAdded();
                statistics.totalLinesRemoved += commitDetail.getLinesRemoved();
                statistics.totalFilesChanged += commitDetail.getFilesChanged();
                Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
                calendar.setTime(commitDetail.getDate());
                statistics.commitsByHour[calendar.get(Calendar.HOUR_OF_DAY)]++;
                statistics.commitsByWeekday[calendar.get(Calendar.DAY_OF_WEEK) - 1]++;
                statistics.totalCommits++;

                if (statistics.firstCommitDate != null) {
                    if (commitDetail.getDate().before(statistics.firstCommitDate)) {
                        statistics.firstCommitDate = commitDetail.getDate();
                    }
                } else {
                    statistics.firstCommitDate = commitDetail.getDate();
                }

            }
            statistics.totalCommitters = committers.size();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statistics;
    }

    /**
     * Return the commit Details of a given commit.
     * 
     * @param commitId: Identifier of the commit to analyze.
     * @return the details of such commit.
     */
    private CommitDetails getCommitDetails(String commitId) throws Exception
    {
        CommitDetails commitDetails = new CommitDetails();
        String[] command = {gitPath, "diff-tree", "--pretty=format:%cn%n%cd", "--shortstat", commitId};

        Process process = Runtime.getRuntime().exec(command, null, repositoryLocalPath);
        InputStream inputStream = process.getInputStream();

        String out = IOUtils.toString(inputStream);

        if (!out.isEmpty()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(out.getBytes())));
            commitDetails.setCommitID(commitId);
            commitDetails.setCommitter(reader.readLine());
            String dateStr = reader.readLine();

            DateFormat parser = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy X", Locale.ENGLISH);
            Date date = parser.parse(dateStr);
            commitDetails.setDate(date);
            String commitChanges = reader.readLine();
            Pattern pattern1 = Pattern.compile("([0-9])+.*changed");
            Pattern pattern2 = Pattern.compile("([0-9])+ insertion");
            Pattern pattern3 = Pattern.compile("([0-9])+ deletion");

            Matcher matcher = pattern1.matcher(commitChanges);
            if (matcher.find()) {
                commitDetails.setFilesChanged(Integer.parseInt(matcher.group(1)));
            } else {
                commitDetails.setFilesChanged(0);
            }

            matcher = pattern2.matcher(commitChanges);
            if (matcher.find()) {
                commitDetails.setLinesAdded(Integer.parseInt(matcher.group(1)));
            } else {
                commitDetails.setLinesAdded(0);
            }

            matcher = pattern3.matcher(commitChanges);
            if (matcher.find()) {
                commitDetails.setLinesRemoved(Integer.parseInt(matcher.group(1)));
            } else {
                commitDetails.setLinesRemoved(0);
            }

            reader.close();

            return commitDetails;
        } else {
            return null;
        }
    }

    /**
     * Retrieves the list of all commit IDs since the given date specified in gitInitialDate
     * 
     * @return the list of commit ids.
     */
    private List<String> getCommitsIds() throws Exception
    {

        String[] command = {gitPath, "rev-list", "HEAD", "--since=\"" + initialDate + "\""};

        Process process = Runtime.getRuntime().exec(command, null, repositoryLocalPath);
        InputStream inputStream = process.getInputStream();
        String gitOutput = IOUtils.toString(inputStream);

        List<String> commitsIds;

        if (gitOutput.equals("")) {
            commitsIds = new ArrayList<String>();
        } else {
            String[] lines = gitOutput.split("\n");
            commitsIds = new ArrayList<String>(Arrays.asList(lines));
        }

        return commitsIds;
    }

    /**
     * Obtain the indicators from the statistics and stores them into the im
     * 
     * @param im indicatorMap to store the indicators
     * @param statistics statistics of the git.
     */
    private void storeAllMeasures(IndicatorsMap im, GitLogStatistics statistics)
    {
        DateTime dt = new DateTime(statistics.firstCommitDate);
        Months months = Months.monthsBetween(dt, new DateTime());
        Weeks weeks = Weeks.weeksBetween(dt, new DateTime());

        im.add("git average-commits-per-month", (double) statistics.totalCommits / months.getMonths());
        im.add("git average-commits-per-week", (double) statistics.totalCommits / weeks.getWeeks());
        im.add("git average-commits-per-committer", (double) statistics.totalCommits / statistics.totalCommitters);
        im.add("git average-files-changed-per-committer", (double) statistics.totalFilesChanged
            / statistics.totalCommitters);
        im.add("git average-lines-added-per-commmit", (double) statistics.totalLinesAdded / statistics.totalCommits);
        im.add("git average-lines-removed-per-commit", (double) statistics.totalLinesRemoved / statistics.totalCommits);
        im.add("git average-files-changed-per-commit", (double) statistics.totalFilesChanged / statistics.totalCommits);

        im.add("git distribution-commits-by-hour", RiskDataType.DISTRIBUTION,
            getDistribution(statistics.commitsByHour, statistics.totalCommits));

        im.add("git distribution-commits-by-weekday", RiskDataType.DISTRIBUTION,
            getDistribution(statistics.commitsByWeekday, statistics.totalCommits));

    }

    /**
     * Given an array of integers with absolute values, it returns the distribution in %.
     * 
     * @param values the array to convert to distribution
     * @param total sum of the values of all the elements of the array. Note: although it can be easily computed, it is
     *            passed through parameter because (usually) it has already been computed before.
     */
    private Distribution getDistribution(int[] values, int total)
    {
        List<Double> distributionValues = new ArrayList<Double>();
        for (int i = 0; i < values.length; i++) {
            distributionValues.add((double) values[i] / (double) total);
        }

        Distribution distribution = new Distribution();
        distribution.setValues(distributionValues);

        return distribution;
    }

    protected boolean cloneRepository(String repositoryURI, File destination) throws Exception
    {
        String[] cmd = {gitPath, "clone", repositoryURI.toString(), destination.toString()};

        Process p = Runtime.getRuntime().exec(cmd);
        int result = p.waitFor();

        return result == 0;
    }

    protected boolean updateRepository(File repository) throws Exception
    {
        String[] cmd = {gitPath, "pull", repository.toString()};

        Process p = Runtime.getRuntime().exec(cmd);
        int result = p.waitFor();

        return result == 0;
    }
}
