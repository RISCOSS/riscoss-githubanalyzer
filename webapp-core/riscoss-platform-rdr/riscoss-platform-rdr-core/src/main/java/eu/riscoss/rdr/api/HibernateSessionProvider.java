package eu.riscoss.rdr.api;

import org.hibernate.Session;

/**
 * HibernateSessionProvider.
 * 
 * @version $Id$
 */
public interface HibernateSessionProvider
{
    Session getSession();
}
