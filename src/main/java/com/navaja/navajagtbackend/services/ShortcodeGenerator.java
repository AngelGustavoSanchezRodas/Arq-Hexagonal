package com.navaja.navajagtbackend.services;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class ShortcodeGenerator {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int DEFAULT_LENGTH = 7;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        StringBuilder shortCode = new StringBuilder(DEFAULT_LENGTH);
        for (int i = 0; i < DEFAULT_LENGTH; i++) {
            int randomIndex = secureRandom.nextInt(ALPHABET.length());
            shortCode.append(ALPHABET.charAt(randomIndex));
        }
        return shortCode.toString();
    }
}

