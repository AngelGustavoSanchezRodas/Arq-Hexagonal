package com.navaja.navajagtbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearEnlaceRequest(
        @Size(max = 50) String codigoCorto,
        @NotBlank @Size(max = 2048) String urlOriginal,
        Boolean esDinamico,
        Long usuarioId,
        @Size(max = 50) String tipoHerramienta,
        @Size(max = 50) String alias
) {
}
