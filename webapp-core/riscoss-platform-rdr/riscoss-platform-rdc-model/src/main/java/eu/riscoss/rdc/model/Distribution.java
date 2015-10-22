package eu.riscoss.rdc.model;

import java.util.ArrayList;
import java.util.List;

public class Distribution
{
    private List<Double> values;

    public Distribution(Double... values)
    {
        this.values = new ArrayList<Double>();
        for (Double d : values) {
            this.values.add(d);
        }
    }

    public List<Double> getValues()
    {
        return values;
    }

    public void setValues(List<Double> values)
    {
        this.values = values;
    }
}
