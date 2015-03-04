package eu.riscoss.datacollector.fossology;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.riscoss.datacollector.RiskDataCollector;
import eu.riscoss.datacollector.common.IndicatorsMap;

public class FossologyRiskDataCollector implements RiskDataCollector
{
    private static Logger LOGGER = LoggerFactory.getLogger(FossologyRiskDataCollector.class);

    /* Properties needed by the data collector */
    private static final String TARGET_FOSSOLOGY_PROPERTY = "targetFossology";

    /* String used to check when a file doens't have a license associated */
    private static final String NO_LICENSE_FOUND = "No_license_found";

    /* These are the license types defined in licenseType.properties */
    private static final String PERMISSIVE_LICENSE_TYPE = "permissive";

    private static final String COPYLEFT_LICENSE_TYPE = "copyleft";

    private static final String COPYLEFT_WITH_LINKING_LICENSE_TYPE = "copyleft-with-linking";

    private static final String COMMERCIAL_LICENSE_TYPE = "commercial";

    private static final String UNKNOWN_LICENSE_TYPE = "unknown";

    /**
     * @return A map that associates a license to its type (License -> LicenseType)
     */
    private Map<String, String> getLicenseTypes() throws IOException
    {
        Map<String, String> result = new HashMap<String, String>();

        Properties licenseTypes = new Properties();
        licenseTypes.load(FossologyRiskDataCollector.class.getResourceAsStream("/licenseTypes.properties"));

        for (Map.Entry e : licenseTypes.entrySet()) {
            String[] parts = ((String) e.getValue()).split(",");
            for (String license : parts) {
                result.put(license.trim(), e.getKey().toString());
            }
        }

        return result;
    }

    /**
     * @param licenseTypes The map containing the license associations (@see getLicenseTypes)
     * @param license The license to be identified.
     * @return The type of the license or UNKNOWN.
     */
    private String getLicenseTypeForLicense(Map<String, String> licenseTypes, String license)
    {
        for (String l : licenseTypes.keySet()) {
            if (license.toLowerCase().contains(l.toLowerCase())) {
                return licenseTypes.get(l);
            }
        }

        return UNKNOWN_LICENSE_TYPE;
    }

    private LicenseAnalysisReport parseLicenseAnalysisReport(InputStream in) throws IOException
    {
        LicenseAnalysisReport licenseAnalysisReport = new LicenseAnalysisReport();
        Map<String, String> licenseTypes = getLicenseTypes();

        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        /* Calculate the occurrences for each license type */
        Map<String, Integer> licenseOccurrences = new HashMap<String, Integer>();
        int totalFiles = 0;
        String line;
        while ((line = br.readLine()) != null) {
            /* Parse only the lines that contains a ':' */
            if (line.contains(":")) {
                String[] parts = line.split(":", 2);

                String license = parts[1].trim();

                if (licenseOccurrences.get(license) == null) {
                    licenseOccurrences.put(license, 1);
                } else {
                    licenseOccurrences.put(license, licenseOccurrences.get(license) + 1);
                }

                totalFiles++;
            }
        }

        /* Find license types */
        Map<String, Integer> licenseTypeOccurrences = new HashMap<String, Integer>();
        for (String license : licenseOccurrences.keySet()) {
            if (!NO_LICENSE_FOUND.equals(license)) {
                String licenseType = getLicenseTypeForLicense(licenseTypes, license);

                if (licenseTypeOccurrences.get(licenseType) == null) {
                    licenseTypeOccurrences.put(licenseType, licenseOccurrences.get(license));
                } else {
                    licenseTypeOccurrences.put(licenseType,
                            licenseTypeOccurrences.get(licenseType) + licenseOccurrences.get(license));
                }
            }
        }

        licenseAnalysisReport.totalFiles = totalFiles;

        if (licenseOccurrences.get(NO_LICENSE_FOUND) != null) {
            /* Remove the NO_LICENSE_FOUND pseudolicense from the number of licenses found. */
            licenseAnalysisReport.numberOfLicenses = licenseOccurrences.keySet().size() - 1;
        } else {
            licenseAnalysisReport.numberOfLicenses = licenseOccurrences.keySet().size();
        }

        Integer value = licenseOccurrences.get(NO_LICENSE_FOUND);
        licenseAnalysisReport.filesWithoutLicense = (value == null ? 0 : value);

        value = licenseTypeOccurrences.get(COMMERCIAL_LICENSE_TYPE);
        licenseAnalysisReport.filesWithCommercialLicense = (value == null ? 0 : value);

        value = licenseTypeOccurrences.get(COPYLEFT_LICENSE_TYPE);
        licenseAnalysisReport.filesWithCopyleftLicense = (value == null ? 0 : value);

        value = licenseTypeOccurrences.get(COPYLEFT_WITH_LINKING_LICENSE_TYPE);
        licenseAnalysisReport.filesWithCopyleftWithLinkingLicense = (value == null ? 0 : value);

        value = licenseTypeOccurrences.get(PERMISSIVE_LICENSE_TYPE);
        licenseAnalysisReport.filesWithPermissiveLicense = (value == null ? 0 : value);

        value = licenseTypeOccurrences.get(UNKNOWN_LICENSE_TYPE);
        licenseAnalysisReport.filesWithUnknownLicense = (value == null ? 0 : value);

        return licenseAnalysisReport;
    }

    public void createIndicators(IndicatorsMap indicatorsMap, Properties properties) throws Exception
    {
        String targetFossology = properties.getProperty(TARGET_FOSSOLOGY_PROPERTY);
        if (targetFossology == null) {
            throw new Exception(String.format("%s property not speficied", TARGET_FOSSOLOGY_PROPERTY));
        }

        LicenseAnalysisReport licenseAnalysisReport = null;

        if (targetFossology.toLowerCase().startsWith("http")) {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet get = new HttpGet(targetFossology);
            CloseableHttpResponse response = httpClient.execute(get);
            try {
                HttpEntity entity = response.getEntity();
                licenseAnalysisReport = parseLicenseAnalysisReport(entity.getContent());
                EntityUtils.consume(entity);
            } finally {
                response.close();
            }
        } else {
            licenseAnalysisReport = parseLicenseAnalysisReport(new FileInputStream(targetFossology));
        }

        indicatorsMap.add("number-of-different-licenses",
                licenseAnalysisReport.numberOfLicenses); //Number of (different?) component licenses

        indicatorsMap.add("percentage-of-files-without-license",
                ((double) licenseAnalysisReport.filesWithoutLicense /
                        (double) licenseAnalysisReport.totalFiles));

        indicatorsMap.add("files-with-unknown-license",
                ((double) licenseAnalysisReport.filesWithUnknownLicense /
                        (double) licenseAnalysisReport.totalFiles));

        indicatorsMap.add("copyleft-licenses",
                ((double) licenseAnalysisReport.filesWithCopyleftLicense /
                        (double) licenseAnalysisReport.totalFiles));

        indicatorsMap.add("copyleft-licenses-with-linking",
                ((double) licenseAnalysisReport.filesWithCopyleftWithLinkingLicense /
                        (double) licenseAnalysisReport.totalFiles));

        indicatorsMap.add("percentage-of-files-with-permissive-license",
                ((double) licenseAnalysisReport.filesWithPermissiveLicense /
                        (double) licenseAnalysisReport.totalFiles));

        indicatorsMap.add("files-with-commercial-license",
                ((double) licenseAnalysisReport.filesWithCommercialLicense /
                        (double) licenseAnalysisReport.totalFiles));

        //TODO
        //"percentage-of-files-with-public-domain-license"
        //"files-with-ads-required-license", 0);
    }
}
