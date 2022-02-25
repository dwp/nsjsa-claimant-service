package uk.gov.dwp.jsa.claimant.service.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.dwp.jsa.adaptors.NotificationServiceAdaptor;
import uk.gov.dwp.jsa.adaptors.http.api.SubmittedClaimsTally;
import uk.gov.dwp.jsa.claimant.service.repositories.ReportClaimantRepository;
import uk.gov.dwp.jsa.claimant.service.security.PrivateKeyProvider;
import uk.gov.dwp.jsa.security.JWTAuthenticationFactory;

import java.time.LocalDate;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
public class ReportServiceTest {

    private static final SubmittedClaimsTally SUBMITTED_CLAIMS_TALLY = new SubmittedClaimsTally(LocalDate.now(), 1L, 2L, 3L);

    @Mock
    private ReportClaimantRepository repository;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private NotificationServiceAdaptor notificationServiceAdaptor;
    @Mock
    private JWTAuthenticationFactory jwtAuthenticationFactory;
    @Mock
    private PrivateKeyProvider privateKeyProvider;

    private ReportService reportService;


    @Before
    public void beforeEachTest() {
        initMocks(this);
        reportService = new ReportService(repository, notificationServiceAdaptor, jwtAuthenticationFactory, privateKeyProvider);
    }

    @Test
    public void ensureThatWeCanSubmitTheClaimsTally() {
        givenWeHaveASubmittedClaimsTally();
        whenWeSubmittedClaimsCount();
        thenWeExpectToHaveCalledTheNotificationService();
    }

    private void givenWeHaveASubmittedClaimsTally() {
        when(repository.getSubmittedClaimsCount()).thenReturn(SUBMITTED_CLAIMS_TALLY);
    }

    private void whenWeSubmittedClaimsCount() {
        reportService.submitClaimsCount();
    }

    private void thenWeExpectToHaveCalledTheNotificationService() {
        verify(notificationServiceAdaptor, times(1)).sendSubmittedClaimsCountEmail(SUBMITTED_CLAIMS_TALLY);
    }

}
