package eu.riscoss.datacollector;

import java.util.Properties;

import eu.riscoss.datacollector.common.IndicatorsMap;

public interface RiskDataCollector
{
    void createIndicators(IndicatorsMap im, Properties properties) throws Exception;
}