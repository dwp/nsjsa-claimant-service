package uk.gov.dwp.jsa.claimant.service.services;

import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class NinoSanitiser {

    private static final String SPACE = "\\s+";
    private static final String EMPTY = "";

    /**
     * This function removes any spaces from the nino and transforms everything to
     * uppercase.
     *
     * @param nino value to be sanitised
     * @return sanitised nino
     */
    public String sanitise(final String nino) {
        String correctedCase = correctCase(nino);
        return removeSpaces(correctedCase);
    }

    private static String correctCase(final String nino) {
        return nino.toUpperCase(Locale.ENGLISH);
    }

    private static String removeSpaces(final String nino) {
        return nino.replaceAll(SPACE, EMPTY);
    }
}
