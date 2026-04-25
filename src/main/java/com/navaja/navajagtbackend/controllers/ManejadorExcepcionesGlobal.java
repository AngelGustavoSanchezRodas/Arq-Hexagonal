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
    public ResponseEntity<ErrorPaywallResponse> handleLimiteExcedido(LimiteExcedidoException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorPaywallResponse(
                "QUOTA_EXCEEDED",
                "Has alcanzado el limite de tu plan actual.",
                "Actualiza a Premium para continuar creando."
        ));
    }

    @ExceptionHandler(AliasEnUsoException.class)
    @SuppressWarnings("unused")
    public ResponseEntity<ErrorSimpleResponse> handleAliasEnUso(AliasEnUsoException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorSimpleResponse(
                "ALIAS_IN_USE",
                exception.getMessage() == null ? "Este alias ya esta ocupado. Por favor elige otro." : exception.getMessage()
        ));
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    @SuppressWarnings("unused")
    public ResponseEntity<ErrorSimpleResponse> handleAccesoDenegado(AccesoDenegadoException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorSimpleResponse(
                "PREMIUM_FEATURE",
                "Mejora tu plan para personalizar tus enlaces."
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @SuppressWarnings("unused")
    public ResponseEntity<ErrorSimpleResponse> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorSimpleResponse(
                "BAD_REQUEST",
                exception.getMessage() == null ? "Solicitud invalida" : exception.getMessage()
        ));
    }

    public record ErrorPaywallResponse(String error, String message, String details) {
    }

    public record ErrorSimpleResponse(String error, String message) {
    }
}
