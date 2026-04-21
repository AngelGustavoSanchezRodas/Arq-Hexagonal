package com.navaja.navajagtbackend.services;

import com.navaja.navajagtbackend.dto.CrearEnlaceRequest;
import com.navaja.navajagtbackend.dto.EnlaceResponse;
import com.navaja.navajagtbackend.models.Enlace;
import com.navaja.navajagtbackend.models.Usuario;
import com.navaja.navajagtbackend.repositories.EnlaceRepository;
import com.navaja.navajagtbackend.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.List;

@Service
public class EnlaceService {

    private static final String CACHE_PREFIX = "shortcode:";

    private final EnlaceRepository enlaceRepository;
    private final UsuarioRepository usuarioRepository;
    private final StringRedisTemplate redisTemplate;
    private final ShortcodeGenerator shortcodeGenerator;
    private final ClicAsyncService clicAsyncService;
    private final Duration linkCacheTtl;

    public EnlaceService(
            EnlaceRepository enlaceRepository,
            UsuarioRepository usuarioRepository,
            StringRedisTemplate redisTemplate,
            ShortcodeGenerator shortcodeGenerator,
            ClicAsyncService clicAsyncService,
            @Value("${app.cache.shortcode-ttl-hours:24}") long cacheTtlHours
    ) {
        this.enlaceRepository = enlaceRepository;
        this.usuarioRepository = usuarioRepository;
        this.redisTemplate = redisTemplate;
        this.shortcodeGenerator = shortcodeGenerator;
        this.clicAsyncService = clicAsyncService;
        this.linkCacheTtl = Duration.ofHours(cacheTtlHours);
    }

    @Transactional
    public EnlaceResponse crearEnlace(CrearEnlaceRequest request) {
        String codigoCorto = StringUtils.hasText(request.codigoCorto())
                ? request.codigoCorto().trim()
                : generateUniqueShortcode();

        if (enlaceRepository.existsByCodigoCorto(codigoCorto)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El codigo corto ya existe");
        }

        Usuario usuario = null;
        if (request.usuarioId() != null) {
            usuario = usuarioRepository.findById(request.usuarioId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        }

        Enlace enlace = new Enlace();
        enlace.setCodigoCorto(codigoCorto);
        enlace.setUrlOriginal(request.urlOriginal());
        enlace.setEsDinamico(Boolean.TRUE.equals(request.esDinamico()));
        enlace.setUsuario(usuario);

        Enlace saved = enlaceRepository.save(enlace);
        cacheLink(saved.getCodigoCorto(), saved.getUrlOriginal());

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

        if (urlOriginal != null) {
            enlaceRepository.findByCodigoCorto(shortcode)
                    .ifPresent(enlace -> clicAsyncService.registrarClicAsync(enlace.getId(), direccionIp, userAgent));
            return urlOriginal;
        }

        Enlace enlace = enlaceRepository.findByCodigoCorto(shortcode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shortcode no encontrado"));

        cacheLink(shortcode, enlace.getUrlOriginal());
        clicAsyncService.registrarClicAsync(enlace.getId(), direccionIp, userAgent);
        return enlace.getUrlOriginal();
    }

    private void cacheLink(String shortcode, String urlOriginal) {
        redisTemplate.opsForValue().set(CACHE_PREFIX + shortcode, urlOriginal, linkCacheTtl);
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
                enlace.getFechaCreacion()
        );
    }
}

