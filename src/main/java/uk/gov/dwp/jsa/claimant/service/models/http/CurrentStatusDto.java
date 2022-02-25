package uk.gov.dwp.jsa.claimant.service.models.http;

import uk.gov.dwp.jsa.adaptors.dto.claim.status.BookingStatus;
import uk.gov.dwp.jsa.adaptors.dto.claim.status.CurrentStatus;
import uk.gov.dwp.jsa.adaptors.dto.claim.status.PushStatus;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class CurrentStatusDto extends CurrentStatus {

    public CurrentStatusDto() {
    }

    public CurrentStatusDto(
            final BookingStatus bookingStatus,
            final PushStatus pushStatus,
            final LocalDateTime createdTimestamp
    ) {
        super(bookingStatus, pushStatus, createdTimestamp);
    }

    @NotNull
    @Override
    public BookingStatus getBookingStatus() {
        return super.getBookingStatus();
    }

    @NotNull
    @Override
    public PushStatus getPushStatus() {
        return super.getPushStatus();
    }

    @NotNull
    @Override
    public LocalDateTime getCreatedTimestamp() {
        return super.getCreatedTimestamp();
    }

    public uk.gov.dwp.jsa.claimant.service.models.db.CurrentStatus toEntity() {
        final uk.gov.dwp.jsa.claimant.service.models.db.CurrentStatus currentStatus =
                new uk.gov.dwp.jsa.claimant.service.models.db.CurrentStatus();
        currentStatus.setStatus(this.getBookingStatus().getStatus());
        currentStatus.setSubstatus(this.getBookingStatus().getSubstatus());
        currentStatus.setPushStatus(this.getPushStatus().getStatus());
        currentStatus.setCreatedTimestamp(this.getCreatedTimestamp());
        return currentStatus;
    }
}
