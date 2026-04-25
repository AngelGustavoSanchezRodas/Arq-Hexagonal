package com.navaja.navajagtbackend.controllers;

import com.navaja.navajagtbackend.dto.EnlaceResponse;
import com.navaja.navajagtbackend.models.Enlace;
import com.navaja.navajagtbackend.repositories.EnlaceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/v1/public/enlaces")
public class PublicLinkController {

    private final EnlaceRepository enlaceRepository;

    public PublicLinkController(EnlaceRepository enlaceRepository) {
        this.enlaceRepository = enlaceRepository;
    }

    @GetMapping("/{alias}")
    public ResponseEntity<EnlaceResponse> getPublicLink(@PathVariable String alias) {
        Enlace enlace = enlaceRepository.findByCodigoCorto(alias)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enlace no encontrado"));

        if (estaExpirado(enlace)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Enlace expirado");
        }

        return ResponseEntity.ok(toResponse(enlace));
    }

    private EnlaceResponse toResponse(Enlace enlace) {
        return new EnlaceResponse(
                enlace.getId(),
                enlace.getCodigoCorto(),
                enlace.getUrlOriginal(),
                enlace.isEsDinamico(),
                null,
                enlace.getFechaCreacion(),
                enlace.getTipoHerramienta(),
                enlace.getFechaExpiracion(),
                enlace.getTipo(),
                enlace.getMetadata()
        );
    }

    private boolean estaExpirado(Enlace enlace) {
        return enlace.getFechaExpiracion() != null && enlace.getFechaExpiracion().isBefore(OffsetDateTime.now());
    }
}
