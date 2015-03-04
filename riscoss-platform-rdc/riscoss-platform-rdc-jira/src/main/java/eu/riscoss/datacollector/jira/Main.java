package eu.riscoss.datacollector.jira;

import eu.riscoss.RDCRunner;

public class Main
{
    public static void main(String[] args)
    {
        RDCRunner.exec(args, new JiraRiskDataCollector());
    }
}
