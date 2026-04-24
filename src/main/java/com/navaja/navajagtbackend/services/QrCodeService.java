package com.navaja.navajagtbackend.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class QrCodeService {

    public byte[] generateStandardQr(String data, int width, int height) {
        return writeQrPng(data, width, height);
    }

    public byte[] generateWifiQr(String ssid, String password, String encryptionType, int width, int height) {
        String wifiData = "WIFI:S:%s;T:%s;P:%s;;".formatted(ssid, encryptionType, password == null ? "" : password);
        return writeQrPng(wifiData, width, height);
    }

    private byte[] writeQrPng(String data, int width, int height) {
        try {
            BitMatrix matrix = new QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, width, height);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", outputStream);
            return outputStream.toByteArray();
        } catch (WriterException | IOException exception) {
            throw new IllegalArgumentException("No se pudo generar el codigo QR con los parametros enviados", exception);
        }
    }
}

