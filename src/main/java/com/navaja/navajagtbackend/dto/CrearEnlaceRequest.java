package com.navaja.navajagtbackend.dto;

import com.navaja.navajagtbackend.models.TipoEnlace;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record CrearEnlaceRequest(
        @Size(max = 50) String codigoCorto,
        @Size(max = 2048) String urlOriginal,
        Boolean esDinamico,
        Long usuarioId,
        @Size(max = 50) String tipoHerramienta,
        @Size(max = 50) String alias,
        TipoEnlace tipo,
        Map<String, Object> metadata
) {
}
