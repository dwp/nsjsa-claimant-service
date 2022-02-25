package uk.gov.dwp.jsa.claimant.service.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uk.gov.dwp.jsa.adaptors.http.api.SubmittedClaimsTally;

@Component
public class ReportClaimantRepository {

    public static final String SQL = "select * from %s.get_submitted_counts()";
    @Value("${claimant.db.schema:claimant_schema}")
    private String dbSchema;


    private JdbcTemplate jdbcTemplate;

    @Autowired
    public ReportClaimantRepository(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public SubmittedClaimsTally getSubmittedClaimsCount() {
        return (SubmittedClaimsTally) jdbcTemplate.queryForObject(
                String.format(SQL, dbSchema),
                new BeanPropertyRowMapper(SubmittedClaimsTally.class));
    }

}
