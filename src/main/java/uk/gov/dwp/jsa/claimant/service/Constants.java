package uk.gov.dwp.jsa.claimant.service;

import java.nio.charset.Charset;

public final class Constants {

    public static final String DEFAULT_ERROR_CODE = "default-error-code";
    public static final int LAST = 999;
    public static final String NO_SECURE_PROFILE = "nosecure";

    private Constants() {
    }

    public static final String NINO_REPLACEMENT_KEY = "nino.first.character.replacement";

    public static final Charset UTF8 = Charset.forName("UTF-8");

}
