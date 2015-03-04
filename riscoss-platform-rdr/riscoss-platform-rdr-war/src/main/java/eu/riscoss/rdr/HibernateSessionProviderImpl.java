package eu.riscoss.rdr;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.riscoss.rdr.api.HibernateSessionProvider;

public class HibernateSessionProviderImpl implements HibernateSessionProvider
{
    private Logger logger = LoggerFactory.getLogger(HibernateSessionProviderImpl.class);

    private SessionFactory sessionFactory;

    public HibernateSessionProviderImpl() throws Exception
    {
        try {
            // Create the SessionFactory from hibernate.cfg.xml
            sessionFactory = new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            logger.error("Unable to create session", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public Session getSession()
    {
        return sessionFactory.getCurrentSession();
    }
}
