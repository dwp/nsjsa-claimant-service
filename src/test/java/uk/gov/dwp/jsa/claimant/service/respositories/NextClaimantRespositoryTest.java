package uk.gov.dwp.jsa.claimant.service.respositories;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.dwp.jsa.claimant.service.repositories.NextClaimantRepository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class NextClaimantRespositoryTest {

    private static int LOCK_DURATION = 20;

    private static int START_WORK_HOUR = 8;
    private static int END_WORK_HOUR = 20;

    @Mock
    EntityManager entityManager;

    @Mock
    Query query;

    @Mock
    StoredProcedureQuery procedureQuery;

    NextClaimantRepository nextClaimantRepository;

    @Before
    public void beforeEachTest() {
        initMocks(this);
    }

    @Test
    public void givenQueryReturnsNextClaimantThenReturnsGuid() {
        // Given
        String nextClaimant = UUID.randomUUID().toString();

        // When
        when(query.getSingleResult()).thenReturn(nextClaimant);
        when(entityManager.createNativeQuery("SELECT CAST(get_next_claimant AS varchar(50)) FROM claimant_schema.get_next_claimant(8,20)")).thenReturn(query);

        createRepo();
        UUID nextClaimantId = nextClaimantRepository.getNextClaimantId(START_WORK_HOUR, END_WORK_HOUR);

        // Then
        assertThat(nextClaimantId, is(UUID.fromString(nextClaimant)));
    }

    private void createRepo() {
        nextClaimantRepository = new NextClaimantRepository(entityManager);
        ReflectionTestUtils.setField(nextClaimantRepository, "dbSchema", "claimant_schema");
    }

    @Test
    public void givenQueryDoesntReturnsNextClaimantThenReturnsNull() {
        // Given
        String nextClaimant = null;

        // When
        when(query.getSingleResult()).thenReturn(nextClaimant);
        when(entityManager.createNativeQuery("SELECT CAST(get_next_claimant AS varchar(50)) FROM claimant_schema.get_next_claimant(8,20)")).thenReturn(query);

        createRepo();
        UUID nextClaimantId = nextClaimantRepository.getNextClaimantId(START_WORK_HOUR, END_WORK_HOUR);

        // Then
        assertNull(nextClaimantId);
    }

    @Test
    public void givenClearLockedClaimantsCalled() {
        // When
        when(entityManager.createStoredProcedureQuery("claimant_schema.clear_locked_claimants")).thenReturn(procedureQuery);
        when(procedureQuery.registerStoredProcedureParameter(eq(1), any(), any())).thenReturn(procedureQuery);
        when(procedureQuery.setParameter(eq(1), eq(20))).thenReturn(procedureQuery);

        createRepo();
        nextClaimantRepository.clearLockedClaimants(LOCK_DURATION);

        // Then
        verify(procedureQuery, times(1)).execute();
    }
}
