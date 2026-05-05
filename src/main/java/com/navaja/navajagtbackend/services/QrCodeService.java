package com.navaja.navajagtbackend.services;

import com.navaja.navajagtbackend.dto.QrGenerateRequest;
import com.navaja.navajagtbackend.exceptions.AccesoDenegadoException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class QrCodeService {

    private static final int QR_SIZE = 512;
    private static final String COLOR_FRENTE_ESTANDAR = "#000000";
    private static final String COLOR_FONDO_ESTANDAR = "#FFFFFF";

    private final QuotaService quotaService;

    public QrCodeService(QuotaService quotaService) {
        this.quotaService = quotaService;
    }

    public byte[] generateStandardQr(String data, int width, int height) {
        return writeQrPng(data, MatrixToImageConfig.BLACK, MatrixToImageConfig.WHITE);
    }

    public byte[] generarQrPremium(QrGenerateRequest request, String usuarioId) {
        String colorFrente = colorNormalizado(request.colorFrente(), COLOR_FRENTE_ESTANDAR);
        String colorFondo = colorNormalizado(request.colorFondo(), COLOR_FONDO_ESTANDAR);

        if (!COLOR_FRENTE_ESTANDAR.equals(colorFrente) || !COLOR_FONDO_ESTANDAR.equals(colorFondo)) {
            try {
                if (!quotaService.validarPlanPremium(usuarioId)) {
                    throw new AccesoDenegadoException("La personalización visual del QR requiere plan PRO");
                }
            } catch (RuntimeException exception) {
                throw new AccesoDenegadoException("La personalización visual del QR requiere plan PRO");
            }
        }

        String contenidoFinal = parsearPayload(request.tipo(), request.payload());
        int frenteArgb = hexAArgb(colorFrente, "frente");
        int fondoArgb = hexAArgb(colorFondo, "fondo");

        return writeQrPng(contenidoFinal, frenteArgb, fondoArgb);
    }

    private byte[] writeQrPng(String data, int foregroundColor, int backgroundColor) {
        try {
            BitMatrix matrix = new QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);
            MatrixToImageConfig imageConfig = new MatrixToImageConfig(foregroundColor, backgroundColor);
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                MatrixToImageWriter.writeToStream(matrix, "PNG", outputStream, imageConfig);
                return outputStream.toByteArray();
            }
        } catch (WriterException | IOException exception) {
            throw new IllegalArgumentException("No se pudo generar el codigo QR con los parametros enviados", exception);
        }
    }

    private String parsearPayload(QrGenerateRequest.TipoQr tipo, Map<String, String> payload) {
        return switch (tipo) {
            case URL -> obligatorio(payload, "url");
            case PHONE -> "tel:" + obligatorio(payload, "numero");
            case WHATSAPP -> {
                String numero = obligatorio(payload, "numero");
                String mensaje = payload.getOrDefault("mensaje", "");
                if (mensaje.isBlank()) {
                    yield "https://wa.me/" + numero;
                }
                yield "https://wa.me/" + numero + "?text=" + URLEncoder.encode(mensaje, StandardCharsets.UTF_8);
            }
            case EMAIL -> {
                String correo = obligatorio(payload, "correo");
                String asunto = payload.getOrDefault("asunto", "");
                String cuerpo = payload.getOrDefault("cuerpo", "");
                StringBuilder builder = new StringBuilder("mailto:").append(correo);
                boolean tieneQuery = false;
                if (StringUtils.hasText(asunto)) {
                    builder.append("?subject=").append(URLEncoder.encode(asunto, StandardCharsets.UTF_8));
                    tieneQuery = true;
                }
                if (StringUtils.hasText(cuerpo)) {
                    builder.append(tieneQuery ? "&" : "?")
                            .append("body=")
                            .append(URLEncoder.encode(cuerpo, StandardCharsets.UTF_8));
                }
                yield builder.toString();
            }
        };
    }

    private String obligatorio(Map<String, String> payload, String key) {
        String value = payload.get(key);
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("El campo '" + key + "' es obligatorio para este tipo de QR");
        }
        return value;
    }

    private String colorNormalizado(String value, String fallback) {
        if (!StringUtils.hasText(value)) {
            return fallback;
        }
        if (!value.matches("^#[0-9A-Fa-f]{6}$")) {
            throw new IllegalArgumentException("El color debe tener formato HEX #RRGGBB");
        }
        return value.toUpperCase();
    }

    private int hexAArgb(String colorHex, String field) {
        if (!colorHex.matches("^#[0-9A-Fa-f]{6}$")) {
            throw new IllegalArgumentException("El color de " + field + " debe tener formato HEX #RRGGBB");
        }
        return (0xFF << 24) | Integer.parseInt(colorHex.substring(1), 16);
    }
}

