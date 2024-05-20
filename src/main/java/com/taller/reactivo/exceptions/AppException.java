package com.taller.reactivo.exceptions;

import java.util.ArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;

public class AppException extends ResponseEntityExceptionHandler{

    @ExceptionHandler(RegisterNotFoundException.class)
    public final ResponseEntity<ResponseForError> gestorSinDatosException(RegisterNotFoundException ex, WebRequest request) {
        ArrayList<String> details = new ArrayList<>();
        details.add(ex.getLocalizedMessage());
        ResponseForError error = new ResponseForError("No se encontraron datos", details);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
}
