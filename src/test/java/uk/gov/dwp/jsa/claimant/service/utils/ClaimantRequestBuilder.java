package uk.gov.dwp.jsa.claimant.service.utils;

import uk.gov.dwp.jsa.adaptors.dto.claim.Address;
import uk.gov.dwp.jsa.adaptors.dto.claim.ContactDetails;
import uk.gov.dwp.jsa.adaptors.dto.claim.Name;
import uk.gov.dwp.jsa.claimant.service.models.http.ClaimantRequest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ClaimantRequestBuilder {

    private List<Consumer<ClaimantRequest>> operations;

    public ClaimantRequestBuilder() {
        this.operations = new ArrayList<>();
    }

    public ClaimantRequestBuilder withNino(final String nino) {
        operations.add(c -> c.setNino(nino));
        return this;
    }

    public ClaimantRequestBuilder withName(final Name name) {
        operations.add(c -> c.setName(name));
        return this;
    }

    public ClaimantRequestBuilder withDOB(final LocalDate dob) {
        operations.add(c -> c.setDateOfBirth(dob));
        return this;
    }

    public ClaimantRequestBuilder withContactDetails(final ContactDetails contactDetails) {
        operations.add(c -> c.setContactDetails(contactDetails));
        return this;
    }

    public ClaimantRequestBuilder withAddress(final Address address) {
        operations.add(c -> c.setAddress(address));
        return this;
    }

    public ClaimantRequestBuilder withPostalAddress(final Address postalAddress) {
        operations.add(c -> c.setPostalAddress(postalAddress));
        return this;
    }

    public ClaimantRequestBuilder withServiceVersion(final String serviceVersion) {
        operations.add(c -> c.setServiceVersion(serviceVersion));
        return this;
    }

    public ClaimantRequestBuilder withDateOfClaim(final LocalDate dateOfClaim) {
        operations.add(c -> c.setDateOfClaim(dateOfClaim));
        return this;
    }

    public ClaimantRequest build() {
        ClaimantRequest claimant = new ClaimantRequest();
        operations.forEach(operation -> operation.accept(claimant));
        return claimant;
    }
}
