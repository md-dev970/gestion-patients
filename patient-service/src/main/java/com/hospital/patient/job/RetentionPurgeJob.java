package com.hospital.patient.job;

import com.hospital.patient.audit.SecurityAuditSender;
import com.hospital.patient.model.Patient;
import com.hospital.patient.repository.PatientRepository;
import com.hospital.patient.service.PatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * T1.18: Scheduled job that purges patient data past retention period.
 * Cascade delete is triggered via PatientService.deletePatient (medical-record, consultations, appointments).
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "retention.purge.enabled", havingValue = "true", matchIfMissing = false)
public class RetentionPurgeJob {

    private final PatientRepository patientRepository;
    private final PatientService patientService;
    private final SecurityAuditSender securityAuditSender;

    @Scheduled(cron = "${retention.purge.cron:0 0 2 * * ?}")
    public void purgeExpiredPatients() {
        LocalDate today = LocalDate.now();
        List<Patient> expired = patientRepository.findByRetentionUntilBefore(today);
        if (expired.isEmpty()) {
            log.debug("Retention purge: no patients past retention date");
            return;
        }
        log.info("Retention purge: purging {} patient(s) past retention date", expired.size());
        int purged = 0;
        for (Patient p : expired) {
            try {
                patientService.deletePatient(p.getId());
                purged++;
            } catch (Exception e) {
                log.warn("Retention purge: failed to delete patient id {}: {}", p.getId(), e.getMessage());
            }
        }
        securityAuditSender.sendRetentionPurgeCompleted(purged);
        log.info("Retention purge completed: {} patient(s) purged", purged);
    }
}
