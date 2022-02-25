package uk.gov.dwp.jsa.claimant.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.vladmihalcea.hibernate.type.util.ObjectMapperSupplier;

public class ClaimantServiceObjectMapperProvider implements ObjectMapperSupplier {
    @Override
    public ObjectMapper get() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        objectMapper.setDateFormat(new StdDateFormat());
        return objectMapper;
    }
}
