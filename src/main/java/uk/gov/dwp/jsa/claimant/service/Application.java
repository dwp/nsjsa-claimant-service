package uk.gov.dwp.jsa.claimant.service;

import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import uk.gov.dwp.jsa.claimant.service.config.ClaimantServiceObjectMapperProvider;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class,
        scanBasePackages = "uk.gov.dwp.jsa")
public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ObjectMapper objectMapper() {
        final ClaimantServiceObjectMapperProvider objectMapperProvider = new ClaimantServiceObjectMapperProvider();
        return objectMapperProvider.get();
    }

}
