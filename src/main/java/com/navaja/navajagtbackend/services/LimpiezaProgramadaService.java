package com.navaja.navajagtbackend.services;

import com.navaja.navajagtbackend.repositories.EnlaceRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class LimpiezaProgramadaService {

    private final EnlaceRepository enlaceRepository;

    public LimpiezaProgramadaService(EnlaceRepository enlaceRepository) {
        this.enlaceRepository = enlaceRepository;
    }

    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void limpiarEnlacesExpirados() {
        enlaceRepository.deleteByFechaExpiracionBefore(OffsetDateTime.now());
    }
}

