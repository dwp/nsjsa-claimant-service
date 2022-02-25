package uk.gov.dwp.jsa.claimant.service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.dwp.jsa.claimant.service.exceptions.ClaimantByIdNotFoundException;
import uk.gov.dwp.jsa.claimant.service.helper.StringEncoder;
import uk.gov.dwp.jsa.claimant.service.models.db.Claimant;
import uk.gov.dwp.jsa.claimant.service.models.http.ClaimantRequest;
import uk.gov.dwp.jsa.claimant.service.models.http.ClaimantResponse;
import uk.gov.dwp.jsa.claimant.service.repositories.ClaimantRepository;
import uk.gov.dwp.jsa.claimant.service.repositories.CurrentStatusRepository;
import uk.gov.dwp.jsa.claimant.service.utils.ClaimantRequestCaseBuilder;
import uk.gov.dwp.jsa.security.AuthenticationToken;
import uk.gov.dwp.jsa.security.roles.Role;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ClaimantServiceTest {

    private static final String EXPECTED_JSON = "DUMMY_JSON";

    private static final UUID EXPECTED_CLAIMANT_ID = UUID.fromString("c633bec7-a5c3-4dba-94ab-9f80c9cc5b1e");
    private static final UUID INVALID_CLAIMANT_ID = UUID.randomUUID();
    private static final UUID DUPLICATE_CLAIMANT_ID = UUID.randomUUID();

    private static final String VALID_NINO = "ST123456789_NINO";
    private static final String ENCODED_NINO = "123456789_ENCODED";
    private static final String INVALID_NINO = "FAKE_NINO";

    private static final LocalDate DOB = LocalDate.of(1982, 9,21);

    private static final ClaimantRequest CLAIMANT_REQUEST =
            ClaimantRequestCaseBuilder.standard().withNino(VALID_NINO).withDOB(DOB).build();


    private static final Claimant EXPECTED_CLAIMANT = getExpectedClaimant();

    private static final ClaimantResponse EXPECTED_RESPONSE = new ClaimantResponse(EXPECTED_CLAIMANT);

    private ClaimantService sut;

    @Mock
    private ClaimantRepository repository;

    @Mock
    private CurrentStatusRepository currentStatusRepository;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private StringEncoder mockEncoder;

    @Mock
    private ValidationService validationService;

    @Before
    public void setUp() throws JsonProcessingException {
        final List<Claimant> claimantList = singletonList(EXPECTED_CLAIMANT);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));

        sut = new ClaimantService(repository, currentStatusRepository, mapper, mockEncoder, validationService);
        when(mapper.writeValueAsString(CLAIMANT_REQUEST)).thenReturn(EXPECTED_JSON);
        when(repository.save(any())).thenReturn(EXPECTED_CLAIMANT);
        when(repository.findById(EXPECTED_CLAIMANT_ID)).thenReturn(Optional.of(EXPECTED_CLAIMANT));
        when(repository.findByNinoSearchHashOrderByCreatedTimestampDesc(ENCODED_NINO)).thenReturn(Optional.of(claimantList));
        when(mockEncoder.encode(VALID_NINO)).thenReturn(ENCODED_NINO);
    }

    @Test
    public void givenValidClaimantRequest_Save_ShouldReturnExpectedClaimantId() {
        AuthenticationToken authenticationToken = new AuthenticationToken("", "", "",
                singletonList(new SimpleGrantedAuthority(Role.CCA.name())));
        authenticate(authenticationToken);

        UUID claimantId = sut.save(CLAIMANT_REQUEST);
        assertEquals(EXPECTED_CLAIMANT_ID, claimantId);
    }

    @Test
    public void givenValidClaimantId_getClaimantById_ShouldReturnExpectedClaimant() {
        ClaimantResponse claimantResult = sut.getClaimantById(EXPECTED_CLAIMANT_ID);
        assertEquals(EXPECTED_RESPONSE, claimantResult);
    }

    @Test(expected = ClaimantByIdNotFoundException.class)
    public void givenUnvalidClaimantId_getClaimantById_ShouldThrowNotFoundException() {
        sut.getClaimantById(INVALID_CLAIMANT_ID);
    }

    @Test
    public void givenValidNino_getClaimantByNino_ShouldReturnClaimantList() {

        assertEquals(singletonList(EXPECTED_RESPONSE), sut.getClaimantByNino(VALID_NINO));
    }

    public void givenUnvalidNino_getClaimantByNino_ShouldThrowNotFoundException() {
        assertThat(sut.getClaimantByNino(INVALID_NINO), is(empty()));
    }

    @Test
    public void givenValidClaimantRequest_Save_ShouldSaveTheExpectedDataToRepository() {

        AuthenticationToken authenticationToken = new AuthenticationToken("", "", "",
                singletonList(new SimpleGrantedAuthority(Role.CCA.name())));
        authenticate(authenticationToken);

        sut.save(CLAIMANT_REQUEST);

        ArgumentCaptor<Claimant> captor = ArgumentCaptor.forClass(Claimant.class);
        verify(repository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getClaimantJson(), is(CLAIMANT_REQUEST));
        assertThat(captor.getValue().getNinoSearchHash(), is(ENCODED_NINO));
    }

    @Test
    public void givenValidClaimantWithExistingClaim_Save_ShouldInvalidDuplicateClaimant() {

        AuthenticationToken authenticationToken = new AuthenticationToken("", "", "",
                singletonList(new SimpleGrantedAuthority(Role.CCA.name())));
        authenticate(authenticationToken);

        whenDuplicateClaimExists();

        sut.save(CLAIMANT_REQUEST);

        ArgumentCaptor<Claimant> captor = ArgumentCaptor.forClass(Claimant.class);
        verify(repository, times(1)).save(captor.capture());

        ArgumentCaptor<List<UUID>> statusRepoCaptor = ArgumentCaptor.forClass(List.class);
        verify(currentStatusRepository, times(1)).invalidClaim(statusRepoCaptor.capture());
        verify(validationService, times(1)).invalidateClaimStatus(statusRepoCaptor.capture());

        assertThat(captor.getValue().getClaimantJson(), is(CLAIMANT_REQUEST));
        assertThat(captor.getValue().getNinoSearchHash(), is(ENCODED_NINO));
        assertEquals(1, statusRepoCaptor.getValue().size());
        assertThat(statusRepoCaptor.getValue().get(0), is(DUPLICATE_CLAIMANT_ID));

    }

    @Test
    public void givenValidNinoMatchsMultipleHash_getClaimantByNino_ShouldReturnOnlyTheOneMatchingTheJsonNino() {
        Claimant claimantWithSimilarNino =
                generateClaimant(ClaimantRequestCaseBuilder.standard().withNino(replaceNino2FirstCharacters()).build());
        when(repository.findByNinoSearchHashOrderByCreatedTimestampDesc(ENCODED_NINO)).thenReturn(Optional.of(Arrays.asList(EXPECTED_CLAIMANT, claimantWithSimilarNino)));

        assertEquals(singletonList(EXPECTED_RESPONSE), sut.getClaimantByNino(VALID_NINO));
    }

    @Test
    public void testGivenValidClaimantIdShouldDeleteTheExpectedData() {
        sut.delete(EXPECTED_CLAIMANT_ID);

        ArgumentCaptor<UUID> captor = ArgumentCaptor.forClass(UUID.class);
        verify(repository, times(1)).deleteById(captor.capture());
        assertThat(captor.getValue(), is(EXPECTED_CLAIMANT_ID));
    }

    @Test
    public void givenValidClaimantRequest_Upate_ShouldSaveTheExpectedDataToRepository() {

        AuthenticationToken authenticationToken = new AuthenticationToken("", "", "",
                singletonList(new SimpleGrantedAuthority(Role.CCA.name())));
        authenticate(authenticationToken);

        sut.update(EXPECTED_CLAIMANT_ID, CLAIMANT_REQUEST);

        ArgumentCaptor<Claimant> captor = ArgumentCaptor.forClass(Claimant.class);
        verify(repository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getClaimantJson(), is(CLAIMANT_REQUEST));
        assertThat(captor.getValue().getNinoSearchHash(), is(ENCODED_NINO));
    }

    private String replaceNino2FirstCharacters() {
        return "TT" + VALID_NINO.substring(2);
    }

    private Claimant generateClaimant(ClaimantRequest claimantRequest) {
        return new Claimant(claimantRequest);
    }

    private static Claimant getExpectedClaimant() {
        final Claimant claimant = new Claimant(CLAIMANT_REQUEST);
        claimant.setClaimantId(EXPECTED_CLAIMANT_ID);
        return claimant;
    }

    private static Claimant getDuplicateClaimant() {
        final Claimant claimant = new Claimant(CLAIMANT_REQUEST);
        claimant.setClaimantId(DUPLICATE_CLAIMANT_ID);
        return claimant;
    }

    private static Claimant getDifferentClaimant() {
        ClaimantRequest anyClaimant = ClaimantRequestCaseBuilder.standard().withNino("ANY_NINO").withDOB(DOB).build();
        final Claimant claimant = new Claimant(anyClaimant);
        claimant.setClaimantId(UUID.randomUUID());
        return claimant;
    }

    private void whenDuplicateClaimExists() {
        List<Claimant> claimantList = Arrays.asList(EXPECTED_CLAIMANT, getDuplicateClaimant(), getDifferentClaimant());
        when(repository.findByNinoSearchHashOrderByCreatedTimestampDesc(ENCODED_NINO)).thenReturn(Optional.of(claimantList));
    }

    private void authenticate(final AuthenticationToken authenticationToken) {
        final SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authenticationToken);
        SecurityContextHolder.setContext(securityContext);
    }
}
