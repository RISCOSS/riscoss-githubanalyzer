package eu.riscoss.datacollector.common;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import eu.riscoss.rdc.model.Distribution;

public class RiskDataUtils
{
    //private List<Double> values;

    private static class QuantityTable extends LinkedHashMap<String, Integer>
    {
        private static final long serialVersionUID = -8155109308043206164L;

        public LinkedHashMap<String, Double> getFractions(int total)
        {
            LinkedHashMap<String, Double> fractions = new LinkedHashMap<String, Double>();
            for (String key : keySet()) {
                fractions.put(key, (double) get(key) / total);
            }
            return fractions;
        }
    }

    public static Distribution getNumberDistribution(List<Double> datapoints, double... levels)
    {
        int[] quaitityList = new int[levels.length + 1];
//		if (datapoints==null) ...
        for (int i = 0; i < quaitityList.length; i++) {
            quaitityList[i] = 0;
        }

        for (Double datapoint : datapoints) {
            if (datapoint > levels[levels.length - 1]) { //if bigger than the maximum
                quaitityList[levels.length]++;
            } else {
                for (int i = 0; i < levels.length; i++) {
                    if (datapoint <= levels[i]) {
                        quaitityList[i]++;
                        break;
                    }
                }
            }
        }
        Double[] distribList = new Double[levels.length + 1];

        for (int i = 0; i < distribList.length; i++) {
            distribList[i] = (double) quaitityList[i] / datapoints.size();
        }
        return new Distribution(distribList);
    }

    public static Distribution getNumberDistributionInt(List<Integer> datapoints, double... levels)
    {
        int[] quaitityList = new int[levels.length + 1];
//		if (datapoints==null) ...
        for (int i = 0; i < quaitityList.length; i++) {
            quaitityList[i] = 0;
        }

        for (Integer datapoint : datapoints) {
            if (datapoint > levels[levels.length - 1]) { //if bigger than the maximum
                quaitityList[levels.length]++;
            } else {
                for (int i = 0; i < levels.length; i++) {
                    if (datapoint <= levels[i]) {
                        quaitityList[i]++;
                        break;
                    }
                }
            }
        }
        Double[] distribList = new Double[levels.length + 1];

        for (int i = 0; i < distribList.length; i++) {
            distribList[i] = (double) quaitityList[i] / datapoints.size();
        }
        return new Distribution(distribList);
    }

    /**
     * Puts datapoints into distribution sets defined by slots. No slot "others", non-matching strings are not
     * considered (for compatibility with the platform)
     *
     * @param datapoints retrieved data list
     * @param slots named slots. Key: slot name, Value: array of strings assigned to this slot
     */
    public static LinkedHashMap<String, Double> getStringDistributionNoOthers(List<String> datapoints,
            Map<String, String[]> slots)
    {
//		Hashtable<String, Integer> quantityTable = new Hashtable<String, Integer>();

        QuantityTable quantityTable = new QuantityTable();

//		final String noMatch = "other";

        for (String slot : slots.keySet()) {
            quantityTable.put(slot, 0);
        }
//		quantityTable.put(noMatch,0);

        boolean match;
        for (String datapoint : datapoints) {
            match = false;
            for (String key : slots.keySet()) {
                for (String string : slots.get(key)) {
                    if (datapoint.equals(string)) {
                        quantityTable.put(key, quantityTable.get(key) + 1);
                        match = true;
                        break;
                    }
                }
            }
//			if (!match)
//				quantityTable.put(noMatch,quantityTable.get(noMatch)+1);
        }

        //change values to a fraction
        return quantityTable.getFractions(datapoints.size());
    }

    /**
     * Puts datapoints into distribution sets defined by slots
     *
     * @param datapoints retrieved data list
     * @param slots named slots. Key: slot name, Value: array of strings assigned to this slot
     */
    public static LinkedHashMap<String, Double> getStringDistribution(List<String> datapoints,
            Map<String, String[]> slots)
    {
//		Hashtable<String, Integer> quantityTable = new Hashtable<String, Integer>();

        QuantityTable quantityTable = new QuantityTable();

        final String noMatch = "other";

        for (String slot : slots.keySet()) {
            quantityTable.put(slot, 0);
        }
        quantityTable.put(noMatch, 0);

        boolean match;
        for (String datapoint : datapoints) {
            match = false;
            for (String key : slots.keySet()) {
                for (String string : slots.get(key)) {
                    if (datapoint.equals(string)) {
                        quantityTable.put(key, quantityTable.get(key) + 1);
                        match = true;
                        break;
                    }
                }
            }
            if (!match) {
                quantityTable.put(noMatch, quantityTable.get(noMatch) + 1);
            }
        }

        //change values to a fraction
        return quantityTable.getFractions(datapoints.size());
    }
}