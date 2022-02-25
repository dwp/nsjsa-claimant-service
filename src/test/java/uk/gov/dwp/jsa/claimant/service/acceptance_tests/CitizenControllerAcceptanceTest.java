package uk.gov.dwp.jsa.claimant.service.acceptance_tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.dwp.jsa.adaptors.http.api.ApiError;
import uk.gov.dwp.jsa.adaptors.http.api.ApiResponse;
import uk.gov.dwp.jsa.adaptors.http.api.ApiSuccess;
import uk.gov.dwp.jsa.claimant.service.AppInfo;
import uk.gov.dwp.jsa.claimant.service.Constants;
import uk.gov.dwp.jsa.claimant.service.config.WithVersionUriComponentsBuilder;
import uk.gov.dwp.jsa.claimant.service.controllers.CitizenController;
import uk.gov.dwp.jsa.claimant.service.exceptions.ClaimantByIdNotFoundException;
import uk.gov.dwp.jsa.claimant.service.models.db.Claimant;
import uk.gov.dwp.jsa.claimant.service.models.db.CurrentStatus;
import uk.gov.dwp.jsa.claimant.service.models.http.ClaimantRequest;
import uk.gov.dwp.jsa.claimant.service.models.http.ClaimantResponse;
import uk.gov.dwp.jsa.claimant.service.models.http.CurrentStatusDto;
import uk.gov.dwp.jsa.claimant.service.services.ClaimantService;
import uk.gov.dwp.jsa.claimant.service.services.CurrentStatusService;
import uk.gov.dwp.jsa.claimant.service.services.NinoSanitiser;
import uk.gov.dwp.jsa.claimant.service.utils.ClaimantRequestCaseBuilder;
import uk.gov.dwp.jsa.claimant.service.utils.CurrentStatusRequestCaseBuilder;
import uk.gov.dwp.jsa.security.WithMockUser;
import uk.gov.dwp.jsa.security.roles.Role;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.dwp.jsa.adaptors.dto.claim.status.BookingStatusType.NEW_CLAIM;

@RunWith(SpringRunner.class)
@WebMvcTest(CitizenController.class)
public class CitizenControllerAcceptanceTest {

    private static final UUID VALID_CLAIMANT_ID = UUID.fromString("59cb3e5b-9deb-4e36-b3bb-8828d18f1e7f");
    private static final String INVALID_CLAIMANT_ID = "FAKE_ID";

    private static final String VALID_NINO_ID = "12345678_NINO";
    private static final String INVALID_NINO_ID = "FAKE_NINO";

    private static final String CLAIMANT_BASE_PATH = "/nsjsa/citizen";
    private static final URI CLAIMANT_BASE_URL = URI.create(CLAIMANT_BASE_PATH);
    private static final URI VALID_CLAIMANT_URL = URI.create(CLAIMANT_BASE_PATH + "/" + VALID_CLAIMANT_ID);
    private static final URI INVALID_CLAIMANT_URL = URI.create(CLAIMANT_BASE_PATH + "/" + INVALID_CLAIMANT_ID);
    private static final URI NON_EXISTING_CLAIMANT_URL = URI.create(CLAIMANT_BASE_PATH + "/" + UUID.randomUUID());
    private static final URI UPDATE_STATUS_URL = URI.create(CLAIMANT_BASE_PATH + "/" + VALID_CLAIMANT_ID + "/status");
    private static final URI FIND_BY_NINO_URL = URI.create(CLAIMANT_BASE_PATH + "/nino");

    private static final ClaimantRequest CLAIMANT_REQUEST = ClaimantRequestCaseBuilder.standard().build();

    private static final Claimant CLAIMANT = getClaimant();


    private static final ClaimantResponse CLAIMANT_RESPONSE = new ClaimantResponse(CLAIMANT);
    private static final List<ClaimantResponse> CLAIMANT_RESPONSE_LIST = Collections.singletonList(CLAIMANT_RESPONSE);

    private static final ApiSuccess<ClaimantResponse> CLAIMANT_API_SUCCESS_BY_ID =
            new ApiSuccess<>(VALID_CLAIMANT_URL, CLAIMANT_RESPONSE);

    private static final ApiResponse CLAIMANT_API_RESPONSE_BY_ID =
            new ApiResponse<>(Collections.singletonList(CLAIMANT_API_SUCCESS_BY_ID));

    private static final ApiSuccess<ClaimantResponse> CLAIMANT_LIST_API_SUCCESS_BY_ID =
            new ApiSuccess<>(VALID_CLAIMANT_URL, CLAIMANT_RESPONSE);

    private static final ApiResponse CLAIMANT_LIST_API_RESPONSE_BY_ID =
            new ApiResponse<>(Collections.singletonList(CLAIMANT_LIST_API_SUCCESS_BY_ID));

    private static final ApiResponse<UUID> EXPECTED_FAILURE_DELETE_API_RESPONSE = new ApiResponse<>(new ApiError(
            Constants.DEFAULT_ERROR_CODE,
            HttpStatus.NOT_FOUND.getReasonPhrase()
    ));

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private NinoSanitiser ninoSanitiser;

    @MockBean
    private ClaimantService service;

    @MockBean
    private CurrentStatusService currentStatusService;

    @MockBean(name = "appInfo")
    private AppInfo appInfo;

    @MockBean
    private WithVersionUriComponentsBuilder uriBuilder;

    @Autowired
    private MockMvc mockMvc;

    @Before
    public void setUp() {
        when(appInfo.getVersion()).thenReturn(StringUtils.EMPTY);
        when(uriBuilder.cloneBuilder()).thenReturn(new WithVersionUriComponentsBuilder(appInfo));
        when(service.save(any())).thenReturn(VALID_CLAIMANT_ID);
        when(service.getClaimantById(VALID_CLAIMANT_ID)).thenReturn(CLAIMANT_RESPONSE);
        when(service.getClaimantByNino(VALID_NINO_ID)).thenReturn(CLAIMANT_RESPONSE_LIST);
        when(ninoSanitiser.sanitise(VALID_NINO_ID)).thenReturn(VALID_NINO_ID);
        when(ninoSanitiser.sanitise(INVALID_NINO_ID)).thenReturn(INVALID_NINO_ID);
    }

    @WithMockUser
    @Test
    public void GivenValidAndPopulatedClaimantRequest_ShouldCreateClaimantRecordAndReturnExpectedURL() throws Exception {
        mockMvc.perform(post("/nsjsa/citizen")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .content(toJson(CLAIMANT_REQUEST)))
                .andExpect(content().string(containsString(VALID_CLAIMANT_URL.toString())))
                .andExpect(status().isCreated());
    }

    @WithMockUser(role = Role.CCA)
    @Test
    public void GivenNoClaimantRequest_ShouldNotCreateClaimantRecordAndReturnExpectedStatus() throws Exception {
        mockMvc.perform(post(CLAIMANT_BASE_URL)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser(role = Role.CCA)
    @Test
    public void GivenInvalidClaimantRequestJson_Save_ShouldReturnBadRequest() throws Exception {
        when(service.save(any())).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        mockMvc.perform(post(CLAIMANT_BASE_URL)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser(role = Role.CCA)
    @Test
    public void GivenValidClaimantId_ShouldReturnClaimantRecord() throws Exception {
        mockMvc.perform(get(VALID_CLAIMANT_URL)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(toJson(CLAIMANT_API_RESPONSE_BY_ID)))
                .andExpect(status().isOk());
    }


    @WithMockUser(role = Role.CCA)
    @Test
    public void GivenValidClaimantIdWithCurrentStatus_ShouldReturnClaimantRecordWithCurrentStatus() throws Exception {
        mockMvc.perform(get(VALID_CLAIMANT_URL)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(toJson(CLAIMANT_API_RESPONSE_BY_ID)))
                .andExpect(status().isOk());
    }

    @WithMockUser(role = Role.CCA)
    @Test
    public void GivenInvalidClaimantId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get(INVALID_CLAIMANT_URL)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser(role = Role.CCA)
    @Test
    public void GivenValidNino_ShouldReturnClaimantRecordList() throws Exception {
        CLAIMANT_RESPONSE.setClaimantId(VALID_CLAIMANT_ID);
        mockMvc.perform(get(FIND_BY_NINO_URL)
                .with(csrf())
                .content(" { \"nino\" : \"" + VALID_NINO_ID + "\" }")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(toJson(CLAIMANT_LIST_API_RESPONSE_BY_ID)))
                .andExpect(status().isOk());
    }

    @WithMockUser(role = Role.CCA)
    @Test
    public void GivenInvalidNino_ShouldReturnOK_WithEmptyList() throws Exception {
        mockMvc.perform(get(FIND_BY_NINO_URL)
                .with(csrf())
                .content(" { \"nino\" : \"" + INVALID_NINO_ID + "\" }")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @WithMockUser(role = Role.CCA)
    @Test
    public void testGivenValidClaimantIdShouldDeleteAndReturnExpectedURL() throws Exception {
        mockMvc.perform(delete(VALID_CLAIMANT_URL)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString(VALID_CLAIMANT_URL.toString())))
                .andExpect(status().isOk());
    }

    @WithMockUser(role = Role.WC)
    @Test
    public void testGivenValidClaimantIdShouldReturnNotFoundAndReturnExpectedURL() throws Exception {
        mockMvc.perform(delete(NON_EXISTING_CLAIMANT_URL)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(toJson(EXPECTED_FAILURE_DELETE_API_RESPONSE)))
                .andExpect(status().isNotFound());
    }

    @WithMockUser(role = Role.CCA)
    @Test
    public void testGivenValidCurrentStatusShouldReturnSuccessResponseWithEntityPath() throws Exception {
        mockMvc.perform(put(UPDATE_STATUS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .content(toJson(CurrentStatusRequestCaseBuilder.standardBookingStatus())))
                .andExpect(content().string(containsString(VALID_CLAIMANT_ID.toString())))
                .andExpect(status().isOk());

    }


    @WithMockUser(role = Role.CCA)
    @Test
    public void testGivenMissingPushStatusShouldReturnBadRequest() throws Exception {
        final CurrentStatusDto currentStatusDto = CurrentStatusRequestCaseBuilder.standardBookingStatus();
        currentStatusDto.setPushStatus(null);
        mockMvc.perform(put(UPDATE_STATUS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .content(toJson(currentStatusDto)))
                .andExpect(status().isBadRequest());
    }


    @WithMockUser(role = Role.CCA)
    @Test
    public void testGivenNoCurrentStatusShouldReturnBadRequest() throws Exception {
        mockMvc.perform(put(UPDATE_STATUS_URL)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser(role = Role.CCA)
    @Test
    public void testGivenInvalidCurrentStatusShouldReturnBadRequest() throws Exception {
        mockMvc.perform(put(UPDATE_STATUS_URL)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(CurrentStatusRequestCaseBuilder.invalid())))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser(role = Role.CCA)
    @Test
    public void testGivenValidCurrentStatusForNonExistentClaimantShouldReturnBadRequest() throws Exception {
        when(currentStatusService.save(any(), any(CurrentStatusDto.class))).thenThrow(new ClaimantByIdNotFoundException());

        mockMvc.perform(put(UPDATE_STATUS_URL)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(CurrentStatusRequestCaseBuilder.standardBookingStatus())))
                .andExpect(status().isNotFound());

    }

    @WithMockUser(role = Role.WC)
    @Test
    public void GivenValidAndPopulatedClaimantRequest_ShouldUpdateClaimantRecordAndReturnExpectedURL() throws Exception {
        final ClaimantRequest claimantRequest = ClaimantRequestCaseBuilder
                .standard()
                .build();
        mockMvc.perform(patch(VALID_CLAIMANT_URL)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(claimantRequest)))
                .andExpect(content().string(containsString(VALID_CLAIMANT_URL.toString())))
                .andExpect(status().isOk());
    }

    @WithMockUser(role = Role.WC)
    @Test
    public void GivenRequestMissingDateOfClaim_ShouldReturnBadRequest() throws Exception {
        final ClaimantRequest claimantRequestBuilder = ClaimantRequestCaseBuilder
                .standard()
                .withDateOfClaim(null)
                .build();

        mockMvc.perform(patch(VALID_CLAIMANT_URL)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(claimantRequestBuilder)))
                .andExpect(status().isBadRequest());
    }


    private <T> String toJson(T objectToConverted) throws JsonProcessingException {
        return mapper.writeValueAsString(objectToConverted);
    }

    private static Claimant getClaimant() {
        final Claimant claimant = new Claimant(CLAIMANT_REQUEST);
        final CurrentStatus currentStatus = new CurrentStatus();
        currentStatus.setStatus(NEW_CLAIM);
        currentStatus.setSubstatus("subStatus");
        claimant.setCurrentStatus(currentStatus);
        return claimant;
    }
}
