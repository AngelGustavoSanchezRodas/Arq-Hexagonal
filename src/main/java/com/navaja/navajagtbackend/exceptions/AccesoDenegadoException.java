package com.navaja.navajagtbackend.exceptions;

public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException() {
        super("Solo usuarios Premium pueden usar alias personalizados");
    }
}

