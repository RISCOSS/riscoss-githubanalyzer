package eu.riscoss.rdc.model.internal;

import java.util.Date;

import eu.riscoss.rdc.model.RiskData;
import eu.riscoss.rdc.model.RiskDataType;

public class RiskDataImpl implements RiskData
{
    private String id;

    private String target;

    private Date date;

    private RiskDataType type;

    private Object value;

    public String getId()
    {
        return id;
    }

    public String getTarget()
    {
        return target;
    }

    public Date getDate()
    {
        return date;
    }

    public RiskDataType getType()
    {
        return type;
    }

    public Object getValue()
    {
        return value;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public void setTarget(String target)
    {
        this.target = target;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }

    public void setType(RiskDataType type)
    {
        this.type = type;
    }

    public void setValue(Object value)
    {
        this.value = value;
    }
}
