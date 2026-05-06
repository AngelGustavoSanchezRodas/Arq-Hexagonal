package com.navaja.navajagtbackend.controllers;

import com.navaja.navajagtbackend.dto.OpenGraphData;
import com.navaja.navajagtbackend.dto.QrGenerateRequest;
import com.navaja.navajagtbackend.services.OpenGraphService;
import com.navaja.navajagtbackend.services.QrCodeService;
import com.navaja.navajagtbackend.services.QuotaService;
import jakarta.validation.Valid;
import net.coobird.thumbnailator.Thumbnails;
import com.navaja.navajagtbackend.security.UsuarioPrincipal;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ContentDisposition;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/api/v1/tools")
public class ToolsController {

    private final QrCodeService qrCodeService;
    private final OpenGraphService openGraphService;
    private final QuotaService quotaService;

    public ToolsController(QrCodeService qrCodeService, OpenGraphService openGraphService, QuotaService quotaService) {
        this.qrCodeService = qrCodeService;
        this.openGraphService = openGraphService;
        this.quotaService = quotaService;
    }

    @GetMapping("/qr")
    public ResponseEntity<byte[]> generateQr(
            @RequestParam String url,
            @RequestParam(defaultValue = "300") int width,
            @RequestParam(defaultValue = "300") int height
    ) {
        validateHttpUri(url);
        byte[] image = qrCodeService.generateStandardQr(url, width, height);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE)
                .body(image);
    }

    @GetMapping("/opengraph")
    public ResponseEntity<OpenGraphData> getOpenGraph(@RequestParam String url) {
        validateHttpUri(url);
        return ResponseEntity.ok(openGraphService.extract(url));
    }

    @PostMapping("/qr/generate")
    public ResponseEntity<byte[]> generatePremiumQr(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @Valid @RequestBody QrGenerateRequest request
    ) {
        String usuarioId = principal == null ? null : String.valueOf(principal.getId());
        byte[] image = qrCodeService.generarQrPremium(request, usuarioId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE)
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .body(image);
    }

    @PostMapping(value = "/convert-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> convertImage(
            @AuthenticationPrincipal UsuarioPrincipal principal,
            @RequestParam("file") MultipartFile file,
            @RequestParam("format") String format
    ) {
        String usuarioId = principal == null ? null : String.valueOf(principal.getId());

        String formatoValidado = validarYNormalizarFormato(format);

        quotaService.validarConversionPremium(usuarioId, formatoValidado);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            Thumbnails.of(file.getInputStream())
                    .scale(1.0)
                    .outputFormat(formatoValidado.toLowerCase())
                    .toOutputStream(os);

            MediaType mediaType = resolverMediaType(formatoValidado);
            String nombreArchivo = "converted-image." + formatoValidado.toLowerCase();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            headers.setContentDisposition(ContentDisposition.attachment().filename(nombreArchivo).build());
            return new ResponseEntity<>(os.toByteArray(), headers, HttpStatus.OK);
        } catch (IOException exception) {
            throw new IllegalArgumentException("No fue posible convertir la imagen", exception);
        }
    }

    private String validarYNormalizarFormato(String formato) {
        if (formato == null || formato.isBlank()) {
            throw new IllegalArgumentException("El formato no puede estar vacío");
        }

        String formatoUpper = formato.toUpperCase();

        if ("SVG".equals(formatoUpper)) {
            throw new IllegalArgumentException("El formato SVG no está soportado");
        }

        if ("JPG".equals(formatoUpper) || "JPEG".equals(formatoUpper) || "PNG".equals(formatoUpper) ||
            "WEBP".equals(formatoUpper) || "TIFF".equals(formatoUpper) || "TIF".equals(formatoUpper) ||
            "BMP".equals(formatoUpper) || "GIF".equals(formatoUpper)) {
            return "JPG".equals(formatoUpper) ? "jpg" : ("JPEG".equals(formatoUpper) ? "jpg" : ("TIF".equals(formatoUpper) ? "tiff" : formatoUpper.toLowerCase()));
        }

        throw new IllegalArgumentException("El formato " + formato + " no está soportado");
    }

    private MediaType resolverMediaType(String formato) {
        return switch (formato.toLowerCase()) {
            case "jpg" -> MediaType.IMAGE_JPEG;
            case "png" -> MediaType.IMAGE_PNG;
            case "webp" -> MediaType.valueOf("image/webp");
            case "tiff" -> MediaType.valueOf("image/tiff");
            case "bmp" -> MediaType.valueOf("image/bmp");
            case "gif" -> MediaType.IMAGE_GIF;
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }

    private void validateHttpUri(String value) {
        try {
            URI uri = new URI(value);
            if (!uri.isAbsolute() || uri.getHost() == null) {
                throw new IllegalArgumentException("La URL debe ser absoluta y valida");
            }
            String scheme = uri.getScheme();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                throw new IllegalArgumentException("La URL debe usar esquema http o https");
            }
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException("La URL enviada no tiene un formato valido", exception);
        }
    }
}

