package com.navaja.navajagtbackend.dto;

public record OpenGraphData(
        String title,
        String ogTitle,
        String ogDescription,
        String ogImage
) {
    public static OpenGraphData empty() {
        return new OpenGraphData(null, null, null, null);
    }
}

