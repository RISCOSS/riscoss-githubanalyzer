package eu.riscoss.datacollector.fossology;

import eu.riscoss.RDCRunner;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        RDCRunner.exec(args, new FossologyRiskDataCollector());
    }
}
