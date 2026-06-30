package com.montero.app.repository;

import com.montero.app.model.SesionUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SesionRepository extends JpaRepository<SesionUsuario, Long> {

    List<SesionUsuario> findByUsuarioIdAndActivaTrue(Long usuarioId);

    Optional<SesionUsuario> findBySessionIdAndActivaTrue(String sessionId);

    List<SesionUsuario> findTop3ByUsuarioIdOrUsuarioIdIsNullOrderByFechaInicioDesc(Long usuarioId);

    long countByUsuarioIdAndActivaTrue(Long usuarioId);
}