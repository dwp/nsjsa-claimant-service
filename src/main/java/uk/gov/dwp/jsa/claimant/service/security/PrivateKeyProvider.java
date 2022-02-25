package uk.gov.dwp.jsa.claimant.service.security;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.dwp.jsa.security.KeyLoader;

import java.security.PrivateKey;

@Component
public class PrivateKeyProvider {

    private PrivateKey privateKey;

    public PrivateKeyProvider(@Value("${claimant.app.key.private:}") final String privateKeyString,
                              final KeyLoader<String> keyLoader) {
        if (StringUtils.isNotEmpty(privateKeyString)) {
            this.privateKey = keyLoader.loadPrivateKey(privateKeyString);
        }

    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }
}
