package eu.riscoss.rdc.model;

public class Evidence
{
    private double positive;

    private double negative;

    public Evidence(double positive, double negative)
    {
        this.positive = positive;
        this.negative = negative;
    }

    public double getPositive()
    {
        return positive;
    }

    public double getNegative()
    {
        return negative;
    }

    @Override public String toString()
    {
        return "Evidence{" +
                "positive=" + positive +
                ", negative=" + negative +
                '}';
    }
}
