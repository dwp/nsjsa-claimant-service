package uk.gov.dwp.jsa.claimant.service.services;

import org.springframework.stereotype.Component;
import uk.gov.dwp.jsa.claimant.service.models.http.ClaimantRequest;

@Component
public class PhoneValidator {

    private static final String EMPTY = "";
    public static final String MOBILE_PREFIX = "07";

    boolean wasLandlineProvided(final ClaimantRequest claimantJson) {
        boolean isLandline = false;

        if (phoneNumberWasProvided(claimantJson)) {
            final String number = claimantJson.getContactDetails().getNumber();
            isLandline = !sanitise(number).startsWith(MOBILE_PREFIX);
        }

        return isLandline;
    }

    private boolean phoneNumberWasProvided(final ClaimantRequest claimantJson) {
        return claimantJson != null
                && claimantJson.getContactDetails() != null
                && claimantJson.getContactDetails().getNumber() != null
                && !claimantJson.getContactDetails().getNumber().trim().isEmpty();
    }

    private String sanitise(final String number) {
        return number.replaceAll("\\s", EMPTY)
                .replaceAll("-", EMPTY)
                .replaceAll("–", EMPTY)
                .replaceAll("\\(", EMPTY)
                .replaceAll("\\)", EMPTY);
    }

}
