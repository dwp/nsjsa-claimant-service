package uk.gov.dwp.jsa.claimant.service.exceptions;

import org.springframework.http.HttpStatus;

public class ClaimantByIdNotFoundException extends RuntimeException {
    static final String CODE = String.valueOf(HttpStatus.NOT_FOUND.value());
    static final String MESSAGE = "Could not find a claimant by id";
}
