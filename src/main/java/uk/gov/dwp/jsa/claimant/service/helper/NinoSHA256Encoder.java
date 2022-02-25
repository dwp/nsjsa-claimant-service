package uk.gov.dwp.jsa.claimant.service.helper;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static uk.gov.dwp.jsa.claimant.service.Constants.NINO_REPLACEMENT_KEY;

@Component
public class NinoSHA256Encoder implements StringEncoder {

    private final String ninoReplacement;

    public NinoSHA256Encoder(@Value("${" + NINO_REPLACEMENT_KEY + "}")
                             final String ninoReplacement) {
        this.ninoReplacement = ninoReplacement;
    }

    @Override
    public String encode(final String ninoToBeEncoded) {
        Objects.requireNonNull(ninoToBeEncoded);
        return DigestUtils.sha256Hex(ninoReplacement + ninoToBeEncoded.substring(2));
    }
}
