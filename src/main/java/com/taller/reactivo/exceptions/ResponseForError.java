package com.taller.reactivo.exceptions;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResponseForError {
    public ResponseForError(String mensaje, List<String> detalles) {
        super();
        this.mensaje = mensaje;
        this.detalles = detalles;
    }

    private String mensaje;
    private List<String> detalles;
}
