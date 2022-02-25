package uk.gov.dwp.jsa.claimant.service.services;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.dwp.jsa.claimant.service.config.ClaimantProperties;
import uk.gov.dwp.jsa.claimant.service.config.StatusProperties;
import uk.gov.dwp.jsa.claimant.service.repositories.NextClaimantRepository;

import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class AppointmentServiceTest {

    private static int LOCK_DURATION = 20;

    private static int START_WORK_HOUR = 8;
    private static int END_WORK_HOUR = 20;

    @Mock
    NextClaimantRepository nextClaimantRepository;

    @Mock
    ClaimantProperties claimantProperties;
    @Mock
    StatusProperties statusProperties;

    AppointmentService appointmentService;

    @Before
    public void beforeEachTest() {

        initMocks(this);
        when(claimantProperties.getLockDurationMinutes()).thenReturn(20);
        when(statusProperties.getStartWorkHour()).thenReturn(8);
        when(statusProperties.getEndWorkHour()).thenReturn(20);
    }

    @Test
    public void givenRespositoryReturnsNextClaimantServiceThenReturnsGuid() {
        // Given
        UUID nextClaimant = UUID.randomUUID();

        // When
        when(nextClaimantRepository.getNextClaimantId(START_WORK_HOUR, END_WORK_HOUR)).thenReturn(nextClaimant);
        appointmentService = new AppointmentService(nextClaimantRepository, claimantProperties, statusProperties);
        Optional<UUID> nextClaimantId = appointmentService.getNextClaimantId();

        // Then
        assertThat(nextClaimantId.isPresent(), is(true));
        assertThat(nextClaimantId.get(), is(nextClaimant));
    }


    @Test
    public void givenGetNextAppointmentIsCalledShouldAlsoCallUnlockSP() {
        // Given
        UUID nextClaimant = UUID.randomUUID();

        // When
        when(nextClaimantRepository.getNextClaimantId(START_WORK_HOUR, END_WORK_HOUR)).thenReturn(nextClaimant);
        appointmentService = new AppointmentService(nextClaimantRepository, claimantProperties, statusProperties);
        appointmentService.getNextClaimantId();

        // Then
        verify(nextClaimantRepository, times(1)).clearLockedClaimants(LOCK_DURATION);

    }

    @Test
    public void givenRespositoryReturnsNullNextClaimantServiceThenReturnsEmptyOptional() {
        // When
        when(nextClaimantRepository.getNextClaimantId(START_WORK_HOUR, END_WORK_HOUR)).thenReturn(null);
        appointmentService = new AppointmentService(nextClaimantRepository, claimantProperties, statusProperties);
        Optional<UUID> nextClaimantId = appointmentService.getNextClaimantId();

        // Then
        assertThat(nextClaimantId.isPresent(), is(false));
    }

    @Test
    public void givenAappointeeServiceCalled() {
        // When
        appointmentService = new AppointmentService(nextClaimantRepository, claimantProperties, statusProperties);
        appointmentService.clearLockedClaimants();

        // Then
        verify(nextClaimantRepository, times(1)).clearLockedClaimants(LOCK_DURATION);
    }

}
