package eu.riscoss.rdr;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.riscoss.rdr.rest.RiskDataRepositoryProvider;

public class RiskDataRepositoryBootstrap implements ServletContextListener
{
    private Logger logger = LoggerFactory.getLogger(RiskDataRepositoryBootstrap.class);

    public void contextDestroyed(ServletContextEvent event)
    {
        RiskDataRepositoryProvider.setRiskDataRepository(null);
    }

    public void contextInitialized(ServletContextEvent event)
    {
        try {
            RiskDataRepositoryProvider.setRiskDataRepository(RiskDataRepositoryFactory
                    .create(new HibernateSessionProviderImpl()));
        } catch (Exception e) {
            logger.error("Unable to initialize Risk Data Repository", e);
            throw new RuntimeException(e);
        }

        logger.info("Risk data repository initialized.");
    }
}
