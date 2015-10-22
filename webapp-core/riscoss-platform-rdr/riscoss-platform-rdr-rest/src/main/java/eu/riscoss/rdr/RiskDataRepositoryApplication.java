package eu.riscoss.rdr;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.riscoss.rdr.rest.RiskDataResource;

public class RiskDataRepositoryApplication extends Application
{
    Logger logger = LoggerFactory.getLogger(RiskDataRepositoryApplication.class);

    private HashSet<Class<?>> classes;

    public RiskDataRepositoryApplication()
    {
        logger.info("Initializing Risk Data Repository");

        classes = new HashSet<Class<?>>();
        classes.add(RiskDataResource.class);
    }

    @Override
    public Set<Class<?>> getClasses()
    {
        return classes;
    }
}
