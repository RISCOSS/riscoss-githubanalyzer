package eu.riscoss.rdr.api.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.riscoss.rdc.RiskDataFactory;
import eu.riscoss.rdc.model.RiskData;
import eu.riscoss.rdr.api.HibernateSessionProvider;
import eu.riscoss.rdr.api.RiskDataRepository;

public class RiskDataRepositoryImpl implements RiskDataRepository
{
    private Logger logger = LoggerFactory.getLogger(RiskDataRepositoryImpl.class);

    private HibernateSessionProvider hibernateSessionProvider;

    public RiskDataRepositoryImpl(HibernateSessionProvider hibernateSessionProvider)
    {
        this.hibernateSessionProvider = hibernateSessionProvider;
    }

    @Override
    public List<RiskData> getRiskData(String target, int offset, int limit)
    {
        org.hibernate.Session hibernateSession = hibernateSessionProvider.getSession();
        hibernateSession.beginTransaction();

        try {
            Query query =
                    hibernateSession.createQuery(
                            "FROM RiskDataEntity AS RDE WHERE RDE.target = :target ORDER BY RDE.date DESC");
            query.setParameter("target", target);
            query.setFirstResult(offset);
            query.setMaxResults(limit);
            List<RiskDataEntity> riskDataEntities = query.list();

            List<RiskData> result = new ArrayList<RiskData>();
            for (RiskDataEntity riskDataEntity : riskDataEntities) {
                RiskData riskData = RiskDataFactory
                        .createRiskData(riskDataEntity.getId(), riskDataEntity.getTarget(), riskDataEntity.getDate(),
                                riskDataEntity.getType(), riskDataEntity.getValue());

                result.add(riskData);
            }

            return result;
        } catch (Exception e) {
            logger.error("Unable to get risk data", e);
            hibernateSession.getTransaction().rollback();
        } finally {
            hibernateSession.getTransaction().commit();
        }

        return Collections.EMPTY_LIST;
    }

    @Override
    public List<RiskData> getRiskData(String target, String id, int offset, int limit)
    {
        org.hibernate.Session hibernateSession = hibernateSessionProvider.getSession();
        hibernateSession.beginTransaction();

        try {
            Query query =
                    hibernateSession.createQuery(
                            "FROM RiskDataEntity AS RDE WHERE RDE.id = :id AND RDE.target = :target ORDER BY RDE.date DESC");
            query.setParameter("id", id);
            query.setParameter("target", target);
            query.setFirstResult(offset);
            query.setMaxResults(limit);
            List<RiskDataEntity> riskDataEntities = query.list();

            List<RiskData> result = new ArrayList<RiskData>();
            for (RiskDataEntity riskDataEntity : riskDataEntities) {
                RiskData riskData = RiskDataFactory
                        .createRiskData(riskDataEntity.getId(), riskDataEntity.getTarget(), riskDataEntity.getDate(),
                                riskDataEntity.getType(), riskDataEntity.getValue());

                result.add(riskData);
            }

            return result;
        } catch (Exception e) {
            logger.error("Unable to get risk data", e);
            hibernateSession.getTransaction().rollback();
        } finally {
            hibernateSession.getTransaction().commit();
        }

        return Collections.EMPTY_LIST;
    }

    @Override
    public void storeRiskData(RiskData riskData)
    {
        RiskDataEntity riskDataEntity = new RiskDataEntity();
        riskDataEntity.setId(riskData.getId());
        riskDataEntity.setTarget(riskData.getTarget());
        riskDataEntity.setType(riskData.getType());
        riskDataEntity.setDate(new Date());
        riskDataEntity.setValue(riskData.getValue());

        hibernateStore(riskDataEntity);
    }

    private void hibernateStore(Object object)
    {
        org.hibernate.Session session = hibernateSessionProvider.getSession();
        session.beginTransaction();
        try {
            session.saveOrUpdate(object);
        } catch (Exception e) {
            logger.error("Unable to store object", e);
            session.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            session.getTransaction().commit();
        }
    }
}
