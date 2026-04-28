package com.navaja.navajagtbackend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class EnlaceNoEncontradoException extends ResponseStatusException {
    public EnlaceNoEncontradoException() {
        super(HttpStatus.NOT_FOUND, "Enlace no encontrado");
    }
}

