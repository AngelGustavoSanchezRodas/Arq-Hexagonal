package com.navaja.arqhexagonal.infrastructure.controller.dto;

public record UserResponse(
        Long id,
        String firsName,
        String lastName
) {
}
