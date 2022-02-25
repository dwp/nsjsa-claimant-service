package uk.gov.dwp.jsa.claimant.service.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.dwp.jsa.adaptors.http.api.ApiResponse;
import uk.gov.dwp.jsa.claimant.service.services.AppointmentService;
import uk.gov.dwp.jsa.claimant.service.services.ResponseBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.UUID;

import static uk.gov.dwp.jsa.claimant.service.config.WithVersionUriComponentsBuilder.VERSION_SPEL;

@RestController
@RequestMapping("/nsjsa/" + VERSION_SPEL + "/claim")
public class ClaimController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClaimController.class);

    private final AppointmentService appointmentService;

    @Autowired
    public ClaimController(
            final AppointmentService appointmentService
    ) {
        this.appointmentService = appointmentService;
    }

    @PreAuthorize("hasAnyAuthority('CCM', 'CCA')")
    @GetMapping("/status/to-book")
    public ResponseEntity<ApiResponse<String>> getNextClaim(
            final HttpServletRequest request
    ) {
        LOGGER.debug("Getting next claim");
        return buildSuccessfulResponse(
                request.getRequestURI(),
                appointmentService.getNextClaimantId()
                        .map(UUID::toString)
                        .orElse(null),
                HttpStatus.OK
        );
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

}
