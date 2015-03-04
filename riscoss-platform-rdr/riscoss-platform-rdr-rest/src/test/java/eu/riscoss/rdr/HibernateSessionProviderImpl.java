package eu.riscoss.rdr;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import eu.riscoss.rdr.api.HibernateSessionProvider;

public class HibernateSessionProviderImpl implements HibernateSessionProvider
{
    private SessionFactory sessionFactory;

    public HibernateSessionProviderImpl() throws Exception
    {
        try {
            // Create the SessionFactory from hibernate.cfg.xml
            sessionFactory = new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            ex.printStackTrace();
            throw new ExceptionInInitializerError(ex);
        }
    }

    @Override
    public Session getSession()
    {
        return sessionFactory.getCurrentSession();
    }
}
