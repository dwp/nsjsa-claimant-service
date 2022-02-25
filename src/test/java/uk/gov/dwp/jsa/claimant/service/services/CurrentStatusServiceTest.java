package uk.gov.dwp.jsa.claimant.service.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.dwp.jsa.adaptors.dto.claim.status.PushStatus;
import uk.gov.dwp.jsa.claimant.service.exceptions.ClaimantByIdNotFoundException;
import uk.gov.dwp.jsa.claimant.service.models.db.Claimant;
import uk.gov.dwp.jsa.claimant.service.models.db.CurrentStatus;
import uk.gov.dwp.jsa.claimant.service.models.http.ClaimantRequest;
import uk.gov.dwp.jsa.claimant.service.models.http.CurrentStatusDto;
import uk.gov.dwp.jsa.claimant.service.repositories.ClaimantRepository;
import uk.gov.dwp.jsa.claimant.service.repositories.CurrentStatusRepository;
import uk.gov.dwp.jsa.claimant.service.utils.CurrentStatusRequestCaseBuilder;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.dwp.jsa.adaptors.dto.claim.status.BookingStatusType.NEW_CLAIM;
import static uk.gov.dwp.jsa.adaptors.dto.claim.status.PushStatusType.NOT_PUSHED;
import static uk.gov.dwp.jsa.adaptors.dto.claim.status.PushStatusType.PUSHED;

@RunWith(MockitoJUnitRunner.class)
public class CurrentStatusServiceTest {

    private CurrentStatusService sut;

    private static final CurrentStatusDto EXPECTED_STATUS_DTO = CurrentStatusRequestCaseBuilder.standardBookingStatus();
    private static final UUID EXPECTED_CLAIMANT_ID = UUID.fromString("c633bec7-a5c3-4dba-94ab-9f80c9cc5b1e");
    private static final CurrentStatus EXPECTED_STATUS = getExpectedCurrentStatus();
    private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(2018, Month.DECEMBER, 31, 12, 0, 0);

    @Mock
    private Claimant EXPECTED_CLAIMANT;

    @Mock
    private ClaimantRequest EXPECTED_CLAIMANT_REQUEST;

    @Mock
    private ClaimantRepository claimantRepository;

    @Mock
    private CurrentStatusRepository statusRepository;

    @Mock
    private NextExpectedTime nextExpectedTime;

    @Mock
    private PhoneValidator phoneValidator;

    @Before
    public void setUp() {
        sut = new CurrentStatusService(statusRepository, claimantRepository, nextExpectedTime, phoneValidator);

        when(claimantRepository.findById(EXPECTED_CLAIMANT_ID)).thenReturn(Optional.of(EXPECTED_CLAIMANT));
        when(EXPECTED_CLAIMANT.getClaimantJson()).thenReturn(EXPECTED_CLAIMANT_REQUEST);
        when(statusRepository.save(any())).thenReturn(EXPECTED_STATUS);
    }

    @Test
    public void givenValidCurrentStatusRequest_Save_ShouldReturnExpectedClaimantId() {
        UUID claimantId = sut.save(EXPECTED_CLAIMANT_ID, EXPECTED_STATUS_DTO);
        assertEquals(EXPECTED_CLAIMANT_ID, claimantId);
    }

    @Test
    public void givenValidCurrentStatusRequest_Save_ShouldSaveTheExpectedDataToRepository() {
        UUID claimantId = sut.save(EXPECTED_CLAIMANT_ID, EXPECTED_STATUS_DTO);

        ArgumentCaptor<CurrentStatus> captor = ArgumentCaptor.forClass(CurrentStatus.class);
        verify(statusRepository, times(1)).save(captor.capture());
        verify(nextExpectedTime, times(1)).calculate(captor.getValue());

        ArgumentCaptor<ClaimantRequest> requestCaptor = ArgumentCaptor.forClass(ClaimantRequest.class);
        verify(phoneValidator, times(1)).wasLandlineProvided(requestCaptor.capture());

        assertThat(requestCaptor.getValue(), is(EXPECTED_CLAIMANT_REQUEST));
        assertThat(captor.getValue().getStatus(), is(EXPECTED_STATUS_DTO.getBookingStatus().getStatus()));
        assertThat(captor.getValue().getSubstatus(), is(EXPECTED_STATUS_DTO.getBookingStatus().getSubstatus()));
        assertThat(captor.getValue().getClaimantReference(), is(EXPECTED_CLAIMANT_ID));
        assertThat(claimantId, is(EXPECTED_CLAIMANT_ID));
        assertThat(captor.getValue().getCreatedTimestamp(), is(EXPECTED_STATUS_DTO.getCreatedTimestamp()));
    }

    @Test
    public void givenValidCurrentStatusRequest_getClaimantById_ShouldGetTheExpectedDataToRepository() {
        UUID claimantId = sut.save(EXPECTED_CLAIMANT_ID, EXPECTED_STATUS_DTO);

        ArgumentCaptor<UUID> captor = ArgumentCaptor.forClass(UUID.class);
        verify(claimantRepository, times(1)).findById(captor.capture());

        assertThat(captor.getValue(), is(EXPECTED_CLAIMANT_ID));
        assertThat(claimantId, is(EXPECTED_CLAIMANT_ID));
    }

    @Test(expected = ClaimantByIdNotFoundException.class)
    public void givenInvalidCurrentStatusRequest_getClaimantById_ShouldGetTheExpectedDataToRepository() {
        when(claimantRepository.findById(EXPECTED_CLAIMANT_ID)).thenReturn(Optional.empty());
        sut.save(EXPECTED_CLAIMANT_ID, EXPECTED_STATUS_DTO);
    }

    @Test
    public void GivenNullPushStatus_Save_ShouldSetTheDefault() {
        when(claimantRepository.findById(EXPECTED_CLAIMANT_ID)).thenReturn(Optional.of(EXPECTED_CLAIMANT));

        sut.save(EXPECTED_CLAIMANT_ID, EXPECTED_STATUS_DTO);

        ArgumentCaptor<CurrentStatus> captor = ArgumentCaptor.forClass(CurrentStatus.class);

        verify(statusRepository, times(1)).save(captor.capture());

        assertThat(captor.getValue().getPushStatus(), is(NOT_PUSHED));

    }


    @Test
    public void GivenNullPushStatus_Save_ShouldSetExpectedValue() {
        when(claimantRepository.findById(EXPECTED_CLAIMANT_ID)).thenReturn(Optional.of(EXPECTED_CLAIMANT));
        final CurrentStatusDto currentStatusDto = CurrentStatusRequestCaseBuilder.standardBookingStatus();
        currentStatusDto.setPushStatus(new PushStatus(PUSHED, null));

        sut.save(EXPECTED_CLAIMANT_ID, currentStatusDto);

        ArgumentCaptor<CurrentStatus> captor = ArgumentCaptor.forClass(CurrentStatus.class);

        verify(statusRepository, times(1)).save(captor.capture());

        assertThat(captor.getValue().getPushStatus(), is(PUSHED));

    }

    private static CurrentStatus getExpectedCurrentStatus() {
        final CurrentStatus currentStatus = new CurrentStatus();
        currentStatus.setClaimantReference(EXPECTED_CLAIMANT_ID);
        currentStatus.setLocked(true);
        currentStatus.setDuplicate(true);
        currentStatus.setCreatedTimestamp(LOCAL_DATE_TIME);
        currentStatus.setUpdatedTimestamp(LOCAL_DATE_TIME);
        currentStatus.setNextExpectedTimestamp(LOCAL_DATE_TIME);
        currentStatus.setStatus(NEW_CLAIM);
        currentStatus.setSubstatus("subStatus");
        return currentStatus;
    }
}
