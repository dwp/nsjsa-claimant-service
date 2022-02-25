package uk.gov.dwp.jsa.claimant.service.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.dwp.jsa.claimant.service.models.db.Claimant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClaimantRepository extends CrudRepository<Claimant, UUID> {

    Optional<List<Claimant>> findByNinoSearchHashOrderByCreatedTimestampDesc(final String nino);

}
