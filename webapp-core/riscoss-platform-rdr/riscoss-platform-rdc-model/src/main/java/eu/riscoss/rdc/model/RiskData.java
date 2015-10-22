package eu.riscoss.rdc.model;

import java.util.Date;

public interface RiskData
{
    String getId();

    String getTarget();

    Date getDate();

    RiskDataType getType();

    Object getValue();
}
