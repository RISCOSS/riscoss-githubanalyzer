package eu.riscoss.rdr.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import eu.riscoss.rdc.RiskDataFactory;
import eu.riscoss.rdc.model.Distribution;
import eu.riscoss.rdc.model.Evidence;
import eu.riscoss.rdc.model.RiskData;
import eu.riscoss.rdc.model.RiskDataType;
import eu.riscoss.rdr.api.RiskDataRepository;

@Path("/")
public class RiskDataResource
{
    private static final Gson gson = new Gson();

    @GET
    @Path("/{target}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam(value = "target") String target, @QueryParam(value = "offset") int offset,
            @QueryParam(value = "limit") @DefaultValue(value = "20") int limit, @QueryParam(value = "id") String id)

    {
        RiskDataRepository riskDataRepository = RiskDataRepositoryProvider.getRiskDataRepository();

        List<RiskData> riskData;
        if (id != null) {
            riskData = riskDataRepository.getRiskData(target, id, offset, limit);
        } else {
            riskData = riskDataRepository.getRiskData(target, offset, limit);
        }

        /* Build response */
        JsonObject response = new JsonObject();
        response.addProperty("offset", offset);
        response.addProperty("limit", limit);

        JsonArray results = new JsonArray();
        for (RiskData rd : riskData) {
            JsonObject object = new JsonObject();
            object.addProperty("id", rd.getId());
            object.addProperty("target", rd.getTarget());
            object.addProperty("date", rd.getDate().getTime());
            object.addProperty("type", rd.getType().toString());

            JsonElement value = null;

            switch (rd.getType()) {
                case NUMBER:
                    value = gson.toJsonTree(rd.getValue());
                    break;
                case EVIDENCE:
                    Evidence evidence = (Evidence) rd.getValue();
                    double[] values = { evidence.getPositive(), evidence.getNegative() };
                    value = gson.toJsonTree(values).getAsJsonArray();
                    break;
                case DISTRIBUTION:
                    Distribution distribution = (Distribution) rd.getValue();
                    value = gson.toJsonTree(distribution.getValues()).getAsJsonArray();
                    break;
            }

            object.add("value", value);

            results.add(object);
        }

        response.add("results", results);

        return Response.ok(gson.toJson(response)).build();
    }

    @POST
    public Response post(String body)
    {
        RiskDataRepository riskDataRepository = RiskDataRepositoryProvider.getRiskDataRepository();

        JsonArray riskDataArray;
        try {
            riskDataArray = gson.fromJson(body, JsonArray.class);
        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        for (int i = 0; i < riskDataArray.size(); i++) {
            JsonObject riskDataObject = riskDataArray.get(i).getAsJsonObject();

            String id = riskDataObject.get("id").getAsString();
            String target = riskDataObject.get("target").getAsString();
            RiskDataType type = RiskDataType.valueOf(riskDataObject.get("type").getAsString().toUpperCase());

            JsonElement valueElement = riskDataObject.get("value");

            Object value = null;
            switch (type) {
                case NUMBER:
                    value = valueElement.getAsDouble();
                    break;
                case EVIDENCE:
                    JsonArray array = valueElement.getAsJsonArray();
                    value = new Evidence(array.get(0).getAsDouble(), array.get(1).getAsDouble());
                    break;
                case DISTRIBUTION:
                    array = valueElement.getAsJsonArray();
                    List<Double> values = new ArrayList<Double>();
                    for (int j = 0; j < array.size(); j++) {
                        values.add(array.get(j).getAsDouble());
                    }

                    value = new Distribution(values.toArray(new Double[0]));
                    break;
            }

            RiskData riskData = RiskDataFactory.createRiskData(id, target, new Date(), type, value);

            riskDataRepository.storeRiskData(riskData);
        }

        return Response.status(Response.Status.ACCEPTED).build();
    }
}
