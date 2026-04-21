package com.navaja.navajagtbackend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class ServicioJwt {

    private final SecretKey secretKey;
    private final long expirationMillis;

    public ServicioJwt(
            @Value("${app.jwt.secret:NavajaGtBackendJwtSecretKey1234567890123456}") String secret,
            @Value("${app.jwt.expiration-millis:86400000}") long expirationMillis
    ) {
        this.secretKey = buildKey(secret);
        this.expirationMillis = expirationMillis;
    }

    public String generarToken(UserDetails userDetails) {
        return generarToken(userDetails.getUsername(), Map.of());
    }

    public String generarToken(String subject, Map<String, Object> extraClaims) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claims(new HashMap<>(extraClaims))
                .subject(subject)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMillis))
                .signWith(secretKey)
                .compact();
    }

    public String extraerUsername(String token) {
        return extraerClaims(token).getSubject();
    }

    public boolean tokenValido(String token, UserDetails userDetails) {
        try {
            String username = extraerUsername(token);
            return username.equals(userDetails.getUsername()) && !estaExpirado(token);
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean estaExpirado(String token) {
        return extraerClaims(token).getExpiration().before(new Date());
    }

    private SecretKey buildKey(String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            keyBytes = Decoders.BASE64.decode("TmF2YWphR3RCYWNrZW5kSnd0U2VjcmV0S2V5MTIzNDU2Nzg5MDEyMzQ1Ng==");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

