package uk.gov.dwp.jsa.claimant.service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.dwp.jsa.claimant.service.exceptions.ClaimantAlreadyExistsException;
import uk.gov.dwp.jsa.claimant.service.exceptions.ClaimantByIdNotFoundException;
import uk.gov.dwp.jsa.claimant.service.helper.StringEncoder;
import uk.gov.dwp.jsa.claimant.service.models.db.Claimant;
import uk.gov.dwp.jsa.claimant.service.models.http.ClaimantRequest;
import uk.gov.dwp.jsa.claimant.service.models.http.ClaimantResponse;
import uk.gov.dwp.jsa.claimant.service.repositories.ClaimantRepository;
import uk.gov.dwp.jsa.claimant.service.repositories.CurrentStatusRepository;
import uk.gov.dwp.jsa.security.roles.Role;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ClaimantService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClaimantService.class);

    private final ClaimantRepository repository;
    private final CurrentStatusRepository currentStatusRepository;
    private final ObjectMapper mapper;
    private final StringEncoder encoder;
    private final ValidationService validationService;

    @Autowired
    public ClaimantService(final ClaimantRepository repository,
                           final CurrentStatusRepository currentStatusRepository,
                           final ObjectMapper mapper,
                           final StringEncoder encoder,
                           final ValidationService validationService) {
        this.repository = repository;
        this.currentStatusRepository = currentStatusRepository;
        this.mapper = mapper;
        this.encoder = encoder;
        this.validationService = validationService;
    }

    @Transactional
    public UUID save(final ClaimantRequest claimantRequest) {

        final Claimant claimant = new Claimant();

        setupClaimantEntity(claimant, claimantRequest);

        final Claimant claimantCreated;

        try {
            claimantCreated = repository.save(claimant);
            List<UUID> oldClaims =
                    this.getClaimByNinoAndDoB(
                            claimantCreated.getClaimantId(),
                            claimantRequest.getNino(),
                            claimantRequest.getDateOfBirth());
            if (!oldClaims.isEmpty()) {
                this.currentStatusRepository.invalidClaim(oldClaims);
                this.validationService.invalidateClaimStatus(oldClaims);
            }

        } catch (DuplicateKeyException e) {
            LOGGER.error("Error saving claimant for claimantId: {}",
                                       claimantRequest.getClaimantId(), e);
            throw new ClaimantAlreadyExistsException();
        }
        return claimantCreated.getClaimantId();
    }

    public UUID update(final UUID id, final ClaimantRequest claimantRequest) {
        final Claimant claimant = repository.findById(id).orElseThrow(ClaimantByIdNotFoundException::new);

        setupClaimantEntity(claimant, claimantRequest);

        try {
            repository.save(claimant);
        } catch (DuplicateKeyException e) {
            LOGGER.error("Error updating claimant for claimantId: {}",
                                       claimantRequest.getClaimantId(), e);
            throw new ClaimantAlreadyExistsException();
        }
        return claimant.getClaimantId();
    }


    private void setupClaimantEntity(final Claimant claimant, final ClaimantRequest claimantRequest) {
        claimant.setClaimantJson(claimantRequest);
        claimant.setServiceVersion(claimantRequest.getServiceVersion());
        claimant.setNinoSearchHash(encoder.encode(claimantRequest.getNino()));
        claimant.setHash(DigestUtils.sha256Hex(produceClaimantRequestJson(claimantRequest)));
        claimant.setSource(Role.userType().toString());
    }

    private String produceClaimantRequestJson(final ClaimantRequest claimantRequest) {
        String claimantRequestJson;
        try {
            claimantRequestJson = mapper.writeValueAsString(claimantRequest);
        } catch (JsonProcessingException e) {
            LOGGER.error("Error creating JSON for claimant for claimantId: {}",
                                      claimantRequest.getClaimantId(), e);
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        }
        return claimantRequestJson;
    }

    public ClaimantResponse getClaimantById(final UUID claimantId) {
        return repository.findById(claimantId)
                .map(ClaimantResponse::new)
                .orElseThrow(ClaimantByIdNotFoundException::new);
    }

    public List<ClaimantResponse> getClaimantByNino(final String nino) {
        return repository.findByNinoSearchHashOrderByCreatedTimestampDesc(encoder.encode(nino))
                .orElseGet(ArrayList::new).stream()
                .map(ClaimantResponse::new)
                .filter(c -> nino.equals(c.getNino()))
                .collect(Collectors.toList());

    }

    public void delete(final UUID claimantId) {
        repository.deleteById(claimantId);
    }

    private List<UUID> getClaimByNinoAndDoB(final UUID newClaimantId, final String nino, final LocalDate dob) {
        List<ClaimantResponse> claimsWithSameNino =
                repository.findByNinoSearchHashOrderByCreatedTimestampDesc(encoder.encode(nino))
                .orElseGet(ArrayList::new).stream()
                .filter(c -> c.getCurrentStatus() == null || !c.getCurrentStatus().isDuplicate())
                .map(ClaimantResponse::new)
                .filter(c -> nino.equals(c.getNino()))
                .collect(Collectors.toList());
        return claimsWithSameNino.stream()
                .filter(c -> !newClaimantId.equals(c.getClaimantId()))
                .filter(c -> dob.equals(c.getDateOfBirth()))
                .map(ClaimantResponse::getClaimantId)
                .collect(Collectors.toList());
    }
}
