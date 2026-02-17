package com.hospital.consultation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception levée quand une consultation n'est pas trouvée.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ConsultationNotFoundException extends RuntimeException {
    
    public ConsultationNotFoundException(String message) {
        super(message);
    }
}
