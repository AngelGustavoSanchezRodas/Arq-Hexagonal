package com.navaja.navajagtbackend.controllers;

import com.navaja.navajagtbackend.dto.CrearEnlaceRequest;
import com.navaja.navajagtbackend.dto.EnlaceResponse;
import com.navaja.navajagtbackend.services.EnlaceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CoreLinksController {

    private final EnlaceService enlaceService;

    public CoreLinksController(EnlaceService enlaceService) {
        this.enlaceService = enlaceService;
    }

    @PostMapping("/api/core/links/create/")
    public ResponseEntity<EnlaceResponse> crearEnlace(@Valid @RequestBody CrearEnlaceRequest request) {
        EnlaceResponse response = enlaceService.crearEnlace(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

