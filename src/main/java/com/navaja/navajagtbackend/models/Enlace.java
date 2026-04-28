package com.navaja.navajagtbackend.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.JdbcTypeCode;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "enlaces")
public class Enlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_corto", nullable = false, unique = true, length = 50)
    private String codigoCorto;

    @Column(name = "url_original", nullable = true, length = 2048)
    private String urlOriginal;

    @Column(name = "tipo_herramienta", nullable = false, length = 50)
    private String tipoHerramienta;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, columnDefinition = "varchar(20)")
    private TipoEnlace tipo = TipoEnlace.STANDARD;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "fecha_expiracion")
    private OffsetDateTime fechaExpiracion;

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

    public String getTipoHerramienta() {
        return tipoHerramienta;
    }

    public void setTipoHerramienta(String tipoHerramienta) {
        this.tipoHerramienta = tipoHerramienta;
    }

    public OffsetDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(OffsetDateTime fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
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

    public TipoEnlace getTipo() {
        return tipo;
    }

    public void setTipo(TipoEnlace tipo) {
        this.tipo = tipo;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    @PrePersist
    void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = OffsetDateTime.now();
        }
        if (tipoHerramienta == null || tipoHerramienta.isBlank()) {
            tipoHerramienta = "QR";
        }
        if (tipo == null) {
            tipo = TipoEnlace.STANDARD;
        }
    }
}

