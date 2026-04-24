package com.navaja.navajagtbackend.services;

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

    public void verificarLimite(Usuario usuario, String tipoHerramienta) {
        if (usuario == null || !StringUtils.hasText(tipoHerramienta)) {
            return;
        }

        String herramienta = tipoHerramienta.trim().toUpperCase();
        Integer limite = limiteMaximo(usuario.getPlan(), herramienta);
        if (limite == null) {
            return;
        }

        long usados = enlaceRepository.countByUsuarioIdAndTipoHerramienta(usuario.getId(), herramienta);
        if (usados >= limite) {
            throw new LimiteExcedidoException("Has alcanzado el límite de enlaces para tu plan");
        }
    }

    private Integer limiteMaximo(PlanUsuario planUsuario, String tipoHerramienta) {
        PlanUsuario plan = planUsuario == null ? PlanUsuario.FREE : planUsuario;

        return switch (tipoHerramienta) {
            case "QR" -> plan == PlanUsuario.PREMIUM ? 10 : 1;
            default -> null;
        };
    }
}

