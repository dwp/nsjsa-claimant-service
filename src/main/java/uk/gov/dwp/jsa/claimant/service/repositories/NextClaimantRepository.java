package uk.gov.dwp.jsa.claimant.service.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import java.util.UUID;

@Component
public class NextClaimantRepository {

    private final EntityManager entityManager;

    @Value("${claimant.db.schema:claimant_schema}")
    private String dbSchema;

    @Autowired
    public NextClaimantRepository(final EntityManager entityManager) {

        this.entityManager = entityManager;
    }

    public UUID getNextClaimantId(final int startHours, final int endHours) {
        Query query = entityManager.createNativeQuery(
                String.format(
                        "SELECT CAST(get_next_claimant AS varchar(50)) FROM %s.get_next_claimant(%s,%s)",
                        dbSchema, startHours, endHours));

        Object singleResult = query.getSingleResult();
        if (singleResult == null) {
            return null;
        }
        return UUID.fromString((String) singleResult);
    }

    public void clearLockedClaimants(final Integer lockMinutes) {
        StoredProcedureQuery query = entityManager
                .createStoredProcedureQuery(dbSchema + ".clear_locked_claimants")
                .registerStoredProcedureParameter(1, Integer.class, ParameterMode.IN)
                .setParameter(1, lockMinutes);

        query.execute();
    }
}
