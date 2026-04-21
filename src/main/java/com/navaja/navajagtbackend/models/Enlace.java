package com.navaja.navajagtbackend.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "enlaces")
public class Enlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_corto", nullable = false, unique = true, length = 50)
    private String codigoCorto;

    @Column(name = "url_original", nullable = false, length = 2048)
    private String urlOriginal;

    @Column(name = "es_dinamico", nullable = false)
    private boolean esDinamico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaCreacion;

    @OneToMany(mappedBy = "enlace", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Clic> clics = new ArrayList<>();

    public Enlace() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoCorto() {
        return codigoCorto;
    }

    public void setCodigoCorto(String codigoCorto) {
        this.codigoCorto = codigoCorto;
    }

    public String getUrlOriginal() {
        return urlOriginal;
    }

    public void setUrlOriginal(String urlOriginal) {
        this.urlOriginal = urlOriginal;
    }

    public boolean isEsDinamico() {
        return esDinamico;
    }

    public void setEsDinamico(boolean esDinamico) {
        this.esDinamico = esDinamico;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public OffsetDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(OffsetDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public List<Clic> getClics() {
        return clics;
    }

    public void setClics(List<Clic> clics) {
        this.clics = clics;
    }

    @PrePersist
    void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = OffsetDateTime.now();
        }
    }
}

