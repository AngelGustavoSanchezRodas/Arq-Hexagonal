package com.navaja.navajagtbackend.services;

import com.navaja.navajagtbackend.dto.CrearEnlaceRequest;
import com.navaja.navajagtbackend.dto.EnlaceResponse;
import com.navaja.navajagtbackend.exceptions.AccesoDenegadoException;
import com.navaja.navajagtbackend.exceptions.AliasEnUsoException;
import com.navaja.navajagtbackend.models.Enlace;
import com.navaja.navajagtbackend.models.PlanUsuario;
import com.navaja.navajagtbackend.models.Usuario;
import com.navaja.navajagtbackend.repositories.EnlaceRepository;
import com.navaja.navajagtbackend.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class EnlaceService {

    private static final String CACHE_PREFIX = "shortcode:";

    private final EnlaceRepository enlaceRepository;
    private final UsuarioRepository usuarioRepository;
    private final StringRedisTemplate redisTemplate;
    private final ShortcodeGenerator shortcodeGenerator;
    private final ClicAsyncService clicAsyncService;
    private final QuotaService quotaService;
    private final Duration linkCacheTtl;

    public EnlaceService(
            EnlaceRepository enlaceRepository,
            UsuarioRepository usuarioRepository,
            StringRedisTemplate redisTemplate,
            ShortcodeGenerator shortcodeGenerator,
            ClicAsyncService clicAsyncService,
            QuotaService quotaService,
            @Value("${app.cache.shortcode-ttl-hours:24}") long cacheTtlHours
    ) {
        this.enlaceRepository = enlaceRepository;
        this.usuarioRepository = usuarioRepository;
        this.redisTemplate = redisTemplate;
        this.shortcodeGenerator = shortcodeGenerator;
        this.clicAsyncService = clicAsyncService;
        this.quotaService = quotaService;
        this.linkCacheTtl = Duration.ofHours(cacheTtlHours);
    }

    @Transactional
    public EnlaceResponse crearEnlace(CrearEnlaceRequest request) {
        Usuario usuario = obtenerUsuarioAutenticado();
        String codigoCorto = resolverCodigoCorto(request, usuario);
        String tipoHerramienta = normalizarTipoHerramienta(request.tipoHerramienta());

        OffsetDateTime fechaExpiracion = null;
        if (usuario != null) {
            quotaService.verificarLimite(usuario, tipoHerramienta);
        } else {
            fechaExpiracion = OffsetDateTime.now().plusDays(30);
        }

        Enlace enlace = new Enlace();
        enlace.setCodigoCorto(codigoCorto);
        enlace.setUrlOriginal(request.urlOriginal());
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
        String urlOriginal = redisTemplate.opsForValue().get(cacheKey);
        Enlace enlace = enlaceRepository.findByCodigoCorto(shortcode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shortcode no encontrado"));

        if (estaExpirado(enlace)) {
            redisTemplate.delete(cacheKey);
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
            redisTemplate.opsForValue().set(CACHE_PREFIX + enlace.getCodigoCorto(), enlace.getUrlOriginal(), ttl);
        }
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

    private String resolverCodigoCorto(CrearEnlaceRequest request, Usuario usuario) {
        if (!StringUtils.hasText(request.alias())) {
            return generateUniqueShortcode();
        }

        if (usuario == null || usuario.getPlan() != PlanUsuario.PREMIUM) {
            throw new AccesoDenegadoException();
        }

        String aliasLimpio = limpiarAlias(request.alias());
        if (!StringUtils.hasText(aliasLimpio)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Alias invalido");
        }

        if (enlaceRepository.existsByCodigoCorto(aliasLimpio)) {
            throw new AliasEnUsoException();
        }

        return aliasLimpio;
    }

    private String limpiarAlias(String alias) {
        String sinAcentos = Normalizer.normalize(alias, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        String normalizado = sinAcentos.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "-");
        String limpio = normalizado
                .replaceAll("[^a-z0-9-]", "")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-+|-+$", "");

        if (limpio.length() > 50) {
            return limpio.substring(0, 50);
        }
        return limpio;
    }
}
