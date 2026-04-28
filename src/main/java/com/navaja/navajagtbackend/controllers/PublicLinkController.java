package com.navaja.navajagtbackend.controllers;

import com.navaja.navajagtbackend.dto.EnlaceResponse;
import com.navaja.navajagtbackend.models.Enlace;
import com.navaja.navajagtbackend.services.EnlaceService;
import com.navaja.navajagtbackend.services.ClicAsyncService;
import com.navaja.navajagtbackend.models.TipoEnlace;
import jakarta.servlet.http.HttpServletRequest;
import com.navaja.navajagtbackend.exceptions.EnlaceNoEncontradoException;
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

    private final EnlaceService enlaceService;
    private final ClicAsyncService clicAsyncService;

    public PublicLinkController(EnlaceService enlaceService, ClicAsyncService clicAsyncService) {
        this.enlaceService = enlaceService;
        this.clicAsyncService = clicAsyncService;
    }

    @GetMapping("/{alias}")
    public ResponseEntity<EnlaceResponse> getPublicLink(@PathVariable String alias) {
        Enlace enlace = enlaceService.obtenerEnlacePorCodigoCorto(alias);

        if (estaExpirado(enlace)) {
            enlaceService.eliminarEnlace(enlace);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Enlace expirado");
        }

        return ResponseEntity.ok(toResponse(enlace));
    }

    @GetMapping("/bio/{alias}")
    public ResponseEntity<EnlaceResponse> getBioProfile(@PathVariable String alias, HttpServletRequest request) {
        Enlace enlace = enlaceService.obtenerEnlacePorCodigoCorto(alias);
        if (enlace == null) {
            throw new EnlaceNoEncontradoException();
        }

        if (enlace.getTipo() != TipoEnlace.BIOLINK) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este alias no pertenece a un Biolink");
        }

        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        // registrar analitica sin redirigir
        clicAsyncService.registrarClicAsync(alias, ip, userAgent);

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
