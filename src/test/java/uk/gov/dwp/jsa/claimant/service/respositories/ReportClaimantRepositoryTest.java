package uk.gov.dwp.jsa.claimant.service.respositories;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.dwp.jsa.adaptors.http.api.SubmittedClaimsTally;
import uk.gov.dwp.jsa.claimant.service.repositories.ReportClaimantRepository;

import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ReportClaimantRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private ReportClaimantRepository reportClaimantRepository;

    private SubmittedClaimsTally expectedSubmittedClaimsTally;
    private SubmittedClaimsTally actualSubmittedClaimsTally;

    @Before
    public void beforeEachTest() {
        initMocks(this);
        reportClaimantRepository = new ReportClaimantRepository(jdbcTemplate);
        ReflectionTestUtils.setField(reportClaimantRepository, "dbSchema", "claimant_schema");
    }

    @Test
    public void ensureThatWeCanGetTheSubmittedClaimsTally() {
        givenWeHaveAnExpectedSubmittedClaimsTally();
        whenWeCallTheStoredProcedure();
        thenWeExpectTheSubmittedClaimsCountToBeCorrect();
    }

    private void givenWeHaveAnExpectedSubmittedClaimsTally() {
        expectedSubmittedClaimsTally = createSubmittedClaimsTally();
        when(jdbcTemplate.queryForObject(eq(String.format(ReportClaimantRepository.SQL, "claimant_schema")),
                any(BeanPropertyRowMapper.class))).thenReturn(expectedSubmittedClaimsTally);
    }

    private void whenWeCallTheStoredProcedure() {
        actualSubmittedClaimsTally = reportClaimantRepository.getSubmittedClaimsCount();
    }

    private void thenWeExpectTheSubmittedClaimsCountToBeCorrect() {
        assertThat(actualSubmittedClaimsTally, is(expectedSubmittedClaimsTally));
    }

    private SubmittedClaimsTally createSubmittedClaimsTally() {
        return new SubmittedClaimsTally(LocalDate.now(), 1L, 2L, 3L);
    }
}
