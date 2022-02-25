package uk.gov.dwp.jsa.claimant.service.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uk.gov.dwp.jsa.adaptors.NotificationServiceAdaptor;
import uk.gov.dwp.jsa.adaptors.http.api.SubmittedClaimsTally;
import uk.gov.dwp.jsa.claimant.service.repositories.ReportClaimantRepository;
import uk.gov.dwp.jsa.claimant.service.security.PrivateKeyProvider;
import uk.gov.dwp.jsa.security.JWTAuthenticationFactory;
import uk.gov.dwp.jsa.security.roles.Role;

@Service
public class ReportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportService.class);
    private static final String SERVICE_NAME = "claimant";

    private final ReportClaimantRepository reportClaimantRepository;
    private final NotificationServiceAdaptor notificationServiceAdaptor;
    private final JWTAuthenticationFactory jwtAuthenticationFactory;
    private final PrivateKeyProvider privateKeyProvider;

    @Autowired
    public ReportService(final ReportClaimantRepository reportClaimantRepository,
                         final NotificationServiceAdaptor notificationServiceAdaptor,
                         final JWTAuthenticationFactory jwtAuthenticationFactory,
                         final PrivateKeyProvider privateKeyProvider) {
        this.reportClaimantRepository = reportClaimantRepository;
        this.notificationServiceAdaptor = notificationServiceAdaptor;
        this.jwtAuthenticationFactory = jwtAuthenticationFactory;
        this.privateKeyProvider = privateKeyProvider;
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings("REC_CATCH_EXCEPTION")
    @Scheduled(cron = "${claimant.schedule.submit-claims-count}")
    public void submitClaimsCount() {

        setJWTTokenForSystemRole();

        try {
            SubmittedClaimsTally submittedClaimsTally = reportClaimantRepository.getSubmittedClaimsCount();
            notificationServiceAdaptor.sendSubmittedClaimsCountEmail(submittedClaimsTally).get();
            LOGGER.debug("Running the schedule to get the previous days submitted claims");
        } catch (final Exception x) {
            // we can allow normal operation to continue in this scenario.
            LOGGER.error("Problem communicating with Notification Service.", x);
        }
    }

    private void setJWTTokenForSystemRole() {
        Authentication authentication =
                jwtAuthenticationFactory.create(
                        privateKeyProvider.getPrivateKey(),
                        SERVICE_NAME,
                        Role.SYSTEM.getGroup());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
