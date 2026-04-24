package com.navaja.navajagtbackend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WifiQrRequest(
        @NotBlank @Size(max = 64) String ssid,
        @Size(max = 64) String password,
        @NotBlank @Size(max = 10) String encryptionType,
        @Min(64) @Max(2048) Integer width,
        @Min(64) @Max(2048) Integer height
) {
}

