package com.navaja.navajagtbackend.services;

import com.navaja.navajagtbackend.dto.CrearEnlaceRequest;
import com.navaja.navajagtbackend.dto.EnlaceResponse;
import com.navaja.navajagtbackend.exceptions.AliasEnUsoException;
import com.navaja.navajagtbackend.models.Enlace;
import com.navaja.navajagtbackend.models.TipoEnlace;
import com.navaja.navajagtbackend.models.Usuario;
import com.navaja.navajagtbackend.repositories.EnlaceRepository;
import com.navaja.navajagtbackend.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EnlaceService {

    private static final String CACHE_PREFIX = "shortcode:";

    private final EnlaceRepository enlaceRepository;
    private final UsuarioRepository usuarioRepository;
    private final ShortcodeGenerator shortcodeGenerator;
    private final ClicAsyncService clicAsyncService;
    private final QuotaService quotaService;
    private final Duration linkCacheTtl;
    private final Map<String, CachedUrl> cacheEnMemoria = new ConcurrentHashMap<>();

    public EnlaceService(
            EnlaceRepository enlaceRepository,
            UsuarioRepository usuarioRepository,
            ShortcodeGenerator shortcodeGenerator,
            ClicAsyncService clicAsyncService,
            QuotaService quotaService,
            @Value("${app.cache.shortcode-ttl-hours:24}") long cacheTtlHours
    ) {
        this.enlaceRepository = enlaceRepository;
        this.usuarioRepository = usuarioRepository;
        this.shortcodeGenerator = shortcodeGenerator;
        this.clicAsyncService = clicAsyncService;
        this.quotaService = quotaService;
        this.linkCacheTtl = Duration.ofHours(cacheTtlHours);
    }

@Transactional
    public EnlaceResponse crearEnlace(CrearEnlaceRequest request) {
        Usuario usuario = obtenerUsuarioAutenticado();
        String codigoCorto = resolverCodigoCorto(request);
        String tipoHerramienta = normalizarTipoHerramienta(request.tipoHerramienta());

        OffsetDateTime fechaExpiracion = null;
        if (usuario != null) {
            quotaService.verificarLimite(usuario, tipoHerramienta);
        } else {
            fechaExpiracion = OffsetDateTime.now().plusDays(30);
        }

        Enlace enlace = new Enlace();
        enlace.setCodigoCorto(codigoCorto);

        // --- INICIO DEL PARCHE (Sintaxis de Java Records) ---
        if (request.tipo() == TipoEnlace.BIOLINK) {
            // Si es un Biolink, le asignamos una URL "placeholder" usando el codigoCorto
            String placeholderUrl = "https://navaja.gt/bio/" + codigoCorto;
            enlace.setUrlOriginal(placeholderUrl);
        } else {
            // Para STANDARD, WHATSAPP o MENU_QR, la URL es obligatoria
            if (request.urlOriginal() == null || request.urlOriginal().isBlank()) {
                throw new IllegalArgumentException("La URL original es obligatoria para este tipo de enlace.");
            }
            enlace.setUrlOriginal(request.urlOriginal());
        }
        // --- FIN DEL PARCHE ---

        enlace.setEsDinamico(Boolean.TRUE.equals(request.esDinamico()));
        enlace.setUsuario(usuario);
        enlace.setTipoHerramienta(tipoHerramienta);
        enlace.setFechaExpiracion(fechaExpiracion);
        enlace.setTipo(request.tipo() != null ? request.tipo() : com.navaja.navajagtbackend.models.TipoEnlace.STANDARD);
        enlace.setMetadata(request.metadata());

        Enlace saved = enlaceRepository.save(enlace);
        cacheLink(saved);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<EnlaceResponse> listarEnlaces(Long usuarioId) {
        List<Enlace> enlaces = usuarioId == null
                ? enlaceRepository.findAll()
                : enlaceRepository.findByUsuarioId(usuarioId);

        return enlaces.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public String resolverUrlOriginal(String shortcode, String direccionIp, String userAgent) {
        String cacheKey = CACHE_PREFIX + shortcode;
        String urlOriginal = getCacheValue(cacheKey);
        Enlace enlace = enlaceRepository.findByCodigoCorto(shortcode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shortcode no encontrado"));

        if (estaExpirado(enlace)) {
            cacheEnMemoria.remove(cacheKey);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Shortcode no encontrado");
        }

        if (urlOriginal != null) {
            clicAsyncService.registrarClicAsync(enlace.getId(), direccionIp, userAgent);
            return urlOriginal;
        }

        cacheLink(enlace);
        clicAsyncService.registrarClicAsync(enlace.getId(), direccionIp, userAgent);
        return enlace.getUrlOriginal();
    }

    private void cacheLink(Enlace enlace) {
        Duration ttl = enlace.getFechaExpiracion() != null
                ? Duration.between(OffsetDateTime.now(), enlace.getFechaExpiracion())
                : linkCacheTtl;

        if (ttl != null && !ttl.isZero() && !ttl.isNegative()) {
            OffsetDateTime expiresAt = OffsetDateTime.now().plus(ttl);
            cacheEnMemoria.put(CACHE_PREFIX + enlace.getCodigoCorto(), new CachedUrl(enlace.getUrlOriginal(), expiresAt));
        }
    }

    private String getCacheValue(String cacheKey) {
        CachedUrl cached = cacheEnMemoria.get(cacheKey);
        if (cached == null) {
            return null;
        }
        if (cached.isExpired()) {
            cacheEnMemoria.remove(cacheKey, cached);
            return null;
        }
        return cached.urlOriginal();
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

    private record CachedUrl(String urlOriginal, OffsetDateTime expiresAt) {
        private boolean isExpired() {
            return expiresAt != null && expiresAt.isBefore(OffsetDateTime.now());
        }
    }
}
