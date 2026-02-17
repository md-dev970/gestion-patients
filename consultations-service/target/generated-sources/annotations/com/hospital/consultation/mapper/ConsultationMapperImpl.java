package com.hospital.consultation.mapper;

import com.hospital.consultation.dto.ConsultationCreateRequest;
import com.hospital.consultation.dto.ConsultationDTO;
import com.hospital.consultation.dto.ConsultationUpdateRequest;
import com.hospital.consultation.model.Consultation;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-14T16:44:27+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class ConsultationMapperImpl implements ConsultationMapper {

    @Override
    public ConsultationDTO toDTO(Consultation consultation) {
        if ( consultation == null ) {
            return null;
        }

        ConsultationDTO.ConsultationDTOBuilder consultationDTO = ConsultationDTO.builder();

        consultationDTO.consultationId( consultation.getConsultationId() );
        consultationDTO.patientId( consultation.getPatientId() );
        consultationDTO.userId( consultation.getUserId() );
        consultationDTO.consultationDate( consultation.getConsultationDate() );
        consultationDTO.consultationType( consultation.getConsultationType() );
        consultationDTO.diagnostic( consultation.getDiagnostic() );
        consultationDTO.notes( consultation.getNotes() );
        consultationDTO.motif( consultation.getMotif() );
        consultationDTO.prescriptions( consultation.getPrescriptions() );
        consultationDTO.status( consultation.getStatus() );
        consultationDTO.createdAt( consultation.getCreatedAt() );
        consultationDTO.updatedAt( consultation.getUpdatedAt() );

        return consultationDTO.build();
    }

    @Override
    public List<ConsultationDTO> toDTOList(List<Consultation> consultations) {
        if ( consultations == null ) {
            return null;
        }

        List<ConsultationDTO> list = new ArrayList<ConsultationDTO>( consultations.size() );
        for ( Consultation consultation : consultations ) {
            list.add( toDTO( consultation ) );
        }

        return list;
    }

    @Override
    public Consultation toEntity(ConsultationCreateRequest request) {
        if ( request == null ) {
            return null;
        }

        Consultation.ConsultationBuilder consultation = Consultation.builder();

        consultation.patientId( request.getPatientId() );
        consultation.userId( request.getUserId() );
        consultation.consultationDate( request.getConsultationDate() );
        consultation.consultationType( request.getConsultationType() );
        consultation.notes( request.getNotes() );
        consultation.motif( request.getMotif() );

        return consultation.build();
    }

    @Override
    public void updateEntityFromRequest(ConsultationUpdateRequest request, Consultation consultation) {
        if ( request == null ) {
            return;
        }

        if ( request.getConsultationDate() != null ) {
            consultation.setConsultationDate( request.getConsultationDate() );
        }
        if ( request.getConsultationType() != null ) {
            consultation.setConsultationType( request.getConsultationType() );
        }
        if ( request.getDiagnostic() != null ) {
            consultation.setDiagnostic( request.getDiagnostic() );
        }
        if ( request.getNotes() != null ) {
            consultation.setNotes( request.getNotes() );
        }
        if ( request.getMotif() != null ) {
            consultation.setMotif( request.getMotif() );
        }
        if ( request.getPrescriptions() != null ) {
            consultation.setPrescriptions( request.getPrescriptions() );
        }
        if ( request.getStatus() != null ) {
            consultation.setStatus( request.getStatus() );
        }
    }
}
