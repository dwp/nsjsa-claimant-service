package uk.gov.dwp.jsa.claimant.service.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.dwp.jsa.adaptors.dto.claim.status.BookingStatusType;
import uk.gov.dwp.jsa.adaptors.dto.claim.status.BookingSubStatus;
import uk.gov.dwp.jsa.claimant.service.config.StatusProperties;
import uk.gov.dwp.jsa.claimant.service.models.db.CurrentStatus;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static uk.gov.dwp.jsa.adaptors.dto.claim.status.BookingStatusType.FIRST_FAIL;
import static uk.gov.dwp.jsa.adaptors.dto.claim.status.BookingStatusType.PREVIEW;
import static uk.gov.dwp.jsa.adaptors.dto.claim.status.BookingSubStatus.CALLBACK_1HR;
import static uk.gov.dwp.jsa.adaptors.dto.claim.status.BookingSubStatus.CALLBACK_2HR;
import static uk.gov.dwp.jsa.adaptors.dto.claim.status.BookingSubStatus.CALLBACK_3HR;
import static uk.gov.dwp.jsa.adaptors.dto.claim.status.BookingSubStatus.FAIL_TO_ATTEND;
import static uk.gov.dwp.jsa.adaptors.dto.claim.status.BookingSubStatus.LANDLINE_ONLY;
import static uk.gov.dwp.jsa.adaptors.dto.claim.status.BookingSubStatus.PV_FLAG;
import static uk.gov.dwp.jsa.adaptors.dto.claim.status.BookingSubStatus.TOO_EARLY_FIRST_PERIOD;
import static uk.gov.dwp.jsa.adaptors.dto.claim.status.BookingSubStatus.TOO_EARLY_SECOND_PERIOD;
import static uk.gov.dwp.jsa.adaptors.dto.claim.status.BookingSubStatus.TOO_LATE;
import static uk.gov.dwp.jsa.adaptors.dto.claim.status.BookingSubStatus.WRONG_NINO;

@Component
public class NextExpectedTime {

    public static final int NEXT_DAY = 1;
    public static final int START_MINUTE = 0;

    private StatusProperties statusProperties;

    @Autowired
    public NextExpectedTime(final StatusProperties statusProperties) {
        this.statusProperties = statusProperties;
    }

    public LocalDateTime calculate(final CurrentStatus currentStatus) {
        if (isCurrentStatus(currentStatus, PREVIEW, TOO_EARLY_FIRST_PERIOD)) {
            return calculateNextTimeTooEarly(currentStatus, statusProperties.getTooEarlyFirstPeriodStartHr());
        }
        if (isCurrentStatus(currentStatus, PREVIEW, TOO_EARLY_SECOND_PERIOD)) {
            return calculateNextTimeTooEarly(currentStatus, statusProperties.getTooEarlySecondPeriodStartHr());
        }
        if (isCurrentStatus(currentStatus, PREVIEW, PV_FLAG)) {
            return calculateNextTimeTooEarly(currentStatus, statusProperties.getTooEarlySecondPeriodStartHr());
        }
        if (isFailFirstCallback(currentStatus)) {
            return calculateNextTime(currentStatus, statusProperties.getFailFirstCallbackHr());
        }
        if (isCurrentStatus(currentStatus, FIRST_FAIL, CALLBACK_2HR)) {
            return calculateNextTime(currentStatus, statusProperties.getFailSecondCallbackHr());
        }
        if (isCurrentStatus(currentStatus, FIRST_FAIL, CALLBACK_3HR)) {
            return calculateNextTime(currentStatus, statusProperties.getFailThirdCallbackHr());
        }
        if (isCurrentStatus(currentStatus, PREVIEW, TOO_LATE)) {
            return calculateNextTimeTooLate(currentStatus, statusProperties.getNextdayStartEarlyHr());
        }
        return null;
    }

    private boolean isCurrentStatus(final CurrentStatus currentStatus,
                                    final BookingStatusType status, final BookingSubStatus subStatus) {
        return currentStatus.getStatus() == status
                && currentStatus.getSubstatus().equalsIgnoreCase(subStatus.toString());
    }

    private boolean isFailFirstCallback(final CurrentStatus currentStatus) {
        return currentStatus.getStatus() == FIRST_FAIL
                && isSubStatusForCallbackIn1Hr(currentStatus);
    }

    private boolean isSubStatusForCallbackIn1Hr(final CurrentStatus currentStatus) {
        String subStatus = currentStatus.getSubstatus();
        return subStatus.equalsIgnoreCase(CALLBACK_1HR.toString())
                || subStatus.equalsIgnoreCase(WRONG_NINO.toString())
                || subStatus.equalsIgnoreCase(LANDLINE_ONLY.toString())
                || subStatus.equalsIgnoreCase(FAIL_TO_ATTEND.toString());
    }

    private LocalDateTime calculateNextTimeTooEarly(final CurrentStatus currentStatus, final Integer earlyHr) {
        LocalDateTime updatedTimestamp = currentStatus.getUpdatedTimestamp();
        return updatedTimestamp.with(LocalTime.of(earlyHr, START_MINUTE));
    }

    private LocalDateTime calculateNextTime(final CurrentStatus currentStatus, final Integer callbackHr) {
        LocalDateTime updatedTimestamp = currentStatus.getUpdatedTimestamp();
        return updatedTimestamp.plusHours(callbackHr);
    }

    private LocalDateTime calculateNextTimeTooLate(
            final CurrentStatus currentStatus, final Integer nextdayStartEarlyHr) {
        LocalDateTime updatedTimestamp = currentStatus.getUpdatedTimestamp();
        return updatedTimestamp.plusDays(NEXT_DAY).with(LocalTime.of(nextdayStartEarlyHr, START_MINUTE));
    }
}
