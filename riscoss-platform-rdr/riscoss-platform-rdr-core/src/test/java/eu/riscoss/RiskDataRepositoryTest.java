package eu.riscoss;

import java.util.Date;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.riscoss.rdc.RiskDataFactory;
import eu.riscoss.rdc.model.RiskData;
import eu.riscoss.rdc.model.RiskDataType;
import eu.riscoss.rdr.RiskDataRepositoryFactory;
import eu.riscoss.rdr.api.RiskDataRepository;

import static org.junit.Assert.assertEquals;

public class RiskDataRepositoryTest
{
    private static RiskDataRepository riskDataRepository;

    @BeforeClass
    public static void beforeClass() throws Exception
    {
        riskDataRepository = RiskDataRepositoryFactory.create(new HibernateSessionProviderImpl());
    }

    @Test
    public void storeAndGetRiskData() throws Exception
    {
        final String TARGET = "foo";

        for (int i = 0; i < 5; i++) {
            RiskData riskData = RiskDataFactory.createRiskData("rd" + i, TARGET, new Date(), RiskDataType.NUMBER, 1.0d);

            riskDataRepository.storeRiskData(riskData);
        }

        List<RiskData> riskData = riskDataRepository.getRiskData(TARGET, 0, 5);

        assertEquals(5, riskData.size());

        riskData = riskDataRepository.getRiskData(TARGET, "rd0", 0, 5);

        assertEquals(1, riskData.size());
    }
}
