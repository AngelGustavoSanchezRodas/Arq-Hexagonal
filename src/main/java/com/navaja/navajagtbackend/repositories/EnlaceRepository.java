package com.navaja.navajagtbackend.repositories;

import com.navaja.navajagtbackend.models.Enlace;
import com.navaja.navajagtbackend.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface EnlaceRepository extends JpaRepository<Enlace, Long> {
    Optional<Enlace> findByCodigoCorto(String codigoCorto);

    boolean existsByCodigoCorto(String codigoCorto);

    List<Enlace> findByUsuarioId(Long usuarioId);

    long countByUsuario(Usuario usuario);

    long countByUsuarioIdAndTipoHerramienta(Long usuarioId, String tipoHerramienta);

    List<Enlace> findByFechaExpiracionBefore(OffsetDateTime fechaExpiracion);

    long deleteByFechaExpiracionBefore(OffsetDateTime fechaExpiracion);
}

