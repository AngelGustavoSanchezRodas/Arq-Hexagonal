package com.navaja.navajagtbackend.exceptions;

public class AliasEnUsoException extends RuntimeException {

    public AliasEnUsoException() {
        super("Este alias ya esta ocupado. Por favor elige otro.");
    }
}

