package com.navaja.navajagtbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.Map;

public record SignatureMetadata(
        @NotBlank String nombreCompleto,
        @NotBlank String cargo,
        @NotBlank String empresa,
        @NotBlank String templateId,
        String telefono,
        @Pattern(regexp = "https?://.+") String sitioWeb,
        @Pattern(regexp = "https?://.+") String avatarUrl,
        Map<String, String> redesSociales
) {
}

