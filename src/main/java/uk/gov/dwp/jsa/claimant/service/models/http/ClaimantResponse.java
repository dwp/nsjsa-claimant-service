package uk.gov.dwp.jsa.claimant.service.models.http;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.beans.BeanUtils;
import uk.gov.dwp.jsa.claimant.service.models.db.Claimant;
import uk.gov.dwp.jsa.claimant.service.models.db.CurrentStatus;

import java.util.Objects;

public class ClaimantResponse extends ClaimantRequest {

    public ClaimantResponse() {
        super();
    }

    public ClaimantResponse(final Claimant claimant) {
        Objects.requireNonNull(claimant);
        Objects.requireNonNull(claimant.getClaimantJson());

        BeanUtils.copyProperties(claimant.getClaimantJson(), this);

        populateClaimantCurrentStatus(claimant.getCurrentStatus());

        this.setClaimantId(claimant.getClaimantId());
    }

    private void populateClaimantCurrentStatus(final CurrentStatus currentStatus) {
        if (currentStatus != null) {
            this.setCurrentStatus(currentStatus.toDto());
        }
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
