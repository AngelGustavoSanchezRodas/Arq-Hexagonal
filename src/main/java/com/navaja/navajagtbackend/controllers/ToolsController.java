package com.navaja.navajagtbackend.controllers;

import com.navaja.navajagtbackend.dto.OpenGraphData;
import com.navaja.navajagtbackend.dto.WifiQrRequest;
import com.navaja.navajagtbackend.services.OpenGraphService;
import com.navaja.navajagtbackend.services.QrCodeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/api/v1/tools")
public class ToolsController {

    private final QrCodeService qrCodeService;
    private final OpenGraphService openGraphService;

    public ToolsController(QrCodeService qrCodeService, OpenGraphService openGraphService) {
        this.qrCodeService = qrCodeService;
        this.openGraphService = openGraphService;
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

    @PostMapping("/qr/wifi")
    public ResponseEntity<byte[]> generateWifiQr(@Valid @RequestBody WifiQrRequest request) {
        int width = request.width() == null ? 300 : request.width();
        int height = request.height() == null ? 300 : request.height();
        byte[] image = qrCodeService.generateWifiQr(
                request.ssid().trim(),
                request.password(),
                request.encryptionType().trim().toUpperCase(),
                width,
                height
        );
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE)
                .body(image);
    }

    @GetMapping("/opengraph")
    public ResponseEntity<OpenGraphData> getOpenGraph(@RequestParam String url) {
        validateHttpUri(url);
        return ResponseEntity.ok(openGraphService.extract(url));
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

