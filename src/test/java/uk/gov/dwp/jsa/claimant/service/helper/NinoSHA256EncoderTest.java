package uk.gov.dwp.jsa.claimant.service.helper;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NinoSHA256EncoderTest {

    private static final String DUMMY_NINO_FIRST_CHARACTER = "GB";

    private static final String ORIGINAL_NINO = "ST1234567889A";

    // This is the hash value for the string GB1234567889A
    private static final String EXPECTED_ENCODED_VALUE =
            "f94ba468d179cda728727c16e1dc08ee66f117f2b3497585ed4ab1afde9d0f5f";

    private NinoSHA256Encoder testSubject;

    @Before
    public void setUp() {
        testSubject = new NinoSHA256Encoder(DUMMY_NINO_FIRST_CHARACTER);
    }

    @Test
    public void givenNino_ShouldEncodeNinoCorrectly() {
        assertEquals(EXPECTED_ENCODED_VALUE, testSubject.encode(ORIGINAL_NINO));
    }

    @Test(expected=NullPointerException.class)
    public void givenNull_ShouldThrowNPE() {
        testSubject.encode(null);
    }

}
