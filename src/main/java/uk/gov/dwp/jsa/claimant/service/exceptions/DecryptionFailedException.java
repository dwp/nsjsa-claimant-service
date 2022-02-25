package uk.gov.dwp.jsa.claimant.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.gov.dwp.health.crypto.exception.CryptoException;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Something went wrong")
public class DecryptionFailedException extends RuntimeException {
    public DecryptionFailedException(final CryptoException e) {
        super(e);
    }
}
