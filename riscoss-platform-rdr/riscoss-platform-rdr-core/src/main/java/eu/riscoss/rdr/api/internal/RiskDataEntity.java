package eu.riscoss.rdr.api.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import eu.riscoss.rdc.model.Distribution;
import eu.riscoss.rdc.model.Evidence;
import eu.riscoss.rdc.model.RiskDataType;

public class RiskDataEntity implements Serializable
{
    private static final long serialVersionUID = 5733314975852733360L;

    private static Gson gson = new Gson();

    private String id;

    private String target;

    private Date date;

    private RiskDataType type;

    private String serializedValue;

    public RiskDataEntity()
    {
    }

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
        switch (type) {
            case NUMBER:
                return deserializeNumber(serializedValue);
            case EVIDENCE:
                return deserializeEvidence(serializedValue);
            case DISTRIBUTION:
                return deserializeDistribution(serializedValue);
            default:
                return null;
        }
    }

    public String getSerializedValue()
    {
        return serializedValue;
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

    public void setValue(Object object)
    {
        if (object instanceof Double) {
            type = RiskDataType.NUMBER;
            serializedValue = serializeNumber((Double) object);
        } else if (object instanceof Evidence) {
            type = RiskDataType.EVIDENCE;
            serializedValue = serializeEvidence((Evidence) object);
        } else if (object instanceof Distribution) {
            type = RiskDataType.DISTRIBUTION;
            serializedValue = serializeDistribution((Distribution) object);
        } else {
            throw new IllegalArgumentException(String.format("Unsupported type: %s", object.getClass().getName()));
        }
    }

    public void setSerializedValue(String serializedValue)
    {
        this.serializedValue = serializedValue;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RiskDataEntity riskDataEntity = (RiskDataEntity) o;

        if (!date.equals(riskDataEntity.date)) {
            return false;
        }
        if (!id.equals(riskDataEntity.id)) {
            return false;
        }
        if (!target.equals(riskDataEntity.target)) {
            return false;
        }
        if (type != riskDataEntity.type) {
            return false;
        }
        if (!serializedValue.equals(riskDataEntity.serializedValue)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id.hashCode();
        result = 31 * result + target.hashCode();
        result = 31 * result + date.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + serializedValue.hashCode();
        return result;
    }

    private double deserializeNumber(String value)
    {
        JsonElement jsonElement = gson.fromJson(value, JsonElement.class);

        return jsonElement.getAsDouble();
    }

    private String serializeNumber(double value)
    {
        return gson.toJson(value);
    }

    private Evidence deserializeEvidence(String value)
    {
        JsonElement jsonElement = gson.fromJson(value, JsonElement.class);
        JsonArray jsonArray = jsonElement.getAsJsonArray();

        return new Evidence(jsonArray.get(0).getAsDouble(), jsonArray.get(1).getAsDouble());
    }

    private String serializeEvidence(Evidence evidence)
    {
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(gson.fromJson(String.format("%f", evidence.getPositive()), JsonElement.class));
        jsonArray.add(gson.fromJson(String.format("%f", evidence.getNegative()), JsonElement.class));

        return gson.toJson(jsonArray);
    }

    private Distribution deserializeDistribution(String value)
    {
        List<Double> values = new ArrayList<Double>();

        JsonElement jsonElement = gson.fromJson(value, JsonElement.class);
        JsonArray jsonArray = jsonElement.getAsJsonArray();

        for (int i = 0; i < jsonArray.size(); i++) {
            values.add(jsonArray.get(i).getAsDouble());
        }

        return new Distribution(values.toArray(new Double[0]));
    }

    private String serializeDistribution(Distribution distribution)
    {
        JsonArray jsonArray = new JsonArray();
        for (Double d : distribution.getValues()) {
            jsonArray.add(gson.fromJson(String.format("%s", d), JsonElement.class));
        }

        return gson.toJson(jsonArray);
    }
}
