package com.navaja.navajagtbackend.controllers;

import com.navaja.navajagtbackend.dto.OpenGraphData;
import com.navaja.navajagtbackend.services.OpenGraphService;
import com.navaja.navajagtbackend.services.QrCodeService;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

    @GetMapping("/opengraph")
    public ResponseEntity<OpenGraphData> getOpenGraph(@RequestParam String url) {
        validateHttpUri(url);
        return ResponseEntity.ok(openGraphService.extract(url));
    }

    @PostMapping(value = "/convert-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> convertImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("format") String format
    ) {
        if (!"jpg".equalsIgnoreCase(format) && !"png".equalsIgnoreCase(format)) {
            throw new IllegalArgumentException("El formato debe ser jpg o png");
        }

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            Thumbnails.of(file.getInputStream())
                    .scale(1.0)
                    .outputFormat(format.toLowerCase())
                    .toOutputStream(os);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("image/" + (format.equalsIgnoreCase("jpg") ? "jpeg" : "png")));
            headers.setContentDisposition(ContentDisposition.attachment().filename("converted-image." + format.toLowerCase()).build());
            return new ResponseEntity<>(os.toByteArray(), headers, HttpStatus.OK);
        } catch (IOException exception) {
            throw new IllegalArgumentException("No fue posible convertir la imagen", exception);
        }
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

