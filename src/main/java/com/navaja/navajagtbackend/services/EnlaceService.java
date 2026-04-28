package com.navaja.navajagtbackend.services;

import com.navaja.navajagtbackend.dto.CrearEnlaceRequest;
import com.navaja.navajagtbackend.dto.EnlaceResponse;
import com.navaja.navajagtbackend.exceptions.AliasEnUsoException;
import com.navaja.navajagtbackend.models.Enlace;
import com.navaja.navajagtbackend.models.TipoEnlace;
import com.navaja.navajagtbackend.models.Usuario;
import com.navaja.navajagtbackend.repositories.EnlaceRepository;
import com.navaja.navajagtbackend.repositories.UsuarioRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class EnlaceService {

    private final EnlaceRepository enlaceRepository;
    private final UsuarioRepository usuarioRepository;
    private final ShortcodeGenerator shortcodeGenerator;
    private final QuotaService quotaService;

    public EnlaceService(
            EnlaceRepository enlaceRepository,
            UsuarioRepository usuarioRepository,
            ShortcodeGenerator shortcodeGenerator,
            QuotaService quotaService
    ) {
        this.enlaceRepository = enlaceRepository;
        this.usuarioRepository = usuarioRepository;
        this.shortcodeGenerator = shortcodeGenerator;
        this.quotaService = quotaService;
    }

    @Transactional
    public EnlaceResponse crearEnlace(CrearEnlaceRequest request) {
        Usuario usuario = obtenerUsuarioAutenticado();
        String aliasPersonalizado = request.alias();
        String tipoHerramienta = normalizarTipoHerramienta(request.tipoHerramienta());
        TipoEnlace tipoEnlace = request.tipo() != null ? request.tipo() : TipoEnlace.STANDARD;

        if (tipoEnlace != TipoEnlace.BIOLINK) {
            if (request.urlOriginal() == null || request.urlOriginal().isBlank()) {
                throw new IllegalArgumentException("La URL original es obligatoria para este tipo de enlace");
            }
        }

        OffsetDateTime fechaExpiracion = null;
        if (usuario != null) {
            quotaService.verificarLimite(usuario, aliasPersonalizado);
        } else {
            fechaExpiracion = OffsetDateTime.now().plusDays(30);
        }

        String codigoCorto = resolverCodigoCorto(request);

        Enlace enlace = new Enlace();
        enlace.setCodigoCorto(codigoCorto);

        if (request.tipo() == TipoEnlace.BIOLINK) {
            // Si es un Biolink, le asignamos una URL "placeholder" usando el codigoCorto
            String placeholderUrl = "https://navaja.gt/bio/" + codigoCorto;
            enlace.setUrlOriginal(placeholderUrl);
        } else {
            enlace.setUrlOriginal(request.urlOriginal());
        }

        enlace.setEsDinamico(Boolean.TRUE.equals(request.esDinamico()));
        enlace.setUsuario(usuario);
        enlace.setTipoHerramienta(tipoHerramienta);
        enlace.setFechaExpiracion(fechaExpiracion);
        enlace.setTipo(tipoEnlace);
        enlace.setMetadata(request.metadata());

        Enlace saved = guardarEnlace(enlace);

        return toResponse(saved);
    }

    @Transactional
    @CachePut(value = "enlaces", key = "#result.codigoCorto", unless = "#result == null")
    public Enlace guardarEnlace(Enlace enlace) {
        return enlaceRepository.save(enlace);
    }

    @Transactional(readOnly = true)
    public List<EnlaceResponse> listarEnlaces(Long usuarioId) {
        List<Enlace> enlaces = usuarioId == null
                ? enlaceRepository.findAll()
                : enlaceRepository.findByUsuarioId(usuarioId);

        return enlaces.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "enlaces", key = "#codigoCorto", unless = "#result == null")
    public Enlace obtenerEnlacePorCodigoCorto(String codigoCorto) {
        Enlace enlace = enlaceRepository.findByCodigoCorto(codigoCorto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shortcode no encontrado"));

        if (estaExpirado(enlace)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Shortcode no encontrado");
        }

        return enlace;
    }

    @Transactional
    @CacheEvict(value = "enlaces", key = "#enlace.codigoCorto")
    public void eliminarEnlace(Enlace enlace) {
        enlaceRepository.delete(enlace);
    }

    private String generateUniqueShortcode() {
        String shortcode = shortcodeGenerator.generate();
        while (enlaceRepository.existsByCodigoCorto(shortcode)) {
            shortcode = shortcodeGenerator.generate();
        }
        return shortcode;
    }

    private EnlaceResponse toResponse(Enlace enlace) {
        Long usuarioId = enlace.getUsuario() != null ? enlace.getUsuario().getId() : null;
        return new EnlaceResponse(
                enlace.getId(),
                enlace.getCodigoCorto(),
                enlace.getUrlOriginal(),
                enlace.isEsDinamico(),
                usuarioId,
                enlace.getFechaCreacion(),
                enlace.getTipoHerramienta(),
                enlace.getFechaExpiracion(),
                enlace.getTipo(),
                enlace.getMetadata()
        );
    }

    private Usuario obtenerUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return usuarioRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario autenticado no encontrado"));
        }

        return null;
    }

    private boolean estaExpirado(Enlace enlace) {
        return enlace.getFechaExpiracion() != null && enlace.getFechaExpiracion().isBefore(OffsetDateTime.now());
    }

    private String normalizarTipoHerramienta(String tipoHerramienta) {
        return StringUtils.hasText(tipoHerramienta) ? tipoHerramienta.trim().toUpperCase() : "QR";
    }

    private String resolverCodigoCorto(CrearEnlaceRequest request) {
        if (!StringUtils.hasText(request.alias())) {
            return generateUniqueShortcode();
        }

        String alias = request.alias().trim();
        if (enlaceRepository.existsByCodigoCorto(alias)) {
            throw new AliasEnUsoException("El alias ya esta en uso");
        }

        return alias;
    }
}
