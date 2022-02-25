package uk.gov.dwp.jsa.claimant.service.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.dwp.jsa.adaptors.ValidationServiceAdaptor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ValidationService {

    private ValidationServiceAdaptor validationServiceAdaptor;


    @Autowired
    public ValidationService(final ValidationServiceAdaptor validationServiceAdaptor) {
        this.validationServiceAdaptor = validationServiceAdaptor;
    }

    public boolean invalidateClaimStatus(final List<UUID> claimantId) {
        Optional<Boolean> result = validationServiceAdaptor.invalidateStatus(claimantId);
        return result.isPresent() ? result.get() : false;
    }
}
