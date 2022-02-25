package uk.gov.dwp.jsa.claimant.service.controllers;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.dwp.jsa.adaptors.http.api.ApiResponse;
import uk.gov.dwp.jsa.adaptors.http.api.ApiSuccess;
import uk.gov.dwp.jsa.claimant.service.AppInfo;
import uk.gov.dwp.jsa.claimant.service.Constants;
import uk.gov.dwp.jsa.claimant.service.config.WithVersionUriComponentsBuilder;
import uk.gov.dwp.jsa.claimant.service.exceptions.ClaimantByIdNotFoundException;
import uk.gov.dwp.jsa.claimant.service.models.http.ClaimantRequest;
import uk.gov.dwp.jsa.claimant.service.models.http.ClaimantResponse;
import uk.gov.dwp.jsa.claimant.service.models.http.CurrentStatusDto;
import uk.gov.dwp.jsa.claimant.service.services.ClaimantService;
import uk.gov.dwp.jsa.claimant.service.services.CurrentStatusService;
import uk.gov.dwp.jsa.claimant.service.services.NinoSanitiser;
import uk.gov.dwp.jsa.claimant.service.services.ResponseBuilder;
import uk.gov.dwp.jsa.claimant.service.utils.ClaimantResponseBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CitizenControllerTest {


    private static final String NSJSA_CITIZEN_BASE_URL = "/nsjsa/citizen/";

    private static final UUID VALID_CLAIMANT_ID = UUID.fromString("0e1060f4-1153-42c7-b4be-47c5cb403879");
    private static final UUID INVALID_CLAIMANT_ID = UUID.randomUUID();

    private static final URI VALID_CLAIMANT_URL = URI.create(NSJSA_CITIZEN_BASE_URL + VALID_CLAIMANT_ID);

    private static final URI EXPECTED_URL = URI.create(NSJSA_CITIZEN_BASE_URL + VALID_CLAIMANT_ID);

    private static final ResponseEntity<ApiResponse<UUID>> EXPECTED_DELETE_RESPONSE =
            new ResponseBuilder<UUID>().withStatus(HttpStatus.OK).withSuccessData(
                    EXPECTED_URL,
                    VALID_CLAIMANT_ID
            ).build();

    private static final ResponseEntity<ApiResponse<UUID>> EXPECTED_UNSUCCESSFUL_DELETE_RESPONSE =
            new ResponseBuilder<UUID>().withStatus(HttpStatus.NOT_FOUND).withApiError(
                    Constants.DEFAULT_ERROR_CODE,
                    HttpStatus.NOT_FOUND.getReasonPhrase()
            ).build();
    public static final String FAKE_VALID_NINO = "fake-valid-nino";
    private static final LocalDate NEWER_DATE_OF_CLAIM = LocalDate.now();
    private static final LocalDate OLDER_DATE_OF_CLAIM = NEWER_DATE_OF_CLAIM.minusDays(1);
    private static final ClaimantResponse OLDER_CLAIM = new ClaimantResponseBuilder()
            .withDateOfClaim(OLDER_DATE_OF_CLAIM)
            .build();
    private static final ClaimantResponse NEWER_CLAIM = new ClaimantResponseBuilder()
            .withDateOfClaim(NEWER_DATE_OF_CLAIM)
            .build();
    private static final String NINO = "NINO";


    @Mock
    private ClaimantService claimantService;
    @Mock
    private CurrentStatusService currentStatusService;
    @Mock
    private ClaimantRequest claimantRequest;
    @Mock
    private CurrentStatusDto currentStatusDto;
    @Mock
    private ClaimantResponse expectedResponse;
    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private AppInfo appInfo;

    private ResponseEntity<ApiResponse<ClaimantResponse>> findByNinoResponse;
    private CitizenController sut;

    @Before
    public void setUp() {
        when(appInfo.getVersion()).thenReturn(StringUtils.EMPTY);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));


        sut = new CitizenController(claimantService, new WithVersionUriComponentsBuilder(appInfo), new NinoSanitiser(),
                currentStatusService);

        when(claimantService.save(any())).thenReturn(VALID_CLAIMANT_ID);
        when(claimantService.getClaimantById(VALID_CLAIMANT_ID)).thenReturn(expectedResponse);
        when(httpServletRequest.getRequestURI()).thenReturn(VALID_CLAIMANT_URL.toString());
        when(claimantRequest.getNino()).thenReturn(FAKE_VALID_NINO);

    }

    @Test
    public void bringsBackLatestClaimFirstIfInOrderOfNewestFirst() {
        givenAClaimantServiceThatReturnsClaimantsInOrderOfNewestFirst();
        whenIGetClaimantByNino();
        thenTheLatestClaimComesBack();
    }

    @Test
    public void givenValidClaimantRequest_CreateClaimant_ShouldReturnExpectedUrl() {
        ResponseEntity<ApiResponse<UUID>> uriResponseEntity = sut.createClaimant(claimantRequest);
        ApiSuccess<UUID> apiSuccess = uriResponseEntity.getBody().getSuccess().get(0);
        assertEquals(HttpStatus.CREATED, uriResponseEntity.getStatusCode());
        assertEquals(VALID_CLAIMANT_URL, apiSuccess.getPath());
        assertEquals(VALID_CLAIMANT_ID, apiSuccess.getData());
    }

    @Test
    public void givenValidClaimantRequest_ClaimantServiceSave_ShouldBeCalledOnce() {
        sut.createClaimant(claimantRequest);

        ArgumentCaptor<ClaimantRequest> captor = ArgumentCaptor.forClass(ClaimantRequest.class);

        verify(claimantService, times(1)).save(captor.capture());

        assertThat(captor.getValue(), is(claimantRequest));
    }

    @Test
    public void givenValidClaimantId_getClaimantById_ShouldReturnTheClaimantInformation() {
        ResponseEntity<ApiResponse<ClaimantResponse>> claimantResponse = sut.getClaimantById(VALID_CLAIMANT_ID, httpServletRequest);
        assertEquals(expectedResponse, claimantResponse.getBody().getSuccess().get(0).getData());
        assertEquals(HttpStatus.OK, claimantResponse.getStatusCode());
    }

    @Test(expected = ClaimantByIdNotFoundException.class)
    public void givenUnvalidClaimantId_getClaimantById_ShouldReturn404() {
        when(claimantService.getClaimantById(any())).thenThrow(ClaimantByIdNotFoundException.class);
        sut.getClaimantById(VALID_CLAIMANT_ID, httpServletRequest);
    }

    @Test
    public void testGivenValidClaimantIdDeleteClaimantShouldReturnExpectedUrl() {
        ResponseEntity<ApiResponse<UUID>> uriResponseEntity = sut.deleteClaimantById(VALID_CLAIMANT_ID);
        assertEquals(EXPECTED_DELETE_RESPONSE.getStatusCode(), uriResponseEntity.getStatusCode());
        assertEquals(EXPECTED_DELETE_RESPONSE.getBody().getSuccess().get(0).getPath(), uriResponseEntity.getBody().getSuccess().get(0).getPath());
        assertEquals(EXPECTED_DELETE_RESPONSE.getBody().getSuccess().get(0).getData(), uriResponseEntity.getBody().getSuccess().get(0).getData());
    }

    @Test
    public void testGivenInValidClaimantIdDeleteClaimantShouldReturnExpectedUrl() {
        ResponseEntity<ApiResponse<UUID>> uriResponseEntity = sut.deleteClaimantById(INVALID_CLAIMANT_ID);
        assertEquals(EXPECTED_UNSUCCESSFUL_DELETE_RESPONSE.getStatusCode(), uriResponseEntity.getStatusCode());
        assertEquals(EXPECTED_UNSUCCESSFUL_DELETE_RESPONSE.getBody().getError().getMessage(), uriResponseEntity.getBody().getError().getMessage());
        assertEquals(EXPECTED_UNSUCCESSFUL_DELETE_RESPONSE.getBody().getError().getCode(), uriResponseEntity.getBody().getError().getCode());
    }

    @Test
    public void givenValidUpdateStatusRequest_CurrentStatusServiceSave_ShouldBeCalledOnce() {
        sut.updateClaimantStatus(UUID.randomUUID(), currentStatusDto);
        ArgumentCaptor<CurrentStatusDto> captor = ArgumentCaptor.forClass(CurrentStatusDto.class);
        verify(currentStatusService, times(1)).save(any(), captor.capture());
        assertThat(captor.getValue(), is(currentStatusDto));
    }

    @Test
    public void givenValidUpdateStatusRequest_UpdateStatus_ShouldReturnExpectedUrl() {
        ResponseEntity<ApiResponse<UUID>> uriResponseEntity = sut.updateClaimantStatus(VALID_CLAIMANT_ID, currentStatusDto);
        ApiSuccess<UUID> apiSuccess = uriResponseEntity.getBody().getSuccess().get(0);
        assertEquals(HttpStatus.OK, uriResponseEntity.getStatusCode());
        assertEquals(VALID_CLAIMANT_URL, apiSuccess.getPath());
        assertEquals(VALID_CLAIMANT_ID, apiSuccess.getData());
    }

    @Test(expected = ClaimantByIdNotFoundException.class)
    public void givenInvalidClaimantId_updateClaimantStatus_ShouldReturn404() {
        when(currentStatusService.save(any(), any())).thenThrow(ClaimantByIdNotFoundException.class);
        sut.updateClaimantStatus(INVALID_CLAIMANT_ID, currentStatusDto);
    }

    @Test
    public void givenValidClaimantRequest_ClaimantServiceUpdate_ShouldBeCalledOnce() {
        sut.updateClaimant(VALID_CLAIMANT_ID, claimantRequest);

        ArgumentCaptor<ClaimantRequest> captor = ArgumentCaptor.forClass(ClaimantRequest.class);

        verify(claimantService, times(1)).update(eq(VALID_CLAIMANT_ID), captor.capture());

        assertThat(captor.getValue(), is(claimantRequest));
    }

    @Test(expected = ClaimantByIdNotFoundException.class)
    public void givenInvalidClaimantId_updateClaimant_ShouldReturn404() {
        when(claimantService.update(any(), any())).thenThrow(ClaimantByIdNotFoundException.class);
        sut.updateClaimant(INVALID_CLAIMANT_ID, claimantRequest);
    }

    private void givenAClaimantServiceThatReturnsClaimantsInOrderOfOldestFirst() {
        List<ClaimantResponse> claimants = Arrays.asList(OLDER_CLAIM, NEWER_CLAIM);
        when(claimantService.getClaimantByNino(NINO)).thenReturn(claimants);
    }

    private void givenAClaimantServiceThatReturnsClaimantsInOrderOfNewestFirst() {
        List<ClaimantResponse> claimants = Arrays.asList(NEWER_CLAIM, OLDER_CLAIM);
        when(claimantService.getClaimantByNino(NINO)).thenReturn(claimants);
    }

    private void whenIGetClaimantByNino() {
        final Map<String, Object> payload = new HashMap<>();
        payload.put("nino", NINO);
        findByNinoResponse = sut.getClaimantByNino(payload);
    }

    private void thenTheLatestClaimComesBack() {
        assertThat(findByNinoResponse.getBody().getSuccess().get(0).getData().getDateOfClaim(), is(NEWER_DATE_OF_CLAIM));
    }
}
