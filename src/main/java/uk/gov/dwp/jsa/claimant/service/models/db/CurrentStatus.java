package uk.gov.dwp.jsa.claimant.service.models.db;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import uk.gov.dwp.jsa.adaptors.dto.claim.status.BookingStatus;
import uk.gov.dwp.jsa.adaptors.dto.claim.status.BookingStatusType;
import uk.gov.dwp.jsa.adaptors.dto.claim.status.PushStatus;
import uk.gov.dwp.jsa.adaptors.dto.claim.status.PushStatusType;
import uk.gov.dwp.jsa.claimant.service.models.http.CurrentStatusDto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "current_status")
public class CurrentStatus {

    @Id
    @Column(name = "claimant_id", updatable = false, nullable = false, unique = true)
    private UUID claimantId;

    @Enumerated(EnumType.STRING)
    private BookingStatusType status;

    private String substatus;

    @Enumerated(EnumType.STRING)
    private PushStatusType pushStatus;

    @CreationTimestamp
    private LocalDateTime createdTimestamp;

    @UpdateTimestamp
    private LocalDateTime updatedTimestamp;

    private LocalDateTime nextExpectedTimestamp;

    private Integer statusId;

    private Integer substatusId;

    private boolean isLocked;

    private boolean isDuplicate;

    private boolean isLandline;

    @OneToOne(fetch = FetchType.EAGER)
    @PrimaryKeyJoinColumn(name = "claimant_id", referencedColumnName = "claimant_id")
    private Claimant claimant;

    public CurrentStatus() {
        // for bean
    }

    public UUID getClaimantReference() {
        return claimantId;
    }

    public void setClaimantReference(final UUID claimantId) {
        this.claimantId = claimantId;
    }

    public BookingStatusType getStatus() {
        return status;
    }

    public void setStatus(final BookingStatusType status) {
        this.status = status;
    }

    public String getSubstatus() {
        return substatus;
    }

    public void setSubstatus(final String substatus) {
        this.substatus = substatus;
    }

    public LocalDateTime getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(final LocalDateTime createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public LocalDateTime getUpdatedTimestamp() {
        return updatedTimestamp;
    }

    public void setUpdatedTimestamp(final LocalDateTime updatedTimestamp) {
        this.updatedTimestamp = updatedTimestamp;
    }

    public LocalDateTime getNextExpectedTimestamp() {
        return nextExpectedTimestamp;
    }

    public void setNextExpectedTimestamp(final LocalDateTime nextExpectedTimestamp) {
        this.nextExpectedTimestamp = nextExpectedTimestamp;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(final boolean locked) {
        isLocked = locked;
    }

    public boolean isDuplicate() {
        return isDuplicate;
    }

    public void setDuplicate(final boolean duplicate) {
        isDuplicate = duplicate;
    }

    public Claimant getClaimant() {
        return claimant;
    }

    public void setClaimant(final Claimant claimant) {
        this.claimant = claimant;
    }

    public boolean isLandline() {
        return isLandline;
    }

    public void setLandline(final boolean landline) {
        isLandline = landline;
    }

    public Integer getStatusId() {
        return statusId;
    }

    public void setStatusId(final Integer statusId) {
        this.statusId = statusId;
    }

    public Integer getSubstatusId() {
        return substatusId;
    }

    public void setSubstatusId(final Integer substatusId) {
        this.substatusId = substatusId;
    }

    public PushStatusType getPushStatus() {
        return pushStatus;
    }

    public void setPushStatus(final PushStatusType pushStatus) {
        this.pushStatus = pushStatus;
    }

    public CurrentStatusDto toDto() {
        final CurrentStatusDto currentStatusDto = new CurrentStatusDto();
        currentStatusDto.setCreatedTimestamp(this.getCreatedTimestamp());
        currentStatusDto.setUpdatedTimestamp(this.getUpdatedTimestamp());
        currentStatusDto.setBookingStatus(new BookingStatus(this.getStatus(), this.getSubstatus(), "", ""));
        currentStatusDto.setPushStatus(new PushStatus(this.getPushStatus(), null));
        return currentStatusDto;
    }

}
