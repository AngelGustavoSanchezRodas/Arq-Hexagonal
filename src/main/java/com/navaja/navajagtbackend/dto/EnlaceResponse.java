package com.navaja.navajagtbackend.dto;

import java.time.OffsetDateTime;

public record EnlaceResponse(
        Long id,
        String codigoCorto,
        String urlOriginal,
        boolean esDinamico,
        Long usuarioId,
        OffsetDateTime fechaCreacion,
        String tipoHerramienta,
        OffsetDateTime fechaExpiracion
) {
}

