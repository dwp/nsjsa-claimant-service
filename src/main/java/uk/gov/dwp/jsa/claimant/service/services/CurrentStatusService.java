package uk.gov.dwp.jsa.claimant.service.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.dwp.jsa.adaptors.dto.claim.status.BookingSubStatus;
import uk.gov.dwp.jsa.claimant.service.Constants;
import uk.gov.dwp.jsa.claimant.service.exceptions.ClaimantByIdNotFoundException;
import uk.gov.dwp.jsa.claimant.service.models.db.Claimant;
import uk.gov.dwp.jsa.claimant.service.models.db.CurrentStatus;
import uk.gov.dwp.jsa.claimant.service.models.http.CurrentStatusDto;
import uk.gov.dwp.jsa.claimant.service.repositories.ClaimantRepository;
import uk.gov.dwp.jsa.claimant.service.repositories.CurrentStatusRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CurrentStatusService {

    private final CurrentStatusRepository statusRepository;
    private final ClaimantRepository claimantRepository;
    private final NextExpectedTime nextExpectedTime;
    private final PhoneValidator phoneValidator;

    @Autowired
    public CurrentStatusService(
            final CurrentStatusRepository statusRepository,
            final ClaimantRepository claimantRepository,
            final NextExpectedTime nextExpectedTime,
            final PhoneValidator phoneValidator
    ) {
        this.statusRepository = statusRepository;
        this.claimantRepository = claimantRepository;
        this.nextExpectedTime = nextExpectedTime;
        this.phoneValidator = phoneValidator;
    }

    public UUID save(final UUID claimantId, final CurrentStatusDto currentStatusDto) {
        final Optional<Claimant> optionalClaimant = claimantRepository.findById(claimantId);

        if (optionalClaimant.isPresent()) {

            final Claimant claimant = optionalClaimant.get();
            final CurrentStatus currentStatus = createOrUpdateCurrentStatusEntity(claimant, currentStatusDto);

            currentStatus.setClaimant(claimant);
            currentStatus.setClaimantReference(claimantId);

            claimant.setCurrentStatus(currentStatus);
            claimantRepository.save(claimant);

            return statusRepository.save(currentStatus).getClaimantReference();

        } else {
            throw new ClaimantByIdNotFoundException();
        }
    }

    private CurrentStatus createOrUpdateCurrentStatusEntity(final Claimant claimant,
                                                            final CurrentStatusDto currentStatusDto) {
        CurrentStatus entityToBeSaved = claimant.getCurrentStatus();
        if (entityToBeSaved == null) {
            entityToBeSaved = currentStatusDto.toEntity();
        }
        entityToBeSaved.setLocked(false);
        entityToBeSaved.setStatus(currentStatusDto.getBookingStatus().getStatus());
        entityToBeSaved.setSubstatus(currentStatusDto.getBookingStatus().getSubstatus());
        entityToBeSaved.setNextExpectedTimestamp(nextExpectedTime.calculate(entityToBeSaved));
        entityToBeSaved.setLandline(phoneValidator.wasLandlineProvided(claimant.getClaimantJson()));
        entityToBeSaved.setStatusId(entityToBeSaved.getStatus().getStatusId());
        entityToBeSaved.setSubstatusId(getBookingSubStatusOrderId(entityToBeSaved));
        entityToBeSaved.setPushStatus(currentStatusDto.getPushStatus().getStatus());

        return entityToBeSaved;
    }

    private int getBookingSubStatusOrderId(final CurrentStatus statusToBeSaved) {
        final List<String> substatuses = Arrays
                .stream(BookingSubStatus.values())
                .map(Enum::toString)
                .collect(Collectors.toList());

        if (substatuses.contains(statusToBeSaved.getSubstatus())) {
            return BookingSubStatus.valueOf(statusToBeSaved.getSubstatus()).getSubstatusOrderId();
        }

        return Constants.LAST;
    }
}
