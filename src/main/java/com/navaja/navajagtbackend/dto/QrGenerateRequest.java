package com.navaja.navajagtbackend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.Map;

public record QrGenerateRequest(
        @NotNull TipoQr tipo,
        @NotEmpty Map<String, String> payload,
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String colorFondo,
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String colorFrente
) {

    public enum TipoQr {
        URL,
        PHONE,
        WHATSAPP,
        EMAIL
    }

    public QrGenerateRequest {
        if (colorFondo != null && !isHexColor(colorFondo)) {
            throw new IllegalArgumentException("El color de fondo debe tener formato HEX #RRGGBB");
        }
        if (colorFrente != null && !isHexColor(colorFrente)) {
            throw new IllegalArgumentException("El color de frente debe tener formato HEX #RRGGBB");
        }
    }

    private static boolean isHexColor(String value) {
        return value.matches("^#[0-9A-Fa-f]{6}$");
    }
}

