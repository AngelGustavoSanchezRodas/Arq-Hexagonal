package com.navaja.navajagtbackend.services;

import com.navaja.navajagtbackend.models.Enlace;
import com.navaja.navajagtbackend.repositories.EnlaceRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.OffsetDateTime;

@Service
public class LimpiezaProgramadaService {

    private final EnlaceRepository enlaceRepository;
    private final EnlaceService enlaceService;

    public LimpiezaProgramadaService(EnlaceRepository enlaceRepository, EnlaceService enlaceService) {
        this.enlaceRepository = enlaceRepository;
        this.enlaceService = enlaceService;
    }

    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void limpiarEnlacesExpirados() {
        List<Enlace> expirados = enlaceRepository.findByFechaExpiracionBefore(OffsetDateTime.now());
        expirados.forEach(enlaceService::eliminarEnlace);
    }
}

