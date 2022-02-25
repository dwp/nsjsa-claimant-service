package uk.gov.dwp.jsa.claimant.service.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.dwp.jsa.adaptors.http.api.ApiResponse;
import uk.gov.dwp.jsa.adaptors.http.api.ApiSuccess;
import uk.gov.dwp.jsa.claimant.service.services.AppointmentService;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class ClaimControllerTest {


    private static final String NSJSA_CITIZEN_BASE_URL = "/nsjsa/claim/";
    private static final UUID VALID_CLAIMANT_ID = UUID.fromString("0e1060f4-1153-42c7-b4be-47c5cb403879");
    private static final URI VALID_CLAIMANT_URL = URI.create(NSJSA_CITIZEN_BASE_URL + VALID_CLAIMANT_ID);

    private ClaimController sut;

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Before
    public void setUp() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));

        sut = new ClaimController(appointmentService);

        given(httpServletRequest.getRequestURI()).willReturn(VALID_CLAIMANT_URL.toString());
    }

    @Test
    public void getNextClaim_NextClaimantAvailable_UuidOfNextClaimant() {
        given(appointmentService.getNextClaimantId()).willReturn(Optional.of(VALID_CLAIMANT_ID));
        ResponseEntity<ApiResponse<String>> uriResponseEntity = sut.getNextClaim(httpServletRequest);
        ApiSuccess<String> apiSuccess = uriResponseEntity.getBody().getSuccess().get(0);
        assertEquals(HttpStatus.OK, uriResponseEntity.getStatusCode());
        assertThat(apiSuccess.getPath(), is(equalTo(VALID_CLAIMANT_URL)));
        assertThat(apiSuccess.getData(), is(equalTo(VALID_CLAIMANT_ID.toString())));
    }

    @Test
    public void getNextClaim_NoClaimantAvailable_UuidOfNextClaimant() {
        given(appointmentService.getNextClaimantId()).willReturn(Optional.ofNullable(null));
        ResponseEntity<ApiResponse<String>> uriResponseEntity = sut.getNextClaim(httpServletRequest);
        ApiSuccess<String> apiSuccess = uriResponseEntity.getBody().getSuccess().get(0);
        assertEquals(HttpStatus.OK, uriResponseEntity.getStatusCode());
        assertThat(apiSuccess.getPath(), is(equalTo(VALID_CLAIMANT_URL)));
        assertThat(apiSuccess.getData(), is(nullValue()));
    }
}
