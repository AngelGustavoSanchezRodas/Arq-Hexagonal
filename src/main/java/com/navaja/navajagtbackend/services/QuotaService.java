package com.navaja.navajagtbackend.services;

import com.navaja.navajagtbackend.exceptions.AccesoDenegadoException;
import com.navaja.navajagtbackend.exceptions.LimiteExcedidoException;
import com.navaja.navajagtbackend.models.PlanUsuario;
import com.navaja.navajagtbackend.models.Usuario;
import com.navaja.navajagtbackend.repositories.EnlaceRepository;
import com.navaja.navajagtbackend.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class QuotaService {

    private final EnlaceRepository enlaceRepository;
    private final UsuarioRepository usuarioRepository;

    public QuotaService(EnlaceRepository enlaceRepository, UsuarioRepository usuarioRepository) {
        this.enlaceRepository = enlaceRepository;
        this.usuarioRepository = usuarioRepository;
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

    public boolean validarPlanPremium(String usuarioId) {
        if (!StringUtils.hasText(usuarioId)) {
            return false;
        }

        long id;
        try {
            id = Long.parseLong(usuarioId);
        } catch (NumberFormatException exception) {
            return false;
        }

        return usuarioRepository.findById(id)
                .map(usuario -> usuario.getPlan() == PlanUsuario.PREMIUM)
                .orElse(false);
    }

    public void validarConversionPremium(String usuarioId, String formatoSalida) {
        String formatoUpper = formatoSalida == null ? "" : formatoSalida.toUpperCase();

        if ("WEBP".equals(formatoUpper) || "TIFF".equals(formatoUpper) ||
            "BMP".equals(formatoUpper) || "GIF".equals(formatoUpper)) {
            if (!validarPlanPremium(usuarioId)) {
                throw new AccesoDenegadoException("El formato " + formatoSalida + " es exclusivo del plan PRO");
            }
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

