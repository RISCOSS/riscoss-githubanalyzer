package eu.riscoss.datacollector.jira;

/**
 * @author Oscar
 */
enum IssueStatus
{

    TODO("TO DO"),
    INPROGRESS("IN PROGRESS"),
    INREVIEW("IN REVIEW"),
    OPEN("OPEN"),
    DONE("DONE"),
    CLOSED("CLOSED"),
    RESOLVED("RESOLVED"),
    UNRESOLVED("UNRESOLVED");

    private String status;

    IssueStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return this.status;
    }
}

enum IssuePriority
{
    CRITICAL("CRITICAL"),
    BLOCKER("BLOCKER"),
    MAJOR("MAJOR"),
    MINOR("MINOR"),
    TRIVIAL("TRIVIAL");

    private String priority;

    IssuePriority(String priority)
    {
        this.priority = priority;
    }

    public String getStatus()
    {
        return this.priority;
    }
}
