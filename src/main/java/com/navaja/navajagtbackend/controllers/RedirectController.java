package com.navaja.navajagtbackend.controllers;

import com.navaja.navajagtbackend.models.Enlace;
import com.navaja.navajagtbackend.services.ClicAsyncService;
import com.navaja.navajagtbackend.services.EnlaceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.OffsetDateTime;

@RestController
public class RedirectController {

    private final EnlaceService enlaceService;
    private final ClicAsyncService clicAsyncService;
    private final String frontendUrl;

    public RedirectController(
            EnlaceService enlaceService,
            ClicAsyncService clicAsyncService,
            @Value("${app.frontend.url:http://localhost:3000}") String frontendUrl
    ) {
        this.enlaceService = enlaceService;
        this.clicAsyncService = clicAsyncService;
        this.frontendUrl = frontendUrl;
    }

    @GetMapping("/{shortcode}")
    public ResponseEntity<Void> redirigir(
            @PathVariable String shortcode,
            HttpServletRequest request
    ) {
        Enlace enlace = enlaceService.obtenerEnlacePorCodigoCorto(shortcode);

        if (estaExpirado(enlace)) {
            enlaceService.eliminarEnlace(enlace);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Shortcode expirado");
        }

        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        clicAsyncService.registrarClicAsync(enlace.getId(), ip, userAgent);

        String redirectUrl = resolverRedirect(enlace, shortcode);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
    }

    private String resolverRedirect(Enlace enlace, String shortcode) {
        return switch (enlace.getTipo()) {
            case BIOLINK -> frontendUrl + "/bio/" + shortcode;
            case STANDARD, WHATSAPP, MENU_QR -> enlace.getUrlOriginal();
        };
    }

    private boolean estaExpirado(Enlace enlace) {
        return enlace.getFechaExpiracion() != null && enlace.getFechaExpiracion().isBefore(OffsetDateTime.now());
    }
}

