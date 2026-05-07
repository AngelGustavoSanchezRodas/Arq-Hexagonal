package com.navaja.navajagtbackend.services;

import com.navaja.navajagtbackend.exceptions.AccesoDenegadoException;
import com.navaja.navajagtbackend.exceptions.LimiteExcedidoException;
import com.navaja.navajagtbackend.models.PlanUsuario;
import com.navaja.navajagtbackend.models.Usuario;
import com.navaja.navajagtbackend.repositories.EnlaceRepository;
import com.navaja.navajagtbackend.models.TipoEnlace;
import java.util.Set;
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

    public void validarCreacionFirma(String usuarioId, String templateId) {
        if (templateId == null || templateId.isBlank()) {
            throw new IllegalArgumentException("El campo templateId es obligatorio para crear una firma");
        }

        Set<String> TEMPLATES_BASICOS = Set.of("1", "2");

        if (!TEMPLATES_BASICOS.contains(templateId)) {
            if (!validarPlanPremium(usuarioId)) {
                throw new AccesoDenegadoException("La plantilla seleccionada es exclusiva del plan PRO");
            }
        }

        if (!java.util.Objects.isNull(usuarioId) && usuarioId.trim().length() > 0) {
            long id;
            try {
                id = Long.parseLong(usuarioId);
            } catch (NumberFormatException ex) {
                return; // cannot evaluate quota without valid id
            }

            long firmasActivas = enlaceRepository.countByUsuarioIdAndTipo(id, TipoEnlace.SIGNATURE);

            boolean esFree = usuarioRepository.findById(id)
                    .map(usuario -> usuario.getPlan() == PlanUsuario.FREE)
                    .orElse(true);

            if (esFree && firmasActivas >= 1) {
                throw new AccesoDenegadoException("Los usuarios gratuitos solo pueden tener 1 firma activa");
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

