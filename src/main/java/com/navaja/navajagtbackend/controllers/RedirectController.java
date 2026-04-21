package com.navaja.navajagtbackend.controllers;

import com.navaja.navajagtbackend.services.EnlaceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class RedirectController {

    private final EnlaceService enlaceService;

    public RedirectController(EnlaceService enlaceService) {
        this.enlaceService = enlaceService;
    }

    @GetMapping("/{shortcode}")
    public ResponseEntity<Void> redirigir(@PathVariable String shortcode, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        String urlOriginal = enlaceService.resolverUrlOriginal(shortcode, ip, userAgent);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(urlOriginal))
                .build();
    }
}

