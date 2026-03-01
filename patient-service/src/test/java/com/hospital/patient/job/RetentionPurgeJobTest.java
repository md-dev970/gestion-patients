package com.hospital.patient.job;

import com.hospital.patient.audit.SecurityAuditSender;
import com.hospital.patient.model.Patient;
import com.hospital.patient.repository.PatientRepository;
import com.hospital.patient.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RetentionPurgeJob Unit Tests")
class RetentionPurgeJobTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientService patientService;

    @Mock
    private SecurityAuditSender securityAuditSender;

    @InjectMocks
    private RetentionPurgeJob retentionPurgeJob;

    private Patient expiredPatient;

    @BeforeEach
    void setUp() {
        expiredPatient = Patient.builder()
                .id(10L)
                .retentionUntil(LocalDate.now().minusDays(1))
                .build();
    }

    @Test
    @DisplayName("purgeExpiredPatients - no expired patients - does not delete or send audit")
    void purgeExpiredPatients_noExpired_doesNothing() {
        when(patientRepository.findByRetentionUntilBefore(LocalDate.now())).thenReturn(List.of());

        retentionPurgeJob.purgeExpiredPatients();

        verify(patientService, never()).deletePatient(anyLong());
        verify(securityAuditSender, never()).sendRetentionPurgeCompleted(anyLong());
    }

    @Test
    @DisplayName("purgeExpiredPatients - expired patients - deletes each and sends audit")
    void purgeExpiredPatients_expiredPatients_deletesAndSendsAudit() {
        when(patientRepository.findByRetentionUntilBefore(LocalDate.now())).thenReturn(List.of(expiredPatient));
        doNothing().when(patientService).deletePatient(10L);

        retentionPurgeJob.purgeExpiredPatients();

        verify(patientService).deletePatient(10L);
        verify(securityAuditSender).sendRetentionPurgeCompleted(1L);
    }
}
