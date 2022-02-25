package uk.gov.dwp.jsa.claimant.service.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.dwp.jsa.adaptors.http.api.ApiResponse;
import uk.gov.dwp.jsa.adaptors.http.api.ApiSuccess;
import uk.gov.dwp.jsa.claimant.service.Constants;
import uk.gov.dwp.jsa.claimant.service.config.WithVersionUriComponentsBuilder;
import uk.gov.dwp.jsa.claimant.service.models.http.ClaimantRequest;
import uk.gov.dwp.jsa.claimant.service.models.http.ClaimantResponse;
import uk.gov.dwp.jsa.claimant.service.models.http.CurrentStatusDto;
import uk.gov.dwp.jsa.claimant.service.services.ClaimantService;
import uk.gov.dwp.jsa.claimant.service.services.CurrentStatusService;
import uk.gov.dwp.jsa.claimant.service.services.NinoSanitiser;
import uk.gov.dwp.jsa.claimant.service.services.ResponseBuilder;
import uk.gov.dwp.jsa.security.roles.AnyRole;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.fromController;
import static uk.gov.dwp.jsa.claimant.service.config.WithVersionUriComponentsBuilder.VERSION_SPEL;

@RestController
@RequestMapping("/nsjsa/" + VERSION_SPEL + "/citizen")
public class CitizenController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CitizenController.class);

    private final ClaimantService claimantService;
    private final CurrentStatusService currentStatusService;
    private final WithVersionUriComponentsBuilder uriBuilder;
    private final NinoSanitiser ninoSanitiser;

    @Autowired
    public CitizenController(
            final ClaimantService claimantService,
            final WithVersionUriComponentsBuilder uriBuilder,
            final NinoSanitiser ninoSanitiser,
            final CurrentStatusService currentStatusService
    ) {
        this.currentStatusService = currentStatusService;
        this.claimantService = claimantService;
        this.uriBuilder = uriBuilder;
        this.ninoSanitiser = ninoSanitiser;
    }


    @AnyRole
    @GetMapping("/{claimantId}")
    public ResponseEntity<ApiResponse<ClaimantResponse>> getClaimantById(
            @PathVariable final UUID claimantId,
            final HttpServletRequest request

    ) {
        LOGGER.debug("Getting claimant for claimantId: {}", claimantId);
        return buildSuccessfulResponse(
                request.getRequestURI(),
                claimantService.getClaimantById(claimantId),
                HttpStatus.OK
        );
    }

    @AnyRole
    @GetMapping("/nino")
    public ResponseEntity<ApiResponse<ClaimantResponse>> getClaimantByNino(
            @RequestBody final Map<String, Object> payload) {
        LOGGER.debug("Getting claimant for nino");

        final String nino = (String) payload.get("nino");

        final List<ApiSuccess<ClaimantResponse>> successes = claimantService.getClaimantByNino(
                ninoSanitiser.sanitise(nino)).stream()
                .map(claimantResponse -> new ApiSuccess<>(
                        URI.create(buildResourceUriFor(claimantResponse.getClaimantId())),
                        claimantResponse
                ))
                .collect(toList());

        return buildMultipleSuccessfulResponse(
                successes
        );
    }

    private ResponseEntity<ApiResponse<ClaimantResponse>> buildMultipleSuccessfulResponse(
            final List<ApiSuccess<ClaimantResponse>> successes
    ) {
        return new ResponseBuilder<ClaimantResponse>()
                .withStatus(HttpStatus.OK)
                .withSuccessData(successes)
                .build();
    }

    @PreAuthorize("!hasAnyAuthority('WC', 'SCA')")
    @PostMapping
    public ResponseEntity<ApiResponse<UUID>> createClaimant(
            @RequestBody @Validated final ClaimantRequest claimantRequest
    ) {
        LOGGER.debug("Creating claimant for claimantId: {}", claimantRequest.getClaimantId());
        claimantRequest.setNino(ninoSanitiser.sanitise(claimantRequest.getNino()));
        final UUID claimantId = claimantService.save(claimantRequest);

        return buildSuccessfulResponse(
                buildResourceUriFor(claimantId),
                claimantId,
                HttpStatus.CREATED
        );
    }

    @AnyRole
    @DeleteMapping("/{claimantId}")
    public ResponseEntity<ApiResponse<UUID>> deleteClaimantById(@PathVariable final UUID claimantId) {
        LOGGER.debug("Deleting claimant for claimantId: {}", claimantId);

        if (nonNull(claimantService.getClaimantById(claimantId))) {
            claimantService.delete(claimantId);
            return buildSuccessfulResponse(
                    buildResourceUriFor(claimantId),
                    claimantId,
                    HttpStatus.OK
            );
        } else {
            LOGGER.error("Error deleting claimant for claimantId: {}", claimantId);
            return buildErrorResponse(
                    HttpStatus.NOT_FOUND
            );
        }
    }

    @PreAuthorize("hasAnyAuthority('CITIZEN', 'CCA', 'CCM', 'WC')")
    @PutMapping("/{claimantId}/status")
    public ResponseEntity<ApiResponse<UUID>> updateClaimantStatus(
            @PathVariable final UUID claimantId,
            @RequestBody @Validated final CurrentStatusDto currentStatusDto
    ) {
        LOGGER.debug("Updating claimant status for claimantId: {}", claimantId);
        currentStatusService.save(claimantId, currentStatusDto);
        return buildSuccessfulResponse(
                buildResourceUriFor(claimantId),
                claimantId,
                HttpStatus.OK
        );
    }

    @PreAuthorize("hasAnyAuthority('CCM', 'CCA', 'WC', 'SCA')")
    @PatchMapping("/{claimantId}")
    public ResponseEntity<ApiResponse<UUID>> updateClaimant(
            @PathVariable final UUID claimantId,
            @RequestBody @Validated final ClaimantRequest claimantRequest

    ) {
        LOGGER.debug("Updating claimant for claimantId: {}", claimantId);
        claimantService.update(claimantId, claimantRequest);

        return buildSuccessfulResponse(
                buildResourceUriFor(claimantId),
                claimantId,
                HttpStatus.OK
        );
    }

    private String buildResourceUriFor(final UUID claimantId) {
        return fromController(uriBuilder, getClass())
                .path("/{id}")
                .buildAndExpand(claimantId)
                .toUri().getPath();
    }


    private <T> ResponseEntity<ApiResponse<T>> buildSuccessfulResponse(
            final String path,
            final T objectToReturn,
            final HttpStatus status
    ) {
        return new ResponseBuilder<T>()
                .withStatus(status)
                .withSuccessData(URI.create(path), objectToReturn)
                .build();
    }

    private <T> ResponseEntity<ApiResponse<T>> buildErrorResponse(
            final HttpStatus status
    ) {
        return new ResponseBuilder<T>()
                .withStatus(status)
                .withApiError(Constants.DEFAULT_ERROR_CODE, status.getReasonPhrase())
                .build();
    }

}
