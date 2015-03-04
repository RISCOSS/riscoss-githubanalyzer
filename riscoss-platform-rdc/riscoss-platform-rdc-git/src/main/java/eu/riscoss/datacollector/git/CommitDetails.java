package eu.riscoss.datacollector.git;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Class that stores all the details of a commit.
 *
 * @author Marc
 */
public class CommitDetails
{
    String commitID;

    int filesChanged;

    int linesAdded;

    int linesRemoved;

    String committer;

    private Date date;

    public String getCommitID()
    {
        return commitID;
    }

    public void setCommitID(String commitID)
    {
        this.commitID = commitID;
    }

    public int getFilesChanged()
    {
        return filesChanged;
    }

    public void setFilesChanged(int filesChanged)
    {
        this.filesChanged = filesChanged;
    }

    public int getLinesAdded()
    {
        return linesAdded;
    }

    public void setLinesAdded(int linesAdded)
    {
        this.linesAdded = linesAdded;
    }

    public int getLinesRemoved()
    {
        return linesRemoved;
    }

    public Date getDate()
    {
        return date;
    }

    public void setLinesRemoved(int linesRemoved)
    {
        this.linesRemoved = linesRemoved;
    }

    public String getCommitter()
    {
        return committer;
    }

    public void setCommitter(String committer)
    {
        this.committer = committer;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    public void setDate(Date date)
    {
        this.date = date;
    }
}
