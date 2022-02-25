package uk.gov.dwp.jsa.claimant.service.services;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.dwp.jsa.adaptors.ValidationServiceAdaptor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.Mockito.when;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ValidationServiceTest {

    @Mock
    private ValidationServiceAdaptor validationServiceAdaptor;

    private ValidationService validationService;

    @Before
    public void beforeEachTest() {
        initMocks(this);
        validationService = new ValidationService(validationServiceAdaptor);
    }

    @Test
    public void invalidateClaimStatus() {
        List<UUID> claimantIds = Arrays.asList(new UUID[] { UUID.randomUUID() });
        when(validationServiceAdaptor.invalidateStatus(claimantIds)).thenReturn(Optional.of(true));
        boolean result = validationService.invalidateClaimStatus(claimantIds);
        assertThat(result, is(true));
    }
}
