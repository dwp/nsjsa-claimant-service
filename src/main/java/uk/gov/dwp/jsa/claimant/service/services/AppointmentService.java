package uk.gov.dwp.jsa.claimant.service.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.dwp.jsa.claimant.service.config.ClaimantProperties;
import uk.gov.dwp.jsa.claimant.service.config.StatusProperties;
import uk.gov.dwp.jsa.claimant.service.repositories.NextClaimantRepository;

import java.util.Optional;
import java.util.UUID;

@Service
public class AppointmentService {

    private final NextClaimantRepository nextClaimantRepository;
    private final ClaimantProperties claimantProperties;
    private final StatusProperties statusProperties;

    @Autowired
    public AppointmentService(final NextClaimantRepository nextClaimantRepository,
                              final ClaimantProperties claimantProperties,
                              final StatusProperties statusProperties) {
        this.nextClaimantRepository = nextClaimantRepository;
        this.claimantProperties = claimantProperties;
        this.statusProperties = statusProperties;
    }

    public Optional<UUID> getNextClaimantId() {
        clearLockedClaimants();
        return Optional.ofNullable(nextClaimantRepository.getNextClaimantId(
                statusProperties.getStartWorkHour(), statusProperties.getEndWorkHour()));
    }

    public void clearLockedClaimants() {
        nextClaimantRepository.clearLockedClaimants(claimantProperties.getLockDurationMinutes());
    }

}
