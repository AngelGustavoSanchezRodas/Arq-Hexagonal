package com.navaja.navajagtbackend.controllers;

import com.navaja.navajagtbackend.exceptions.AccesoDenegadoException;
import com.navaja.navajagtbackend.exceptions.AliasEnUsoException;
import com.navaja.navajagtbackend.exceptions.LimiteExcedidoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ManejadorExcepcionesGlobal {

    @ExceptionHandler(LimiteExcedidoException.class)
    @SuppressWarnings("unused")
    public ResponseEntity<ErrorResponse> handleLimiteExcedido(LimiteExcedidoException exception) {
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(new ErrorResponse(
                "LIMITE_EXCEDIDO",
                exception.getMessage()
        ));
    }

    @ExceptionHandler(AliasEnUsoException.class)
    @SuppressWarnings("unused")
    public ResponseEntity<ErrorResponse> handleAliasEnUso(AliasEnUsoException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(
                "ALIAS_EN_USO",
                exception.getMessage()
        ));
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    @SuppressWarnings("unused")
    public ResponseEntity<ErrorResponse> handleAccesoDenegado(AccesoDenegadoException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(
                "REQUIERE_PREMIUM",
                exception.getMessage()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @SuppressWarnings("unused")
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(
                "BAD_REQUEST",
                exception.getMessage() == null ? "Solicitud invalida" : exception.getMessage()
        ));
    }

    public record ErrorResponse(String error, String message) {
    }
}
