package eu.riscoss.rdr;

import eu.riscoss.rdr.api.HibernateSessionProvider;
import eu.riscoss.rdr.api.RiskDataRepository;
import eu.riscoss.rdr.api.internal.RiskDataRepositoryImpl;

public class RiskDataRepositoryFactory
{
    public static RiskDataRepository create(HibernateSessionProvider hibernateSessionProvider)
    {
        return new RiskDataRepositoryImpl(hibernateSessionProvider);
    }
}
