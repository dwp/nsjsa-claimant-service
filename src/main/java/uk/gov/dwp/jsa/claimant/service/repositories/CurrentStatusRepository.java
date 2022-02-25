package uk.gov.dwp.jsa.claimant.service.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.dwp.jsa.claimant.service.models.db.CurrentStatus;

import java.util.List;
import java.util.UUID;

@Repository
public interface CurrentStatusRepository extends CrudRepository<CurrentStatus, UUID> {

    @Modifying
    @Query(value = "UPDATE current_status as st SET st.isDuplicate = true WHERE st.claimantId IN (:claimantIdList)")
    void invalidClaim(@Param("claimantIdList") List<UUID> claimantIdList);

}
