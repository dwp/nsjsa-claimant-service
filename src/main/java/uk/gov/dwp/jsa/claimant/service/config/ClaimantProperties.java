package uk.gov.dwp.jsa.claimant.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "claimant")
public class ClaimantProperties {

    private int lockDurationMinutes;

    public ClaimantProperties() {
        //Default constructor.
    }

    public int getLockDurationMinutes() {
        return lockDurationMinutes;
    }

    public void setLockDurationMinutes(final int lockDurationMinutes) {
        this.lockDurationMinutes = lockDurationMinutes;
    }

}
