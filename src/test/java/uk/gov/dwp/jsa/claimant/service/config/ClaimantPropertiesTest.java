package uk.gov.dwp.jsa.claimant.service.config;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ClaimantPropertiesTest.TestConfiguration.class})
@ActiveProfiles("test")
public class ClaimantPropertiesTest {

    @Autowired
    private ClaimantProperties config;

    @Test
    public void givensPropertiesThenLockDurationSet() {
        assertThat(config.getLockDurationMinutes(), is(20));
    }

    @EnableConfigurationProperties(ClaimantProperties.class)
    public static class TestConfiguration {
        // nothing
    }
}
