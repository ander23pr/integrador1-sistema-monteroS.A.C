package com.montero.app.repository;

import com.montero.app.model.Notificacion;
import com.montero.app.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    /**
     * Obtiene todas las notificaciones de un usuario ordenadas por fecha descendente
     */
    List<Notificacion> findByUsuarioOrderByFechaDesc(Usuario usuario);

    /**
     * Obtiene las notificaciones no leídas de un usuario
     */
    List<Notificacion> findByUsuarioAndLeidaFalseOrderByFechaDesc(Usuario usuario);

    /**
     * Cuenta las notificaciones no leídas de un usuario
     */
    Long countByUsuarioAndLeidaFalse(Usuario usuario);

    /**
     * Marca todas las notificaciones de un usuario como leídas
     */
    @Modifying
    @Transactional
    @Query("UPDATE Notificacion n SET n.leida = true WHERE n.usuario = :usuario")
    void marcarTodasComoLeidas(@Param("usuario") Usuario usuario);

    /**
     * Marca una notificación como leída
     */
    @Modifying
    @Transactional
    @Query("UPDATE Notificacion n SET n.leida = true WHERE n.id = :id")
    void marcarComoLeida(@Param("id") Long id);
}
