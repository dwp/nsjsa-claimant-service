package uk.gov.dwp.jsa.claimant.service.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.dwp.jsa.adaptors.dto.claim.status.BookingStatusType;
import uk.gov.dwp.jsa.adaptors.dto.claim.status.BookingSubStatus;
import uk.gov.dwp.jsa.claimant.service.config.StatusProperties;
import uk.gov.dwp.jsa.claimant.service.models.db.CurrentStatus;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static uk.gov.dwp.jsa.adaptors.dto.claim.status.BookingStatusType.FIRST_FAIL;
import static uk.gov.dwp.jsa.adaptors.dto.claim.status.BookingStatusType.PREVIEW;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {NextExpectedTimeTest.TestConfiguration.class})
@ActiveProfiles("test")
public class NextExpectedTimeTest {

    private static final int FAIL_FIRST_CALLBACK_HR = 1;
    private static final int FAIL_SECOND_CALLBACK_HR = 2;
    private static final int FAIL_THIRD_CALLBACK_HR = 3;
    private static final int NEXT_DAY_START_EARLY_TIME_HR = 8;
    private static final int TOO_EARLY_FIRST_PERIOD_START_HR = 8;
    private static final int TOO_EARLY_SECOND_PERIOD_START_HR = 9;

    private NextExpectedTime nextExpectedTime;

    private CurrentStatus currentStatus;

    private LocalDateTime nextTime;

    private LocalDateTime currentTime;

    private BookingSubStatus[] bookingSubStatuses = bookingSubStatusParameters();

    @Autowired
    private StatusProperties statusProperties;

    @Before
    public void setUp() {
        nextExpectedTime = new NextExpectedTime(statusProperties);
        currentStatus = new CurrentStatus();
        currentTime = LocalDateTime.now();
        bookingSubStatuses = bookingSubStatusParameters();
    }

    @Test
    public void testThatICanGetTheNextTimeForTheFirstCallback() {
        for (BookingSubStatus subStatus : bookingSubStatuses) {
            givenWeHaveAUpdatedTimestamp();
            givenWeHaveAStatusOf(FIRST_FAIL, subStatus);
            whenWeGetTheNextTime();
            thenWeExpectTheNextTimeToBeTheStartOfTheFollowingDay(FAIL_FIRST_CALLBACK_HR);
        }
    }

    @Test
    public void testThatICanGetTheNextTimeForTheSecondCallback() {
        givenWeHaveAUpdatedTimestamp();
        givenWeHaveAStatusOf(FIRST_FAIL, BookingSubStatus.CALLBACK_2HR);
        whenWeGetTheNextTime();
        thenWeExpectTheNextTimeToBeTheStartOfTheFollowingDay(FAIL_SECOND_CALLBACK_HR);
    }

    @Test
    public void testThatICanGetTheNextTimeForTheThirdCallback() {
        givenWeHaveAUpdatedTimestamp();
        givenWeHaveAStatusOf(FIRST_FAIL, BookingSubStatus.CALLBACK_3HR);
        whenWeGetTheNextTime();
        thenWeExpectTheNextTimeToBeTheStartOfTheFollowingDay(FAIL_THIRD_CALLBACK_HR);
    }

    @Test
    public void testThatICanGetTheNextTimeWhenTheStatusIsTooLate() {
        givenWeHaveAUpdatedTimestamp();
        givenWeHaveAStatusOf(PREVIEW, BookingSubStatus.TOO_LATE);
        whenWeGetTheNextTime();
        thenWeExpectTheNextTimeToBeTheStartOfTheFollowingDay();
    }

    @Test
    public void testThatICanGetTheNextTimeWhenTheStatusIsTooEarlyFirstPeriod() {
        givenWeHaveAUpdatedTimestamp();
        givenWeHaveAStatusOf(PREVIEW, BookingSubStatus.TOO_EARLY_FIRST_PERIOD);
        whenWeGetTheNextTime();
        thenWeExpectTheNextTimeToBeTheStartTimeOf(TOO_EARLY_FIRST_PERIOD_START_HR);
    }

    @Test
    public void testThatICanGetTheNextTimeWhenTheStatusIsTooEarlySecondPeriod() {
        givenWeHaveAUpdatedTimestamp();
        givenWeHaveAStatusOf(PREVIEW, BookingSubStatus.TOO_EARLY_SECOND_PERIOD);
        whenWeGetTheNextTime();
        thenWeExpectTheNextTimeToBeTheStartTimeOf(TOO_EARLY_SECOND_PERIOD_START_HR);
    }

    @Test
    public void testThatICanGetTheNextTimeWhenTheStatusIsPvFlag() {
        givenWeHaveAUpdatedTimestamp();
        givenWeHaveAStatusOf(PREVIEW, BookingSubStatus.PV_FLAG);
        whenWeGetTheNextTime();
        thenWeExpectTheNextTimeToBeTheStartTimeOf(TOO_EARLY_SECOND_PERIOD_START_HR);
    }

    @Test
    public void testThatIDontGetTheNextTimeForAStatusIsNotSet() {
        givenWeHaveAUpdatedTimestamp();
        whenWeGetTheNextTime();
        thenWeExpectTheNextTimeToNotBeSet();
    }

    private void thenWeExpectTheNextTimeToNotBeSet() {
        assertNull(nextTime);
    }

    private void givenWeHaveAStatusOf(final BookingStatusType status, final BookingSubStatus subStatus) {
        currentStatus.setStatus(status);
        currentStatus.setSubstatus(subStatus.toString());
    }

    private void givenWeHaveAUpdatedTimestamp() {
        currentStatus.setUpdatedTimestamp(currentTime);
    }

    private void whenWeGetTheNextTime() {
        nextTime = nextExpectedTime.calculate(currentStatus);
    }

    private void thenWeExpectTheNextTimeToBeTheStartOfTheFollowingDay(int hour) {
        assertThat(nextTime, is(currentTime.plusHours(hour)));
    }

    private void thenWeExpectTheNextTimeToBeTheStartOfTheFollowingDay() {
        assertThat(nextTime, is(currentTime.plusDays(1).with(LocalTime.of(NEXT_DAY_START_EARLY_TIME_HR, 0))));
    }

    private void thenWeExpectTheNextTimeToBeTheStartTimeOf(final int periodStartHr) {
        assertThat(nextTime, is(currentTime.with(LocalTime.of(periodStartHr, 0))));
    }

    private BookingSubStatus[] bookingSubStatusParameters() {
        return new BookingSubStatus[]{
                BookingSubStatus.CALLBACK_1HR,
                BookingSubStatus.WRONG_NINO,
                BookingSubStatus.LANDLINE_ONLY,
                BookingSubStatus.FAIL_TO_ATTEND
        };
    }

    @EnableConfigurationProperties(StatusProperties.class)
    public static class TestConfiguration {
        // nothing
    }
}
