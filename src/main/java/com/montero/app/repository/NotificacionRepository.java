package com.montero.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.montero.app.model.Notificacion;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    long countByUsuarioIdAndLeidaFalse(Long usuarioId);

    boolean existsByReservaIdAndTipo(Long reservaId, Notificacion.TipoNotificacion tipo);

    List<Notificacion> findByDestinatarioRolOrderByFechaCreacionDesc(String rol);

    long countByDestinatarioRolAndLeidaFalse(String rol);
}