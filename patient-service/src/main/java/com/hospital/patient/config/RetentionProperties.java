package com.hospital.patient.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * T1.17: Retention policy configuration. Data past retention period may be purged.
 */
@Component
@ConfigurationProperties(prefix = "retention")
@Data
public class RetentionProperties {

    /**
     * Default retention period in years for patient data (e.g. 10).
     */
    private int patientYears = 10;
}
