package uk.gov.dwp.jsa.claimant.service.exceptions;

import uk.gov.dwp.jsa.claimant.service.Constants;

public class ClaimantAlreadyExistsException extends RuntimeException {
    static final String CODE = Constants.DEFAULT_ERROR_CODE;
    static final String MESSAGE = "Claimant already exists";
}
