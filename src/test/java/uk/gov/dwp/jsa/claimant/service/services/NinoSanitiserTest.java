package uk.gov.dwp.jsa.claimant.service.services;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(JUnitParamsRunner.class)
public class NinoSanitiserTest {

    final NinoSanitiser ninoSanitiser = new NinoSanitiser();

    @Test
    @Parameters({
            "AB123456A", // correctly formatted Nino
            "AB 12 34 56 A", // nicely formatted Nino
            "AB    12 34  56    A", // nino with random spaces
            "ab 12 34 56 a", // nino with lower case characters
            "Ab 12 34 56 a" // nino with mixed cases
    })
    public void testGivenAnyNinoReturnSanitisedNino(String nino) {
        String actual = ninoSanitiser.sanitise(nino);
        assertThat("Should match", actual, is("AB123456A"));
    }

    @Test
    public void testGivenNinoWhichIsEmptyReturnEmptyNino() {
        String expected = "";
        String actual = ninoSanitiser.sanitise(expected);
        assertThat("Should match", actual, is(expected));
    }
}
