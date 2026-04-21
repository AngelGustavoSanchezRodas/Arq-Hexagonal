package com.navaja.navajagtbackend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "clics")
public class Clic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enlace_id", nullable = false)
    private Enlace enlace;

    @Column(name = "direccion_ip", length = 50)
    private String direccionIp;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "fecha_clic", nullable = false)
    private OffsetDateTime fechaClic;

    public Clic() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Enlace getEnlace() {
        return enlace;
    }

    public void setEnlace(Enlace enlace) {
        this.enlace = enlace;
    }

    public String getDireccionIp() {
        return direccionIp;
    }

    public void setDireccionIp(String direccionIp) {
        this.direccionIp = direccionIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public OffsetDateTime getFechaClic() {
        return fechaClic;
    }

    public void setFechaClic(OffsetDateTime fechaClic) {
        this.fechaClic = fechaClic;
    }

    @PrePersist
    void prePersist() {
        if (fechaClic == null) {
            fechaClic = OffsetDateTime.now();
        }
    }
}

