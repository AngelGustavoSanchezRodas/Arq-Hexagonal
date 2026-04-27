package com.navaja.navajagtbackend.services;

import com.navaja.navajagtbackend.exceptions.AccesoDenegadoException;
import com.navaja.navajagtbackend.exceptions.LimiteExcedidoException;
import com.navaja.navajagtbackend.models.PlanUsuario;
import com.navaja.navajagtbackend.models.Usuario;
import com.navaja.navajagtbackend.repositories.EnlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class QuotaService {

    private final EnlaceRepository enlaceRepository;

    public QuotaService(EnlaceRepository enlaceRepository) {
        this.enlaceRepository = enlaceRepository;
    }

    public void verificarLimite(Usuario usuario, String aliasPersonalizado) {
        if (usuario == null) {
            return;
        }

        if (usuario.getPlan() == null) {
            usuario.setPlan(PlanUsuario.FREE);
        }

        if (usuario.getPlan() == PlanUsuario.FREE && StringUtils.hasText(aliasPersonalizado)) {
            throw new AccesoDenegadoException();
        }

        long usados = enlaceRepository.countByUsuario(usuario);
        int limite = limiteMaximo(usuario.getPlan());
        if (usados >= limite) {
            throw new LimiteExcedidoException("Has alcanzado el límite de enlaces de tu plan.");
        }
    }

    private int limiteMaximo(PlanUsuario planUsuario) {
        PlanUsuario plan = planUsuario == null ? PlanUsuario.FREE : planUsuario;

        return switch (plan) {
            case PREMIUM -> Integer.MAX_VALUE;
            case FREE -> 10;
        };
    }
}

