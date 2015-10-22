package eu.riscoss.rdr.rest;

import eu.riscoss.rdr.api.RiskDataRepository;

public class RiskDataRepositoryProvider
{
    private static RiskDataRepository riskDataRepository;

    public static RiskDataRepository getRiskDataRepository()
    {
        return riskDataRepository;
    }

    public static void setRiskDataRepository(RiskDataRepository riskDataRepository)
    {
        RiskDataRepositoryProvider.riskDataRepository = riskDataRepository;
    }
}
