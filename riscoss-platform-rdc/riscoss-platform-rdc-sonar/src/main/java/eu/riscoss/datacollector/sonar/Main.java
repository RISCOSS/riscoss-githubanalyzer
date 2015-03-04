package eu.riscoss.datacollector.sonar;

import eu.riscoss.RDCRunner;

public class Main
{
    public static void main(String[] args)
    {
        RDCRunner.exec(args, new SonarRiskDataCollector());
    }
}
