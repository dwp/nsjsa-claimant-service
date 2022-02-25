package uk.gov.dwp.jsa.claimant.service.utils;

import uk.gov.dwp.jsa.adaptors.dto.claim.Address;
import uk.gov.dwp.jsa.adaptors.dto.claim.ContactDetails;
import uk.gov.dwp.jsa.adaptors.dto.claim.Name;
import uk.gov.dwp.jsa.claimant.service.models.http.ClaimantRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ClaimantRequestCaseBuilder {

    private static final String NINO = "SL709883D";
    private static final Name NAME = new Name("M.", "Test", "Case");
    private static final Address ADDRESS = new Address(
            "First Line",
            "Second Line",
            "Town",
            "FY2 0UZ",
            "England"
    );
    private static final Address POSTAL_ADDRESS = new Address(
            "Postal First Line",
            "Postal Second Line",
            "Postal Town",
            "FY2 0UZ",
            "Postal England"
    );
    private static final ContactDetails CONTACT_DETAILS = new ContactDetails("077777777777", "email@email.com", true, true);

    private static final Supplier<String> CLAIMANT_ID_SUPPLIER = () -> UUID.randomUUID().toString();

    private static final LocalDate DOB = LocalDate.of(1980, 12, 31);
    private static final LocalDate DATE_OF_CLAIM = LocalDate.now();
    public static final String VERSION = "v1";


    private List<Consumer<ClaimantRequest>> operations;

    public static ClaimantRequestBuilder standard() {
        return new ClaimantRequestBuilder()
                .withDateOfClaim(DATE_OF_CLAIM)
                .withAddress(ADDRESS)
                .withContactDetails(CONTACT_DETAILS)
                .withDOB(DOB)
                .withName(NAME)
                .withNino(NINO)
                .withServiceVersion(VERSION)
                .withPostalAddress(POSTAL_ADDRESS);
    }

}
