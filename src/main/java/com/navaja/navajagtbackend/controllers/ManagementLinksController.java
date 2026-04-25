package com.navaja.navajagtbackend.controllers;

import com.navaja.navajagtbackend.dto.EnlaceResponse;
import com.navaja.navajagtbackend.services.EnlaceService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@SecurityRequirement(name = "bearerAuth")
public class ManagementLinksController {

    private final EnlaceService enlaceService;

    public ManagementLinksController(EnlaceService enlaceService) {
        this.enlaceService = enlaceService;
    }

    @GetMapping("/api/management/links/list/")
    public ResponseEntity<List<EnlaceResponse>> listarEnlaces(@RequestParam(required = false) Long usuarioId) {
        return ResponseEntity.ok(enlaceService.listarEnlaces(usuarioId));
    }
}


