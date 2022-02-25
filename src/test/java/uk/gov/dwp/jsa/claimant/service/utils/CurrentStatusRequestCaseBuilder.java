package uk.gov.dwp.jsa.claimant.service.utils;

import uk.gov.dwp.jsa.adaptors.dto.claim.status.BookingStatus;
import uk.gov.dwp.jsa.adaptors.dto.claim.status.BookingStatusType;
import uk.gov.dwp.jsa.adaptors.dto.claim.status.PushStatus;
import uk.gov.dwp.jsa.claimant.service.models.http.CurrentStatusDto;

import java.time.LocalDateTime;
import java.time.Month;

import static uk.gov.dwp.jsa.adaptors.dto.claim.status.PushStatusType.NOT_PUSHED;

public class CurrentStatusRequestCaseBuilder {

    public static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(2018, Month.DECEMBER, 31, 12, 0, 0);

    public static CurrentStatusDto standardBookingStatus() {
        final CurrentStatusDto statusDto = new CurrentStatusDto();
        statusDto.setBookingStatus(new BookingStatus(BookingStatusType.NEW_CLAIM, "subStatus", "", ""));
        statusDto.setPushStatus(new PushStatus(NOT_PUSHED, null));
        statusDto.setCreatedTimestamp(LOCAL_DATE_TIME);
        return statusDto;
    }

    public static CurrentStatusDto invalid() {
        return new CurrentStatusDto();
    }
}
