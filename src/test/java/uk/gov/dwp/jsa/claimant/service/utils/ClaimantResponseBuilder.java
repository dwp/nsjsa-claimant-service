package uk.gov.dwp.jsa.claimant.service.utils;

import uk.gov.dwp.jsa.claimant.service.models.http.ClaimantResponse;

import java.time.LocalDate;
import java.util.UUID;

public class ClaimantResponseBuilder {
    private static final UUID CLAIMANT_ID = UUID.randomUUID();
    private static final LocalDate DATE_OF_CLAIM = LocalDate.now();

    private UUID claimantId = CLAIMANT_ID;
    private LocalDate dateOfClaim = DATE_OF_CLAIM;

    public ClaimantResponse build() {
        ClaimantResponse response = new ClaimantResponse();
        response.setClaimantId(claimantId);
        response.setDateOfClaim(dateOfClaim);
        return response;
    }

    public ClaimantResponseBuilder withClaimantId(final UUID claimantId) {
        this.claimantId = claimantId;
        return this;
    }

    public ClaimantResponseBuilder withDateOfClaim(final LocalDate dateOfClaim) {
        this.dateOfClaim = dateOfClaim;
        return this;
    }
}

