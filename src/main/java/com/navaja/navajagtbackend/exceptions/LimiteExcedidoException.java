package com.navaja.navajagtbackend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class LimiteExcedidoException extends ResponseStatusException {

    public LimiteExcedidoException(String reason) {
        super(HttpStatus.FORBIDDEN, reason);
    }
}

