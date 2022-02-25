package uk.gov.dwp.jsa.claimant.service.models.http;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import uk.gov.dwp.jsa.adaptors.dto.claim.Claimant;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

public class ClaimantRequest extends Claimant {


    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @Override
    public LocalDate getDateOfBirth() {
        return super.getDateOfBirth();
    }

    @NotNull
    @Override
    public LocalDate getDateOfClaim() {
        return super.getDateOfClaim();
    }

    @NotBlank
    @Override
    public String getServiceVersion() {
        return super.getServiceVersion();
    }

}
