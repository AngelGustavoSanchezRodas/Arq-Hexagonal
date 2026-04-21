package com.navaja.navajagtbackend.services;

import com.navaja.navajagtbackend.models.Clic;
import com.navaja.navajagtbackend.models.Enlace;
import com.navaja.navajagtbackend.repositories.ClicRepository;
import com.navaja.navajagtbackend.repositories.EnlaceRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClicAsyncService {

    private final ClicRepository clicRepository;
    private final EnlaceRepository enlaceRepository;

    public ClicAsyncService(ClicRepository clicRepository, EnlaceRepository enlaceRepository) {
        this.clicRepository = clicRepository;
        this.enlaceRepository = enlaceRepository;
    }

    @Async
    @Transactional
    public void registrarClicAsync(Long enlaceId, String direccionIp, String userAgent) {
        Enlace enlace = enlaceRepository.findById(enlaceId).orElse(null);
        if (enlace == null) {
            return;
        }

        Clic clic = new Clic();
        clic.setEnlace(enlace);
        clic.setDireccionIp(direccionIp);
        clic.setUserAgent(userAgent);
        clicRepository.save(clic);
    }
}

