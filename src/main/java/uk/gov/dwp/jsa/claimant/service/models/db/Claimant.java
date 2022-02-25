package uk.gov.dwp.jsa.claimant.service.models.db;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.UpdateTimestamp;
import uk.gov.dwp.jsa.claimant.service.models.http.ClaimantRequest;
import uk.gov.dwp.jsa.security.encryption.SecuredJsonBinaryType;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "claimants")
@TypeDef(name = "jsonb", typeClass = SecuredJsonBinaryType.class)
public class Claimant {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "claimant_id", updatable = false, nullable = false, unique = true)
    private UUID claimantId;

    private String ninoSearchHash;

    @CreationTimestamp
    private LocalDateTime createdTimestamp;

    @UpdateTimestamp
    private LocalDateTime updatedTimestamp;

    @Column(name = "data_claim_submitted")
    private LocalDateTime dateOfClaim;

    private String hash;
    private String source;
    private String serviceVersion;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private ClaimantRequest claimantJson;

    @OneToOne(mappedBy = "claimant", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private CurrentStatus currentStatus;

    public Claimant() {
    }

    public Claimant(final ClaimantRequest claimantJson) {
        this.claimantJson = claimantJson;
    }

    public Claimant(
            final ClaimantRequest claimantJson,
            final String ninoSearchHash,
            final String hash,
            final String source,
            final String serviceVersion
    ) {
        this.claimantJson = claimantJson;
        this.ninoSearchHash = ninoSearchHash;
        this.hash = hash;
        this.source = source;
        this.serviceVersion = serviceVersion;
    }

    public Claimant(
            final UUID claimantId
    ) {
        this.claimantId = claimantId;
    }

    public LocalDateTime getDateOfClaim() {
        return dateOfClaim;
    }

    public void setDateOfClaim(final LocalDateTime dateOfClaim) {
        this.dateOfClaim = dateOfClaim;
    }

    public UUID getClaimantId() {
        return claimantId;
    }

    public void setClaimantId(final UUID claimantId) {
        this.claimantId = claimantId;
    }

    public String getNinoSearchHash() {
        return ninoSearchHash;
    }

    public void setNinoSearchHash(final String ninoSearchHash) {
        this.ninoSearchHash = ninoSearchHash;
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

    public String getHash() {
        return hash;
    }

    public void setHash(final String hash) {
        this.hash = hash;
    }

    public String getSource() {
        return source;
    }

    public void setSource(final String source) {
        this.source = source;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(final String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public ClaimantRequest getClaimantJson() {
        return claimantJson;
    }

    public void setClaimantJson(final ClaimantRequest claimantJson) {
        this.claimantJson = claimantJson;
    }

    public CurrentStatus getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(final CurrentStatus currentStatus) {
        this.currentStatus = currentStatus;
    }
}
