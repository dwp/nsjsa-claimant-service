package uk.gov.dwp.jsa.claimant.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "claimant.status")
public class StatusProperties {

    private Integer failFirstCallbackHr;
    private Integer failSecondCallbackHr;
    private Integer failThirdCallbackHr;
    private Integer nextdayStartEarlyHr;
    private Integer tooEarlyFirstPeriodStartHr;
    private Integer tooEarlySecondPeriodStartHr;
    private Integer startWorkHour;
    private Integer endWorkHour;

    public Integer getFailFirstCallbackHr() {
        return failFirstCallbackHr;
    }

    public void setFailFirstCallbackHr(final Integer failFirstCallbackHr) {
        this.failFirstCallbackHr = failFirstCallbackHr;
    }

    public Integer getFailSecondCallbackHr() {
        return failSecondCallbackHr;
    }

    public void setFailSecondCallbackHr(final Integer failSecondCallbackHr) {
        this.failSecondCallbackHr = failSecondCallbackHr;
    }

    public Integer getFailThirdCallbackHr() {
        return failThirdCallbackHr;
    }

    public void setFailThirdCallbackHr(final Integer failThirdCallbackHr) {
        this.failThirdCallbackHr = failThirdCallbackHr;
    }

    public Integer getNextdayStartEarlyHr() {
        return nextdayStartEarlyHr;
    }

    public void setNextdayStartEarlyHr(final Integer nextdayStartEarlyHr) {
        this.nextdayStartEarlyHr = nextdayStartEarlyHr;
    }

    public Integer getTooEarlyFirstPeriodStartHr() {
        return tooEarlyFirstPeriodStartHr;
    }

    public void setTooEarlyFirstPeriodStartHr(final Integer tooEarlyFirstPeriodStartHr) {
        this.tooEarlyFirstPeriodStartHr = tooEarlyFirstPeriodStartHr;
    }

    public Integer getTooEarlySecondPeriodStartHr() {
        return tooEarlySecondPeriodStartHr;
    }

    public void setTooEarlySecondPeriodStartHr(final Integer tooEarlySecondPeriodStartHr) {
        this.tooEarlySecondPeriodStartHr = tooEarlySecondPeriodStartHr;
    }

    public Integer getStartWorkHour() {
        return startWorkHour;
    }

    public void setStartWorkHour(final Integer startWorkHour) {
        this.startWorkHour = startWorkHour;
    }

    public Integer getEndWorkHour() {
        return endWorkHour;
    }

    public void setEndWorkHour(final Integer endWorkHour) {
        this.endWorkHour = endWorkHour;
    }
}
