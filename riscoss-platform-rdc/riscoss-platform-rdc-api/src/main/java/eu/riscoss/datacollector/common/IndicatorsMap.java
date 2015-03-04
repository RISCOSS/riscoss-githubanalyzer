package eu.riscoss.datacollector.common;

import java.util.Date;

import eu.riscoss.rdc.RiskDataFactory;
import eu.riscoss.rdc.model.RiskData;
import eu.riscoss.rdc.model.RiskDataType;

public class IndicatorsMap extends java.util.HashMap<String, RiskData>
{
    /*
     * the entity (OSS component) on which the measurement was made
     */
    private final String targetEntity;

    public IndicatorsMap(String targetEntity)
    {
        super();
        this.targetEntity = targetEntity;
    }

    public String getTargetEntity()
    {
        return targetEntity;
    }

    /**
     * Saves a RiskData with the specified parameters, the actual date and the stored Target.
     *
     * @return the previous value associated with key, or null if there was no mapping for key. (A null return can also
     * indicate that the map previously associated null with key.)
     */
    public RiskData add(String indicatorName, RiskDataType type, Object value)
    {
        RiskData rd = RiskDataFactory.createRiskData(indicatorName, targetEntity, new Date(), type, value);
        return put(indicatorName, rd);
    }

    public RiskData add(String indicatorName, double value)
    {
        RiskData rd =
                RiskDataFactory.createRiskData(indicatorName, targetEntity, new Date(), RiskDataType.NUMBER, value);
        return put(indicatorName, rd);
    }

    @Override
    public String toString()
    {
        return values().toString();
    }
}

