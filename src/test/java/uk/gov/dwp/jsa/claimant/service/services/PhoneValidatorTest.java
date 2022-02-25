package uk.gov.dwp.jsa.claimant.service.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.dwp.jsa.adaptors.dto.claim.ContactDetails;
import uk.gov.dwp.jsa.claimant.service.models.http.ClaimantRequest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class PhoneValidatorTest {

    private PhoneValidator sut = new PhoneValidator();

    @Test
    public void givenNullClaimantRequest_IsLandLine_ReturnsFalse() {
        // given
        final ClaimantRequest claimantRequest = null;

        // when
        final boolean landline = sut.wasLandlineProvided(claimantRequest);

        // then
        assertFalse(landline);
    }

    @Test
    public void givenNullContactDetails_IsLandLine_ReturnsFalse() {
        // given
        final ClaimantRequest claimantRequest = new ClaimantRequest();

        // when
        final boolean landline = sut.wasLandlineProvided(claimantRequest);

        // then
        assertFalse(landline);
    }

    @Test
    public void givenNullNumber_IsLandLine_ReturnsFalse() {
        // given
        final ClaimantRequest claimantRequest = new ClaimantRequest();
        claimantRequest.setContactDetails(new ContactDetails());

        // when
        final boolean landline = sut.wasLandlineProvided(claimantRequest);

        // then
        assertFalse(landline);
    }

    @Test
    public void givenEmptyNumber_IsLandLine_ReturnsFalse() {
        // given
        final ClaimantRequest claimantRequest = new ClaimantRequest();
        claimantRequest.setContactDetails(new ContactDetails("", "", false, false));

        // when
        final boolean landline = sut.wasLandlineProvided(claimantRequest);

        // then
        assertFalse(landline);
    }

    @Test
    public void givenLandlineNumber_IsLandLine_ReturnsTrue() {
        // given
        final ClaimantRequest claimantRequest = new ClaimantRequest();
        claimantRequest.setContactDetails(new ContactDetails("09", "", false, false));

        // when
        final boolean landline = sut.wasLandlineProvided(claimantRequest);

        // then
        assertTrue(landline);
    }

    @Test
    public void givenLandlineNumberWithSpaces_IsLandLine_ReturnsTrue() {
        // given
        final ClaimantRequest claimantRequest = new ClaimantRequest();
        claimantRequest.setContactDetails(new ContactDetails(" 0 9 ", "", false, false));

        // when
        final boolean landline = sut.wasLandlineProvided(claimantRequest);

        // then
        assertTrue(landline);
    }

    @Test
    public void givenUnSanitisedLandlineNumber_IsLandLine_ReturnsTrue() {
        // given
        final ClaimantRequest claimantRequest = new ClaimantRequest();
        claimantRequest.setContactDetails(new ContactDetails("( 0 9 ) - _", "", false, false));

        // when
        final boolean landline = sut.wasLandlineProvided(claimantRequest);

        // then
        assertTrue(landline);
    }

    @Test
    public void givenMobileNumber_IsLandLine_ReturnsFalse() {
        // given
        final ClaimantRequest claimantRequest = new ClaimantRequest();
        claimantRequest.setContactDetails(new ContactDetails(PhoneValidator.MOBILE_PREFIX, "", false , false));

        // when
        final boolean landline = sut.wasLandlineProvided(claimantRequest);

        // then
        assertFalse(landline);
    }


}
