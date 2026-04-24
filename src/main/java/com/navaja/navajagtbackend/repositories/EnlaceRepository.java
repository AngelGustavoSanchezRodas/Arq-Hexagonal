package com.navaja.navajagtbackend.repositories;

import com.navaja.navajagtbackend.models.Enlace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface EnlaceRepository extends JpaRepository<Enlace, Long> {
    Optional<Enlace> findByCodigoCorto(String codigoCorto);

    boolean existsByCodigoCorto(String codigoCorto);

    List<Enlace> findByUsuarioId(Long usuarioId);

    long countByUsuarioIdAndTipoHerramienta(Long usuarioId, String tipoHerramienta);

    long deleteByFechaExpiracionBefore(OffsetDateTime fechaExpiracion);
}

